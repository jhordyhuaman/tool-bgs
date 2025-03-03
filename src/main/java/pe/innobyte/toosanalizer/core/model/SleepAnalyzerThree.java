package pe.innobyte.toosanalizer.core.model;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.*;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.AWAKE_THRESHOLD;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.SleepState.*;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.SleepState.REPOSE;
import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepAnalyzerThree {
    private List<? extends MiBandActivitySample> samples;

    int StageSleepThirteen = 0; // AH
    int StageSleepFifteen = 0; // AJ
    int StageSleepSixteen = 0; // AK
    int StageSleepSeventeen = 0; // AL
    int StageSleepEighteen = 0; // AM
    int StageSleepNineteen = 0; // AN
    int StageSleepTwenty = 0; // AO
    int StageSleepFinal = 0; // AQ

    int heartAVGLastSleep = 0; //FILTER_HEART_AVG_DOWN
    int heartMINLastSleep = 0; //FILTER_HEART_MIN_PERIOD_SLEEP

    public int getHeartAVGLastSleep() {
        return heartAVGLastSleep;
    }

    public SleepAnalyzerThree setHeartAVGLastSleep(int heartAVGLastSleep) {
        this.heartAVGLastSleep = heartAVGLastSleep;
        return this;
    }

    public int getHeartMINLastSleep() {
        return heartMINLastSleep;
    }

    public SleepAnalyzerThree setHeartMINLastSleep(int heartMINLastSleep) {
        this.heartMINLastSleep = heartMINLastSleep;
        return this;
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
                        if (sample.getStageTen() < getHeartAVGLastSleep()) {
                            StageSleepThirteen = SLEEPING;
                        } else {
                            if (sample.getHeartRate() > getHeartAVGLastSleep()) {
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
            if (getKind(sample.getKind()) == SLEEPING) {
                if (sample.getStageTen() == -1) {
                    StageSleepThirteen = ACTIVITY;
                } else {
                    StageSleepThirteen = REPOSE;
                }
            } else {
                StageSleepThirteen = getKind(sample.getKind());
            }
        }


        // Stage 15: Validate Heart Valid distinct to zero                                                        // AJ  todo: new
        if (sample.getStageFourteen() == 0) {
            StageSleepFifteen = 0;
        } else {
            int sumEight = getSumStageEightUpDown(sample.getTimestamp(), samples);
            int sumFourteen = getSumStageFourteenUpDown(sample.getTimestamp(), samples);
            StageSleepFifteen = (int) Math.round((double) sumEight / sumFourteen);
            //System.out.println("Date :"+new Date(sample.getTimestamp() * 1000L)+" kind: "+sample.getKind()+" sumEight: "+ sumEight + " sumFourteen: " + sumFourteen + " StageFifteen: "+StageSleepFifteen);
        }

        // Stage 16: Validate new start sleep with awake and heart rate                                           // AK  todo: new
        if (sample.getStageTen() == -1) {
            if (getKind(sample.getKind()) == SLEEPING) {
                if (StageSleepThirteen == REPOSE) {
                    if (StageSleepFifteen > getHeartAVGLastSleep()) {
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
                if (StageSleepFifteen < getHeartAVGLastSleep()) {
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
            if (StageSleepFifteen < getHeartMINLastSleep()) {
                StageSleepSixteen = 4;
            } else {
                if (StageSleepFifteen < getHeartAVGLastSleep()) {
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
                if (StageSleepFifteen > getHeartAVGLastSleep()) {
                    if (sample.getKind() == AWAKE) {
                        if (sample.getStageTen() == -1) {
                            StageSleepEighteen = ACTIVITY;
                        } else {
                            StageSleepEighteen = AWAKE;
                        }
                    } else {
                        if (sample.getHeartRate() > getHeartAVGLastSleep()) {
                            StageSleepEighteen = ACTIVITY;
                        } else {
                            StageSleepEighteen = StageSleepSeventeen;
                        }
                    }
                } else {
                    if (sample.getHeartRate() > getHeartAVGLastSleep()) {
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

        /*if(getKind(sample.getKind()) == SLEEPING) {
            if(StageSleepThirteen == ACTIVITY){
                StageSleepNineteen = 0;
            }else{
                if(){ // todo : AL 15 min down count Sleep (4) > 10

                }
            }
        }else{
            StageSleepNineteen = 0;
        }*/

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

    public MiBandActivitySample[] runProcess( List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
        MiBandActivitySample[] list = new MiBandActivitySample[0];
        for (MiBandActivitySample sample : samples) {
            filtersV3(sample);
            sample.setKind(StageSleepFinal); // FINAL RAW KIND
            list = ArrayUtils.add(list, sample);
        }
        return list;
    }


    // Methods only view data
      List<Object[]> dataListTable3= new ArrayList<>();
    public Object[][] getLogUI() {
        return dataListTable3.toArray(new Object[0][]);
    }

}
