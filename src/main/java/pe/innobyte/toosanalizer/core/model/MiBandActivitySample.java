package pe.innobyte.toosanalizer.core.model;

public class MiBandActivitySample implements ActivitySample  {
    private int timestamp;
    private long deviceId;
    private long userId;
    private int Kind;
    private float Intensity;
    private int steps;
    private int heartRate;

    public MiBandActivitySample() {
    }

    public MiBandActivitySample(ActivitySample sample) {
        this.timestamp = sample.getTimestamp();
        //this.Kind = sample.getKind();
        this.Intensity = sample.getIntensity();
        this.steps = sample.getSteps();
        this.heartRate = sample.getHeartRate();
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
}
