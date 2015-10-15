package com.sbhstimetable.sbhs_timetable_android;

import android.app.Application;
import android.content.Context;

public class TimetableApp extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        TimetableApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
}
