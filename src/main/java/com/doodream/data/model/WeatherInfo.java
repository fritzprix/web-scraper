package com.doodream.data.model;

import com.doodream.data.client.model.weather.Location;
import com.doodream.data.client.model.weather.Channel;
import com.doodream.data.client.model.weather.RSS;
import io.reactivex.Observable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherInfo {

    private String pubTime;
    private String kstTime;
    private String province;
    private String city;
    private String forecast;
    private int tempMin;
    private int tempMax;
    private static ThreadLocal<SimpleDateFormat> PUB_DATE_PARSER = new ThreadLocal<>();
    private static ThreadLocal<SimpleDateFormat> TM_DATE_PARSER = new ThreadLocal<>();
    private static ThreadLocal<SimpleDateFormat> DATE_FORMATTER = new ThreadLocal<>();


    public static Observable<WeatherInfo> fromRSS(Response<RSS> weatherRSSResponse) {

        if(PUB_DATE_PARSER.get() == null) {
            PUB_DATE_PARSER.set(new SimpleDateFormat("yyyy년 MM월 dd일 (E)요일 hh:mm",Locale.KOREAN));
        }
        if(TM_DATE_PARSER.get() == null) {
            TM_DATE_PARSER.set(new SimpleDateFormat("yyyy-MM-dd hh:mm"));
        }
        if(DATE_FORMATTER.get() == null) {
            DATE_FORMATTER.set(new SimpleDateFormat("yyyyMMddhhmm"));
        }

        return Observable.create(emitter -> {

            RSS rss = weatherRSSResponse.body();
            if(rss == null) {
                emitter.onError(new NullPointerException("Body is Null"));
                return;
            }
            Channel channel = rss.getChannel();
            List<Location> locations = channel.getItem().getDescription().getBody().getLocation();
            locations.forEach(location -> location.getData().forEach(weatherData -> {
                try {
                    emitter.onNext(WeatherInfo.builder()
                            .tempMax(weatherData.getMaxTemp())
                            .tempMin(weatherData.getMinTemp())
                            .forecast(weatherData.getForecast())
                            .kstTime(DATE_FORMATTER.get().format(TM_DATE_PARSER.get().parse(weatherData.getTime())))
                            .pubTime(DATE_FORMATTER.get().format(PUB_DATE_PARSER.get().parse(channel.getPubDate())))
                            .province(location.getProvince())
                            .city(location.getCity()).build());
                } catch (ParseException e) {
                    emitter.onError(e);
                }
            }));
            emitter.onComplete();
        });
    }
}
