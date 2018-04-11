package com.doodream.data.client.model.weather;

import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@Root(name="channel", strict = false)
public class Channel {
    @Element(name = "title")
    String title;

    @Element(name = "pubDate")
    String pubDate;

    @Element(name = "item")
    RssItem item;
}
