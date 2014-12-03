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

package com.sbhstimetable.sbhs_timetable_android.debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.secret.LLandActivity;

public class DebugActivity extends ActionBarActivity {
	public Toolbar mToolbar;
	public TypedValue mTypedValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    ThemeHelper.setTheme(this);
        super.onCreate(savedInstanceState);
		IntentFilter i = new IntentFilter();
		i.addAction(ApiAccessor.ACTION_TIMETABLE_JSON);
		LocalBroadcastManager.getInstance(this).registerReceiver(new DebugReceiver(this), i);
        setContentView(R.layout.activity_debug);

	    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	    mTypedValue = new TypedValue();
	    getTheme().resolveAttribute(R.attr.colorPrimary, mTypedValue, true);
	    int colorPrimary = mTypedValue.data;
	    mToolbar = (Toolbar) findViewById(R.id.toolbar);
	    mToolbar.setBackgroundColor(colorPrimary);
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

		this.findViewById(R.id.launch_topsecret).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(view.getContext(), LLandActivity.class);
				view.getContext().startActivity(i);
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
