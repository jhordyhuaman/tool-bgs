package pe.innobyte.toosanalizer.core.others;

import pe.innobyte.toosanalizer.core.model.MiBandActivitySample;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pe.innobyte.toosanalizer.utils.DateTimeUtils.getDateFromSample;

public class SleepActivityAnalyzer3 {
    private List<? extends MiBandActivitySample> samples;
    public static final float MOVEMENT_DIVISOR = 180.0f;

    public SleepActivityAnalyzer3(List<? extends MiBandActivitySample> samples) {
        this.samples = samples;
    }

    // Constantes de filtro
    private static final float FILTER_UP = 0.262f;
    private static final float FILTER_DOWN = 0.0135f;
    private static final float FITTER_05_MIN = 0.075f;
    private static final float FITTER_30_MIN = 0.110f;
    private static final float FITTER_60_MIN = 0.150f;
    private static final float FITTER_90_MIN = 0.160f;
    private static final float FILTER_MARK_UP = 0.092f;
    private static final float FILTER_MARK_DOWN = 0.1000f;
    private static final float FITTER_05_UP = 0.090f;
    private static final float FITTER_05_DOWN = 0.0985f;
    private static final float FITTER_15_DOWN = 0.0900f;
    private static final float FILTER_END_SLEEP = 0.0683f;
    private static final float MIN_AWAKE_FILTER = 0.035f;
    private static final float AWAKE_RANK = 0.075f;

    // Variables de estado
    private float average60Up, average60Down;
    private float start05min, start30min, start60min, start90min;
    private float up05Min, down05min, down15min;
    private int end_sleep, last_var3, ok, var1, var2, var3, var4, var5;
    private int rawkind, last_rawkind = 1;
    private float last_awake = 0;

    // Estructuras para promedios precomputados
    private Map<Integer, float[]> avgPrev = new HashMap<>();
    private Map<Integer, float[]> avgNext = new HashMap<>();


    // Estructuras para promedios precomputados
    private float[] avgPrev2, avgPrev5, avgPrev15, avgPrev30, avgPrev45, avgPrev60;
    private float[] avgNext3, avgNext5, avgNext15, avgNext30, avgNext45, avgNext60;

    public List<MiBandActivitySample> applyFormKind() {
        int n = samples.size();
        List<MiBandActivitySample> list = new ArrayList<>(n);

        precomputeAverages();

        for (int i = 0; i < n; i++) {
            MiBandActivitySample sample = samples.get(i);
            float IntensityForm = sample.getIntensity();

            if (end_sleep == 1) { end_sleep = 0; }

            filters(i);

            // Cambios de estado de vigilia
            if (rawkind == 4 || rawkind == 3) {
                if (IntensityForm > AWAKE_RANK) {
                    rawkind = 3; // despierto
                    last_awake = 1;
                } else {
                    if (last_awake == 1) {
                        if (IntensityForm > MIN_AWAKE_FILTER) {
                            rawkind = 3; // despierto
                            last_awake = 1;
                        } else {
                            last_awake = 0;
                        }
                    } else {
                        last_awake = 0;
                    }
                }
            }
            sample.setKind(rawkind); // forma correcta

            // Imprimir resultado
            String dateStr = getDateFromSample(sample);
            if (rawkind == 4 || rawkind == 3) {
                System.err.println("Date: " + dateStr + " - sleepState: " + sample.getKind());
            } else {
                System.out.println("Date: " + dateStr + " - sleepState: " + sample.getKind());
            }

            list.add(sample);
        }

        return list;
    }

    private void precomputeAverages() {
        int n = samples.size();

        avgPrev2 = computeAvgPrev(n, 2);
        avgPrev5 = computeAvgPrev(n, 5);
        avgPrev15 = computeAvgPrev(n, 15);
        avgPrev30 = computeAvgPrev(n, 30);
        avgPrev45 = computeAvgPrev(n, 45);
        avgPrev60 = computeAvgPrev(n, 60);

        avgNext3 = computeAvgNext(n, 3);
        avgNext5 = computeAvgNext(n, 5);
        avgNext15 = computeAvgNext(n, 15);
        avgNext30 = computeAvgNext(n, 30);
        avgNext45 = computeAvgNext(n, 45);
        avgNext60 = computeAvgNext(n, 60);
    }

    private float[] computeAvgPrev(int n, int windowSize) {
        float[] avg = new float[n];
        for (int i = 0; i < n; i++) {
            int count = 0;
            float sum = 0;
            for (int j = i; j >= Math.max(0, i - windowSize + 1); j--) {
                sum += samples.get(j).getIntensity();
                count++;
            }
            avg[i] = sum / count;
        }
        return avg;
    }

    private float[] computeAvgNext(int n, int windowSize) {
        float[] avg = new float[n];
        for (int i = 0; i < n; i++) {
            int count = 0;
            float sum = 0;
            for (int j = i; j < Math.min(n, i + windowSize); j++) {
                sum += samples.get(j).getIntensity();
                count++;
            }
            avg[i] = sum / count;
        }
        return avg;
    }

    public void filters(int i) {
        // Acceso a promedios precomputados
        average60Up = avgPrev60[i];
        average60Down = avgNext60[i];

        start05min = (avgPrev2[i] + avgNext3[i]) / 2;
        start30min = (avgPrev15[i] + avgNext15[i]) / 2;
        start60min = (avgPrev30[i] + avgNext30[i]) / 2;
        start90min = (avgPrev45[i] + avgNext45[i]) / 2;

        up05Min = avgPrev5[i];
        down05min = avgNext5[i];
        down15min = avgNext15[i];

        if(last_rawkind == 0){ last_rawkind = 1;}

        if (average60Up < FILTER_UP){
            var1 = 1;
        }


        if(((average60Down+down05min)/2) < FILTER_DOWN){
            var2 = 1;
        }else{
            var2 = 0;
        }

        if(var1 == 1 && var2 ==1){
            ok = 1;
        }else {
            ok = 0;
        }

        // Inicio de sueño
        if( ok == 1 && start05min < FITTER_05_MIN &&
                start30min < FITTER_30_MIN &&
                start60min < FITTER_60_MIN &&
                start90min < FITTER_90_MIN){

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
            if(up05Min < FILTER_MARK_UP && down05min >= FILTER_MARK_DOWN){
                var5 = 1;
            }else {
                var5 = 0;
            }
        }

        ///
        if(var5 == 1){
            if(up05Min < FITTER_05_UP &&
                    down05min> FITTER_05_DOWN &&
                    down15min>FITTER_15_DOWN &&
                    average60Down>FILTER_END_SLEEP){

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

    public float getAverageMiddle(int i, int minutes) {
        int minutesDown = 0, minutesUp = 0;
        minutesUp = switch (minutes) {
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

        float avgUp = avgPrev.get(minutesUp)[i];
        float avgDown = avgNext.get(minutesDown)[i];

        return (avgUp + avgDown) / 2;
    }

}
