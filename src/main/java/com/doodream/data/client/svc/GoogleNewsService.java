package com.doodream.data.client.svc;

import com.doodream.data.client.model.news.GoogleNewsRSS;
import com.google.gson.annotations.SerializedName;
import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GoogleNewsService {

    enum Section {
        @SerializedName("WORLD")
        WORLD,
        @SerializedName("NATION")
        US,
        @SerializedName("BUSINESS")
        BUSINESS,
        @SerializedName("TECHNOLOGY")
        TECH,
        @SerializedName("ENTERTAINMENT")
        ENTERTAINMENT,
        @SerializedName("SCIENCE")
        SCIENCE,
        @SerializedName("HEALTH")
        HEALTH
    }

    @GET("/news/rss/headlines/section/topic/{topic}")
    Single<Response<GoogleNewsRSS>> getHeadlineNewsContents(
            @Path("topic") Section topic,
            @Query("gl") String region,
            @Query("hl") String headline,
            @Query("ned") String ned
    );

    @GET("/news/rss/search/section/q/{keyword}/{keyword}")
    Single<Response<GoogleNewsRSS>> getNewsByKeyword(
            @Path("keyword") String keyword,
            @Query("gl") String region,
            @Query("hl") String headline,
            @Query("ned") String ned
    );
}
