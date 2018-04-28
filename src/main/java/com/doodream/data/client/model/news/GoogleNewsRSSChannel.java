package com.doodream.data.client.model.news;

import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Data
@Root(name = "channel", strict = false)
public class GoogleNewsRSSChannel {

    @Element(name = "generator")
    String generator;

    @Element(name = "title")
    String title;

    @Element(name = "link")
    String link;

    @Element(name = "language")
    String language;

    @Element(name = "webMaster")
    String webMaster;

    @Element(name = "pubDate")
    String pubDate;

    @Element(name = "lastBuildDate")
    String lastBuildDate;

    @ElementList(inline = true)
    List<GoogleNewsRSSItem> items;
}
