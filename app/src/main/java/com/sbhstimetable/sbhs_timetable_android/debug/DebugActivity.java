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
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;

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
				ApiWrapper.loadingTimetable = !ApiWrapper.loadingTimetable;
				status.setText(ApiWrapper.loadingTimetable + "");
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
