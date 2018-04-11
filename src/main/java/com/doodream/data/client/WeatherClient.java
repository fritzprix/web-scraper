package com.doodream.data.client;

import com.doodream.data.client.model.air.AirCharts;
import com.doodream.data.client.svc.AirConditionService;
import com.doodream.data.client.svc.WeatherService;
import com.doodream.data.dagger.DaggerClientComponent;
import com.doodream.data.model.WeatherInfo;
import com.doodream.data.model.air.DailyAirConditionSummary;
import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class WeatherClient extends AsyncWebClient {

    private WeatherService weatherService;
    private AirConditionService airConditionService;
    public WeatherClient() {
        DaggerClientComponent.create().inject(this);
        Retrofit retrofit = new Retrofit.Builder()
                .client(getOkHttpClient())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("http://www.weather.go.kr")
                .build();

        weatherService = retrofit.create(WeatherService.class);

        retrofit = new Retrofit.Builder()
                .client(getOkHttpClient())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("https://www.airkorea.or.kr/")
                .build();

        airConditionService = retrofit.create(AirConditionService.class);
    }


    public Observable<WeatherInfo> getWeatherForecast() {
        return weatherService.getWeatherForecast("108")
                .filter(Response::isSuccessful)
                .flatMapObservable(WeatherInfo::fromRSS)
                .subscribeOn(getScheduler());
    }

    public Single<Boolean> loginAirKorea() {
        return airConditionService.login()
                .map(Response::isSuccessful);
    }

    public Single<DailyAirConditionSummary> getDailyAirCondition(AirConditionService.ItemCode itemCode) {
        return airConditionService.getAirChart(AirConditionService.Period.DAY, itemCode)
                .map(DailyAirConditionSummary::fromAirCharts);
    }


}
