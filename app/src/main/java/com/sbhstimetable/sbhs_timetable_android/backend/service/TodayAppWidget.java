package com.sbhstimetable.sbhs_timetable_android.backend.service;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sbhstimetable.sbhs_timetable_android.R;


/**
 * Implementation of App Widget functionality.
 */
// TODO this needs to update at 3:15 every day, rather than hourly/daily. It only needs to update when the device is on, so we
// TODO should use AlarmManager.
public class TodayAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.today_app_widget);
        Intent i = new Intent(context, TodayWidgetService.class);
        views.setRemoteAdapter(R.id.widget_today_listview, i);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetIds, views);
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


