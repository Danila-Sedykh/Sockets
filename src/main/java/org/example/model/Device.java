package org.example.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

public class Device implements Serializable {
    private int name;
    private int teg;
    private LimitedSizeLinkedHashMap<LocalDateTime, Integer> values;
    private int port;



    public Device(int name, int teg, int port) {
        this.name = name;
        this.values = new LimitedSizeLinkedHashMap<>(10);
        this.teg = teg;
        this.port = port;
    }

    public int getName() {
        return name;
    }

    public void addValue(int value) {
        values.put(LocalDateTime.now(),value);
    }

    public int getPort() {
        return port;
    }

    public LimitedSizeLinkedHashMap<LocalDateTime, Integer> getValues() {
        //System.out.println("Device " + name + " values: " + values);
        return values;
    }

    public int getTeg(){
        return teg;
    }

    public int getFeature(int featureIndex) {
        int index = 0;
        for (Map.Entry<LocalDateTime, Integer> entry : values.entrySet()) {
            if (index == featureIndex) {
                return entry.getValue();
            }
            index++;
        }
        throw new IllegalArgumentException("Неправильный индекс признака");
    }
}
