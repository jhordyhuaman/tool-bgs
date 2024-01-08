package pe.innobyte.toosanalizer.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import pe.innobyte.toosanalizer.core.model.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Jhordy Huaman on 05/01/2024
 * Convert blocks of sleep from list of activity
 * sample by exel file to JSON format
 * to JSON format
 * @version 2.0
 * @since 2.0
 * @see ActivitySample (sample of activity) (kind, steps, heart rate, timestamp)
 * @see SleepBlockData (block of sleep) (start date, end date, list of activity sample)
 * @see SleepModel (model of sleep) (level, seconds, datetime)
 * @see HeartModel (model of heart) (value, time)
 * @see StepsModel (model of step) (value, time)
 **/
public class ConvertBlocksSleep {

    private final DateFormat formatSummaryDate = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat formatHHMM = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat formatDefault = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    private static final long MINUTE_IN_MILLIS = 60000L;
    private static final int MIN_MINUTES_DELIMITER_SLEEP = 25;
    private final JsonArray allDataSleep = new JsonArray();
    private final List<ActivitySample> sleepData = new ArrayList<>();
    private final List<SleepModel> dataSleep = new ArrayList<>();
    private final ArrayList<HeartModel> heard = new ArrayList<>();
    private final ArrayList<StepsModel> steps = new ArrayList<>();
    private final List<SleepBlockData> blockData = new ArrayList<>();
    private final List<SleepModel> sleepModelData = new ArrayList<>();
    private int AgvSteps = 0;
    private int valueAvgHeart;
    private int numberItems;
    private Date endTimeSleep = null;
    private boolean addedSleep = false;


    /**
     * Process data from list of activity sample
     * @param data list of activity sample
     */
    public void processData(List<ActivitySample> data) {
        // process data in reverse order
        for (int i = data.size() - 1; i >= 0; i--) {
            ActivitySample sample = data.get(i);

            if ((sample.getKind() == 4 || sample.getKind() == 3) && i == 0) {
                checkTypeOfActivity(sample);
            }
            checkSleepLineTime(sample);
        }

        // group by date summary
        Map<String, List<SleepBlockData>> dayDataBlock = new TreeMap<>();
        for (SleepBlockData dataBlock : blockData) {
            dayDataBlock.computeIfAbsent(dataBlock.getDateSummary(), k -> new ArrayList<>()).add(dataBlock);
        }

        System.out.println("Group sleep blocks by Date summary");

        for (Map.Entry<String, List<SleepBlockData>> day : dayDataBlock.entrySet()) {
            System.out.println("--------------------------------------------");
            System.out.println("Date Summary :" + day.getKey());
            for (SleepBlockData sleep : day.getValue()) {
                // Process each activity sample in the sleep block
                for (ActivitySample sm : sleep.getSleepData()) {
                    schemeHeart(sm);
                    schemeStep(sm);
                    schemeSleep(sm);
                }

                System.out.println(" * Start Sleep   :" + sleep.getStartDate());
                System.out.println(" * End Sleep     :" + sleep.getEndDate());
                System.out.println(" * Data          :" + sleep.getSleepData().size());

                groupLevelSleep();
            }

            // create JSON for day and reset values
            JsonObject thisDayJSON = schemaSleepOfDay(day.getKey());
            allDataSleep.add(thisDayJSON);
            dataSleep.clear();
            heard.clear();
            steps.clear();
        }

    }

    /**
     * Get all blocks of sleep for save JSON file
     * @return JsonArray with all blocks of sleep
     */
    public JsonArray getJSONBlocksSleep() {
        return allDataSleep;
    }

    /**
     * Check if the activity is asleep and the difference between
     * the start and end time is greater than 25 minutes
     * add to list of sleep block and reset values
     * @param sample sample of activity
     */
    private void checkSleepLineTime(ActivitySample sample) {
        if (isAsleep(sample.getKind())) {
            if (endTimeSleep == null) {
                endTimeSleep = getDateFromSample(sample);
            }
            addedSleep = true;
            sleepData.add(sample);

        } else {
            checkTypeOfActivity(sample);
        }
    }

    /**
     * Check type of activity and add to list of sleep block
     * if the activity is asleep and the difference between
     * the start and end time is greater than 25 minutes
     * add to list of sleep block and reset values
     * @param sample sample of activity
     */
    private void checkTypeOfActivity(ActivitySample sample){
        if (addedSleep) {
            Date startTimeSleep = getDateFromSample(sample);
            // evaluate how many minutes of sleep there are
            long diff = ((endTimeSleep.getTime() - startTimeSleep.getTime()) / MINUTE_IN_MILLIS);
            if (diff >= MIN_MINUTES_DELIMITER_SLEEP) {
                String DateSummary = formatSummaryDate.format(endTimeSleep);
                // add block sleep
                SleepBlockData sleepBlockData = new SleepBlockData();
                sleepBlockData.setDateSummary(DateSummary);
                sleepBlockData.setEndDate(endTimeSleep);
                sleepBlockData.setStartDate(startTimeSleep);
                sleepBlockData.getSleepData().addAll(sleepData);
                blockData.add(sleepBlockData);
            }
            // cut block sleep
            addedSleep = false;
            sleepData.clear();
        }
        endTimeSleep = null;
    }


    /**
     * Group level sleep by date and time,
     * classify by awake and asleep and count seconds
     * and add to list of sleep model data
     */
    private void groupLevelSleep() {
        sleepModelData.sort(Comparator.comparing(SleepModel::getDatetime));

        String lastLevelSleep = sleepModelData.get(0).getLevel();
        Date lastTimeSleep = sleepModelData.get(0).getDatetime();
        long totalSecondsLevel = sleepModelData.get(0).getSeconds();

        SleepModel sleepModel = new SleepModel();

        for (int i = 0; i < sleepModelData.size(); i++) {
            SleepModel currentSleepModel = sleepModelData.get(i);

            if (!lastLevelSleep.equals(currentSleepModel.getLevel()) || i == sleepModelData.size() - 1) {
                sleepModel.setDatetime(lastTimeSleep);
                sleepModel.setLevel(lastLevelSleep);
                sleepModel.setSeconds(totalSecondsLevel);
                dataSleep.add(sleepModel);
                // reset counter seconds and update last level and time
                totalSecondsLevel = 0;
                lastTimeSleep = currentSleepModel.getDatetime();
                lastLevelSleep = currentSleepModel.getLevel();

                sleepModel = new SleepModel();
            }
            totalSecondsLevel += currentSleepModel.getSeconds();
        }
        sleepModelData.clear();
    }

    /**
     * Get date from sample of activity value
     * is in timestamp format, convert to date
     * @param sample sample of activity
     * @return date of type Date
     */
    private Date getDateFromSample(ActivitySample sample) {
        return new Date(sample.getTimestamp() * 1000L);
    }

    /**
     * Check if value is awake
     * @param value value to check kind of activity
     * @return true if value is awake
     */
    private boolean isAwake(float value) {
        return value == 3;
    }

    /**
     * Check if value is asleep
     * @param value value to check kind of activity
     * @return true if value is asleep
     */
    private boolean isAsleep(float value) {
        return value == 4 || value == 3;
    }

    /**
     * Scheme data from list of heart model, step model and sleep model
     * @param dateSummary date summary for data
     * @return JsonObject with data of heart, step and sleep
     */
    private JsonObject schemaSleepOfDay(String dateSummary) {
        JsonObject data = new JsonObject();

        JsonObject heartData = getHeartData(dateSummary);
        JsonObject sleepData = getSleepModelData(dateSummary);
        JsonObject stepData = getStepData(dateSummary);

        data.add("sleepData", sleepData);
        data.add("heartData", heartData);
        data.add("stepData", stepData);

        return data;
    }

    /**
     * Scheme heart data from sample of activity
     * and add to list of heart model data
     * @param sample sample of activity heart detail
     */
    private void schemeHeart(ActivitySample sample) {
        String[] hours = (formatHHMM).format(getDateFromSample(sample)).split(":");
        int heartRate = sample.getHeartRate();
        if ((Integer.parseInt(hours[1]) % 10 == 5) || (Integer.parseInt(hours[1]) % 10 == 0)) {
            if (valueAvgHeart != 0) {
                heard.add(new HeartModel(getDateFromSample(sample), (valueAvgHeart / numberItems)));
                valueAvgHeart = 0;
                numberItems = 0;
            }
        } else {
            valueAvgHeart += heartRate;
            numberItems++;
        }
    }

    /**
     * Scheme step data from sample of activity
     * and add to list of step model data
     * @param sample sample of activity step detail
     */
    private void schemeStep(ActivitySample sample) {
        if (sample.getSteps() > 0) {
            AgvSteps += sample.getSteps();
        }

        String[] hours = (formatHHMM).format(getDateFromSample(sample)).split(":");
        if ((Integer.parseInt(hours[1]) % 10 == 5) || (Integer.parseInt(hours[1]) % 10 == 0)) {
            if (AgvSteps > 0) {
                steps.add(new StepsModel(getDateFromSample(sample), AgvSteps));
                AgvSteps = 0;
            }
        }

    }

    /**
     * Scheme sleep data from sample of activity
     * and add to list of sleep model data
     * @param sample sample of activity sleep detail
     */
    private void schemeSleep(ActivitySample sample) {
        SleepModel itemSleepModel = new SleepModel();

        if (isAwake(sample.getKind())) {
            itemSleepModel.setDatetime(getDateFromSample(sample));
            itemSleepModel.setLevel("awake");
            itemSleepModel.setSeconds(60);
        } else {
            itemSleepModel.setLevel("asleep");
            itemSleepModel.setSeconds(60);
            itemSleepModel.setDatetime(getDateFromSample(sample));
        }
        sleepModelData.add(itemSleepModel);
    }

    /**
     * Get step data from list of step model
     * @param dateSummary date summary for step data
     * @return JsonObject with step data
     */
    private JsonObject getStepData(String dateSummary) {
        JsonObject stepData = new JsonObject();

        JsonArray dataset = new JsonArray();
        for (StepsModel step : steps) {
            JsonObject itemStep = new JsonObject();
            itemStep.addProperty("time", formatDefault.format(step.getTime()));
            itemStep.addProperty("value", step.getValue()); //step.getValue()
            dataset.add(itemStep);
        }
        stepData.add("dataset", dataset);
        stepData.addProperty("scope", "step");
        stepData.addProperty("dateActivity", dateSummary);
        stepData.addProperty("datasetInterval", 5);
        stepData.addProperty("datasetType", "minute");

        return stepData;
    }

    /**
     * Get sleep data from list of sleep model
     * @param dateSummary date summary for sleep data
     * @return JsonObject with sleep data
     */
    private JsonObject getSleepModelData(String dateSummary) {
        JsonObject sleepDataArray = new JsonObject();
        JsonArray data = new JsonArray();
        long secondsAwake = 0, secondsSleep = 0;
        int countSleep = 0, countAwake = 0;

        // order by date and time
        dataSleep.sort(Comparator.comparing(SleepModel::getDatetime));

        for (SleepModel sl : dataSleep) {
            JsonObject item = new JsonObject();
            item.addProperty("dateTime", formatDefault.format(sl.getDatetime()));
            item.addProperty("level", sl.getLevel());
            item.addProperty("seconds", sl.getSeconds());

            if ("awake".equals(sl.getLevel())) {
                secondsAwake += sl.getSeconds();
                countAwake++;
            } else if ("asleep".equals(sl.getLevel())) {
                secondsSleep += sl.getSeconds();
                countSleep++;
            }
            data.add(item);
        }
        // resume sleep and awake
        JsonObject awake = new JsonObject();
        awake.addProperty("count", countAwake);
        awake.addProperty("minutes", secondsAwake / 60);
        awake.addProperty("thirtyDayAvgMinutes", 0);

        JsonObject asleep = new JsonObject();
        asleep.addProperty("count", countSleep);
        asleep.addProperty("minutes", secondsSleep / 60);
        asleep.addProperty("thirtyDayAvgMinutes", 0);

        JsonObject sleepSummary = new JsonObject();
        sleepSummary.add("asleep", asleep);
        sleepSummary.add("awake", awake);

        JsonObject levels = new JsonObject();
        levels.add("data", data);
        levels.add("summary", sleepSummary);

        JsonObject details = new JsonObject();
        details.addProperty("duration", 0);
        details.addProperty("efficiency", 0);
        details.addProperty("isMainSleep", true);
        details.add("levels", levels);
        details.addProperty("logId", 0);
        details.addProperty("minutesAfterWakeup", 0);
        details.addProperty("minutesAsleep", secondsSleep / 60);
        details.addProperty("minutesAwake", secondsAwake / 60);
        details.addProperty("minutesToFallAsleep", 0);
        details.addProperty("startTime", formatDefault.format(dataSleep.get(0).getDatetime()));
        details.addProperty("timeInBed", secondsSleep / 60);
        details.addProperty("type", "classic");

        JsonArray sleep = new JsonArray();
        sleep.add(details);
        sleepDataArray.add("sleep", sleep);

        JsonObject summary = new JsonObject();
        summary.addProperty("dateSummary", dateSummary);
        summary.addProperty("totalMinutesAsleep", secondsSleep / 60);
        summary.addProperty("totalSleepRecords", countSleep);
        summary.addProperty("totalTimeInBed", 400);

        sleepDataArray.add("summary", summary);

        return sleepDataArray;
    }

    /**
     * Get heart data from list of heart model
     * @param dateSummary date summary for heart data
     * @return JsonObject with heart data
     */
    private JsonObject getHeartData(String dateSummary) {
        JsonObject heartData = new JsonObject();
        JsonArray dataset = new JsonArray();

        for (HeartModel hear : heard) {
            JsonObject item = new JsonObject();
            item.addProperty("time", formatDefault.format(hear.getTime()));
            item.addProperty("value", hear.getValue());
            dataset.add(item);
        }

        heartData.add("dataset", dataset);
        heartData.addProperty("scope", "heart");
        heartData.addProperty("dateActivity", dateSummary);
        heartData.addProperty("datasetInterval", 5);
        heartData.addProperty("datasetType", "minute");

        return heartData;
    }
}
