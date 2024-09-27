/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pe.innobyte.toosanalizer.utils;

import com.github.pfichtner.durationformatter.DurationFormatter;
import pe.innobyte.toosanalizer.core.model.MiBandActivitySample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author nik_1
 */
public class DateTimeUtils {
    public static String formatDurationHoursMinutes(long duration, TimeUnit unit) {
        DurationFormatter df = DurationFormatter.Builder.SYMBOLS
                .maximum(TimeUnit.DAYS)
                .minimum(TimeUnit.MINUTES)
                .suppressZeros(DurationFormatter.SuppressZeros.LEADING, DurationFormatter.SuppressZeros.TRAILING)
                .maximumAmountOfUnitsToShow(2)
                .build();
        return df.format(duration, unit);
    }

    public static String getDateFromSample(MiBandActivitySample sample) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
        return dateFormat.format(new Date(sample.getTimestamp() * 1000L));
    }

}
