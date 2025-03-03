package pe.innobyte.toosanalizer.core.others;

import pe.innobyte.toosanalizer.core.model.MiBandActivitySample;

import java.util.ArrayList;
import java.util.List;

import static pe.innobyte.toosanalizer.core.sleep.SleepCoreUtils.*;
import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepActivityAnalyzer {
    private List<? extends MiBandActivitySample> samples;

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

            updateSleepState(sample);

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
              //  System.err.println(STR."Date : \{getDateFromSample(sample)}- sleepState : \{sample.getKind()} | isSleepStarting :\{isSleepStarting} | isSleepOngoing :\{isSleepOngoing} | sleepEndFlag :\{sleepEndFlag} | endSleepFlag :\{endSleepFlag}");
            }else {
              //  System.out.println(STR."Date : \{getDateFromSample(sample)}- sleepState : \{sample.getKind()} | isSleepStarting :\{isSleepStarting} | isSleepOngoing :\{isSleepOngoing} | sleepEndFlag :\{sleepEndFlag} | endSleepFlag :\{endSleepFlag}");
            }

            analyzedSamples.add(sample);
        }

        return analyzedSamples.toArray(new MiBandActivitySample[0]);
    }

    /**
     * Actualiza el estado de sueño basado en las muestras y los filtros aplicados.
     *
     * @param sample Índice de la muestra actual.
     */
    private void updateSleepState(MiBandActivitySample sample) {
        int currentIndex = sample.getTimestamp();
        float average60Up = calculateAverageIntensityUp(60, currentIndex, samples);
        float average60Down = calculateAverageIntensityDown(60, currentIndex, samples);

        float averageStart5Min = calculateAverageIntensityMiddle(samples,currentIndex, 5);
        float averageStart30Min = calculateAverageIntensityMiddle(samples,currentIndex, 30);
        float averageStart60Min = calculateAverageIntensityMiddle(samples,currentIndex, 60);
        float averageStart90Min = calculateAverageIntensityMiddle(samples,currentIndex, 90);


        float averageUp5Min = calculateAverageIntensityUp(5, currentIndex, samples);
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
     * Enumeración de estados de sueño.
     */
    private static class SleepState {
        public static final int ACTIVE = 1;
        public static final int SLEEPING = 4;
        public static final int AWAKE = 3;
    }
}
