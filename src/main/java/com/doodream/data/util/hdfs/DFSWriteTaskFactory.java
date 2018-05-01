package com.doodream.data.util.hdfs;

import io.reactivex.Observable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.io.IOException;

public class DFSWriteTaskFactory {

    private static final Logger LOGGER = LogManager.getLogger(DFSWriteTaskFactory.class);

    private Observable<Path> parentObservable;
//    private FileContext fileContext;
    private FileSystem fileSystem;
    private ConcurrentHashSet<FSDataOutputStream> activeStream; // TODO: manage active task set , instead of stream

    public DFSWriteTaskFactory(String path, Configuration hdConfiguration) throws IOException {
        fileSystem = FileSystem.newInstance(hdConfiguration);
        activeStream = new ConcurrentHashSet<>();
        Path parent = new Path(path);
        Path qualified = fileSystem.makeQualified(parent);
        parentObservable = Observable.create(observableEmitter -> {
            try {
                if (!fileSystem.exists(qualified)) {
                    fileSystem.mkdirs(qualified, FsPermission.getDirDefault());
                }
            } catch (Exception e) {
                observableEmitter.onError(e);
            }
            observableEmitter.onNext(qualified);
            observableEmitter.onComplete();
        });
    }


    public Observable<DFSWriteTask> create(String key, Observable<byte[]> observable) {

        return Observable.create(observableEmitter -> observableEmitter.setDisposable(parentObservable
                .map(path -> new Path(path, key))
                .map(fileSystem::makeQualified)
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
        if(!fileSystem.exists(path.getParent())) {
            fileSystem.mkdirs(path.getParent(),FsPermission.getDirDefault());
        }
    }

    private FSDataOutputStream getDataOutputStream(Path path) throws IOException {
        // TODO : need some enum type define create flags & pre-condition handle before opening output stream
        if (fileSystem.exists(path)) {
//            if (fileSystem.delete(path, true)) {
//                System.out.printf("File Deleted : %s\n", path.getName());
//                LOGGER.debug("File Deleted : {}", path.getName());
//            }
            return fileSystem.append(path);
        }
        return fileSystem.create(path, true);
    }

    public void close() {
        Observable.fromIterable(activeStream).blockingSubscribe(FSDataOutputStream::close);
    }
}
