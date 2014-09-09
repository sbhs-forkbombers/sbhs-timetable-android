package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.JsonParser;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;


public class ClassInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setTintColor(Color.parseColor("#455ede"));
        tintManager.setStatusBarTintEnabled(true);
        setContentView(R.layout.activity_class_info);
        Intent i = this.getIntent();
        if (!i.hasExtra("json")) {
            throw new IllegalStateException("RUDE I NEED SOME DATA PLOX");
        }
        String json = i.getStringExtra("json");
        TodayJson.Period b = new TodayJson.Period(new JsonParser().parse(json).getAsJsonObject());
        TextView subject = (TextView)this.findViewById(R.id.classInfoSubject);
        subject.setText(b.name());
        //subject.setMinimumHeight(50);
        TextView room = (TextView)this.findViewById(R.id.classInfoRoom);
        room.setText(b.room());


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.class_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
