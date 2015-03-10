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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.event.RefreshingStateEvent;

public class DebugActivity extends ActionBarActivity {
	public Toolbar mToolbar;
	public TypedValue mTypedValue;

	private StorageCache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    ThemeHelper.setTheme(this);
        super.onCreate(savedInstanceState);
		cache = new StorageCache(this);
        setContentView(R.layout.activity_debug);

	    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	    mTypedValue = new TypedValue();
	    getTheme().resolveAttribute(R.attr.colorPrimary, mTypedValue, true);
	    int colorPrimary = mTypedValue.data;
	    mToolbar = toolbar;
	    mToolbar.setBackgroundColor(colorPrimary);
	    setSupportActionBar(toolbar);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
		    getTheme().resolveAttribute(R.attr.colorPrimaryDark, mTypedValue, true);
		    int colorPrimaryDark = mTypedValue.data;
		    getWindow().setStatusBarColor(colorPrimaryDark);
	    }

		final TextView status = (TextView)findViewById(R.id.status);

		findViewById(R.id.guess_week).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				status.setText(String.valueOf(cache.loadWeek()));
			}
		});

		findViewById(R.id.loading_timetable).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ApiWrapper.getEventBus().getStickyEvent(RefreshingStateEvent.class).refreshing) {
					status.setText("cancelled refreshing event");
					ApiWrapper.doneRefreshing();
				} else {
					status.setText("posted refreshing event");
					ApiWrapper.notifyRefreshing();
				}
				//status.setText(ApiWrapper.loadingTimetable + "");
			}
		});

		((CheckBox)findViewById(R.id.today_override)).setChecked(ApiWrapper.overrideEnabled);

		((CheckBox)findViewById(R.id.today_override)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			//public boolean runOnce = false;
			@Override
			public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
				/*if (!runOnce) {
					runOnce = true;
					return;
				}*/
				final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(buttonView.getContext());
				if (buttonView.isChecked()) {
					AlertDialog a = new AlertDialog.Builder(buttonView.getContext()).setTitle("Here be dragons!").setMessage("You might get stuff wrong if you do this. Don't do it, except for testing(tm)").setIcon(R.drawable.ic_warning_white_48dp)
							.setNegativeButton("Abort!", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Toast.makeText(buttonView.getContext(), "Crisis averted.", Toast.LENGTH_SHORT).show();
								}
							}).setPositiveButton("I'm ready", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									p.edit().putBoolean("override", true).apply();
									ApiWrapper.overrideEnabled = true;
								}
							}).show();
				} else {
					p.edit().putBoolean("override", false).apply();
					ApiWrapper.overrideEnabled = false;
				}
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


	@Override
	public void onBackPressed() {
		finish();
	}
}
