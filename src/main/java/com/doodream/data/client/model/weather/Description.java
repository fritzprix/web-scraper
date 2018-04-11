package com.doodream.data.client.model.weather;

import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@Root(name = "description", strict = false)
public class Description {
    @Element(name = "header")
    String header;
    @Element(name = "body")
    Body body;
}
