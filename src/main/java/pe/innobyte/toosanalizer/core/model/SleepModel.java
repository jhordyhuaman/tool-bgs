package pe.innobyte.toosanalizer.core.model;

import java.util.Date;

public class SleepModel {
    Date datetime;
    String level;
    long seconds;

    public SleepModel() {
    }

    public SleepModel(Date datetime, String level, long seconds) {
        this.datetime = datetime;
        this.level = level;
        this.seconds = seconds;
    }

    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }
}
