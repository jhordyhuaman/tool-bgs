package pe.innobyte.toosanalizer.core.model;

import java.util.ArrayList;
import java.util.List;

import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.*;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.SleepState.*;
import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepAnalyzerTwo {

    private List<? extends MiBandActivitySample> samples;

    float StageSleepNine = 0; // AC
    int StageSleepTen = 0; // AD
    int lastStageSleepTen = 0; // AD -> last-value
    int StageSleepEleven = 0;     // AE


    // Todo: Columns: AF
    float heartAVGLastSleep = 0;
    int heartMAXvalueSleep = 0;

    public float getHeartAVGLastSleep() {  // in replace to FILTER_HEART_AVG_DOWN
        return heartAVGLastSleep;
    }
    public SleepAnalyzerTwo setHeartMAXvalueSleep(int maxvalueSleep) {
        this.heartMAXvalueSleep = maxvalueSleep;
        return this;
    }
    public SleepAnalyzerTwo setHeartAVGLastSleep(float heartAVGLastSleep) {
        this.heartAVGLastSleep = heartAVGLastSleep;
        return this;
    }

    public void filtersV2(MiBandActivitySample sample) {
        // Stage 9: Calculate Heart Rate Valid average down in the last 30 minutes                                 // AC  todo: new
        StageSleepNine = getAverageHeartDown(30, sample.getTimestamp(), samples.reversed());

        // Stage 10: Detect if sleep is starting with heart rate                                                  // AD  todo: new  -> FIXED stageTen
        if(sample.getStageSeven() == 1){
            if(StageSleepNine < getHeartAVGLastSleep()){
                if(StageSleepNine < getHeartAVGLastSleep()){
                    StageSleepTen = 4;
                }else{
                    StageSleepTen = 0;
                }
            }else{
                if(lastStageSleepTen == 4){
                    if(StageSleepNine < heartMAXvalueSleep){
                        StageSleepTen = 4;
                    }else {
                        StageSleepTen = 0;
                    }
                }else{
                    StageSleepTen = 0;
                }

            }

        }else{
            StageSleepTen = 0;
        }

        // Stage 11: Validate new start sleep with awake and heart rate                                           // AE  todo: new
        if(StageSleepTen == 4){
            if(sample.getKind() == 3){
                StageSleepEleven = AWAKE;
            }else{
                StageSleepEleven = SLEEPING;
            }
        }else {
            StageSleepEleven = ACTIVITY;
        }

        // No Stage: Update indicators for tha next iteration
         lastStageSleepTen = StageSleepTen;


        // Add data to table
        dataListTable2.add(new Object[]{
                getDateFromSample(sample),
                StageSleepNine,    // AC
                StageSleepTen,     // AD
                StageSleepEleven,  // AE
        });
    }

    public List<MiBandActivitySample>  runProcess( List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
        List<MiBandActivitySample>  list = new ArrayList<>();
        for (MiBandActivitySample sample : samples) {
            filtersV2(sample);


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
