/*
 * SBHS-Timetable-Android: Countdown and timetable all at once (Android app).
 * Copyright (C) 2015 Simon Shields, James Ye
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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;

public class WidgetUpdaterService extends Service {
    public static final String ACTION_UPDATE = "doUpdate";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.wtf("WidgetUpdateService", "NULL INTENT WTF");
            return super.onStartCommand(null, flags, startId);
        }
        if (intent.getAction() == null) {
            Log.wtf("WidgetUpdateService", "NULL ACTION " + intent.describeContents());
            return super.onStartCommand(intent, flags, startId);
        }
        if (intent.getAction().equals(ACTION_UPDATE)) {
            ApiWrapper.requestBells(this);
            ApiWrapper.requestToday(this);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
