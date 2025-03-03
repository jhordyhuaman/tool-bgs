package pe.innobyte.toosanalizer.core.model;

import java.util.ArrayList;
import java.util.List;

import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.*;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.SleepState.AWAKE;
import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepAnalyzerTwo {

    private List<? extends MiBandActivitySample> samples;

    int StageSleepNine = 0; // AC
    int StageSleepTen = 0; // AD   FIN -> -1
    int StageSleepEleven = 0;     // AF
    int lastStageSleepEleven = 0; // AF -> last-value
    int StageSleepTwelve = 0; // AG
    int StageSleepFourteen = 0; // AI


    int heartAVGLastSleep = 0;

    public int getHeartAVGLastSleep() {  // in replace to FILTER_HEART_AVG_DOWN
        return heartAVGLastSleep;
    }

    public SleepAnalyzerTwo setHeartAVGLastSleep(int heartAVGLastSleep) {
        this.heartAVGLastSleep = heartAVGLastSleep;
        return this;
    }

    public void filtersV2(MiBandActivitySample sample) {
        // Stage 9: Calculate Heart Rate Valid average down in the last 7 minutes                                 // AC  todo: new
        StageSleepNine = getAverageHeartDown(7, sample.getTimestamp(), samples.reversed());

        // Stage 10: Detect if sleep is starting with heart rate                                                  // AD  todo: new  -> FIXED stageTen
        if (sample.getStageZero() < FILTER_AVG_60_MIN_UP_DOWN) {
            if (StageSleepNine >=  getHeartAVGLastSleep()) {
                // FIN -> -1
                StageSleepTen = -1;
            } else if (sample.getHeartRate() < getHeartAVGLastSleep()) {
                // heartValidAvgDown
                StageSleepTen =  StageSleepNine;
            } else {
                // FIN -> -1
                StageSleepTen = -1;
            }
        } else {
            // FIN -> -1
            StageSleepTen = -1;
        }
        // Stage 11: Validate new start sleep with awake and heart rate                                           // AF  todo: new
        if (StageSleepTen == -1) {
            if (lastStageSleepEleven == 1) {
                if (StageSleepNine <= getHeartAVGLastSleep()) {
                    if (sample.getKind() == AWAKE) {
                        StageSleepEleven = 1;
                    } else {
                        if (sample.getStageZero() < FILTER_AVG_60_MIN_UP_DOWN) {
                            StageSleepEleven = 1;
                        } else {
                            StageSleepEleven = 0;
                        }
                    }
                } else {
                    StageSleepEleven = 1;
                }

            } else {
                StageSleepEleven = 0;
            }
        } else {
            if (sample.getStageZero() <= FILTER_AVG_60_MIN_UP_DOWN) {
                StageSleepEleven = 1;
            } else {
                StageSleepEleven = 0;
            }
        }

        // Stage 12: Validate new start sleep with awake and heart rate                                           // AG  todo: new -> FIXED  stageTwelve
        if (StageSleepEleven == 1) {
            if (sample.getStageZero() < AWAKE_THRESHOLD) {
                StageSleepTwelve = 1;
            } else {
                StageSleepTwelve = 0;
            }
        } else {
            StageSleepTwelve = 0;
        }

        // Stage 14: Validate Heart Valid distinct to zero                                                         // AI  todo: new -> FIXED stageFourteen
        if (sample.getStageEight() > 0) {
            StageSleepFourteen = 1;
        } else {
            StageSleepFourteen = 0;
        }

        // No Stage: Update indicators for tha next iteration
        lastStageSleepEleven = StageSleepEleven;


        // Add data to table
        dataListTable2.add(new Object[]{
                getDateFromSample(sample),
                StageSleepNine,    // AC
                StageSleepTen,     // AD
                StageSleepEleven,  // AF
                StageSleepTwelve,  // AG
                StageSleepFourteen // AI
        });
    }

    public List<MiBandActivitySample>  runProcess( List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
        List<MiBandActivitySample>  list = new ArrayList<>();
        for (MiBandActivitySample sample : samples) {
            filtersV2(sample);
            sample.setStageTwelve(StageSleepTwelve); // AG
            sample.setStageFourteen(StageSleepFourteen); // AI
            sample.setStageTen(StageSleepTen);     // AD
            sample.setStageEleven(StageSleepEleven); // AF
            list.add(sample);
        }
        return list;
    }

    // Methods only view data
     List<Object[]> dataListTable2 = new ArrayList<>();
    public Object[][] getLogUI() {
        return dataListTable2.toArray(new Object[0][]);
    }

}
