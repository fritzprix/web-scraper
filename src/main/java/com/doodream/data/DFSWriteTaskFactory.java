package com.doodream.data;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

public class DFSWriteTaskFactory {

    private static final Logger LOGGER = LogManager.getLogger(DFSWriteTaskFactory.class);

    private Observable<Path> parentObservable;
    private FileContext fileContext;
    private ConcurrentHashSet<FSDataOutputStream> activeStream; // TODO: manage active task set , instead of stream

    public DFSWriteTaskFactory(String path, Configuration hdConfiguration) throws UnsupportedFileSystemException {
        this.fileContext = FileContext.getFileContext(hdConfiguration);
        activeStream = new ConcurrentHashSet<>();
        Path parent = new Path(path);
        Path qualified = fileContext.makeQualified(parent);
        parentObservable = Observable.create(observableEmitter -> {
            try {
                if (!fileContext.util().exists(qualified)) {
                    fileContext.mkdir(qualified, FsPermission.getDirDefault(), true);
                }
            } catch (Exception e) {
                observableEmitter.onError(e);
            }
            observableEmitter.onNext(qualified);
            observableEmitter.onComplete();
        });
    }


    public ObservableSource<DFSWriteTask> create(String key, Observable<byte[]> observable) {

        return Observable.create(observableEmitter -> observableEmitter.setDisposable(parentObservable
                .map(path -> new Path(path, key))
                .map(fileContext::makeQualified)
                .doOnNext(this::checkParentDirectory)
                .map(this::getDataOutputStream)
                .doOnNext(this::addOutputStream)
                .subscribe(fsDataOutputStream -> observableEmitter
                        .onNext(DFSWriteTask.create(fsDataOutputStream, observable.doOnComplete(() -> removeOutputStream(fsDataOutputStream)))), observableEmitter::onError, observableEmitter::onComplete)));
    }

    private void removeOutputStream(FSDataOutputStream fsDataOutputStream) {
        activeStream.remove(fsDataOutputStream);
    }

    private void addOutputStream(FSDataOutputStream fsDataOutputStream) {
        activeStream.add(fsDataOutputStream);
    }

    private void checkParentDirectory(Path path) throws IOException {
        if(!fileContext.util().exists(path.getParent())) {
            fileContext.mkdir(path.getParent(),FsPermission.getDirDefault(),true);
        }
    }

    private FSDataOutputStream getDataOutputStream(Path path) throws IOException {
        // TODO : need some enum type define create flags & pre-condition handle before opening output stream
        if (fileContext.util().exists(path)) {
            if (fileContext.delete(path, true)) {
                System.out.printf("File Deleted : %s\n", path.getName());
                LOGGER.debug("File Deleted : {}", path.getName());
            }
            return fileContext.create(path, EnumSet.of(CreateFlag.APPEND, CreateFlag.CREATE));
        }
        return fileContext.create(path, EnumSet.of(CreateFlag.CREATE, CreateFlag.APPEND));
    }

    public void close() {
        Observable.fromIterable(activeStream).blockingSubscribe(FSDataOutputStream::close);
    }
}
