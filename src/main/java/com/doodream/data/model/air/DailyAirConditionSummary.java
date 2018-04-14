package com.doodream.data.model.air;

import com.doodream.data.client.model.air.AirCharts;
import io.reactivex.Single;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DailyAirConditionSummary {
    List<DailyAirConditionDetail> conditions;


    public static Single<DailyAirConditionSummary> fromAirCharts(AirCharts airCharts) {

        return Single.create(emitter -> emitter.setDisposable(DailyAirConditionDetail.observable(airCharts)
                .toList()
                .subscribe((dailyAirConditionDetails) -> emitter.onSuccess(builder().conditions(dailyAirConditionDetails).build()), emitter::onError)));
    }
}
