package com.doodream.data.client.svc;

import com.doodream.data.client.model.air.AirCharts;
import com.google.gson.annotations.SerializedName;
import io.reactivex.Single;
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
        PM2_5("10008"),
        PM10("10007"),
        O3("10003"),
        NO2("10006"),
        CO("10002"),
        SO2("10001");
        private final String code;
        ItemCode(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    @GET("/sidoCompare")
    Single<Response<ResponseBody>> login();

    @GET("/sidoCompare")
    Single<Response<ResponseBody>> getAirCondition(
            @Query("itemCode") ItemCode itemCode
    );

    @GET("/sidoCompare_p01")
    Single<AirCharts> getAirConditionDetail(
            @Query("itemCode") ItemCode itemCode,
            @Query("ymd") String ymd,
            @Query("areaCode") String area
    );

    @GET("/web/pollution/getSidoChart")
    Single<AirCharts> getAirChart(
            @Query("period") Period period,
            @Query("itemCode") ItemCode itemCode
    );
}
