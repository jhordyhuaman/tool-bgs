package pe.innobyte.toosanalizer.core.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SleepBlockData {
    private Date startDate;
    private Date endDate;
    private String dateSummary;
    private List<ActivitySample> sleepData = new ArrayList<>();

    public SleepBlockData() {
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDateSummary() {
        return dateSummary;
    }

    public void setDateSummary(String dateSummary) {
        this.dateSummary = dateSummary;
    }

    public List<ActivitySample> getSleepData() {
        return sleepData;
    }

    public void setSleepData(List<ActivitySample> sleepData) {
        this.sleepData = sleepData;
    }
}
