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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;

public class DashclockService extends DashClockExtension {
    private TodayJson mine;
    private BelltimesJson bells;
    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        setUpdateWhenScreenOn(true);
        mine = TodayJson.getInstance();
        bells = BelltimesJson.getInstance();
        if (mine == null) {
            ApiAccessor.getToday(this);
        }
        if (bells == null) {
            ApiAccessor.getBelltimes(this);
        }
        IntentFilter wanted = new IntentFilter();
        wanted.addAction(ApiAccessor.ACTION_TODAY_JSON);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ApiAccessor.ACTION_TODAY_JSON)) {
                    mine = new TodayJson(new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject());
                } else if (intent.getAction().equals(ApiAccessor.ACTION_BELLTIMES_JSON)) {
                    bells = new BelltimesJson(new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject());
                }
            }
        }, wanted);
    }

    @Override
    protected void onUpdateData(int reason) {
        int num;
        if (bells != null && bells.valid()) {
            BelltimesJson.Bell b = bells.getNextPeriod();
            if (b == null) {
                // what
                num = 5;
            }
            else {
                num = b.getPeriodNumber();
            }
        }
        else {
            publishUpdate(new ExtensionData().visible(false));
            return;
        }
        if (mine != null) {
            publishUpdate(new ExtensionData()
                            .icon(R.drawable.ic_launcher)
                            .status(mine.getPeriod(num).getShortName() + " - " + mine.getPeriod(num).room())
                            .expandedTitle(mine.getPeriod(num).name())
                            .expandedBody("in " + mine.getPeriod(num).room() + " with " + mine.getPeriod(num).fullTeacher())
                            .visible(true)
            );
        }
    }
}
