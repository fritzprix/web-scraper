package com.doodream.data.model.air;

import com.doodream.data.client.model.air.AirCharts;
import com.doodream.data.util.KVPair;
import com.doodream.data.util.serdes.ArrayConverter;
import com.google.common.collect.Lists;
import io.reactivex.Observable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.doodream.data.model.air.DailyAirConditionDetail.*;
import static com.doodream.data.model.air.DailyAirConditionDetail.MeasureType.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DailyAirConditionSummary {
    List<DailyAirConditionDetail> conditions;

    public static <R> DailyAirConditionSummary fromAirCharts(AirCharts airCharts) {

        int provinceCnt = Province.values().length;
        List<DailyAirConditionDetail> airDetails = ArrayConverter.filledList(DailyAirConditionDetail.class, provinceCnt);
        List<AirCharts.Row> rows = airCharts.getCharts();

        DailyAirConditionSummary summary = DailyAirConditionSummary.builder()
                .conditions(ArrayConverter.filledList(DailyAirConditionDetail.class, provinceCnt))
                .build();

        Observable.fromArray(MeasureType.values()).zipWith(Observable.fromIterable(rows), KVPair::pair)
                .map(measureTypeRowKVPair -> KVPair.pair(measureTypeRowKVPair, ArrayConverter.<AirCharts.Row,String>toArray(measureTypeRowKVPair.getValue())))
                .doOnNext(summary::fill).blockingSubscribe();

        return summary;
    }

    private void fill(KVPair<KVPair<MeasureType, AirCharts.Row>, String[]> typeRowVec) {
        MeasureType type = typeRowVec.getKey().getKey();
    }
}
