package pe.innobyte.toosanalizer.core.others;

import com.google.gson.JsonArray;
import org.apache.commons.lang3.ArrayUtils;
import pe.innobyte.toosanalizer.core.model.MiBandActivitySample;

import java.util.ArrayList;
import java.util.List;

import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.*;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.SleepState.*;
import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepActivityAnalyzer4 {
    private final List<? extends MiBandActivitySample> samples;
    public SleepActivityAnalyzer4(List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
    }

    private int endSleepFlag = 0; //end_sleep
    private int lastSleepStartFlag = 0; // last_var3
    private int isSleepStarting = 0; // ok
    private int sleepState = 0; //rawkind
    private int lastSleepState = 0; //last_rawkind
    private float lastAwakeFlag = 0; // last_awake
    private int stageSleepOne = 0; // var1
    private int stageSleepTwo = 0; // var2
    private int stageSleepThree = 0; // var3
    private int stageSleepFour = 0; // var4
    private int stageSleepFive = 0; // var5

    private JsonArray sleepRawData = new JsonArray();


    public MiBandActivitySample[] applyFormKind(){
        MiBandActivitySample[] list = new MiBandActivitySample[0];
        for (MiBandActivitySample sample : samples){
            filters(sample);
            sample.setKind(sleepState); // correct form
            // add new column : sleepSateLevel2
           // sample.setSleepSateLevel(sleepSateLevel2);
            list = ArrayUtils.add(list, sample);
        }
        System.out.println(sleepRawData.toString());
        return list;
    }

    public void filters(MiBandActivitySample sample){
        float IntensityForm = (sample.getIntensity());
        if(endSleepFlag == 1){ endSleepFlag = 0;}

        int timestamp = sample.getTimestamp();

        float average60Up = calculateAverageIntensityUp(60, timestamp, samples);
        float average60Down = calculateAverageIntensityDown(60, timestamp, samples);
        float start05min = calculateAverageIntensityMiddle(samples, timestamp, 5);
        float start30min = calculateAverageIntensityMiddle(samples, timestamp, 30);
        float start60min = calculateAverageIntensityMiddle(samples, timestamp, 60);
        float start90min = calculateAverageIntensityMiddle(samples, timestamp, 90);
        float up05Min =   calculateAverageIntensityUp(5, timestamp, samples);
        float down05min = calculateAverageIntensityDown(5, timestamp, samples);
        float down15min = calculateAverageIntensityDown(15, timestamp, samples);

        if(lastSleepState == 0){ lastSleepState = 1;}

        // Stage 1: Detect activity low in the last hour
        if (average60Up < FILTER_UP){stageSleepOne = 1;}

        // Stage 2: Detect movement low in the next hour in current timestamp
        if(((average60Down + down05min)/2) < FILTER_DOWN)
        {stageSleepTwo = 1;}else{stageSleepTwo = 0;}

        // Stage 2.1: Detect if sleep is starting
        if(stageSleepOne == 1 && stageSleepTwo ==1)
        {isSleepStarting = 1;}else {isSleepStarting = 0;}

        // Stage 3: Detect if sleep is starting
        if( isSleepStarting == 1 && start05min < FITTER_5_MIN && start30min < FITTER_30_MIN &&
          start60min < FITTER_60_MIN &&start90min < FITTER_90_MIN)
        {stageSleepThree = 1;}else {stageSleepThree = 0;}

        // Stage 4: Detect continuity of sleep
        if(stageSleepThree == 1) {stageSleepFour = 1;}else
        {if(lastSleepStartFlag == 1) {stageSleepFour = 1;}}

        // Stage 5: Detect possible end of sleep
        if(stageSleepFour == 1){
            if(up05Min < FILTER_MARK_UP && down05min >= FILTER_MARK_DOWN)
            {stageSleepFive = 1;}else {stageSleepFive = 0;}
        }

        // Stage 5.1: Detect end of sleep
        if(stageSleepFive == 1){if(up05Min < FITTER_5_UP && down05min > FITTER_5_DOWN &&
           down15min > FITTER_15_DOWN &&average60Down > FILTER_END_SLEEP)
            {endSleepFlag = 1;}else {endSleepFlag = 0;}
        }

        // Stage 5.2: Update sleep state
        if(endSleepFlag == 1) {sleepState = ACTIVITY;}else
        {if(stageSleepThree == 1) {sleepState = SLEEPING;}else
        {sleepState = lastSleepState;}}

        // Stage 5.3: Update indicators for tha next iteration
        lastSleepStartFlag = stageSleepThree;
        lastSleepState = sleepState;

        // Stage 5.4: Detect awake state in periods of sleep
        if (sleepState == 4 || sleepState == 3){if (IntensityForm> AWAKE_THRESHOLD)
        {sleepState = AWAKE;lastAwakeFlag = 1;}else
        {if (lastAwakeFlag == 1){if(IntensityForm > MIN_AWAKE_FILTER)
        {sleepState = AWAKE;}else {lastAwakeFlag = 0;}}else
        {lastAwakeFlag = 0;}}}

        // OUTPUT, print result OR SAVE JSON
        // generate JSON
        /*JsonObject sleepData = new JsonObject();
        sleepData.addProperty("intensity", IntensityForm);
        sleepData.addProperty("timestamp", timestamp);
        sleepData.addProperty("heartRate", sample.getHeartRate());
        sleepData.addProperty("stageSleepOne", stageSleepOne);
        sleepData.addProperty("stageSleepTwo", stageSleepTwo);
        sleepData.addProperty("stageSleepThree", stageSleepThree);
        sleepData.addProperty("stageSleepFour", stageSleepFour);
        sleepData.addProperty("stageSleepFive", stageSleepFive);
        sleepData.addProperty("lastSleepState", lastSleepState);
        sleepData.addProperty("lastAwakeFlag", lastAwakeFlag);
        sleepData.addProperty("isSleepStarting", isSleepStarting);
        sleepData.addProperty("endSleepFlag", endSleepFlag);
        sleepData.addProperty("lastSleepStartFlag", lastSleepStartFlag);
        sleepData.addProperty("sleepState", sleepState);
        sleepData.addProperty("average60Up", average60Up);
        sleepData.addProperty("average60Down", average60Down);
        sleepData.addProperty("start05min", start05min);
        sleepData.addProperty("start30min", start30min);
        sleepData.addProperty("start60min", start60min);
        sleepData.addProperty("start90min", start90min);
        sleepData.addProperty("up05Min", up05Min);
        sleepData.addProperty("down05min", down05min);
        sleepData.addProperty("down15min", down15min);
        sleepRawData.add(sleepData);*/


        // Version 2 : Heart Rate Calculation
        calculateStageSleepLevelsOne(sample,timestamp);

       // TODO : --------------------------------  ITERATE FIRST AFTER NEXT STEPS - GENERATE calculateStageLevelTwo() --------------------------------

    }

    private void calculateStageSleepLevelsOne(MiBandActivitySample sample,int timestamp){
        // AC : cant 4 en per sue
        //=SI(Z792="sueño";SI(D792<250;SI(C792=4;1;0);0);0)

        if(isSleep(sleepState) == SLEEPING){
            if(sample.getHeartRate() < 250){
                if(sleepState == 4){
                    isSleepState = 1;
                }else{
                    isSleepState = 0;
                }
            }else{
                isSleepState = 0;
            }
        }else{isSleepState = 0;}

        // prom 15min abajo
        // =SI(AC1409=1;SUMA(D1409:D1423)/15;0)

        if(isSleepState == 1){
            avgDownHeart15min = calculateAverageHeartRateDown(15, timestamp, samples);
        }else{avgDownHeart15min = 0;}

        // AE
        // =SI(Z1491="Activ";SI(E1491=0;SI(D1491<$AE$3;1;0);0);0)
        if(isSleep(sleepState) == ACTIVITY){
            if(sample.getSteps() == 0){
                if(sample.getHeartRate() < ACTIVITY_MAX){
                    isActivityDetected = 1;
                }else{
                    isActivityDetected = 0;
                }
            }else{
                isActivityDetected = 0;
            }
        } else{isActivityDetected = 0;}

        // AF
        //=SI(AE184=1;D184;0)
        if(isActivityDetected == 1){
            heartInActivity = sample.getHeartRate();
        }else{heartInActivity = 0;}

        // AG :
        // =SI(Z188="SUEÑO";SI(AD188<=$AB$3;SI(C188=4;"SLEEP";0);0);0)
        if(isSleep(sleepState) == SLEEPING){
            if(avgDownHeart15min <= HEART_RATE_MAX_IN_SLEEP){
                if (sleepState == SLEEPING){
                    sleepSateNew = 1;
                }else{
                    sleepSateNew = 0;
                }
            }else{
                sleepSateNew = 0;
            }
        }else {sleepSateNew = 0;}

        // AH :
        // =SI(AG944="SLEEP"; SI(Z944="SUEÑO";1;0);SI(AH943=1;SI(Z944="SUEÑO";SI(AD944>=$AG$3;"FIN";1);0);0))

        if (sleepSateNew == 1){
            if(isSleep(sleepState) == SLEEPING){
                sleepSateLevel2 = 1;
            }else{
                sleepSateLevel2 = 0;
            }
        }else{
            if (sleepSateLevel2Last == 1){
                if(isSleep(sleepState) == SLEEPING){
                    if(avgDownHeart15min >= HEART_RATE_IN_SLEEP){
                        sleepSateLevel2 = 2; // FIN
                    }else{
                        sleepSateLevel2 = 1; // CONTINUA
                    }
                }else {
                    sleepSateLevel2 = 0;
                }
            }else {
                sleepSateLevel2 = 0;
            }

        }

        sleepSateLevel2Last = sleepSateLevel2;

        //System.out.println(STR."\{getDateFromSample(sample)} |  Activ. : \{isSleep(sleepState) }  | AC: \{isSleepState}  | AD: \{avgDownHeart15min}  | AE: \{isActivityDetected}   | AF: \{heartInActivity}   | *AG: \{sleepSateNew}   | *AH: \{sleepSateLevel2}   | *AH-: \{sleepSateLevel2Last}");

        dataList.add(new Object[]{getDateFromSample(sample),isSleep(sleepState),isSleepState,avgDownHeart15min,isActivityDetected,heartInActivity,sleepSateNew,sleepSateLevel2});
    }


    public MiBandActivitySample[] calculateStageSleepLevelsTwo(MiBandActivitySample[] samplesLevels) {
        // sleepStage = sample.getKind();
        // sleepSateLevel2 = sample.getSleepSateLevel();
        String newKind = "";
        int newKindValue = 0;

        MiBandActivitySample[] list = new MiBandActivitySample[0];
        for (MiBandActivitySample sample : samplesLevels){
            int timestamp = sample.getTimestamp();

            // AI
            if(sleepSateLevel3Last == 1){  // sueño-2 = 1
                /*if(isSleep(sample.getKind()) == SLEEPING){
                   /* if (sample.getSleepSateLevel() == 2){
                        sleepSateLevel3 = 2; // END
                    }else {
                        sleepSateLevel3 = 1; // SUEÑO-2
                    }

                }else {
                    sleepSateLevel3 = 0;
                }*/

            }else {
                if(calculateStageLevelDown(10, timestamp, List.of(samplesLevels)) >= STAGE_2_FILTER){
                    sleepSateLevel3 = 1; // SUEÑO-2
                }else {
                    sleepSateLevel3 = 0;
                }
            }

            sleepSateLevel3Last = sleepSateLevel3;

            // AJ
            if (sleepSateLevel3 == 1){
                if(sample.getKind() == SLEEPING){
                    sleepSateLevel4 = 1;
                }else{
                    sleepSateLevel4 = 0;
                }
            }else{
                sleepSateLevel4 = 0;
            }

            // AK
            if(sleepSateLevel3 == 1){
                if(sample.getKind() == SLEEPING){
                    if(sample.getHeartRate() > HEART_RATE_MAX_IN_SLEEP){
                        if(sample.getHeartRate() > HEART_RATE_IN_SLEEP){
                            // move
                            newKind = "move";
                            newKindValue = 8;
                        }else{
                            newKind = "sleep";
                            newKindValue = SLEEPING;
                        }
                    }else{
                        // sleep
                        newKind = "sleep";
                        newKindValue = SLEEPING;
                    }

                }else{
                    // awake
                    newKind = "awake";
                    newKindValue = AWAKE;
                }
            }else{
                // -1 // Activity
                newKind = "activity";
                newKindValue = ACTIVITY;
            }

          //  System.out.println(getDateFromSample(sample)+" | KIND : "+sample.getKind()+" | HEART: "+sample.getHeartRate()+" AH: "+sample.getSleepSateLevel()+" | AI -> Level3: "+sleepSateLevel3 + " | AJ -> Level4: "+sleepSateLevel4+" | "+newKind);
              dataList2.add(new Object[]{getDateFromSample(sample),newKind,sample.getHeartRate(),sleepSateLevel3,sleepSateLevel4,newKind});
            sample.setKind(newKindValue); // todo : ok change to newKindValue
            list = ArrayUtils.add(list, sample);
         }
        return list;
    }



    // CONSTANTS
    private final int ACTIVITY_MAX = 90;
    private final int HEART_RATE_MAX_IN_SLEEP = 61; // AB-3
    private final int HEART_RATE_IN_SLEEP = 66; // AG-3  >=

    private final int STAGE_2_FILTER = 10; // AI-4


    // VARIABLES
    private int isSleepState = 0;// AC-5 : cant 4 en per sue
    private int avgDownHeart15min = 0; // AD-5 :
    private int  isActivityDetected = 0;//AE-5 :
    private int heartInActivity = 0; // AF-5 :
    private int sleepSateNew = 0; // AG-5 :

    private int sleepSateLevel2 = 0; // AH-5 : -> 1 active / 0 false / 2 fin
    private int sleepSateLevel2Last = 0; // AH-5 :

    private int sleepSateLevel3 = 0; // AI-5 : -> 1 active / 0 false / 2 end
    private int sleepSateLevel3Last = 0; // AI-5

    private int sleepSateLevel4 = 0; // AJ-5 : -> 1 active / 0 false / 2 end

    private int isSleep(int sleepState){
        if (sleepState == SLEEPING || sleepState == AWAKE){
            return SLEEPING;
        }
        return ACTIVITY;
    }



    List<Object[]> dataList = new ArrayList<>();
    public Object[][] getDataCalculated() {
        return dataList.toArray(new Object[0][]);
    }

    List<Object[]> dataList2 = new ArrayList<>();
    public Object[][] getDataCalculated2() {
        return dataList2.toArray(new Object[0][]);
    }




}