package pe.innobyte.toosanalizer.core.model;


public interface ActivitySample   {

    int getTimestamp();
    int getKind();
    float getIntensity();
    int getSteps();
    int getHeartRate();

}
