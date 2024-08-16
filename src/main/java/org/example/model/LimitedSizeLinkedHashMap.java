package org.example.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class LimitedSizeLinkedHashMap<K,V> extends LinkedHashMap<K,V> implements Serializable {
    private final int maxSize;

    public LimitedSizeLinkedHashMap(int maxSize){
        super(maxSize + 1, 1.0f, true);
        this.maxSize = maxSize;
    }

    @Override
    public boolean removeEldestEntry(Map.Entry<K, V> eldest){
        return size() > maxSize;
    }
}
