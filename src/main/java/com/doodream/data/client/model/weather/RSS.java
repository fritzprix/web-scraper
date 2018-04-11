package com.doodream.data.client.model.weather;

import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


@Data
@Root(name = "rss",strict = false)
public class RSS {

    @Element(name = "channel")
    private Channel channel;

}
