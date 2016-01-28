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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.FullCycleWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.Lesson;
import com.sbhstimetable.sbhs_timetable_android.authflow.LoginActivity;
import com.sbhstimetable.sbhs_timetable_android.event.TodayEvent;

import org.joda.time.DateTime;


public class TodayWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodayRemoteViewsFactory(this);
    }

    class TodayRemoteViewsFactory implements RemoteViewsFactory {
        private Context con;
		private EventListener e;
		private FullCycleWrapper cycle;
        TodayRemoteViewsFactory(Context c) {
            this.con = c;
			this.e = new EventListener(c);
			this.cycle = new FullCycleWrapper(c);
			ApiWrapper.getEventBus().register(e);
		}

        @Override
        public void onCreate() {
			Log.i("TodayWidgetService", "Hi I am the today widget service your number one source for widgets since 1998");
			DateTime nextUpdate = DateTime.now().withTimeAtStartOfDay().plusHours(15).plusMinutes(15);
			AlarmManager am = (AlarmManager)con.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent();
			intent.setClass(con, WidgetUpdaterService.class);
			PendingIntent updateIntent = PendingIntent.getService(con, 0, intent, 0);
			am.set(AlarmManager.ELAPSED_REALTIME, nextUpdate.getMillis() - DateTime.now().getMillis(), updateIntent);
        }

        @Override
        public void onDataSetChanged() {
        }

        @Override
        public void onDestroy() {
            Log.i("todayWidgetService", "bye");
        }

        @Override
        public int getCount() {
            return this.cycle.ready() ? 6 : 1;
        }

        @Override
        public RemoteViews getViewAt(int i) {
			Log.i("TWS", "Updating " + i);
            if (!this.cycle.ready()) {
                RemoteViews r = new RemoteViews(con.getPackageName(), R.layout.layout_textview);
                r.setTextViewText(R.id.label, "Waiting for data... (Maybe get an internet connection?)");
                Intent t = new Intent(con, LoginActivity.class);
                t.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                r.setOnClickPendingIntent(R.id.label, PendingIntent.getActivity(this.con, 0, t, 0));
                return r;
            }
            if (i == 0) {
                RemoteViews r = new RemoteViews(con.getPackageName(), R.layout.layout_textview);
				Log.i("TWS", "day index " + cycle.getCurrentDayInCycle() + " day " + cycle.getToday());
                String res = cycle.getToday().getDayName() + " " + cycle.getToday().getWeek();
                r.setTextViewText(R.id.label, res);
                //r.setInt(R.id.label, "setGravity", Gravity.LEFT);
                return r;
            }
            Lesson p = this.cycle.getToday().getPeriod(i);
            RemoteViews r = new RemoteViews(con.getPackageName(), R.layout.layout_timetable_classinfo_widget);
            r.setTextViewText(R.id.timetable_class_header, p.getSubject());
            r.setTextViewText(R.id.timetable_class_room, p.getRoom());
            int standout = ContextCompat.getColor(con, R.color.standout);
            if (p.roomChanged()) {
                r.setTextColor(R.id.timetable_class_room, standout);
            } else {
				r.setTextColor(R.id.timetable_class_room, ContextCompat.getColor(con, R.color.primary_text_default_material_dark));
			}
            r.setTextViewText(R.id.timetable_class_teacher, p.getTeacher());
            if (p.teacherChanged()) {
                r.setTextColor(R.id.timetable_class_teacher, standout);
            } else {
				r.setTextColor(R.id.timetable_class_teacher, ContextCompat.getColor(con, R.color.primary_text_default_material_dark));
			}

            if (!p.roomChanged() && !p.teacherChanged()) {
                r.setImageViewBitmap(R.id.timetable_class_changed, null);
            }
			r.setOnClickFillInIntent(R.id.classinfo_root, new Intent());
            return r;
        }

        @Override
        public RemoteViews getLoadingView() {
            RemoteViews r = new RemoteViews(con.getPackageName(), R.layout.layout_textview);
            r.setTextViewText(R.id.label, "Loading…");
            return r;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @SuppressWarnings("unused")
		private class EventListener {
			private Context context;
			public EventListener(Context c) {
				this.context = c;
			}
			public void onEvent(TodayEvent today) {
				if (!today.successful()) return;
				AppWidgetManager a = AppWidgetManager.getInstance(context);
				int ids[] = a.getAppWidgetIds(new ComponentName(context, TodayAppWidget.class));
				a.notifyAppWidgetViewDataChanged(ids, R.id.widget_today_listview);

			}
		}
    }


}
