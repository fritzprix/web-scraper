package com.doodream.data.client;

import com.doodream.data.client.model.air.AirCharts;
import com.doodream.data.client.svc.AirConditionService;
import com.doodream.data.dagger.DaggerClientComponent;
import com.doodream.data.model.air.DailyAirConditionSummary;
import com.google.gson.Gson;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class AirQualityClient extends AsyncWebClient {
    public static final Logger LOGGER = LogManager.getLogger(AirQualityClient.class);
    private ConcurrentHashMap<String, DailyAirConditionSummary> airSummaryCache;
    private AirConditionService airConditionService;

    public AirQualityClient() {
        DaggerClientComponent.create().inject(this);
        airSummaryCache = new ConcurrentHashMap<>();

        Retrofit retrofit = new Retrofit.Builder()
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("https://www.airkorea.or.kr/")
                .build();

        airConditionService = retrofit.create(AirConditionService.class);
    }

    public Single<Boolean> loginAirKorea() {
        return airConditionService.login()
                .map(Response::isSuccessful);
    }

    public Single<AirCharts> getAirCondition(AirConditionService.Period period, AirConditionService.ItemCode itemCode) {
        return airConditionService.getAirChart(period, itemCode.code()).filter(Response::isSuccessful).map(Response::body).toSingle();
    }

    public Single<DailyAirConditionSummary> getDailyAirCondition(AirConditionService.ItemCode itemCode) {
        return airConditionService.getAirChart(AirConditionService.Period.DAY, itemCode.code())
                .map(Response::<AirCharts>body)
                .flatMap(DailyAirConditionSummary::fromAirCharts);
    }

    public Observable<DailyAirConditionSummary> getDailyAirConditions(Long count) {
        return Observable.<DailyAirConditionSummary>create(emitter -> emitter.setDisposable(loginAirKorea().filter(Boolean::booleanValue)
                .flattenAsObservable(aBoolean -> Arrays.asList(AirConditionService.ItemCode.values()))
                .map(this::getDailyAirCondition)
                .doOnComplete(emitter::onComplete)
                .doOnError(emitter::onError)
                .map(Single::blockingGet)
                .doOnNext(this::cacheDailyAirSummary)
                .filter(DailyAirConditionSummary::isUpdated)
                .doOnNext(System.out::println)
                .subscribe(emitter::onNext)))
                .subscribeOn(getScheduler());
    }

    private void cacheDailyAirSummary(DailyAirConditionSummary dailyAirConditionSummary) throws ParseException {
        String key = DailyAirConditionSummary.getUniqueKey(dailyAirConditionSummary);
        DailyAirConditionSummary lastSummary = airSummaryCache.get(key);
        if(lastSummary == null) {
            airSummaryCache.put(key, dailyAirConditionSummary);
            System.out.printf("New Item Cached %s(%s)\n", key, dailyAirConditionSummary.isUpdated());
        } else {
            if(lastSummary.hashCode() != dailyAirConditionSummary.hashCode()) {
                airSummaryCache.put(key, dailyAirConditionSummary);
                System.out.printf("Cache Updated for %s(%s)\n", key, dailyAirConditionSummary.isUpdated());
            } else {
                dailyAirConditionSummary.setUpdated(false);
                System.out.printf("Cache Hit for %s(%s)\n", key, dailyAirConditionSummary.isUpdated());
            }
        }
    }
}
