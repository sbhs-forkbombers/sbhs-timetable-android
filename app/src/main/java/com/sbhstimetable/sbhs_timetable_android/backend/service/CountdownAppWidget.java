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


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatterBuilder;


public class CountdownAppWidget extends AppWidgetProvider {
    private static PendingIntent pending;
	private DateTimeHelper dth;
	private StorageCache cache;

    @Override
	@SuppressLint("NewApi")
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
		if (this.cache == null) {
			this.cache = new StorageCache(context);
		}
		if (this.dth == null) {
			this.dth = new DateTimeHelper(context);
		}
        int home[] = new int[appWidgetIds.length];
        int lock[] = new int[appWidgetIds.length];
        int lockIdx = 0;
        int homeIdx = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) { // no lockscreen widgets < 4.2, so don't check.
            for (int i : appWidgetIds) {
                if (appWidgetManager.getAppWidgetOptions(i).getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1) == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD) {
                    lock[lockIdx++] = i;
                } else {
                    home[homeIdx++] = i;
                }
            }
        }
        else {
            home = appWidgetIds;
            homeIdx = appWidgetIds.length;
        }


        Belltimes b = cache.loadBells();//.getInstance();
        String label = "";
        String in = "";
        String cntDwn = "";
        if (b == null || !b.valid()) {
            ApiWrapper.requestBells(context);
            if (b == null) cntDwn = "Loading…";
        }
        if (b != null) {
            if (!dth.hasBells()) {
                dth.setBells(b);
            }
			Belltimes.Bell bell = dth.getNextBell();
			if (bell == null) {
				ApiWrapper.requestBells(context);
			}
            long time = dth.getNextEvent().toDateTime().getMillis() - DateTime.now().getMillis();

            label = "School starts";
            in = "in";
            if (bell != null) {
                label = bell.getBellName();
                in = "starts in";
				time = bell.getBellTime().toDateTime().withDate(DateTime.now().toLocalDate()).getMillis() - DateTime.now().getMillis();
            }
			//Log.i("Countdown", "Up next " + bell + " time: " + bell.getBellTime().toString());
            cntDwn = DateTimeHelper.toCountdown((int)(time / 1000));

        }
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_app_widget);
		views.setTextViewText(R.id.widget_label, label);
		views.setTextViewText(R.id.widget_in, in);
		views.setTextViewText(R.id.widget_next_period, cntDwn);
		if (homeIdx > 0) {
            String c = "#";
            String trans = p.getString(PrefUtil.WIDGET_TRANSPARENCY_HS, "32");
            c += "00".substring(trans.length()) + trans;
            c += "000000";
            views.setInt(R.id.widget_countdown_root, "setBackgroundColor", Color.parseColor(c));
            appWidgetManager.updateAppWidget(home, views);
        }

        if (lockIdx > 0) {
            String c = "#";
            String trans = p.getString(PrefUtil.WIDGET_TRANSPARENCY_LS, "32");
            c += "00".substring(trans.length()) + trans;
            c += "000000";
            views.setInt(R.id.widget_countdown_root, "setBackgroundColor", Color.parseColor(c));

            appWidgetManager.updateAppWidget(lock, views);
        }


        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent m = new Intent(context, this.getClass());
        m.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        m.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass())));
        //if (pending != null) am.cancel(pending);
        pending = PendingIntent.getBroadcast(context, appWidgetIds[0], m, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+1000, pending);
    }

	@Override
	public void onEnabled(Context c) {
		Log.i("CountdownAppWidget", "onEnabled");
	}

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (pending != null) {
            ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).cancel(pending);
        }
    }
}
