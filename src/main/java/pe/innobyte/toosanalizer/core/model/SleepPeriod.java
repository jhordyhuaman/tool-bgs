package pe.innobyte.toosanalizer.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SleepPeriod {
    private String dateSummary;
    private Date startSleep;
    private Date endSleep;
    private List<ActivitySample> sleepData;

    // Constructor
    public SleepPeriod(String dateSummary, Date startSleep, Date endSleep , List<ActivitySample> sleepData) {
        this.dateSummary = dateSummary;
        this.startSleep = startSleep;
        this.endSleep = endSleep;
        this.sleepData = new ArrayList<>(sleepData);
    }

    // Getters
    public String getDateSummary() { return dateSummary; }
    public Date getStartSleep() { return startSleep; }
    public Date getEndSleep() { return endSleep; }
    public List<ActivitySample> getSleepData() { return new ArrayList<>(sleepData); }

    public float calculateAverageHeartRateOfLast60() {
        if (sleepData == null || sleepData.isEmpty()) {
            return 0; // Devuelve 0 si la lista está vacía o es nula
        }


        List<ActivitySample> sortedData = new ArrayList<>(sleepData);
        Collections.reverse(sortedData);

        // Obtener los últimos 60 registros en orden cronológico
        int size = sortedData.size();
        int startIndex = Math.max(size - 60, 0);
        List<ActivitySample> last60Samples = sortedData.subList(startIndex, size);

        float totalHeartRate = 0;
        int count = 0;

        for (ActivitySample sample : last60Samples) {
            int heartRate = sample.getHeartRate();
            if (heartRate > 0) { // Ignorar valores no válidos
                totalHeartRate += heartRate;
                count++;
            }
        }

        return count > 0 ? totalHeartRate / count : 0;
    }

    public int calculateMinHeartRateOfLast60() {
        if (sleepData == null || sleepData.isEmpty()) {
            return 0; // Devuelve 0 si la lista está vacía o es nula
        }

        // Crear una copia de sleepData en orden cronológico ascendente
        List<ActivitySample> sortedData = new ArrayList<>(sleepData);
        Collections.reverse(sortedData);

        // Obtener los últimos 60 registros en orden cronológico
        int size = sortedData.size();
        int startIndex = Math.max(size - 60, 0); // Si hay menos de 60 registros, empieza desde 0
        List<ActivitySample> last60Samples = sortedData.subList(startIndex, size);

        // Calcular el valor mínimo del ritmo cardíaco usando Streams
        return last60Samples.stream()
                .mapToInt(ActivitySample::getHeartRate) // Convertir a int
                .filter(hr -> hr > 0) // Filtrar valores válidos
                .min() // Obtener el valor mínimo
                .orElse(0); // Si no hay valores válidos, devolver 0
    }

}
