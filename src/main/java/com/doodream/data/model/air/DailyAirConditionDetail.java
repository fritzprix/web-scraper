package com.doodream.data.model.air;

import com.doodream.data.client.model.air.AirCharts;
import com.doodream.data.client.svc.AirConditionService;
import com.doodream.data.util.serdes.Arrays;
import com.google.gson.annotations.SerializedName;
import io.reactivex.Observable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DailyAirConditionDetail {
    public static final int ROW_HOUR_AVERAGE = 0;
    public static final int ROW_DAY_AVERAGE = 1;
    public static final int DAY_MAX = 2;
    public static final int DAY_MIN = 3;

    public static Observable<DailyAirConditionDetail> observable(AirCharts airCharts) {
        return Observable.create(emitter -> {
            emitter.setDisposable(Observable.fromIterable(airCharts.getCharts())
                    .flatMap(Arrays::<AirCharts.Row>arraySingle)
                    .toList().subscribe((strings, throwable) -> {
                        String[] hAvgRaw = strings.get(DailyAirConditionDetail.ROW_HOUR_AVERAGE);
                        String[] dAvgRaw = strings.get(DailyAirConditionDetail.ROW_DAY_AVERAGE);
                        String[] minRaw = strings.get(DailyAirConditionDetail.DAY_MIN);
                        String[] maxRaw = strings.get(DailyAirConditionDetail.DAY_MAX);


                        for (KoreanProvince province : KoreanProvince.values()) {
                            DailyAirConditionDetail.DailyAirConditionDetailBuilder builder = DailyAirConditionDetail.builder();
                            try {
                                builder.measurements(java.util.Arrays.asList(
                                        Measurement.builder().measureType(MeasureType.HOUR_AVG).value(Double.parseDouble(hAvgRaw[province.ordinal()])).build(),
                                        Measurement.builder().measureType(MeasureType.DAY_AVG).value(Double.parseDouble(dAvgRaw[province.ordinal()])).build(),
                                        Measurement.builder().measureType(MeasureType.MIN).value(Double.parseDouble(minRaw[province.ordinal()])).build(),
                                        Measurement.builder().measureType(MeasureType.MAX).value(Double.parseDouble(maxRaw[province.ordinal()])).build()
                                ));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            builder.publishTime(hAvgRaw[18])
                                    .displayTime(hAvgRaw[17])
                                    .province(province)
                                    .item(AirConditionService.ItemCode.parse(hAvgRaw[20]));
                            emitter.onNext(builder.build());
                        }
                        emitter.onComplete();
                    }));
        });
    }

    enum MeasureType{
        HOUR_AVG(ROW_HOUR_AVERAGE),
        DAY_AVG(ROW_DAY_AVERAGE),
        MAX(DAY_MAX),
        MIN(DAY_MIN);

        final int index;
        MeasureType(int idx) {
            index = idx;
        }
    }

    @SerializedName("disTm")
    private String displayTime;

    @SerializedName("genTm")
    private String publishTime;

    @SerializedName("province")
    private KoreanProvince province;

    @SerializedName("code")
    private AirConditionService.ItemCode item;

    @SerializedName("values")
    private List<Measurement> measurements = new ArrayList<>();

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Measurement {
        @SerializedName("measType")
        MeasureType measureType;

        @SerializedName("value")
        Double value;

    }

}
