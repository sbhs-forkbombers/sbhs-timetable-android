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

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.JsonUtil;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;
import com.sbhstimetable.sbhs_timetable_android.backend.service.NotificationService;


public class TimetableActivity extends ActionBarActivity
		implements NavigationDrawerFragment.NavigationDrawerCallbacks, CommonFragmentInterface {
	private static final String COUNTDOWN_FRAGMENT_TAG = "countdownFragment";
	public static final String BELLTIMES_AVAILABLE = "bellsArePresent";
	public static final String TODAY_AVAILABLE = "todayIsPresent";
	public static final String PREF_DISABLE_DIALOG = "disableFeedbackDialog";
	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	public NavigationDrawerFragment mNavigationDrawerFragment;
	public DrawerLayout mDrawerLayout;
	public Toolbar mToolbar;
	public TypedValue mTypedValue;
	private Menu menu;
	public boolean isActive = false;
	private boolean needToRecreate = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ThemeHelper.setTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timetable);

		mTypedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.colorPrimary, mTypedValue, true);
		int colorPrimary = mTypedValue.data;
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setBackgroundColor(colorPrimary);
		setSupportActionBar(mToolbar);
		ApiAccessor.load(this);
		mDrawerLayout = (DrawerLayout) getWindow().findViewById(R.id.drawer_layout);
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, mDrawerLayout);
		IntentFilter interesting = new IntentFilter(ApiAccessor.ACTION_TODAY_JSON);
		interesting.addAction(ApiAccessor.ACTION_BELLTIMES_JSON);
		interesting.addAction(ApiAccessor.ACTION_NOTICES_JSON);
		interesting.addAction(ApiAccessor.ACTION_TIMETABLE_JSON);
		interesting.addAction(ApiAccessor.ACTION_THEME_CHANGED);
		LocalBroadcastManager.getInstance(this).registerReceiver(new ReceiveBroadcast(this), interesting);
		// Set up the drawer.
		// Grab belltimes.json
		ApiAccessor.getBelltimes(this);
		ApiAccessor.getToday(this);
		ApiAccessor.getNotices(this);
		ApiAccessor.getTimetable(this, true);
		final Context c = this;

		Thread t = new Thread(new Runnable() {
			public void run() {
				StorageCache.cleanCache(c);
			}
		});
		t.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_DISABLE_DIALOG, false)) {
			DialogFragment f = new FeedbackDialogFragment();
			f.show(this.getFragmentManager(), "dialog");
		}
		NotificationService.startUpdatingNotification(this);
		this.mNavigationDrawerFragment.updateList();
		this.isActive = true;
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		switch (position) { // USE THE BREAK, LUKE!
			case 0:
				fragmentManager.beginTransaction()
					.replace(R.id.container, CountdownFragment.newInstance(), COUNTDOWN_FRAGMENT_TAG)
					.commit();
				break;
			case 1:
				fragmentManager.beginTransaction()
					.replace(R.id.container, TimetableFragment.newInstance(), COUNTDOWN_FRAGMENT_TAG)
					.addToBackStack("timetable")
					.commit();
				break;
			case 2:
				fragmentManager.beginTransaction()
					.replace(R.id.container, NoticesFragment.newInstance(), COUNTDOWN_FRAGMENT_TAG)
					.addToBackStack("notices")
					.commit();
				break;
			case 3:
				fragmentManager.beginTransaction()
					.replace(R.id.container, BelltimesFragment.newInstance(), COUNTDOWN_FRAGMENT_TAG)
					.addToBackStack("belltimes")
					.commit();
				break;
			case 4:
				if (!isActive) break; // don't do weirdness
				Log.i("timetableactivity", "isActive = false (launching SettingsActivity)");
				Intent settings = new Intent(this, SettingsActivity.class);
				isActive = false;
				this.startActivity(settings);
				break;
			case 5:
				if (ApiAccessor.isLoggedIn()) {
					ApiAccessor.logout(this);
					this.mNavigationDrawerFragment.updateList();
					Toast.makeText(this, "Logged out! (You may need to restart the app to remove all your data)", Toast.LENGTH_SHORT).show();
					StorageCache.deleteAllCacheFiles(this);
					fragmentManager.beginTransaction().commit();
				} else {
					ApiAccessor.login(this);
				}
				break; // HAVE YOU GOT A PLAN BREAK?®
		}
	}

	@Override
	public void setNavigationStyle(int s) {
		// required
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.timetable, menu);
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

	/*@Override
	protected void onPause() {
		super.onPause();
		Log.i("timetableactivity", "isActive = false");
		this.isActive = false;
	}*/

	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (this.needToRecreate) {
			this.needToRecreate = false;
			this.recreate();
		}
		Log.i("timetableactivity", "isActive = true");
		this.isActive = true;
	}

	public void updateCachedStatus(Menu m) {

	}

	private class ReceiveBroadcast extends BroadcastReceiver {
		private TimetableActivity activity;

		public ReceiveBroadcast(TimetableActivity a) {
			this.activity = a;
		}

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(ApiAccessor.ACTION_TODAY_JSON)) {
				ApiAccessor.todayLoaded = true;

				if (this.activity.hasWindowFocus() && this.activity.getFragmentManager().findFragmentByTag(COUNTDOWN_FRAGMENT_TAG) instanceof TimetableFragment) {
					TimetableFragment frag = ((TimetableFragment) this.activity.getFragmentManager().findFragmentByTag(COUNTDOWN_FRAGMENT_TAG));
					if (frag != null) {
						frag.doTimetable(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
					}
				} else {
					JsonObject o = JsonUtil.safelyParseJson(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
					if (o.has("error")) {
						// reject it silently/
						return;
					}
					StorageCache.cacheTodayJson(this.activity, DateTimeHelper.getDateString(context), intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));

					TodayJson j = new TodayJson(o); // set INSTANCE
					StorageCache.writeCachedDate(context, j.getDate());
				}
				LocalBroadcastManager.getInstance(this.activity).sendBroadcast(new Intent(TimetableActivity.TODAY_AVAILABLE));
			} else if (intent.getAction().equals(ApiAccessor.ACTION_BELLTIMES_JSON)) {
				//activity.setProgressBarIndeterminateVisibility(false);
				ApiAccessor.bellsLoaded = true;
				StorageCache.cacheBelltimes(this.activity, DateTimeHelper.getDateString(context), intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
				DateTimeHelper.bells = new BelltimesJson(JsonUtil.safelyParseJson(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)));
				LocalBroadcastManager.getInstance(this.activity).sendBroadcast(new Intent(TimetableActivity.BELLTIMES_AVAILABLE));
			} else if (intent.getAction().equals(ApiAccessor.ACTION_NOTICES_JSON)) {
				ApiAccessor.noticesLoaded = true;
				StorageCache.cacheNotices(this.activity, DateTimeHelper.getDateString(context), intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
				JsonObject nj = JsonUtil.safelyParseJson(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
				if (nj != null && NoticesJson.isValid(nj)) {
					new NoticesJson(nj);
				}
			} else if (intent.getAction().equals(ApiAccessor.ACTION_TIMETABLE_JSON)) {
				ApiAccessor.timetableLoaded = true;
				StorageCache.cacheTimetable(this.activity, intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
			}
			else if (intent.getAction().equals(ApiAccessor.ACTION_THEME_CHANGED)) {
				if (this.activity != null) {
					activity.needToRecreate = true;
				}
			}
			this.activity.updateCachedStatus(this.activity.menu);
		}
	}
}