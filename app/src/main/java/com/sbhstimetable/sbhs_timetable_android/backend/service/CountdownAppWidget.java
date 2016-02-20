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
import com.sbhstimetable.sbhs_timetable_android.TimetableActivity;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.api.FullCycleWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.Lesson;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.Compat;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;

import org.joda.time.DateTime;


import java.util.TimerTask;


public class CountdownAppWidget extends AppWidgetProvider {
    private static PendingIntent pending;
    private DateTimeHelper dth;
    private FullCycleWrapper cycle;
    private TimerTask tt;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            int[] widgetIds = Compat.getWidgetIds(context, CountdownAppWidget.class);
            debug("custom magic update");
            onUpdate(context, awm, widgetIds);
        } else {
            super.onReceive(context, intent);
        }
    }


    private void debug(String s) {
        Log.i("CountdownWidget", s);
    }

    @Override
    @SuppressLint("NewApi")
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        if (this.dth == null) {
            this.dth = new DateTimeHelper(context);
        }
        if (this.cycle == null) {
            cycle = new FullCycleWrapper(context);
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
        } else {
            home = appWidgetIds;
            homeIdx = appWidgetIds.length;
        }


        String label, connector, cntDwn;
        Lesson p;
        Belltimes.Bell next = dth.getNextBell();
        if (next != null && next.getPreviousBellTime() != null) {
            debug("next - " + next + " " + next.getBellDisplay() + " " + next.getBellName());
            Belltimes.Bell now = next.getPreviousBellTime();
            debug("period start - period => " + now.getBellName() + "(" + now.getPeriodNumber() + ") is ps? " + now.isPeriodStart());
            if (now.isPeriodStart() && now.getPeriodNumber() < 5) { // in a period, it's not last period.
                connector = "ends in";
                if (ApiWrapper.isLoggedIn() && cycle.ready()) {
                    p = cycle.getToday().getPeriod(now.getPeriodNumber());
                    debug("has today - " + p);
                    label = p.getSubject();
                } else {
                    label = now.getBellName();
                }
            } else if (now.isPeriodStart() && now.getPeriodNumber() == 5) { // last period
                connector = "in";
                label = "School ends";
            } else if (!now.isPeriodStart() && next.isPeriodStart()) { // in a break followed by a period - Lunch 2, Recess, Transition.
                connector = "starts in";
                if (ApiWrapper.isLoggedIn() && cycle.ready()) {
                    p = cycle.getToday().getPeriod(next.getPeriodNumber());
                    label = p.getSubject();
                } else {
                    label = next.getBellName();
                }
            } else { // There's consecutive non-periods - i.e lunch 1 -> lunch 2
                label = next.getBellName();
                connector = "starts in";}
        } else {
            // end of day
            label = "School starts";
            connector = "in";
            if (cycle.hasFullTimetable()) {
                p = cycle.getToday().getPeriod(1);
            }
        }
        int secondsLeft;
        if (dth.getNextEvent() != null) {
            secondsLeft = (int) Math.floor((dth.getNextEvent().toDateTime().getMillis() - DateTime.now().getMillis()) / 1000);
        } else {
            Log.w("CountdownWidget", "No next event...");
            secondsLeft = 0;
        }
        int seconds = secondsLeft % 60;
        secondsLeft -= seconds;
        secondsLeft /= 60;
        int minutes = secondsLeft % 60;
        secondsLeft -= minutes;
        secondsLeft /= 60;
        if (secondsLeft == 0) {
            cntDwn = String.format("%02dm %02ds", minutes, seconds);
        } else {
            cntDwn = String.format("%02dh %02dm %02ds", secondsLeft, minutes, seconds);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_app_widget);
        Intent intent = new Intent(context, TimetableActivity.class);
        views.setOnClickPendingIntent(R.id.widget_countdown_root, PendingIntent.getActivity(context, 0, intent, 0));
        views.setTextViewText(R.id.widget_label, label);
        views.setTextViewText(R.id.widget_in, connector);
        views.setTextViewText(R.id.widget_next_period, cntDwn);
        if (homeIdx > 0) {
            String c = "#";
            String trans = prefs.getString(PrefUtil.WIDGET_TRANSPARENCY_HS, "32");
            c += "00".substring(trans.length()) + trans;
            c += "000000";
            views.setInt(R.id.widget_countdown_root, "setBackgroundColor", Color.parseColor(c));
            appWidgetManager.updateAppWidget(home, views);
        }

        if (lockIdx > 0) {
            String c = "#";
            String trans = prefs.getString(PrefUtil.WIDGET_TRANSPARENCY_LS, "32");
            c += "00".substring(trans.length()) + trans;
            c += "000000";
            views.setInt(R.id.widget_countdown_root, "setBackgroundColor", Color.parseColor(c));

            appWidgetManager.updateAppWidget(lock, views);
        }


        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent m = new Intent(context, this.getClass());
        m.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        m.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass())));
        //if (pending != null) am.cancel(pending);
        pending = PendingIntent.getBroadcast(context, appWidgetIds[0], m, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pending);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (pending != null) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(pending);
        }
    }
}
