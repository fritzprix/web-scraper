import com.doodream.data.client.AirQualityClient;
import com.doodream.data.client.GoogleNewClient;
import com.doodream.data.client.WeatherClient;
import com.doodream.data.client.model.news.GoogleNewsRSS;
import com.doodream.data.client.model.news.NewsContent;
import com.doodream.data.model.air.DailyAirConditionDetail;
import com.doodream.data.model.air.DailyAirConditionSummary;
import com.doodream.data.model.weather.WeatherInfo;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static junit.framework.Assert.*;

public class ClientTest {

    @Test
    public void modelConformToGoogleRssResponse() {
        Observable.fromArray(Topic.values())
                .map(Topic::getKeywords)
                .flatMap(Observable::fromIterable)
                .doOnNext(this::testByKeyword)
                .blockingSubscribe();
    }

    private void testByKeyword(String kw) {
        GoogleNewClient googleNewClient = new GoogleNewClient();
        GoogleNewsRSS rss = googleNewClient.getNewsContentsByKeyword(kw,"US","EN","US").blockingGet();
        assertNotNull(rss);
        List<NewsContent> contents = NewsContent.extractNewsContents(rss).toList().blockingGet();
        assertNotNull(contents);
        assertNotSame(contents.size(), 0);
        for (NewsContent content : contents) {
            System.out.println(content);
            assertNotNull(content.getBody());
            assertNotNull(content.getTitle());
            assertFalse(content.getTitle().isEmpty());
            assertNotNull(content.getCategory());
            assertFalse(content.getCategory().isEmpty());
            assertNotSame(content.getPubDateInEpoch(), 0L);
            assertTrue(new Date().toInstant().getEpochSecond() > content.getPubDateInEpoch());
            assertNotNull(content.getAuthor());
            assertFalse(content.getAuthor().isEmpty());
            assertNotNull(content.getSource());
            assertFalse(content.getSource().isEmpty());
            assertNotNull(content.getDescription());
        }
    }

    @Test
    public void modelConformToAirJSONResponse() {
        AirQualityClient airQualityClient = new AirQualityClient();
        List<DailyAirConditionSummary> summary = airQualityClient.loginAirKorea()
                .filter(Boolean::booleanValue)
                .map(aBoolean -> airQualityClient.getDailyAirConditions(0L))
                .map(Observable::toList)
                .map(Single::blockingGet)
                .blockingGet();

        assertNotNull(summary);
        assertFalse(summary.isEmpty());
        for (DailyAirConditionSummary airConditionSummary : summary) {
            List<DailyAirConditionDetail> details = airConditionSummary.getResults();

            assertNotNull(details);
            assertNotNull(airConditionSummary.getItemCode());
            assertFalse(details.isEmpty());

            for (DailyAirConditionDetail detail : details) {
                List<DailyAirConditionDetail.Measurement> measurements = detail.getMeasurements();
                assertNotNull(measurements);
                assertFalse(measurements.isEmpty());

                assertNotNull(detail.getPublishTime());
                assertNotNull(detail.getDisplayTime());
                assertSame(detail.getItem(), airConditionSummary.getItemCode());
                assertNotNull(detail.getProvince());
            }
        }
    }

    @Test
    public void modelConformToWeatherRssResponse() {
        WeatherClient weatherClient = new WeatherClient();
        List<WeatherInfo> weatherInfos = weatherClient.getWeatherForecast().toList().blockingGet();
        assertNotNull(weatherInfos);
        for (WeatherInfo weatherInfo : weatherInfos) {
            assertNotNull(weatherInfo.getCity());
            assertNotNull(weatherInfo.getDisplayTime());
            assertNotNull(weatherInfo.getForecast());
            assertNotNull(weatherInfo.getProvince());
            assertNotNull(weatherInfo.getPubTime());
        }

    }
}
