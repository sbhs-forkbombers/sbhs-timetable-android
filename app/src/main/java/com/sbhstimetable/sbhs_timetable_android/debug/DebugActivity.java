package com.sbhstimetable.sbhs_timetable_android.debug;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;

public class DebugActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		IntentFilter i = new IntentFilter();
		i.addAction(ApiAccessor.ACTION_TIMETABLE_JSON);
		LocalBroadcastManager.getInstance(this).registerReceiver(new DebugReceiver(this), i);
        setContentView(R.layout.activity_debug);
		this.findViewById(R.id.timetablejson_debug).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ApiAccessor.getTimetable(view.getContext(), false);
				Toast.makeText(view.getContext(), "Loading", Toast.LENGTH_SHORT);
			}
		});

		this.findViewById(R.id.timetablejson_load).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				JsonObject j = StorageCache.getTimetable(view.getContext());
				((TextView)findViewById(R.id.status)).setText(j.toString());
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.debug, menu);
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
	private class DebugReceiver extends BroadcastReceiver {
		private DebugActivity a;
		public DebugReceiver(DebugActivity a) {
			this.a = a;
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			TextView t = (TextView) a.findViewById(R.id.status);
			t.setText(t.getText() + "\n" + "Got intent: " + intent.getAction());
		}
	}
}
