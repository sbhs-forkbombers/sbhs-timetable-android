package com.sbhstimetable.sbhs_timetable_android.backend.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootFinishedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (p.getBoolean("notifications_enable",false)) {
            NotificationService.startUpdatingNotification(context);
        }
    }
}
