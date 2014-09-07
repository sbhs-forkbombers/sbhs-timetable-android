package com.sbhstimetable.sbhs_timetable_android.backend;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimeHelper {
    private static Calendar cal() {
        return Calendar.getInstance();
    }
    public static BelltimesJson bells;
    public static int getDateOffset() {
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

    public static String getDateString() {
        return getYear() + "-" + (getMonth()+1) + "-" + (getDate() + getDateOffset() + (needsMidnightCountdown() ? 1 : 0));
    }

    public static boolean needsMidnightCountdown() {
        int offset = getDateOffset();
        return offset > 0 || (getHour() > 15 || (getHour() == 15 && getMinute() > 15));
    }

    public static long milliSecondsUntilNextEvent() {
        long time = 0;
        GregorianCalendar d = new GregorianCalendar(getYear(), getMonth(), getDate() + getDateOffset() + (needsMidnightCountdown() ? 1 : 0), 9, 5);
        if (bells == null) {
            Log.i("datetimehelper", "falling back!");
            d.set(d.HOUR_OF_DAY, 9);
            d.set(d.MINUTE, 5);
        }
        else {
            Log.i("datetimehelper", "got bells.");
            BelltimesJson.Bell b = bells.getNextPeriod();
            Integer[] els = b.getBell();
            d.set(d.HOUR_OF_DAY, els[0]);
            d.set(d.MINUTE, els[1]);
        }
        time = d.getTimeInMillis() - cal().getTimeInMillis();
        Log.i("datetimehelper", ""+time);
        return time;
    }

    public static int getDay() {
        return cal().get(Calendar.DAY_OF_WEEK);
    }
    public static int getHour() {
        return cal().get(Calendar.HOUR_OF_DAY);
    }
    public static int getMinute() {
        return cal().get(Calendar.MINUTE);
    }
    public static int getYear() {
        return cal().get(Calendar.YEAR);
    }
    public static int getMonth() {
        return cal().get(Calendar.MONTH);
    }
    public static int getDate() {
        return cal().get(Calendar.DAY_OF_MONTH);
    }
}
