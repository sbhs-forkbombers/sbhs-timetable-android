package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;

public class DashclockService extends DashClockExtension {
    private TodayJson mine;
    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        setUpdateWhenScreenOn(true);
        if (mine == null) {
            ApiAccessor.getTodayGlobal(this);
        }
        mine = TodayJson.getInstance();
        IntentFilter wanted = new IntentFilter();
        wanted.addAction(ApiAccessor.GLOBAL_ACTION_TODAY_JSON);
        this.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mine = new TodayJson(new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject());
            }
        }, wanted);
    }

    @Override
    protected void onUpdateData(int reason) {
        if (mine != null) {
            publishUpdate(new ExtensionData()
                            .icon(R.drawable.ic_launcher)
                            .status(mine.getPeriod(1).getShortName() + " - " + mine.getPeriod(1).room())
                            .expandedTitle(mine.getPeriod(1).name())
                            .expandedBody("in " + mine.getPeriod(1).room() + " with " + mine.getPeriod(1).fullTeacher())
                            .visible(true)
            );
        }
    }
}
