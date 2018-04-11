package com.doodream.data.model.air;

import com.doodream.data.client.svc.AirConditionService;
import com.doodream.data.util.serdes.ArrayConverter;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public DailyAirConditionDetail measure(MeasureType key, Double value) {
        return this;
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
    private String disDate;
    @SerializedName("genTm")
    private String genDate;
    @SerializedName("province")
    private Province province;

    @SerializedName("code")
    private AirConditionService.ItemCode item;

    @SerializedName("values")
    private List<Measurement> measurements = ArrayConverter.filledList(Measurement.class, 4);

    @Data
    public class Measurement {
        @SerializedName("measType")
        MeasureType measureType;

        @SerializedName("value")
        Double value;
    }



}
