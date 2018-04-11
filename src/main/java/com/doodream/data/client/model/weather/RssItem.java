package com.doodream.data.client.model.weather;

import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="item", strict = false)
@Data
public class RssItem {
    @Element(name = "author")
    String author;

    @Element(name = "category")
    String category;

    @Element(name = "title")
    String title;

    @Element(name = "link")
    String link;

    @Element(name = "description")
    Description description;
}
