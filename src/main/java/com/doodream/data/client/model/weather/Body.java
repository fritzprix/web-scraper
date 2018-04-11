package com.doodream.data.client.model.weather;

import lombok.Data;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Data
@Root(strict = false)
public class Body {
    @ElementList(inline = true)
    List<Location> location;
}
