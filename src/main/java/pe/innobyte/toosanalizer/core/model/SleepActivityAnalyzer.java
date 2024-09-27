package pe.innobyte.toosanalizer.core.model;

import java.util.ArrayList;
import java.util.List;

import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepActivityAnalyzer {
    private List<? extends MiBandActivitySample> samples;
    public static final float MOVEMENT_DIVISOR = 180.0f;

    // Constantes de filtro
    private static final float FILTER_UP = 0.262f;
    private static final float FILTER_DOWN = 0.0135f;
    private static final float FITTER_5_MIN = 0.075f;
    private static final float FITTER_30_MIN = 0.110f;
    private static final float FITTER_60_MIN = 0.150f;
    private static final float FITTER_90_MIN = 0.160f;
    private static final float FILTER_MARK_UP = 0.092f;
    private static final float FILTER_MARK_DOWN = 0.1000f;
    private static final float FITTER_5_UP = 0.090f;
    private static final float FITTER_5_DOWN = 0.0985f;
    private static final float FITTER_15_DOWN = 0.0900f;
    private static final float FILTER_END_SLEEP = 0.0683f;
    private static final float MIN_AWAKE_FILTER = 0.035f;
    private static final float AWAKE_THRESHOLD = 0.075f;

    // Variables de estado
    private int endSleepFlag = 0;
    private int lastSleepStartFlag = 0;
    private int isSleepStarting = 0;
    private int isSleepOngoing = 0;
    private int sleepEndFlag = 0;
    private int sleepState = 0;
    private int lastSleepState = 0;
    private float lastAwakeFlag = 0;

    public SleepActivityAnalyzer(List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
    }

    /**
     * Analiza las muestras de actividad y determina el estado de sueño o actividad.
     *
     * @return Array de muestras con el estado actualizado.
     */
    public MiBandActivitySample[] analyzeSleepActivity() {
        List<MiBandActivitySample> analyzedSamples = new ArrayList<>();

        for (int i = 0; i < samples.size(); i++) {
            MiBandActivitySample sample = samples.get(i);
            float normalizedIntensity = sample.getIntensity();

            if (endSleepFlag == 1) {
                endSleepFlag = 0;
            }

            updateSleepState(i);

            // Actualiza el estado de vigilia
            if (sleepState == SleepState.SLEEPING || sleepState == SleepState.AWAKE) {
                if (normalizedIntensity > AWAKE_THRESHOLD) {
                    sleepState = SleepState.AWAKE;
                    lastAwakeFlag = 1;
                } else {
                    if (lastAwakeFlag == 1) {
                        if (normalizedIntensity > MIN_AWAKE_FILTER) {
                            sleepState = SleepState.AWAKE;
                            lastAwakeFlag = 1;
                        } else {
                            lastAwakeFlag = 0;
                        }
                    } else {
                        lastAwakeFlag = 0;
                    }
                }
            }

            sample.setKind(sleepState); // Actualiza el estado en la muestra
            if(sleepState == 4 || sleepState == 3){
                System.err.println(STR."Date : \{getDateFromSample(sample)}- sleepState : \{sample.getKind()} | isSleepStarting :\{isSleepStarting} | isSleepOngoing :\{isSleepOngoing} | sleepEndFlag :\{sleepEndFlag} | endSleepFlag :\{endSleepFlag}");
            }else {
                System.out.println(STR."Date : \{getDateFromSample(sample)}- sleepState : \{sample.getKind()} | isSleepStarting :\{isSleepStarting} | isSleepOngoing :\{isSleepOngoing} | sleepEndFlag :\{sleepEndFlag} | endSleepFlag :\{endSleepFlag}");
            }

            analyzedSamples.add(sample);
        }

        return analyzedSamples.toArray(new MiBandActivitySample[0]);
    }

    /**
     * Actualiza el estado de sueño basado en las muestras y los filtros aplicados.
     *
     * @param currentIndex Índice de la muestra actual.
     */
    private void updateSleepState(int currentIndex) {
        float average60Up = calculateAverageIntensityUp(60, currentIndex, samples,false);
        float average60Down = calculateAverageIntensityDown(60, currentIndex, samples);

        float averageStart5Min = calculateAverageIntensityMiddle(currentIndex, 5);
        float averageStart30Min = calculateAverageIntensityMiddle(currentIndex, 30);
        float averageStart60Min = calculateAverageIntensityMiddle(currentIndex, 60);
        float averageStart90Min = calculateAverageIntensityMiddle(currentIndex, 90);

        float averageUp5Min = calculateAverageIntensityUp(5, currentIndex, samples,false);
        float averageDown5Min = calculateAverageIntensityDown(5, currentIndex, samples);
        float averageDown15Min = calculateAverageIntensityDown(15, currentIndex, samples);

        if (lastSleepState == 0) {
            lastSleepState = SleepState.ACTIVE;
        }

        int isAboveFilterUp = (average60Up < FILTER_UP) ? 1 : 0;
        int isBelowFilterDown = (((average60Down + averageDown5Min) / 2) < FILTER_DOWN) ? 1 : 0;
        int isSleepConditionMet = (isAboveFilterUp == 1 && isBelowFilterDown == 1) ? 1 : 0;

        // Determina si inicia el sueño
        if (isSleepConditionMet == 1 && averageStart5Min < FITTER_5_MIN &&
                averageStart30Min < FITTER_30_MIN &&
                averageStart60Min < FITTER_60_MIN &&
                averageStart90Min < FITTER_90_MIN) {
            isSleepStarting = 1;
        } else {
            isSleepStarting = 0;
        }

        // Actualiza el estado de sueño en curso
        if (isSleepStarting == 1 || lastSleepStartFlag == 1) {
            isSleepOngoing = 1;
        }

        // Determina si el sueño ha terminado
        if (isSleepOngoing == 1) {
            if (averageUp5Min < FILTER_MARK_UP && averageDown5Min >= FILTER_MARK_DOWN) {
                sleepEndFlag = 1;
            } else {
                sleepEndFlag = 0;
            }
        }

        // Actualiza el indicador de fin de sueño
        if (sleepEndFlag == 1) {
            if (averageUp5Min < FITTER_5_UP &&
                    averageDown5Min > FITTER_5_DOWN &&
                    averageDown15Min > FITTER_15_DOWN &&
                    average60Down > FILTER_END_SLEEP) {
                endSleepFlag = 1; // Fin del sueño
            } else {
                endSleepFlag = 0;
            }
        }

        // Actualiza el estado de sueño actual
        if (endSleepFlag == 1) {
            sleepState = SleepState.ACTIVE;
        } else if (isSleepStarting == 1) {
            sleepState = SleepState.SLEEPING;
        } else {
            sleepState = lastSleepState;
        }

        lastSleepStartFlag = isSleepStarting;
        lastSleepState = sleepState;
    }

    /**
     * Calcula el promedio de intensidad hacia atrás desde la muestra actual.
     *
     * @param windowSize Tamaño de la ventana en minutos.
     * @param currentIndex Índice de la muestra actual.
     * @param samples Lista de muestras.
     * @param skipCurrent Indica si se debe saltar la muestra actual.
     * @return Promedio de intensidad.
     */
    private float calculateAverageIntensityUp(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples, boolean skipCurrent) {
        int offset = skipCurrent ? 1 : 0;
        float sumIntensity = 0;
        int count = 0;

        for (int i = offset; i < windowSize; i++) {
            int index = currentIndex - i;
            if (index >= 0) {
                sumIntensity += samples.get(index).getIntensity();
                count++;
            } else {
                break;
            }
        }

        return (count == 0) ? 0 : roundToThreeDecimals(sumIntensity / count);
    }

    /**
     * Calcula el promedio de intensidad hacia adelante desde la muestra actual.
     *
     * @param windowSize Tamaño de la ventana en minutos.
     * @param currentIndex Índice de la muestra actual.
     * @param samples Lista de muestras.
     * @return Promedio de intensidad.
     */
    private float calculateAverageIntensityDown(int windowSize, int currentIndex, List<? extends MiBandActivitySample> samples) {
        float sumIntensity = 0;
        int count = 0;

        for (int i = 0; i < windowSize; i++) {
            int index = currentIndex + i;
            if (index < samples.size()) {
                sumIntensity += samples.get(index).getIntensity();
                count++;
            } else {
                break;
            }
        }

        return (count == 0) ? 0 : roundToThreeDecimals(sumIntensity / count);
    }

    /**
     * Calcula el promedio de intensidad combinando ventanas hacia atrás y hacia adelante.
     *
     * @param currentIndex Índice de la muestra actual.
     * @param windowSize Tamaño de la ventana en minutos.
     * @return Promedio de intensidad.
     */
    private float calculateAverageIntensityMiddle(int currentIndex, int windowSize) {
        int windowUp = 0, windowDown = 0;
        switch (windowSize) {
            case 90:
                windowUp = 45;
                windowDown = 45;
                break;
            case 60:
                windowUp = 30;
                windowDown = 30;
                break;
            case 30:
                windowUp = 15;
                windowDown = 15;
                break;
            case 5:
                windowUp = 2;
                windowDown = 3;
                break;
            default:
                // Manejo de casos no previstos
                windowUp = windowSize / 2;
                windowDown = windowSize / 2;
                break;
        }

        float avgUp = calculateAverageIntensityUp(windowUp, currentIndex, samples, true);
        float avgDown = calculateAverageIntensityDown(windowDown, currentIndex, samples);

        return roundToThreeDecimals((avgUp + avgDown) / 2);
    }

    /**
     * Redondea un número flotante a tres decimales.
     *
     * @param value Valor a redondear.
     * @return Valor redondeado.
     */
    private float roundToThreeDecimals(float value) {
        return Math.round(value * 1000f) / 1000f;
    }

    /**
     * Enumeración de estados de sueño.
     */
    private static class SleepState {
        public static final int ACTIVE = 1;
        public static final int SLEEPING = 4;
        public static final int AWAKE = 3;
    }
}
