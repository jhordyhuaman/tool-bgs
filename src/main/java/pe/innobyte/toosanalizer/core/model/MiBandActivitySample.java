package pe.innobyte.toosanalizer.core.model;

public class MiBandActivitySample implements ActivitySample  {
    private int timestamp;
    private long deviceId;
    private long userId;
    private int Kind;
    private float Intensity;
    private int steps;
    private int heartRate;

    // Columns for loop 2
    private  int stageSeven;
    private  int stageEight;
    private  float stageZero;
    // Columns for loop 3
    private  int stageTen;
    private  int stageEleven;


    private int sleepSateLevel;

    public MiBandActivitySample() {
    }

    public MiBandActivitySample(ActivitySample sample) {
        this.timestamp = sample.getTimestamp();
        //this.Kind = sample.getKind();
        this.Intensity = sample.getIntensity();
        this.steps = sample.getSteps();
        this.heartRate = sample.getHeartRate();
    }

    public int getStageSeven() {
        return stageSeven;
    }

    public void setStageSeven(int stageSeven) {
        this.stageSeven = stageSeven;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getKind() {
        return Kind;
    }

    public void setKind(int kind) {
        Kind = kind;
    }

    public float getIntensity() {
        return Intensity;
    }
    public void setIntensity(float intensity) {
        Intensity = intensity;
    }
    public int getSteps() {
        return steps;
    }
    public void setSteps(int steps) {
        this.steps = steps;
    }
    public int getHeartRate() {
        return heartRate;
    }
    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
    public int getStageEight() {
        return stageEight;
    }
    public void setStageEight(int stageEight) {
        this.stageEight = stageEight;
    }
    public int getSleepSateLevel() {
        return sleepSateLevel;
    }
    public void setSleepSateLevel(int sleepSateLevel) {
        this.sleepSateLevel = sleepSateLevel;
    }

    public int getStageTen() {
        return stageTen;
    }
    public void setStageTen(int stageTen) {
        this.stageTen = stageTen;
    }

    public float getStageZero() {
        return stageZero;
    }

    public int getStageEleven() {
        return stageEleven;
    }

    public void setStageEleven(int stageEleven) {
        this.stageEleven = stageEleven;
    }

    public void setStageZero(float stageZero) {
        double roundedStageZero = Math.round(stageZero * 1000.0) / 1000.0;
         this.stageZero = (float) roundedStageZero;
    }
}
