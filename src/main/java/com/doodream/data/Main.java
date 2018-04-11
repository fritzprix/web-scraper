package com.doodream.data;

import com.doodream.data.chrono.TimedSchedule;
import com.doodream.data.client.WeatherClient;
import com.doodream.data.dagger.DaggerClientComponent;
import com.doodream.data.model.WeatherInfo;
import com.doodream.data.model.air.DailyAirConditionDetail;
import io.reactivex.disposables.CompositeDisposable;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        DaggerClientComponent.create();
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        WeatherClient weatherClient = new WeatherClient();

        compositeDisposable.add(TimedSchedule.getHeartbeat(2L, TimeUnit.SECONDS)
                .subscribe(aLong -> {
                    compositeDisposable.add(weatherClient.getWeatherForecast()
                            .subscribe(Main::saveWeatherInfo, Main::onError));
//                    compositeDisposable.add(weatherClient.getAirCondition().subscribe(Main::saveAirCondition, Main::onError));
                }, Main::onError));

        Runtime.getRuntime().addShutdownHook(new Thread(compositeDisposable::clear));

        while (true) {
            Thread.sleep(1000);
        }
    }

    private static void saveAirCondition(DailyAirConditionDetail airCondition) {

    }

    private static void onError(Throwable throwable) {
        System.err.println(throwable.getLocalizedMessage());
        System.exit(-1);
    }

    private static void saveWeatherInfo(WeatherInfo weatherInfo) {
        System.out.println(weatherInfo.toString());
    }
}
