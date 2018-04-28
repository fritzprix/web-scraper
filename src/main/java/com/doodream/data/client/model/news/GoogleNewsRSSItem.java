package com.doodream.data.client.model.news;

import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@Root(name = "item", strict = false)
public class GoogleNewsRSSItem {

    @Element(name = "title")
    String title;

    @Element(name = "link")
    String link;

    @Element(name = "category")
    String category;

    @Element(name = "guid", required = false)
    String guid;

    @Element(name = "pubDate")
    String pubDate;

    @Element(name = "description")
    String description;
}
