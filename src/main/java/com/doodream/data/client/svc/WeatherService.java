package com.doodream.data.client.svc;

import com.doodream.data.client.model.weather.RSS;
import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {

    @GET("/weather/forecast/mid-term-rss3.jsp")
    Single<Response<RSS>> getWeatherForecast(@Query("stdId") String stnId);
}
