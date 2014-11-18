package com.sbhstimetable.sbhs_timetable_android.debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;

public class DebugActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		IntentFilter i = new IntentFilter();
		i.addAction(ApiAccessor.ACTION_TIMETABLE_JSON);
		LocalBroadcastManager.getInstance(this).registerReceiver(new DebugReceiver(this), i);
        setContentView(R.layout.activity_debug);

	    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	    setSupportActionBar(toolbar);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		this.findViewById(R.id.get_timetablejson).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ApiAccessor.getTimetable(view.getContext(), false);
				Toast.makeText(view.getContext(), "Loading", Toast.LENGTH_SHORT);
			}
		});

		this.findViewById(R.id.load_timetablejson).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				JsonObject j = StorageCache.getTimetable(view.getContext());
				((TextView)findViewById(R.id.status)).setText(j.toString());
			}
		});
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
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

	@Override
	public void onBackPressed() {
		finish();
	}
}
