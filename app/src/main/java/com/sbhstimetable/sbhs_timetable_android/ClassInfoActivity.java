package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.sbhstimetable.sbhs_timetable_android.backend.TodayJson;


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
        Log.i("classinfo", "json: " + json);
        TodayJson.Period b = new TodayJson.Period(new JsonParser().parse(json).getAsJsonObject());
        TextView subject = (TextView)this.findViewById(R.id.classInfoSubject);
        subject.setText(b.name());
        RelativeLayout r = (RelativeLayout)this.findViewById(R.id.classInfoRoot);
        //r.removeView(subject);
        subject.setMinimumHeight(300);
        //RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
       // p.addRule(RelativeLayout.ALIGN_PARENT_START);
        //r.addView(subject, p);
//        Log.e("classinfo", "json data is " + savedInstanceState.getString("json"));
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
