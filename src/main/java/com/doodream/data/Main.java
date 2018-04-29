package com.doodream.data;

import com.doodream.data.chrono.TimedSchedule;
import com.doodream.data.client.AirQualityClient;
import com.doodream.data.client.GoogleNewClient;
import com.doodream.data.client.WeatherClient;
import com.doodream.data.client.model.news.NewsContent;
import com.doodream.data.dagger.DaggerClientComponent;
import com.doodream.data.model.air.DailyAirConditionSummary;
import com.doodream.data.model.weather.WeatherInfo;
import com.doodream.data.util.hdfs.DFSWriteTask;
import com.doodream.data.util.hdfs.DFSWriteTaskFactory;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger Log = LogManager.getLogger(Main.class);
    private static final Gson GSON = new Gson();
    public static void main(String[] args) throws InterruptedException, IOException {

        DaggerClientComponent.create();

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        WeatherClient weatherClient = new WeatherClient();
        AirQualityClient airQualityClient = new AirQualityClient();
        GoogleNewClient googleNewClient = new GoogleNewClient();

        Configuration hdConfiguration = new Configuration();
        hdConfiguration.set("fs.defaultFS", "hdfs://rpi-cluster-master:9000");


        Observable<Long> minuteHeartbeat = TimedSchedule.getHeartbeat(1L, TimeUnit.MINUTES);
        Observable<Long> fastHeartbeat = TimedSchedule.getHeartbeat(5L, TimeUnit.SECONDS);



//        WebDriver webDriver = new ChromeDriver();
//        webDriver.get("https://www.forbes.com/sites/ralphjennings/2018/04/26/china-is-tightening-its-grip-on-cryptocurrency-to-promote-rather-than-purge-it");
//        Document document = Jsoup.parse(webDriver.getPageSource());
//        Elements elements = document.select("p");
//        for (Element element : elements) {
//            System.out.println(element.text());
//        }


//        DFSWriteTaskFactory airWriteTaskFactory = new DFSWriteTaskFactory("/user/hduser/logs/air", hdConfiguration);
//        DFSWriteTaskFactory weatherWriteTaskFactory = new DFSWriteTaskFactory("/user/hduser/logs/weather", hdConfiguration);
//        DFSWriteTaskFactory newsWriteTaskFactory = new DFSWriteTaskFactory("/user/hduser/logs/weather", hdConfiguration);

        compositeDisposable.add(minuteHeartbeat
//                .doOnNext(Main.collectWeather(weatherClient, compositeDisposable, weatherWriteTaskFactory))
//                .doOnNext(Main.collectAirData(airQualityClient, compositeDisposable, airWriteTaskFactory))
                .doOnNext(Main.collectNews(googleNewClient, compositeDisposable, null))
                .subscribe());

        compositeDisposable.add(fastHeartbeat
                .subscribe());


        Runtime.getRuntime().addShutdownHook(new Thread(()->{
//            airWriteTaskFactory.close();
//            weatherWriteTaskFactory.close();
            compositeDisposable.clear();
        }));


        while (true) {
            Thread.sleep(1000L);
        }
    }

    private static Consumer<? super Long> collectNews(GoogleNewClient googleNewClient, CompositeDisposable compositeDisposable, DFSWriteTaskFactory newsWriteTaskFactory) {
        return aLong -> compositeDisposable.add(googleNewClient.getNewsContentsByKeyword("cryptocurrency","US","EN","EN")
                .toObservable()
                .flatMap(NewsContent::extractNewsContents)
                .doOnNext(System.out::println)
                .subscribe());
    }

    private static Consumer<? super Long> collectAirData(AirQualityClient airQualityClient, CompositeDisposable compositeDisposable, DFSWriteTaskFactory airWriteTaskFactory) {
        return aLong -> compositeDisposable.add(airQualityClient.getDailyAirConditions(aLong)
                .groupBy(DailyAirConditionSummary::getUniqueKey)
                .flatMap(observable -> airWriteTaskFactory.create(observable.getKey(), observable.map(GSON::toJson).map(s -> s.getBytes("UTF-8"))))
                .map(DFSWriteTask::run)
                .map(Single::subscribe)
                .subscribe(compositeDisposable::add));
    }

    private static Consumer<? super Long> collectWeather(WeatherClient weatherClient, CompositeDisposable compositeDisposable, DFSWriteTaskFactory weatherWriteTask) {
        return (Consumer<Long>) aLong -> compositeDisposable.add(weatherClient.getWeatherForecast()
                .groupBy(WeatherInfo::getUniqueKey)
                .flatMap(observable -> weatherWriteTask.create(observable.getKey(), observable.map(GSON::toJson).map(s -> s.getBytes("UTF-8"))))
                .map(DFSWriteTask::run)
                .map(Single::subscribe)
                .subscribe(compositeDisposable::add));
    }


}
