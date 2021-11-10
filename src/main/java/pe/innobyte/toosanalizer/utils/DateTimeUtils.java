/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pe.innobyte.toosanalizer.utils;

import com.github.pfichtner.durationformatter.DurationFormatter;
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
}
