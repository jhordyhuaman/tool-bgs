package pe.innobyte.toosanalizer.core.model;

import java.util.Date;

public class HeartModel {
    Date time;
    int value;

    public HeartModel(Date time, int value) {
        this.time = time;
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}