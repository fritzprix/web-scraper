package com.doodream.data.client.svc;

import com.doodream.data.client.model.air.AirCharts;
import com.google.gson.annotations.SerializedName;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AirConditionService {

    enum Period {
        @SerializedName("1")
        DAY(1),
        @SerializedName("7")
        WEEK(7),
        @SerializedName("30")
        MONTH(30);
        private final int days;
        Period(int days) {
            this.days = days;
        }

        @Override
        public String toString() {
            return String.valueOf(days);
        }
    }

    enum ItemCode {
        @SerializedName("PM2_5")
        PM2_5("10008"),
        @SerializedName("PM10")
        PM10("10007"),
        @SerializedName("O3")
        O3("10003"),
        @SerializedName("NO2")
        NO2("10006"),
        @SerializedName("CO")
        CO("10002"),
        @SerializedName("SO2")
        SO2("10001");
        private final String code;
        ItemCode(String code) {
            this.code = code;
        }

        public static Observable<ItemCode> iterObservable() {
            return Observable.fromPublisher(PublishProcessor.fromArray(values()));
        }

        public String code() {
            return code;
        }

        public static ItemCode parse(String code) {
            switch (code) {
                case "10007":
                    return PM10;
                case "10008":
                    return PM2_5;
                case "10003":
                    return O3;
                case "10006":
                    return NO2;
                case "10002":
                    return CO;
                case "10001":
                    return SO2;
                default:
                    throw new IllegalArgumentException("");
            }
        }
    }

    @GET("/sidoCompare")
    Single<Response<ResponseBody>> login();

    @GET("/sidoCompare")
    Single<Response<ResponseBody>> getAirCondition(
            @Query("itemCode") ItemCode itemCode
    );

    @GET("/sidoCompare_p01")
    Single<Response<AirCharts>> getAirConditionDetail(
            @Query("itemCode") ItemCode itemCode,
            @Query("ymd") String ymd,
            @Query("areaCode") String area
    );

    @GET("/web/pollution/getSidoChart")
    Single<Response<AirCharts>> getAirChart(
            @Query("period") Period period,
            @Query("itemCode") String itemCode
    );
}
