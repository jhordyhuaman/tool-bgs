package pe.innobyte.toosanalizer.core.others;

import com.google.gson.JsonArray;
import org.apache.commons.lang3.ArrayUtils;
import pe.innobyte.toosanalizer.core.model.MiBandActivitySample;

import java.util.ArrayList;
import java.util.List;

import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.*;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.SleepState.*;
import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepActivityAnalyzer5 {
    private List<? extends MiBandActivitySample> samples;

    public SleepActivityAnalyzer5(List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
    }

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

    // VERSION 2 : Forms Calculation

    // VARIABLES
    int StageSleepSeven = 0; // AA
    int StageSleepEight = 0; // AB
    int StageSleepNine = 0; // AC
    int StageSleepTen = 0; // AD   FIN -> -1
    int StageSleepEleven = 0;     // AF
    int lastStageSleepEleven = 0; // AF
    int StageSleepTwelve = 0; // AG
    int StageSleepThirteen = 0; // AH
    int StageSleepFourteen = 0; // AI
    int StageSleepFifteen = 0; // AJ
    int StageSleepSixteen = 0; // AK
    int StageSleepSeventeen = 0; // AL
    int StageSleepEighteen = 0; // AM
    int StageSleepNineteen = 0; // AN
    int StageSleepTwenty = 0; // AO
    int StageSleepFinal = 0; // AQ


    private int FILTER_HEART_AVG_DOWN = 0;
    private int FILTER_HEART_MIN_PERIOD_SLEEP = 0;
    public void setAvg60HeartRate(float value) {
        this.FILTER_HEART_AVG_DOWN = (int) (value);
    }
    public void setMinValueHeart(float value) {
        this.FILTER_HEART_MIN_PERIOD_SLEEP = (int) value;
    }

    private JsonArray sleepRawData = new JsonArray();




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

    public void filtersV2(MiBandActivitySample sample) {
        // Stage 9: Calculate Heart Rate Valid average down in the last 7 minutes                                 // AC  todo: new
        StageSleepNine = getAverageHeartDown(7, sample.getTimestamp(), samples.reversed());

        // Stage 10: Detect if sleep is starting with heart rate                                                  // AD  todo: new  -> FIXED stageTen
        if (sample.getStageZero() < FILTER_AVG_60_MIN_UP_DOWN) {
            if (StageSleepNine >= FILTER_HEART_AVG_DOWN) {       // if (StageSleepNine > FILTER_HEART_AVG_DOWN) {
                // FIN -> -1
                StageSleepTen = -1;
            } else if (sample.getHeartRate() < FILTER_HEART_AVG_DOWN) {
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
                if (StageSleepNine <= FILTER_HEART_AVG_DOWN) {
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

    public void filtersV3(MiBandActivitySample sample) {
        int sumDown = getStageSleepTwelveSumDown(5, sample.getTimestamp(), samples);
       // System.out.println("Date :"+new Date(sample.getTimestamp() * 1000L)+" kind: "+ sample.getKind()+ " s-11: " + sample.getStageEleven()+" s-12: "+sample.getStageTwelve()+" s-13: "+StageSleepThirteen + " sumDown: "+sumDown);

        // Stage 13: Validate new start sleep with awake and heart rate                                           // AH  todo: new values -> SLEEPING, REPOSE, ACTIVITY, AWAKE
        if (sample.getStageEleven() == 1) {
            if (sample.getStageTwelve() == 1) {
                if (sample.getKind() == AWAKE) {
                    StageSleepThirteen = AWAKE;
                } else {

                    if ( sumDown > 4) {
                        if (sample.getStageTen() < FILTER_HEART_AVG_DOWN) {
                            StageSleepThirteen = SLEEPING;
                        } else {
                            if (sample.getHeartRate() > FILTER_HEART_AVG_DOWN) {
                                StageSleepThirteen = REPOSE;
                            } else {
                                StageSleepThirteen = SLEEPING;
                            }
                        }

                    } else {
                        StageSleepThirteen = REPOSE;
                    }
                }
            } else {
                if (getKind(sample.getKind()) == ACTIVITY) {
                    StageSleepThirteen = ACTIVITY;
                } else {
                    StageSleepThirteen = REPOSE;
                }
            }
        } else {
            if (getKind(sleepState) == SLEEPING) {
                if (sample.getStageTen() == -1) {
                    StageSleepThirteen = ACTIVITY;
                } else {
                    StageSleepThirteen = REPOSE;
                }
            } else {
                StageSleepThirteen = getKind(sleepState);
            }
        }


        // Stage 15: Validate Heart Valid distinct to zero                                                        // AJ  todo: new
        if (sample.getStageFourteen() == 0) {
            StageSleepFifteen = 0;
        } else {
            int sumEight = getSumStageEightUpDown(sample.getTimestamp(), samples);
            int sumFourteen = getSumStageFourteenUpDown(sample.getTimestamp(), samples);
            StageSleepFifteen = (sumEight / sumFourteen);

        }
        // Stage 16: Validate new start sleep with awake and heart rate                                           // AK  todo: new
        if (sample.getStageTen() == -1) {
            if (getKind(sample.getKind()) == SLEEPING) {
                if (StageSleepThirteen == REPOSE) {
                    if (StageSleepFifteen > FILTER_HEART_AVG_DOWN) {
                        if (sample.getIntensity() > AWAKE_THRESHOLD) {
                            if (sample.getKind() == AWAKE) {
                                StageSleepSixteen = 3;
                            } else {
                                StageSleepSixteen = 4;
                            }
                        } else {
                            StageSleepSixteen = 4;
                        }
                    } else {
                        StageSleepSixteen = 4;
                    }

                } else {
                    StageSleepSixteen = 2;
                }
            } else {
                if (StageSleepFifteen < FILTER_HEART_AVG_DOWN) {
                    if (sample.getHeartRate() < 250) {
                        StageSleepSixteen = 2;
                    } else {
                        StageSleepSixteen = 1;
                    }
                } else {
                    StageSleepSixteen = 1;
                }
            }
        } else {
            if (StageSleepFifteen < FILTER_HEART_MIN_PERIOD_SLEEP) {
                StageSleepSixteen = 4;
            } else {
                if (StageSleepFifteen < FILTER_HEART_AVG_DOWN) {
                    StageSleepSixteen = 4;
                } else {
                    if (sample.getIntensity() > AWAKE_THRESHOLD) {
                        StageSleepSixteen = sample.getKind();
                    } else {
                        if (getKind(sample.getKind()) == ACTIVITY) {
                            StageSleepSixteen = ACTIVITY;
                        } else {
                            StageSleepSixteen = SLEEPING;
                        }
                    }
                }
            }
        }

        // Stage 17: Pre valid final Sleep stage new calculation                                                  // AL  todo: new
        if (StageSleepThirteen == SLEEPING) {
            if (StageSleepSixteen == 4) {
                StageSleepSeventeen = 4;
            } else {
                if (getKind(sample.getKind()) == ACTIVITY) {
                    StageSleepSeventeen = ACTIVITY;
                } else {
                    StageSleepSeventeen = SLEEPING;
                }
            }
        } else {
            if (StageSleepThirteen == AWAKE) {
                StageSleepSeventeen = AWAKE;
            } else {
                if (StageSleepThirteen == ACTIVITY) {
                    if (StageSleepSixteen == REPOSE) { // ??
                        StageSleepSeventeen = ACTIVITY;
                    } else {
                        StageSleepSeventeen = ACTIVITY;
                    }
                } else {
                    if (StageSleepThirteen == REPOSE) {
                        StageSleepSeventeen = REPOSE;
                    } else {
                        StageSleepSeventeen = ACTIVITY;
                    }
                }
            }
        }

        // Stage 18: Column  Valid start sleep with awake and heart rate                                          // AM  todo: new
        if (StageSleepSeventeen == 1) {
            StageSleepEighteen = 1;
        } else {
            if (getKind(sample.getKind()) == ACTIVITY) {
                if (StageSleepSeventeen == 4) {
                    StageSleepEighteen = 4;
                } else {
                    if (StageSleepThirteen == SLEEPING) {
                        StageSleepEighteen = SLEEPING;
                    } else {
                        StageSleepEighteen = ACTIVITY;
                    }
                }
            } else {
                if (StageSleepFifteen > FILTER_HEART_AVG_DOWN) {
                    if (sample.getKind() == AWAKE) {
                        if (sample.getStageTen() == -1) {
                            StageSleepEighteen = ACTIVITY;
                        } else {
                            StageSleepEighteen = AWAKE;
                        }
                    } else {
                        if (sample.getHeartRate() > FILTER_HEART_AVG_DOWN) {
                            StageSleepEighteen = ACTIVITY;
                        } else {
                            StageSleepEighteen = StageSleepSeventeen;
                        }
                    }
                } else {
                    if (sample.getHeartRate() > FILTER_HEART_AVG_DOWN) {
                        if (sample.getIntensity() > FILTER_AWAKE_END_SLEEP) { // CHECK VARIABLE -> FILTER_AWAKE_END_SLEEP
                            StageSleepEighteen = AWAKE;
                        } else {
                            StageSleepEighteen = SLEEPING;
                        }
                    } else {
                        if (sample.getIntensity() > AWAKE_THRESHOLD) {
                            StageSleepEighteen = AWAKE;
                        } else {
                            StageSleepEighteen = SLEEPING;
                        }
                    }
                }
            }
        }

        // Stage 19: Validate last columns calculated if sleeping                                                 // AN  todo: new
        int sumLastColumns = (StageSleepSixteen + StageSleepSeventeen + StageSleepEighteen);

        if (StageSleepThirteen == ACTIVITY) {
            StageSleepNineteen = 0;
        } else {
            if (sumLastColumns >= 5) {
                StageSleepNineteen = 1;
            } else {
                StageSleepNineteen = 0;
            }
        }

        // Stage: 20 Validate end sleep period                                                                    // AO  todo: new
        if (sumLastColumns < 5) {
            StageSleepTwenty = 1;
        } else {
            if (StageSleepNineteen == 1) {
                StageSleepTwenty = 0;
            } else {
                if (sumLastColumns < 5) {
                    StageSleepTwenty = 1;
                } else {
                    if (StageSleepNineteen == 1) {
                        StageSleepTwenty = 0;
                    } else {
                        if (sumLastColumns < 5) {
                            StageSleepTwenty = 1;
                        } else {
                            StageSleepTwenty = 0;
                        }
                    }
                }
            }
        }

        // Stage Final: final value kind of sleep                                                                 // AQ  todo: new
        if (StageSleepNineteen == 1) {
            if (sample.getKind() == AWAKE) {
                StageSleepFinal = AWAKE;
            } else {
                if (sample.getIntensity() > AWAKE_THRESHOLD) {
                    StageSleepFinal = AWAKE;
                } else {
                    StageSleepFinal = SLEEPING;
                }
            }
        } else {
            if (StageSleepThirteen == ACTIVITY) {
                StageSleepFinal = ACTIVITY;
            } else {
                if (sample.getIntensity() > AWAKE_THRESHOLD) {
                    StageSleepFinal = AWAKE;
                } else {
                    StageSleepFinal = REPOSE;
                }
            }
        }

        // Add data to table
        dataListTable3.add(new Object[]{
                getDateFromSample(sample),
                StageSleepThirteen,    // AH
                StageSleepFifteen,     // AJ
                StageSleepSixteen,     // AK
                StageSleepSeventeen,   // AL
                StageSleepEighteen,    // AM
                StageSleepNineteen,      // AN
                StageSleepTwenty,       // AO
                StageSleepFinal,       // AQ
        });
    }

    public List<MiBandActivitySample>  runCoreStageSleepOne() {
        List<MiBandActivitySample> list =  new ArrayList<>();
        int index = 0;
        for (MiBandActivitySample sample : samples) {
            filters(sample,index);
            sample.setKind(sleepState); // correct form
            sample.setStageEight(StageSleepEight); // AB
            sample.setStageZero(stageSleepZero); // F
            list.add(sample);
            index++;
        }
        return list;
    }
    public List<MiBandActivitySample>  runCoreStageSleepTwo( List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
        List<MiBandActivitySample>  list = new ArrayList<>();
        for (MiBandActivitySample sample : samples) {
            filtersV2(sample);
            //sample.setKind(StageSleepFinal); // correct form
            sample.setStageTwelve(StageSleepTwelve); // AG
            sample.setStageFourteen(StageSleepFourteen); // AI
            sample.setStageTen(StageSleepTen);     // AD
            sample.setStageEleven(StageSleepEleven); // AF
            list.add(sample);
        }
        return list;
    }

    public MiBandActivitySample[] applyCalculatedFormsV3( List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
        MiBandActivitySample[] list = new MiBandActivitySample[0];
        for (MiBandActivitySample sample : samples) {
            filtersV3(sample);
            sample.setKind(StageSleepFinal); // correct form
            list = ArrayUtils.add(list, sample);
        }
        return list;
    }


    // Methods only view data
    List<Object[]> dataListTable1 = new ArrayList<>();
    List<Object[]> dataListTable2 = new ArrayList<>();
    List<Object[]> dataListTable3= new ArrayList<>();
    public Object[][] getDataTable1View() {
         return dataListTable1.toArray(new Object[0][]);
    }
    public Object[][] getDataTable2View() {
        return dataListTable2.toArray(new Object[0][]);
    }
    public Object[][] getDataTable3View() {
        return dataListTable3.toArray(new Object[0][]);
    }

}