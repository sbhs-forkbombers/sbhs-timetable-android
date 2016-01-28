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

package com.sbhstimetable.sbhs_timetable_android;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.gapis.GoogleApiHelper;

public class SettingsActivity extends AppCompatActivity implements SettingsFragment.SettingsFragmentListener {
	public Toolbar mToolbar;
	public TypedValue mTypedValue;
	private SettingsFragment mSettingsFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeHelper.setTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		mTypedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.colorPrimary, mTypedValue, true);
		int colorPrimary = mTypedValue.data;
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setBackgroundColor(colorPrimary);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			getTheme().resolveAttribute(R.attr.colorPrimaryDark, mTypedValue, true);
			int colorPrimaryDark = mTypedValue.data;
			getWindow().setStatusBarColor(colorPrimaryDark);
		}


		setSupportActionBar(mToolbar);
		if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == GoogleApiHelper.MY_PERMS_GEOFENCING_REQUEST) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				GoogleApiHelper.initialise(this);
			} else {
				if (mSettingsFragment != null) {
					mSettingsFragment.onLocationPermissionDenied();
				}
				Toast.makeText(this, "You cannot enable scan in reminders unless you grant SBHS Timetable location access.", Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void setSettingsFragment(SettingsFragment frag) {
		mSettingsFragment = frag;
	}
}
