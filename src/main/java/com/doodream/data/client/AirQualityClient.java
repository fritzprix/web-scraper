package com.doodream.data.client;

import com.doodream.data.client.model.air.AirCharts;
import com.doodream.data.client.svc.AirConditionService;
import com.doodream.data.dagger.DaggerClientComponent;
import com.doodream.data.model.air.DailyAirConditionSummary;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class AirQualityClient extends ReactiveClient {
    private ConcurrentHashMap<String, DailyAirConditionSummary> airSummaryCache;
    private AirConditionService airConditionService;
    private HashSet<Integer> duplicationSearchSet;

    public AirQualityClient() {
        DaggerClientComponent.create().inject(this);
        airSummaryCache = new ConcurrentHashMap<>();
        duplicationSearchSet = new HashSet<>();

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
                .filter(this::noDuplication)
                .filter(DailyAirConditionSummary::isUpdated)
                .subscribe(emitter::onNext)))
                .subscribeOn(getScheduler());
    }

    private boolean noDuplication(DailyAirConditionSummary dailyAirConditionSummary) {
        return duplicationSearchSet.add(dailyAirConditionSummary.hashCode());
    }

}
