package com.doodream.data;

import com.doodream.data.chrono.TimedSchedule;
import com.doodream.data.client.WeatherClient;
import com.doodream.data.client.svc.AirConditionService;
import com.doodream.data.dagger.DaggerClientComponent;
import com.doodream.data.model.air.DailyAirConditionSummary;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Gson gson = new Gson();

        DaggerClientComponent.create();
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        WeatherClient weatherClient = new WeatherClient();
        Observable<Long> minuteHeartbeat = TimedSchedule.getHeartbeat(5L, TimeUnit.SECONDS);

        Observable<Boolean> sessionObservable = minuteHeartbeat.map(aLong -> weatherClient.loginAirKorea()).map(Single::blockingGet).filter(Boolean::booleanValue);
        Observable<AirConditionService.ItemCode> itemCodeObservable = AirConditionService.ItemCode.iterObservable();

        compositeDisposable.add(sessionObservable.zipWith(itemCodeObservable,(aBoolean, itemCode) -> weatherClient
                .getDailyAirCondition(itemCode).blockingGet())
                .map(DailyAirConditionSummary::toString)
                .map(gson::toJson)
                .subscribe(System.out::println, Main::onError));

        compositeDisposable.add(minuteHeartbeat.subscribe(aLong -> compositeDisposable.add(weatherClient.getWeatherForecast()
                .subscribe(System.out::println, Main::onError))));


        Runtime.getRuntime().addShutdownHook(new Thread(compositeDisposable::clear));

        while (true) {
            Thread.sleep(1000);
        }
    }


    private static void onError(Throwable throwable) {
        System.err.println(throwable.getLocalizedMessage());
        System.exit(-1);
    }

}
