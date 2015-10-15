package com.sbhstimetable.sbhs_timetable_android;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;

import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;

public class TimetableApp extends Application {
    private static Context context;

    public static boolean BELLTIME_ALLOW_FAKE_DAY = false;

    @Override
    public void onCreate() {
        super.onCreate();
        TimetableApp.context = getApplicationContext();
        BELLTIME_ALLOW_FAKE_DAY = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PrefUtil.BELLTIMES_DAY_TESTING, false);
    }

    public static Context getAppContext() {
        return context;
    }
}
