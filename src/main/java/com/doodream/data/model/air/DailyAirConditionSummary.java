package com.doodream.data.model.air;

import com.doodream.data.client.model.air.AirCharts;
import com.doodream.data.client.svc.AirConditionService;
import com.doodream.data.util.serdes.ArrayItem;
import com.google.gson.annotations.SerializedName;
import io.reactivex.Single;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DailyAirConditionSummary {
    private static ThreadLocal<SimpleDateFormat> PUB_TIME_FORMAT = new ThreadLocal<>();
    private static ThreadLocal<SimpleDateFormat> KEY_TIME_FORMAT = new ThreadLocal<>();

    @SerializedName("code")
    AirConditionService.ItemCode itemCode;

    @SerializedName("results")
    List<DailyAirConditionDetail> results;

    transient boolean updated;


    public static Single<DailyAirConditionSummary> fromAirCharts(AirCharts airCharts) {

        return Single.create(emitter -> emitter.setDisposable(DailyAirConditionDetail.observable(airCharts)
                .toList()
                .filter(dailyAirConditionDetails -> !dailyAirConditionDetails.isEmpty())
                .subscribe((dailyAirConditionDetails) -> emitter.onSuccess(builder()
                        .results(dailyAirConditionDetails)
                        .updated(true)   // default true
                        .itemCode(dailyAirConditionDetails.get(0).getItem())
                        .build()), emitter::onError)));
    }

    public static String getUniqueKey(DailyAirConditionSummary dailyAirConditionSummary) throws ParseException {
        String pubTime = dailyAirConditionSummary.getResults().get(0).getPublishTime();
        if(PUB_TIME_FORMAT.get() == null) {
            PUB_TIME_FORMAT.set(new SimpleDateFormat("yyyy-MM-dd HH"));
        }
        if(KEY_TIME_FORMAT.get() == null) {
            KEY_TIME_FORMAT.set(new SimpleDateFormat("yyyyMMddHH"));
        }
        return String.format("%s_%s", dailyAirConditionSummary.itemCode, KEY_TIME_FORMAT.get().format(PUB_TIME_FORMAT.get().parse(pubTime)));
    }
}
