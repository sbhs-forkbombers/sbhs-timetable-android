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
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.TimetableActivity;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;


/**
 * Implementation of App Widget functionality.
 */
// TODO this needs to update at 3:15 every day, rather than hourly/daily. It only needs to update when the device is on, so we
// TODO should use AlarmManager.
public class TodayAppWidget extends AppWidgetProvider {



    @Override
	@SuppressLint("NewApi")
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
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
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
		Intent intent = new Intent(context, TimetableActivity.class);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        if (homeIdx > 0) {
            RemoteViews homeScreenWidg = new RemoteViews(context.getPackageName(), R.layout.today_app_widget);
			//homeScreenWidg.setPendingIntentTemplate(R.id.widget_today_listview);
			homeScreenWidg.setOnClickPendingIntent(R.id.widget_today_root, pi);
			homeScreenWidg.setPendingIntentTemplate(R.id.widget_today_listview, pi);;
            String c = "#";
            String trans = p.getString(PrefUtil.WIDGET_TRANSPARENCY_HS, "32");
            c += "00".substring(trans.length()) + trans;
            c += "000000"; // WHY JAVA
            homeScreenWidg.setInt(R.id.widget_today_root, "setBackgroundColor", Color.parseColor(c));
            Intent i = new Intent(context, TodayWidgetService.class);
            homeScreenWidg.setRemoteAdapter(R.id.widget_today_listview, i);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(home, homeScreenWidg);
        }

        if (lockIdx > 0 ) {
            RemoteViews lockScreenWidg = new RemoteViews(context.getPackageName(), R.layout.today_app_widget);
			lockScreenWidg.setOnClickPendingIntent(R.id.widget_today_root, pi);
			lockScreenWidg.setPendingIntentTemplate(R.id.widget_today_listview, pi);
			String c = "#";
            String trans = p.getString(PrefUtil.WIDGET_TRANSPARENCY_LS, "00");
            c += "00".substring(trans.length()) + trans;
            c += "000000"; // WHY JAVA
            Log.i("todaywidget", "bg color (ls): " + c);
            lockScreenWidg.setInt(R.id.widget_today_root, "setBackgroundColor", Color.parseColor(c));
            Intent i = new Intent(context, TodayWidgetService.class);
            lockScreenWidg.setRemoteAdapter(R.id.widget_today_listview, i);
            Log.i("todaywidget", "updating ls!");
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(lock, lockScreenWidg);

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


