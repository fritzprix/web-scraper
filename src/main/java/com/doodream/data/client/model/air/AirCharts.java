package com.doodream.data.client.model.air;

import com.doodream.data.util.serdes.Array;
import com.doodream.data.util.serdes.ArrayItem;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class AirCharts {

    @SerializedName("charts")
    List<Row> charts;

    @Array(cls = String.class)
    @Data
    public class Row {
        @SerializedName("AREA_1")
        @ArrayItem(index = 0)
        String seoulValue;

        @SerializedName("AREA_2")
        @ArrayItem(index = 1)
        String pusanValue;

        @ArrayItem(index = 2)
        @SerializedName("AREA_3")
        String taeguValue;

        @ArrayItem(index = 3)
        @SerializedName("AREA_4")
        String incheonValue;

        @ArrayItem(index = 4)
        @SerializedName("AREA_5")
        String gwangjuValue;

        @ArrayItem(index = 5)
        @SerializedName("AREA_6")
        String daejeonValue;

        @ArrayItem(index = 6)
        @SerializedName("AREA_7")
        String ulsanValue;

        @ArrayItem(index = 7)
        @SerializedName("AREA_8")
        String kyungkiValue;

        @ArrayItem(index = 8)
        @SerializedName("AREA_9")
        String kangwonValue;

        @ArrayItem(index = 9)
        @SerializedName("AREA_10")
        String chungbukValue;

        @ArrayItem(index = 10)
        @SerializedName("AREA_11")
        String chungnamValue;

        @ArrayItem(index = 11)
        @SerializedName("AREA_12")
        String jeonbukValue;

        @ArrayItem(index = 12)
        @SerializedName("AREA_13")
        String jeonnamValue;

        @ArrayItem(index = 13)
        @SerializedName("AREA_14")
        String sejongValue;

        @ArrayItem(index = 14)
        @SerializedName("AREA_15")
        String kyungbukValue;

        @ArrayItem(index = 15)
        @SerializedName("AREA_16")
        String kyungnamValue;

        @ArrayItem(index = 16)
        @SerializedName("AREA_17")
        String jejuValue;

        @ArrayItem(index = 17, alias = "display_time")
        @SerializedName("DIS_TIME")
        String displayTime;

        @ArrayItem(index = 18, alias = "published_time")
        @SerializedName("DATA_TIME")
        String pubTime;

        @ArrayItem(index = 19, alias = "category")
        @SerializedName("DATA_GUBUN")
        String category;

        @ArrayItem(index = 20)
        @SerializedName("ITEM_CODE")
        String itemCode;
    }
}
