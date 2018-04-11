package com.doodream.data.chrono;

import io.reactivex.Observable;

import java.util.concurrent.TimeUnit;

public class TimedSchedule {
    public static Observable<Long> getHeartbeat(long interval, TimeUnit unit) {
        return Observable.interval(interval, unit)
                .map(aLong -> System.currentTimeMillis());
    }
}
