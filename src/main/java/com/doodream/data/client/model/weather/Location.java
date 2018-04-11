package com.doodream.data.client.model.weather;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@lombok.Data
@Root(name = "location", strict = false)
public class Location {

    @Attribute(name = "wl_ver", required = false)
    String wlVer;

    @Element(name = "province")
    String province;
    @Element(name = "city")
    String city;

    @ElementList(inline = true)
    List<Data> data;
}
