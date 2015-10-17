package com.sbhstimetable.sbhs_timetable_android.backend.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sbhstimetable.sbhs_timetable_android.gapis.GeofencingIntentService;

public class NotificationDismissReceiver extends BroadcastReceiver {
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss_notification_plox";

    public NotificationDismissReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals(ACTION_DISMISS_NOTIFICATION)) {
            NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(GeofencingIntentService.GEOFENCING_NOTIFICATION_ID);

        }
    }
}
