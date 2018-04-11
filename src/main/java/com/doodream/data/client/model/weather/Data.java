package com.doodream.data.client.model.weather;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@lombok.Data
@Root(name = "data")
public class Data {

    @Element(name = "mode")
    String mode;

    @Element(name = "tmEf")
    String time;

    @Element(name = "wf")
    String forecast;

    @Element(name = "tmn")
    Integer minTemp;

    @Element(name = "tmx")
    Integer maxTemp;

    @Element(name = "reliability")
    String reliability;
}
