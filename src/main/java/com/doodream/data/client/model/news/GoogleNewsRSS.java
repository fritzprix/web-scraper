package com.doodream.data.client.model.news;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Data
@Root(name = "rss",strict = false)
public class GoogleNewsRSS {

    @Element(name = "channel")
    GoogleNewsRSSChannel channel;
}
