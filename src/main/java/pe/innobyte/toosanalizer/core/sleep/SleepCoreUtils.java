package pe.innobyte.toosanalizer.core.sleep;

import pe.innobyte.toosanalizer.core.model.MiBandActivitySample;

import java.math.BigDecimal;
import java.util.List;

public class SleepCoreUtils {

    public static final float MOVEMENT_DIVISOR = 180.0f;

    // Constantes de filtro
    public static final float FILTER_UP = 0.262f;
    public static final float FILTER_DOWN = 0.0135f;
    public static final float FITTER_5_MIN = 0.075f;
    public static final float FITTER_30_MIN = 0.110f;
    public static final float FITTER_60_MIN = 0.150f;
    public static final float FITTER_90_MIN = 0.160f;
    public static final float FILTER_MARK_UP = 0.092f;
    public static final float FILTER_MARK_DOWN = 0.1000f;
    public static final float FITTER_5_UP = 0.090f;
    public static final float FITTER_5_DOWN = 0.0985f;
    public static final float FITTER_15_DOWN = 0.0900f;
    public static final float FILTER_END_SLEEP = 0.0683f;
    public static final float MIN_AWAKE_FILTER = 0.035f;
    public static final float AWAKE_THRESHOLD = 0.075f;

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


        return   BigDecimal.valueOf(averageIntensity)
                .setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();
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
        return  BigDecimal.valueOf(averageIntensity)
                .setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();
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
        switch (windowSize){
            case 90 : minutesDown = 45; minutesUp = 45;
                break;
            case 60 : minutesDown = 30; minutesUp = 30;
                break;
            case 30 : minutesDown = 15; minutesUp = 15;
                break;
            case 5 : minutesDown = 3; minutesUp = 2;
                break;
        }


        float avgUp =  BigDecimal.valueOf(calculateAverageIntensityUp(minutesUp, currentIndex, samples,1)).setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();
        float avgDown =  BigDecimal.valueOf(calculateAverageIntensityDown(minutesDown, currentIndex, samples)).setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();

        return BigDecimal.valueOf((avgUp+avgDown)/2)
                .setScale( 3 , BigDecimal.ROUND_HALF_EVEN).floatValue();
    }



    /**
     * Enumeración de estados de sueño.
     */
    public static class SleepState {
        public static final int ACTIVITY = 1;
        public static final int SLEEPING = 4;
        public static final int AWAKE = 3;
    }
}
