package com.doodream.data.client.model.air;

import com.doodream.data.model.air.DailyAirConditionDetail;
import com.doodream.data.util.serdes.Array;
import com.doodream.data.util.serdes.ArrayItem;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class AirCharts {

    @SerializedName("charts")
    List<Row> charts;

    public static Iterable<DailyAirConditionDetail> toAirCondition(AirCharts airCharts) {
        List<Row> charts = airCharts.getCharts();
        if(!(charts.size() > 4)) {
            return Collections.EMPTY_LIST;
        }
        Row hourAvgRow = charts.get(0);
        Row dailyAvgRow = charts.get(1);
        return null;
    }


    @Array(cls = String.class)
    @Data
    public class Row {
        @SerializedName("AREA_1")
        @ArrayItem(index = 0)
        private String seoulValue;

        @SerializedName("AREA_2")
        @ArrayItem(index = 1)
        private String pusanValue;

        @ArrayItem(index = 2)
        @SerializedName("AREA_3")
        private String taeguValue;

        @ArrayItem(index = 3)
        @SerializedName("AREA_4")
        private String incheonValue;

        @ArrayItem(index = 4)
        @SerializedName("AREA_5")
        private String gwangjuValue;

        @ArrayItem(index = 5)
        @SerializedName("AREA_6")
        private String daejeonValue;

        @ArrayItem(index = 6)
        @SerializedName("AREA_7")
        private String ulsanValue;

        @ArrayItem(index = 7)
        @SerializedName("AREA_8")
        private String kyungkiValue;

        @ArrayItem(index = 8)
        @SerializedName("AREA_9")
        private String kangwonValue;

        @ArrayItem(index = 9)
        @SerializedName("AREA_10")
        private String chungbukValue;

        @ArrayItem(index = 10)
        @SerializedName("AREA_11")
        private String chungnamValue;

        @ArrayItem(index = 11)
        @SerializedName("AREA_12")
        private String jeonbukValue;

        @ArrayItem(index = 12)
        @SerializedName("AREA_13")
        private String jeonnamValue;

        @ArrayItem(index = 13)
        @SerializedName("AREA_14")
        private String sejongValue;

        @ArrayItem(index = 14)
        @SerializedName("AREA_15")
        private String kyungbukValue;

        @ArrayItem(index = 15)
        @SerializedName("AREA_16")
        private String kyungnamValue;

        @ArrayItem(index = 16)
        @SerializedName("AREA_17")
        private String jejuValue;

        @SerializedName("DIS_TIME")
        private String displayTime;

        @SerializedName("DATA_TIME")
        private String pubTime;

        @SerializedName("DATA_GUBUN")
        private String category;

        @SerializedName("ITEM_CODE")
        private String itemCode;
    }
}
