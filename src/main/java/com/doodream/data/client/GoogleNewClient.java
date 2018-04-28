package com.doodream.data.client;

import com.doodream.data.client.model.news.GoogleNewsRSS;
import com.doodream.data.client.svc.GoogleNewsService;
import com.doodream.data.dagger.DaggerClientComponent;
import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class GoogleNewClient extends ReactiveClient {


    private GoogleNewsService googleNewsService;

    public GoogleNewClient() {
        DaggerClientComponent.create().inject(this);
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(getScheduler()))
                .baseUrl("https://news.google.com")
                .build();

        googleNewsService = retrofit.create(GoogleNewsService.class);
    }

    public Single<GoogleNewsRSS> getHeadlineNewsContents(GoogleNewsService.Section section, String region, String headline, String ned) {
        return googleNewsService.getHeadlineNewsContents(section, region, headline, ned)
                .filter(Response::isSuccessful)
                .map(Response::body)
                .toSingle();
    }

    public Single<GoogleNewsRSS> getNewsContentsByKeyword(String keyword, String region, String headline, String ned) {
        return googleNewsService.getNewsByKeyword(keyword, region, headline, ned)
                .filter(Response::isSuccessful)
                .map(Response::body)
                .toSingle();
    }

}
