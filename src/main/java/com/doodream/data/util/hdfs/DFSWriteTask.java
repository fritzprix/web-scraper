package com.doodream.data.util.hdfs;

import io.reactivex.Observable;
import io.reactivex.Single;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DFSWriteTask {
    private static final Logger LOGGER = LogManager.getLogger(DFSWriteTask.class);
    private Observable<byte[]> dataSource;
    private FSDataOutputStream outputStream;

    public Single<TaskResult> run() {
        return Single.<TaskResult>create(emitter -> emitter.setDisposable(dataSource
                .subscribe(bytes -> outputStream.write(bytes),
                        throwable -> emitter.onSuccess(TaskResult.ERROR),
                        () -> emitter.onSuccess(TaskResult.OK))))
                .doOnSuccess(this::onComplete)
                .doOnError(this::onError);
    }

    public Single<TaskResult> run(long syncPeriod, TimeUnit unit) {
        return Single.<TaskResult>create(emitter -> emitter.setDisposable(dataSource
                .subscribe(bytes -> outputStream.write(bytes),
                        throwable -> emitter.onSuccess(TaskResult.ERROR),
                        () -> emitter.onSuccess(TaskResult.OK))))
                .doOnSuccess(this::onComplete)
                .doOnError(this::onError);
    }

    private void onComplete(TaskResult result) throws IOException {
        outputStream.close();
        System.out.printf("Task Result %s\n", result);
        LOGGER.debug("Task Result {}", result);
    }

    private void onError(Throwable throwable) throws IOException {
        outputStream.close();
        LOGGER.error("Task Failed ", throwable);
    }


    public enum TaskResult {
        OK,
        ERROR
    }
    public static DFSWriteTask create(FSDataOutputStream outputStream, Observable<byte[]> taskObservable) {
        return DFSWriteTask.builder()
                .dataSource(taskObservable)
                .outputStream(outputStream)
                .build();
    }
}
