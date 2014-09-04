package com.sbhstimetable.sbhs_timetable_android.backend;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeHelper {
    private static Calendar cal = Calendar.getInstance();

    public static int getDateOffset() {
        Calendar i = Calendar.getInstance();
        int day = getDay();
        int hour = getHour();
        int minute = getMinute();
        int offset = 0;
        if (day == Calendar.SATURDAY) {
            // push to sunday afternoon.
            offset++;
        }
        else if (day == Calendar.FRIDAY && (hour > 15 || hour == 15 && minute > 15)) {
            offset += 2;
        }
        return offset;
    }
    public static boolean needsMidnightCountdown() {
        int offset = getDateOffset();
        return offset > 0 || (getHour() > 15 || (getHour() == 15 && getMinute() > 15));
    }

    public static long milliSecondsUntilNextEvent() {
        long time = 0;
        GregorianCalendar d = new GregorianCalendar(getYear(), getMonth(), getDate() + getDateOffset() + (needsMidnightCountdown() ? 1 : 0), 9, 5);
        Log.i("datetimehelper", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(d.getTimeInMillis())));
        d.set(d.HOUR_OF_DAY, 9);
        d.set(d.MINUTE, 5);
        time = d.getTimeInMillis() - cal.getTimeInMillis();
        Log.i("datetimehelper", ""+time);
        return time;
    }

    private static int getDay() {
        return cal.get(Calendar.DAY_OF_WEEK);
    }
    private static int getHour() {
        return cal.get(Calendar.HOUR_OF_DAY);
    }
    private static int getMinute() {
        return cal.get(Calendar.MINUTE);
    }
    private static int getYear() {
        return cal.get(Calendar.YEAR);
    }
    private static int getMonth() {
        return cal.get(Calendar.MONTH);
    }
    private static int getDate() {
        return cal.get(Calendar.DAY_OF_MONTH);
    }
}
