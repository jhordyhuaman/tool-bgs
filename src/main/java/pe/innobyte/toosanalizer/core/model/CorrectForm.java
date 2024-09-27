package pe.innobyte.toosanalizer.core.model;

import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class CorrectForm{
    private List<? extends MiBandActivitySample> samples;
    public static float MOVEMENT_DIVISOR = 180.0f; //256.0f;


    public CorrectForm(List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
    }


    float filterUp = (float) 0.262;
    float filterDown = (float) 0.0135; // 0.0145 <- 23/02/2022

    float fitter05min = (float) 0.075;
    float fitter30min = (float) 0.110;
    float fitter60min = (float) 0.150;
    float fitter90min = (float) 0.160;

    float filterMarkUp = (float) 0.092;
    float filterMarkDown = (float) 0.1000; // 0.1100 <- 16/02/2022

    float fitter05Up = (float) 0.090; // 0.080 <- before
    float fitter05Down = (float) 0.0985;
    float fitter15Down = (float) 0.0900;

    //float filterEndSleep = (float) 0.0750; // 0.0616 <- 20/01/2022
    float filterEndSleep = (float) 0.0683; // 0.0616 <- 20/01/2022
    float minAwakeFilter = (float) 0.035;

    float average60Up,average60Down = 0;
    float start05min,start30min,start60min,start90min = 0;
    float up05Min,down05min,down15min = 0;




    public MiBandActivitySample[] applyFormKind(){


        MiBandActivitySample[] list = new MiBandActivitySample[0];

        for (MiBandActivitySample sample : samples){
            //float IntensityForm = (sample.getIntensity()/MOVEMENT_DIVISOR); // activate RAW kind (int)
            float IntensityForm = (sample.getIntensity()); // activate RAW kind (float)

            if(end_sleep == 1){ end_sleep = 0;}
            filters(sample);

            //- - - - - - - - - - -  awake changes- - - - - - - - - - - -//
            if (rawkind == 4 || rawkind == 3){
                if (IntensityForm>AWAKE_RANK){
                    rawkind = 3; // awake
                    last_awake = 1;
                }else{
                    if (last_awake == 1){

                        if(IntensityForm>minAwakeFilter){
                            rawkind = 3; // awake
                            last_awake = 1;
                        }else {
                            last_awake = 0;
                        }

                    }else {
                        last_awake = 0;
                    }

                }
            }
            //sample.setRawKindForm(rawkind); // correct form
            sample.setKind(rawkind); // correct form


            // print result
            if(rawkind == 4 || rawkind == 3){
                System.err.println(STR."Date : \{getDateFromSample(sample)}- rawkind : \{sample.getKind()} | var1 :\{var1} | var2 :\{var2} | var3 :\{var3} | var4 :\{var4} | var5 :\{var5} | OK :\{ok} | END_SLEEP :\{end_sleep}");
            }else {
                System.out.println(STR."Date : \{getDateFromSample(sample)}- rawkind : \{sample.getKind()} | var1 :\{var1} | var2 :\{var2} | var3 :\{var3} | var4 :\{var4} | var5 :\{var5} | OK :\{ok} | END_SLEEP :\{end_sleep}");
            }

            list = ArrayUtils.add(list, sample);
        }

        return list;
    }


    int end_sleep,last_var3,ok,var1,var2,var3,var4,var5 = 0;

    int rawkind,last_rawkind = 0; // 4 sleep // 1 // activity

    // awakes filters

    float last_awake = 0;
    float AWAKE_RANK = (float) 0.075;

    public void filters(MiBandActivitySample sample){
        int timestamp = sample.getTimestamp();
        // first variables
        average60Up = getAverageUp(60,timestamp,samples);

        average60Down = getAverageDown(60,timestamp,samples);


        // seconds variables
        start05min = getAverageMiddle(timestamp,5);
        start30min = getAverageMiddle(timestamp,30);
        start60min = getAverageMiddle(timestamp,60);
        start90min = getAverageMiddle(timestamp,90);

        // three variables
        up05Min = getAverageUp(5,timestamp,samples);
        down05min = getAverageDown(5,timestamp,samples);
        down15min = getAverageDown(15,timestamp,samples);

        if(last_rawkind == 0){ last_rawkind = 1;}

        if (average60Up < filterUp){
            var1 = 1;
        }


        if(((average60Down+down05min)/2) < filterDown){
            var2 = 1;
        }else{
            var2 = 0;
        }

        if(var1 == 1 && var2 ==1){
            ok = 1;
        }else {
            ok = 0;
        }

        //// Inicio de sueño

        if( ok == 1 && start05min < fitter05min &&
                start30min < fitter30min &&
                start60min < fitter60min &&
                start90min < fitter90min){

            var3 = 1;

        }else {
            var3 = 0;
        }

        //--

        if(var3 == 1){
            var4 = 1;
        }else {
            if(last_var3 == 1){  //*****
                var4 = 1;
            }
        }



        //-------
        if(var4 == 1){
            if(up05Min < filterMarkUp && down05min >= filterMarkDown){
                var5 = 1;
            }else {
                var5 = 0;
            }
        }

        ///
        if(var5 == 1){
            if(up05Min < fitter05Up &&
                    down05min> fitter05Down &&
                    down15min>fitter15Down &&
                    average60Down>filterEndSleep){

                end_sleep = 1; // FIN

            }else {
                end_sleep = 0;
            }
        }



        if(end_sleep == 1){
            rawkind = 1; // activity
        }else {
            if(var3 == 1){
                rawkind = 4; // sleep

            }else {

                rawkind = last_rawkind; // sleep || activity
            }
        }


        last_var3 = var3;
        last_rawkind = rawkind;
    }

    //methods others

    public float getAverageUp(int rows, int timestamp, List<? extends ActivitySample> samples, Integer... self){
        int index = (self != null)? 1:0;
        float averageIntensity = 0;
        int num_rows = 0;

        for(int i = 0; i< samples.size(); i++){ // loop numbers rows same [exel]
            if(samples.get(i).getTimestamp() == timestamp){
                for (int j = index; j < rows; j ++){
                    int indexData = (1+ (i-j));
                    if( indexData >= 0){
                        num_rows++;
                        averageIntensity = (averageIntensity + samples.get(indexData).getIntensity());
                        // System.out.println("i : "+i+" | j : "+j+" | num_rows ++ :"+num_rows+ " | index :"+indexData+" avg : "+averageIntensity);
                    }

                }
            }
        }
        averageIntensity = averageIntensity / (num_rows);


        return   BigDecimal.valueOf(averageIntensity)
                .setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();

    }
    public float getAverageDown(int rows, int timestamp, List<? extends ActivitySample> samples){
        float averageIntensity = 0;
        int num_rows = 0;
        for(int i = 0; i< samples.size(); i++){
            if(samples.get(i).getTimestamp() == timestamp){
                for (int j = 0; j < rows; j ++){
                    // change: (i-j) => up (i+j) => down
                    //Timber.e(" index i and indes j : %s"," AverageDown"+rows+": "+(i)+"-"+j);
                    if(i != samples.size() && samples.size() > (i+j)){
                        num_rows ++;
                        averageIntensity = (averageIntensity + samples.get((i+j)).getIntensity());
                    }

                }
            }
        }
        averageIntensity = averageIntensity / num_rows;


        return  BigDecimal.valueOf(averageIntensity)
                .setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();
    }
    public float getAverageMiddle(int timestamp,int minutes){
        int minutesDown = 0,minutesUp =0;
        switch (minutes){
            case 90 : minutesDown = 45; minutesUp = 45;
                break;
            case 60 : minutesDown = 30; minutesUp = 30;
                break;
            case 30 : minutesDown = 15; minutesUp = 15;
                break;
            case 5 : minutesDown = 3; minutesUp = 2;
                break;
        }


        float avgUp =  BigDecimal.valueOf(getAverageUp(minutesUp, timestamp, samples,1)).setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();
        float avgDown =  BigDecimal.valueOf(getAverageDown(minutesDown, timestamp, samples)).setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();

        return BigDecimal.valueOf((avgUp+avgDown)/2)
                .setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();
    }

}