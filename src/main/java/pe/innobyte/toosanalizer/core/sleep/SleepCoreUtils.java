package pe.innobyte.toosanalizer.core.sleep;

import pe.innobyte.toosanalizer.core.model.MiBandActivitySample;

import java.util.Date;
import java.util.List;

import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.SleepState.*;
import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepCoreUtils {

    public static final float MOVEMENT_DIVISOR = 180.0f;

    public static final float FILTER_UP = 0.0650f; // 0.0708f;
    public static final float FILTER_DOWN = 0.0065f; // 0.0040f;
    public static final float FITTER_5_MIN = 0.075f;
    public static final float FITTER_30_MIN = 0.110f;
    public static final float FITTER_60_MIN = 0.150f;
    public static final float FITTER_90_MIN = 0.160f;
    public static final float FILTER_MARK_UP = 0.030f; // 0.092f
    public static final float FILTER_MARK_DOWN = 0.0408f; //0.1000f
    public static final float FITTER_5_UP = 0.090f;
    public static final float FITTER_5_DOWN = 0.0425f; // 0.0485f;
    public static final float FITTER_15_DOWN = 0.0365f; // 0.0165f;
    public static final float FILTER_END_SLEEP = 0.0400f; // 0.0683f;
    public static final float MIN_AWAKE_FILTER = 0.035f;
    public static final float AWAKE_THRESHOLD = 0.035f;
    // CONSTANTS V2
    public static final float FILTER_AWAKE_END_SLEEP = 0.0350f;
    public static final float FILTER_AVG_60_MIN_UP_DOWN = 0.041f;
    public static final float FILTER_HEART_RATE_CALCULATE = 1.054f;


    /**
     * Calcula el promedio de intensidad hacia atrás desde la muestra actual.
     *
     * @param windowSize Tamaño de la ventana en minutos.
     * @param currentIndex Índice de la muestra actual.
     * @param samples Lista de muestras.
     * @param skipCurrent Indica si se debe saltar la muestra actual.
     * @return Promedio de intensidad.
     */
    public static float calculateAverageIntensityUp(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples, Integer... skipCurrent) {
        int index = (skipCurrent != null)? 1:0;
        float averageIntensity = 0;
        int num_rows = 0;

        for(int i = 0; i< samples.size(); i++){
            if(samples.get(i).getTimestamp() == currentIndex){
                for (int j = index; j < windowSize; j ++){
                    int indexData = (1+ (i-j));
                    if( indexData >= 0){
                        num_rows++;
                        averageIntensity = (averageIntensity + samples.get(indexData).getIntensity());
                    }
                }
            }
        }
        averageIntensity = averageIntensity / (num_rows);
        return averageIntensity;
    }


    /**
     * Calcula el promedio de intensidad hacia adelante desde la muestra actual.
     *
     * @param windowSize Tamaño de la ventana en minutos.
     * @param currentIndex Índice de la muestra actual.
     * @param samples Lista de muestras.
     * @return Promedio de intensidad.
     */
    public static float calculateAverageIntensityDown(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples) {
        float averageIntensity = 0;
        int num_rows = 0;
        for(int i = 0; i< samples.size(); i++){
            if(samples.get(i).getTimestamp() == currentIndex){
                for (int j = 0; j < windowSize; j ++){
                    if(i != samples.size() && samples.size() > (i+j)){
                        num_rows ++;
                        averageIntensity = (averageIntensity + samples.get((i+j)).getIntensity());
                    }

                }
            }
        }
        averageIntensity = averageIntensity / num_rows;
        return  averageIntensity;
    }

    /**
     * Calcula el promedio de intensidad hacia adelante desde la muestra actual.
     *
     * @param windowSize Tamaño de la ventana en minutos.
     * @param currentIndex Índice de la muestra actual.
     * @param samples Lista de muestras.
     * @return Promedio de intensidad.
     */
    public static int calculateAverageHeartRateDown(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples) {
        int averageIntensity = 0;
        int num_rows = 0;
        for(int i = 0; i< samples.size(); i++){
            if(samples.get(i).getTimestamp() == currentIndex){
                for (int j = 0; j < windowSize; j ++){
                    if(i != samples.size() && samples.size() > (i+j)){
                        num_rows ++;
                        averageIntensity = (averageIntensity + samples.get((i+j)).getHeartRate());
                    }

                }
            }
        }
        averageIntensity = averageIntensity / num_rows;
        return  averageIntensity;
    }


    /**
     * Calcula el promedio de intensidad hacia adelante desde la muestra actual.
     * @param windowSize Tamaño de la ventana en minutos.
     * @param currentIndex Índice de la muestra actual.
     * @param samples Lista de muestras.
     * @return .
     */
    public static int calculateStageLevelDown(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples){
        int levelStageValue = 0;
        for(int i = 0; i< samples.size(); i++){
            if(samples.get(i).getTimestamp() == currentIndex){
                for (int j = 0; j < windowSize; j ++){
                    if(i != samples.size() && samples.size() > (i+j)){
                       // levelStageValue = (levelStageValue + samples.get((i+j)).getSleepSateLevel());
                    }

                }
            }
        }
       // System.out.println("Level Stage Value: "+levelStageValue);
        return  levelStageValue;
    }
    /**
     * Calcula el promedio de intensidad combinando ventanas hacia atrás y hacia adelante.
     * @param samples Lista de muestras.
     * @param currentIndex Índice de la muestra actual.
     * @param windowSize Tamaño de la ventana en minutos.
     * @return Promedio de intensidad.
     */
    public static float calculateAverageIntensityMiddle(List<? extends MiBandActivitySample> samples,int currentIndex, int windowSize) {
        int minutesDown = 0,minutesUp =0;
        minutesUp = switch (windowSize) {
            case 90 -> {
                minutesDown = 45;
                yield 45;
            }
            case 60 -> {
                minutesDown = 30;
                yield 30;
            }
            case 30 -> {
                minutesDown = 15;
                yield 15;
            }
            case 5 -> {
                minutesDown = 3;
                yield 2;
            }
            default -> minutesUp;
        };


        float avgUp =  calculateAverageIntensityUp(minutesUp, currentIndex, samples,1);
        float avgDown =  calculateAverageIntensityDown(minutesDown, currentIndex, samples);

        return ((avgUp+avgDown)/2);
    }

    public static int getSumStageEightUpDown(int timestamp, List<? extends MiBandActivitySample> samples) {
        float avgUp =  calculateSumStageEightUp(9, timestamp, samples,0);
        float avgDown =  calculateSumEightDown(8, timestamp, samples,1);

        return (int) ((avgUp+avgDown));
    }

    public static int getSumStageFourteenUpDown(int timestamp, List<? extends MiBandActivitySample> samples) {
        float avgUp =  calculateSumStageFourTeenUp(9, timestamp, samples,0); // 9-0 = 9 => ARRAY-IS = 8 rows
        float avgDown =  calculateSumFourteenDown(8, timestamp, samples,1); // 8 -> in array = 7 rows

        return (int) ((avgUp+avgDown));
    }
    public static float calculateSumStageEightUp(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples, Integer... skipCurrent) {
        int index = (skipCurrent != null)? 1:0;
        float averageIntensity = 0;

        for(int i = 0; i< samples.size(); i++){
            if(samples.get(i).getTimestamp() == currentIndex){
                for (int j = index; j < windowSize; j ++){
                    int indexData = (1+ (i-j));
                    if( indexData >= 0){
                       // System.out.println("DateUP :"+new Date(samples.get(indexData).getTimestamp() * 1000L)+" kind: "+samples.get(indexData).getKind()+" sumEight: "+ samples.get(indexData).getStageEight());
                         averageIntensity = (averageIntensity + samples.get(indexData).getStageEight());
                    }
                }
            }
        }
         return averageIntensity;
    }
    public static float calculateSumStageFourTeenUp(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples, Integer... skipCurrent) {
        int index = (skipCurrent != null)? 1:0;
        float averageIntensity = 0;

        for(int i = 0; i< samples.size(); i++){
            if(samples.get(i).getTimestamp() == currentIndex){
                for (int j = index; j < windowSize; j ++){
                    int indexData = (1+ (i-j));
                    if( indexData >= 0){
                        //System.out.println("14-UP :"+new Date(samples.get(indexData).getTimestamp() * 1000L)+" sum14: "+ samples.get(indexData).getStageFourteen());
                        averageIntensity = (averageIntensity + samples.get(indexData).getStageFourteen());
                    }
                }
            }
        }
        return averageIntensity;
    }

    public static float calculateSumEightDown(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples,Integer... skipCurrent) {
        int index = (skipCurrent != null)? 1:0;
        float averageIntensity = 0;
        for(int i = 0; i< samples.size(); i++){
            if(samples.get(i).getTimestamp() == currentIndex){
                for (int j = index; j < windowSize; j ++){
                    if(i != samples.size() && samples.size() > (i+j)){
                      //  System.out.println("DateDW :"+new Date(samples.get((i+j)).getTimestamp() * 1000L)+" kind: "+samples.get((i+j)).getKind()+" sumEight: "+ samples.get((i+j)).getStageEight());
                        averageIntensity = (averageIntensity + samples.get((i+j)).getStageEight());
                    }

                }
            }
        }
        return  averageIntensity;
    }
    public static float calculateSumFourteenDown(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples,Integer... skipCurrent) {
        int index = (skipCurrent != null)? 1:0;
        float averageIntensity = 0;
        for(int i = 0; i< samples.size(); i++){
            if(samples.get(i).getTimestamp() == currentIndex){
                for (int j = index; j < windowSize; j ++){
                    if(i != samples.size() && samples.size() > (i+j)){
                       // System.out.println("14-DW :"+new Date(samples.get((i+j)).getTimestamp() * 1000L) + " sum14: "+ samples.get((i+j)).getStageFourteen());
                        averageIntensity = (averageIntensity + samples.get((i+j)).getStageFourteen());
                    }

                }
            }
        }
        return  averageIntensity;
    }

    public static int getHeartRatePrevValue(MiBandActivitySample sample,List<? extends MiBandActivitySample> samples) {
        int index = samples.indexOf(sample);
        if (index > 0) {
            return samples.get(index - 1).getHeartRate();
        }
        return 0;
    }

    public static int getHeartRateNextValue(MiBandActivitySample sample, List<? extends MiBandActivitySample> samples) {
        int index = samples.indexOf(sample);
        if (index < samples.size() - 1) {
            return samples.get(index + 1).getHeartRate();
        }
        return 0;
    }

    public static int getKind(int sleepState) {
        if (sleepState == SLEEPING || sleepState == AWAKE) {
            return SLEEPING;
        }
        return ACTIVITY;
    }

    public static int getAverageHeartDown(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples) {
        int totalHeartRate = 0;
        int numRows = 0;

        for (int i = 0; i < samples.size(); i++) {
            if (samples.get(i).getTimestamp() == currentIndex) {
                for (int j = 0; j < windowSize; j++) {
                    int indexData = i - j;
                    if (indexData >= 0) {
                        totalHeartRate += samples.get(indexData).getStageEight(); // stage 8 : heart rate calculated
                        numRows++;
                    }
                }
                break;
            }
        }

        return numRows > 0 ? (int) Math.round((double) totalHeartRate / numRows) : 0;
    }


    public static int getStageSleepTwelveSumDown(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples) {
        int totalAvgDown = 0;

        for (int i = 0; i < samples.size(); i++) {
            if (samples.get(i).getTimestamp() == currentIndex) {
                for (int j = 0; j < windowSize; j++) {
                    int indexData = i - j;
                    if (indexData >= 0) {
                        totalAvgDown += samples.get(indexData).getStageTwelve();
                    }
                }
                break;
            }
        }

        return totalAvgDown;
    }
    /**
     * Enumeración de estados de sueño.
     */
    public static class SleepState {
        public static final int ACTIVITY = 1;
        public static final int REPOSE = 2;
        public static final int SLEEPING = 4;
        public static final int AWAKE = 3;
    }
}
