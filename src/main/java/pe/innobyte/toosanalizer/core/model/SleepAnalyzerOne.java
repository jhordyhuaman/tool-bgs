package pe.innobyte.toosanalizer.core.model;

import java.util.ArrayList;
import java.util.List;

import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.*;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.SleepState.*;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.getHeartRateNextValue;
import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepAnalyzerOne {

    private int endSleepFlag = 0; //end_sleep
    private int lastSleepStartFlag = 0; // last_var3
    private int isSleepStarting = 0; // ok
    private int sleepState = 0; //rawkind
    private int lastSleepState = 0; //last_rawkind
    private float lastAwakeFlag = 0; // last_awake
    private int stageSleepOne = 0; // var1
    private float stageSleepZero = 0; //
    private int stageSleepTwo = 0; // var2
    private int stageSleepThree = 0; // var3
    private int stageSleepFour = 0; // var4
    private int stageSleepFive = 0; // var5

    // loop 2 variables
    int StageSleepSeven = 0; // AA
    int StageSleepEight = 0; // AB

    private List<? extends MiBandActivitySample> samples;

    public SleepAnalyzerOne(List<MiBandActivitySample> correctData) {
        this.samples = correctData;
    }

    public void filters(MiBandActivitySample sample, int index) {
        float IntensityForm = (sample.getIntensity());
        if (endSleepFlag == 1) {
            endSleepFlag = 0;
        }
        int timestamp = sample.getTimestamp();

        float average60Up = calculateAverageIntensityUp(60, timestamp, samples);                        // I
        float average60Down = calculateAverageIntensityDown(60, timestamp, samples);                    // J
        float start05min = calculateAverageIntensityMiddle(samples, timestamp, 5);                      // N
        float start30min = calculateAverageIntensityMiddle(samples, timestamp, 30);                     // M
        float start60min = calculateAverageIntensityMiddle(samples, timestamp, 60);                     // L
        float start90min = calculateAverageIntensityMiddle(samples, timestamp, 90);                     // K
        float up05Min = calculateAverageIntensityUp(5, timestamp, samples);                             // O
        float down05min = calculateAverageIntensityDown(5, timestamp, samples);                         // P
        float down15min = calculateAverageIntensityDown(15, timestamp, samples);                        // Q

        if (lastSleepState == 0) {
            lastSleepState = 1;
        }

        // Stage 0: Detect intensity in 60 minute UP and DOWN average                                              // F  todo: New
        stageSleepZero = (average60Up + average60Down) / 2;

        // Stage 1: Detect activity low in the last hour                                                           // R
        if (average60Up < FILTER_UP) {
            stageSleepOne = 1;
        }

        // Stage 2: Detect movement low in the next hour in current timestamp                                      // S  todo: Modified
        if (stageSleepZero < FILTER_AVG_60_MIN_UP_DOWN) {
            if (((average60Down + down05min) / 2) < FILTER_DOWN) {
                stageSleepTwo = 1;
            } else {
                stageSleepTwo = 0;
            }
        } else {
            stageSleepTwo = 0;
        }

        // Stage 2.1: Detect if sleep is starting                                                                  // T
        if (stageSleepOne == 1 && stageSleepTwo == 1) {
            isSleepStarting = 1;
        } else {
            isSleepStarting = 0;
        }

        // Stage 3: Detect if sleep is starting                                                                    // W
        if (isSleepStarting == 1 && start05min < FITTER_5_MIN && start30min < FITTER_30_MIN &&
                start60min < FITTER_60_MIN && start90min < FITTER_90_MIN) {
            stageSleepThree = 1;
        } else {
            stageSleepThree = 0;
        }

        // Stage 4: Detect continuity of sleep                                                                     // X
        if (stageSleepThree == 1) {
            stageSleepFour = 1;
        } else {
            if (lastSleepStartFlag == 1) {
                stageSleepFour = 1;
            }
        }

        // Stage 5: Detect possible end of sleep                                                                   // U
        if (stageSleepFour == 1) {
            if (up05Min < FILTER_MARK_UP && down05min >= FILTER_MARK_DOWN) {
                stageSleepFive = 1;
            } else {
                stageSleepFive = 0;
            }
        }

        // Stage 5.1: Detect end of sleep                                                                          // V
        if (stageSleepFive == 1) {
            if (up05Min < FITTER_5_UP && down05min > FITTER_5_DOWN &&
                    down15min > FITTER_15_DOWN && average60Down > FILTER_END_SLEEP) {
                endSleepFlag = 1;
            } else {
                endSleepFlag = 0;
            }
        }

        // Stage 5.2: Update sleep state                                                                           // Z
        if (endSleepFlag == 1) {
            sleepState = ACTIVITY;
        } else {
            if (stageSleepThree == 1) {
                sleepState = SLEEPING;
            } else {
                sleepState = lastSleepState;
            }
        }

        // No Stage: Update indicators for tha next iteration
        lastSleepStartFlag = stageSleepThree;
        lastSleepState = sleepState;
        // lastStageSleepEleven = StageSleepEleven;

        // Stage 6: Detect Awakes new calculated                                                                   // G  todo: Modified to column Y
        if (sleepState == 4 || sleepState == 3) {
            if (IntensityForm > AWAKE_THRESHOLD) {
                sleepState = AWAKE;
                lastAwakeFlag = 1;
            } else {
                if (lastAwakeFlag == 1) {
                    if (IntensityForm > MIN_AWAKE_FILTER) {
                        sleepState = AWAKE;
                    } else {
                        lastAwakeFlag = 0;
                    }
                } else {
                    lastAwakeFlag = 0;
                }
            }
        }


        //--------------------------------- Version 2 : forms calculation ------------------------------------

        // Stage 7: Detect Heart Rate Valid                                                                       // AA  todo: new
        if (getKind(sleepState) == SLEEPING) {
            if (sample.getHeartRate() < 250) {
                StageSleepSeven = 1;
            } else if (getHeartRatePrevValue(sample,samples) < 250) {
                if (getHeartRateNextValue(sample,samples) < 250) {
                    StageSleepSeven = 1;
                } else {
                    StageSleepSeven = 0;
                }
            } else {
                StageSleepSeven = 0;
            }
        } else {
            StageSleepSeven = 0;
        }

        // Stage 8: Fixed Heart Rate valid next or previous value                                                 // AB  todo: new -> FIXED stageEight
        if (sample.getHeartRate() < 250) {
            StageSleepEight = sample.getHeartRate();
        } else if (getHeartRatePrevValue(sample,samples) < 250) {
            if (getHeartRateNextValue(sample, samples) < 250) {
                StageSleepEight = getHeartRatePrevValue(sample,samples);
            } else if (getHeartRateNextValue(sample, samples) < 250) {
                StageSleepEight = getHeartRateNextValue(sample, samples);
            } else {
                StageSleepEight = 0;
            }
        }

        // Add data to table
        dataListTable1.add(new Object[]{
                getDateFromSample(sample),
                sleepState,
                sample.getHeartRate(),
                lastAwakeFlag,        // G
                stageSleepZero,    // F
                stageSleepOne,     // R
                stageSleepTwo,     // S
                stageSleepThree,   // T
                stageSleepFour,    // W
                stageSleepFive,    // X

                StageSleepSeven,   // AA
                StageSleepEight,   // AB
        });

    }

    public List<MiBandActivitySample>  runProcess() {
        List<MiBandActivitySample> list =  new ArrayList<>();
        int index = 0;
        for (MiBandActivitySample sample : samples) {
            filters(sample,index);
            sample.setKind(sleepState); // correct form
            sample.setStageSeven(StageSleepSeven); // AA
            sample.setStageEight(StageSleepEight); // AB
            sample.setStageZero(stageSleepZero); // F
            list.add(sample);
            index++;
        }
        return list;
    }

    // Methods only view data
    List<Object[]> dataListTable1 = new ArrayList<>();

    public Object[][] getLogUI() {
        return dataListTable1.toArray(new Object[0][]);
    }
}
