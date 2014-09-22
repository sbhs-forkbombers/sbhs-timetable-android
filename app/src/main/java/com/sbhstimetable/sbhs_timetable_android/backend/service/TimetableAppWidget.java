package com.sbhstimetable.sbhs_timetable_android.backend.service;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sbhstimetable.sbhs_timetable_android.R;

import java.text.SimpleDateFormat;
import java.util.Date;


public class TimetableAppWidget extends AppWidgetProvider {
    private PendingIntent pending;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        //for (int i : appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_app_widget);
        views.setTextViewText(R.id.widget_next_period, new SimpleDateFormat("hh:mm:ss").format(new Date()));
        appWidgetManager.updateAppWidget(appWidgetIds, views);
        //}

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent m = new Intent(context, this.getClass());
        m.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        m.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass())));
        pending = PendingIntent.getBroadcast(context, 1, m, PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.ELAPSED_REALTIME, 1000, pending);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if (pending != null) {
            ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE)).cancel(pending);
        }
    }
}
