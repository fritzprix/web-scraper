package com.doodream.data.util.serdes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Array {
    Class<String> cls();
}
