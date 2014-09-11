package com.sbhstimetable.sbhs_timetable_android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;

/**
 * Implementation of App Widget functionality.
 */
public class TimetableAppWidget extends AppWidgetProvider {
    public static final String ACTION_UPDATE_WIDGET = "com.sbhstimetable.sbhs_timetable_android.updateWidget";
/*
    @Override
    public void onReceive(Context c, Intent i) {
        if (i.getAction().equals(com.))
    }*/

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }

        AlarmManager iWouldLikeToDoThisAgain = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent();
        i.setAction(ACTION_UPDATE_WIDGET);
        PendingIntent p = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
        iWouldLikeToDoThisAgain.set(AlarmManager.ELAPSED_REALTIME, 1000, p);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timetable_app_widget);
        views.setTextViewText(R.id.widget_next_period, ""+DateTimeHelper.getTimeMillis());

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


