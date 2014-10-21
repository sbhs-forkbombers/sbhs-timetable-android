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
import android.widget.RemoteViews;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;


public class TimetableAppWidget extends AppWidgetProvider {
    private static PendingIntent pending;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        int home[] = new int[appWidgetIds.length];
        int lock[] = new int[appWidgetIds.length];
        int lockIdx = 0;
        int homeIdx = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { // no lockscreen widgets < 4.2, so don't check.
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


        BelltimesJson b = BelltimesJson.getInstance();
        String label = "";
        String in = "";
        String cntDwn = "";
        if (b == null) {
            ApiAccessor.getBelltimes(context);
            cntDwn = "Loading…";
        }
        else {
            if (DateTimeHelper.bells == null) {
                DateTimeHelper.bells = b;
            }
            long time = DateTimeHelper.milliSecondsUntilNextEvent();
            BelltimesJson.Bell bell = b.getNextBell();
            label = "School starts";
            in = "in";
            if (bell != null) {
                label = bell.getLabel();
                in = "ends in";
            }
            cntDwn = DateTimeHelper.formatToCountdown(time);

        }
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        if (homeIdx > 0) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_app_widget);
            String c = "#";
            String trans = p.getString(PrefUtil.WIDGET_TRANSPARENCY_HS, "32");
            c += "00".substring(trans.length()) + trans;
            c += "000000";
            views.setInt(R.id.widget_countdown_root, "setBackgroundColor", Color.parseColor(c));
            views.setTextViewText(R.id.widget_label, label);
            views.setTextViewText(R.id.widget_in, in);
            views.setTextViewText(R.id.widget_next_period, cntDwn);
            appWidgetManager.updateAppWidget(home, views);
        }

        if (lockIdx > 0) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_app_widget);
            String c = "#";
            String trans = p.getString(PrefUtil.WIDGET_TRANSPARENCY_LS, "32");
            c += "00".substring(trans.length()) + trans;
            c += "000000";
            views.setInt(R.id.widget_countdown_root, "setBackgroundColor", Color.parseColor(c));
            views.setTextViewText(R.id.widget_label, label);
            views.setTextViewText(R.id.widget_in, in);
            views.setTextViewText(R.id.widget_next_period, cntDwn);
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
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (pending != null) {
            ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).cancel(pending);
        }
    }
}
