package com.doodream.data.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class KVPair<K,V> {
    private K Key;
    private V Value;


    public static <K,V> KVPair<K,V> pair(K key, V value) {
        return new KVPair<K,V>(key, value);
    }
}
