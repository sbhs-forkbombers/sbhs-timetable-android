package com.sbhstimetable.sbhs_timetable_android.backend.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.LoginActivity;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;


public class TodayWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodayRemoteViewsFactory(this, TodayJson.getInstance());
    }

    class TodayRemoteViewsFactory implements RemoteViewsFactory {
        private TodayJson today;
        private Context con;
        TodayRemoteViewsFactory(Context c, TodayJson t) {
            this.con = c;
            this.today = t;
        }

        @Override
        public void onCreate() {
            if (today == null) {
                ApiAccessor.load(con);
                ApiAccessor.getToday(con);
                LocalBroadcastManager.getInstance(con).registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        today = new TodayJson(new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject());
                        LocalBroadcastManager.getInstance(con).unregisterReceiver(this);
                    }
                }, new IntentFilter(ApiAccessor.ACTION_TODAY_JSON));
            }
        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return this.today != null && this.today.valid() ? 6 : 1;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            if (this.today == null || !this.today.valid()) {
                RemoteViews r = new RemoteViews(con.getPackageName(), R.layout.layout_textview);
                r.setTextViewText(R.id.label, "You need to log in");
                Intent t = new Intent(con, LoginActivity.class);
                t.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                r.setOnClickPendingIntent(R.id.label, PendingIntent.getActivity(this.con, 0, t, 0));
                return r;
            }
            if (i == 5) {
                RemoteViews r = new RemoteViews(con.getPackageName(), R.layout.layout_textview);
                String res = today.finalised() ? "This info is final" : "This info may change";
                r.setTextViewText(R.id.label, res);
                r.setTextColor(R.id.label, getResources().getColor(R.color.standout));
                return r;
            }
            TodayJson.Period p = this.today.getPeriod(i+1);
            RemoteViews r = new RemoteViews(con.getPackageName(), R.layout.layout_timetable_classinfo);
            r.setTextViewText(R.id.timetable_class_header, p.name());
            r.setTextViewText(R.id.timetable_class_room, p.room());
            r.setTextViewText(R.id.timetable_class_teacher, p.fullTeacher());
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
            return true;
        }
    }
}
