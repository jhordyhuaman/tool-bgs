package pe.innobyte.toosanalizer.core.model;

import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.*;
import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.SleepState.*;
import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepActivityAnalyzer{
    private List<? extends MiBandActivitySample> samples;
    public SleepActivityAnalyzer(List<? extends MiBandActivitySample> samples) {
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




    public MiBandActivitySample[] applyFormKind(){

        MiBandActivitySample[] list = new MiBandActivitySample[0];

        for (MiBandActivitySample sample : samples){

            filters(sample);

            sample.setKind(sleepState); // correct form

            // print result
            if(sleepState == 4 || sleepState == 3){
                System.err.println(STR."Date : \{getDateFromSample(sample)}- rawkind : \{sample.getKind()} | var1 :\{stageSleepOne} | var2 :\{stageSleepTwo} | var3 :\{stageSleepThree} | var4 :\{stageSleepFour} | var5 :\{stageSleepFive} | OK :\{isSleepStarting} | END_SLEEP :\{endSleepFlag}");
            }else {
                System.out.println(STR."Date : \{getDateFromSample(sample)}- rawkind : \{sample.getKind()} | var1 :\{stageSleepOne} | var2 :\{stageSleepTwo} | var3 :\{stageSleepThree} | var4 :\{stageSleepFour} | var5 :\{stageSleepFive} | OK :\{isSleepStarting} | END_SLEEP :\{endSleepFlag}");
            }

            list = ArrayUtils.add(list, sample);
        }

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

    }

}