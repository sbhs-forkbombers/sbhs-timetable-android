/*
 * SBHS-Timetable-Android: Countdown and timetable all at once (Android app).
 * Copyright (C) 2014 Simon Shields, James Ye
 *
 * This file is part of SBHS-Timetable-Android.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sbhstimetable.sbhs_timetable_android.backend.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.sbhstimetable.sbhs_timetable_android.PermissionsRequestActivity;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.gapis.GeofencingIntentService;
import com.sbhstimetable.sbhs_timetable_android.gapis.GoogleApiHelper;

/**
 * Receives the Boot finished intent and also the application upgraded intent
 */
public class BootFinishedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (p.getBoolean("notifications_enable",false)) {
			Intent i = new Intent(context, NotificationService.class);
			i.setAction(NotificationService.ACTION_INITIALISE);
            context.startService(i);
        }

        if (p.getBoolean(PrefUtil.GEOFENCING_ACTIVE,false)) {
            if (GoogleApiHelper.checkPermission(context, null) && !GoogleApiHelper.ready()) {
                GoogleApiHelper.initialise(context);
            } else if (!GoogleApiHelper.checkPermission(context, null)) {
                // post a notification
                GeofencingIntentService.postPermissionsNotification(context);
            }
        }
    }
}
