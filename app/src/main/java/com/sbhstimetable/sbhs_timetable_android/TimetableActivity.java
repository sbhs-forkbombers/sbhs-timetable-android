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
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.authflow.TokenExpiredActivity;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.event.TodayEvent;


public class TimetableActivity extends ActionBarActivity
		implements NavigationDrawerFragment.NavigationDrawerCallbacks, CommonFragmentInterface {
	private static final String COUNTDOWN_FRAGMENT_TAG = "countdownFragment";
	public static final String BELLTIMES_AVAILABLE = "bellsArePresent";
	public static final String TODAY_AVAILABLE = "todayIsPresent";
	public static final String PREF_DISABLE_DIALOG = "disableFeedbackDialog";
	public static final String PREF_LOGGED_IN_ONCE = "hasLoggedInBefore";

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
	private int onMaster = 1;
	private ActivityEventReceiver receiver;
	private StorageCache cache;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeHelper.setTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timetable);

		mTypedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.colorPrimary, mTypedValue, true);
		int colorPrimary = mTypedValue.data;
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setBackgroundColor(colorPrimary);
		setSupportActionBar(mToolbar);

		mDrawerLayout = (DrawerLayout) getWindow().findViewById(R.id.drawer_layout);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			getTheme().resolveAttribute(R.attr.colorPrimaryDark, mTypedValue, true);
			int colorPrimaryDark = mTypedValue.data;
			mDrawerLayout.setBackgroundColor(colorPrimaryDark);
		}
		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, mDrawerLayout);

		//ApiAccessor.load(this);
		ApiWrapper.initialise(this);
		if (!ApiWrapper.isLoggedIn()) {
			Intent toLaunch = new Intent(this, TokenExpiredActivity.class);
			toLaunch.putExtra("firstTime", true);
			this.startActivity(toLaunch);
			finish();
		}

		if (this.receiver == null) {
			this.receiver = new ActivityEventReceiver(this);

		}


		// Grab belltimes.json
		/*ApiAccessor.getBelltimes(this);
		ApiAccessor.getToday(this);
		ApiAccessor.getNotices(this);
		ApiAccessor.getTimetable(this, true);*/
		if (this.cache == null) {
			this.cache = new StorageCache(this);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_DISABLE_DIALOG, false)) {
			DialogFragment f = new FeedbackDialogFragment();
			f.show(this.getFragmentManager(), "dialog");
		}
		if (this.cache.shouldReloadBells()) ApiWrapper.requestBells(this);
		if (this.cache.shouldReloadNotices()) ApiWrapper.requestNotices(this);
		if (this.cache.shouldReloadToday()) ApiWrapper.requestToday(this);
		if (this.cache.shouldReloadTimetable()) ApiWrapper.requestTimetable(this);
		ApiWrapper.getEventBus().register(this.receiver);
		//NotificationService.startUpdatingNotification(this);
		this.mNavigationDrawerFragment.updateList();
		this.isActive = true;
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		SwipeRefreshLayout v = (SwipeRefreshLayout)this.findViewById(R.id.swrl);
		if (v != null) {
			v.setRefreshing(false);
			v.clearAnimation();
		}
		switch (position) { // USE THE BREAK, LUKE!
			case 0:
				fragmentManager.beginTransaction()
					.replace(R.id.container, CountdownFragment.newInstance())
					.commit();
				onMaster = 1;
				break;
			case 1:
				fragmentManager.beginTransaction()
					.replace(R.id.container, TimetableFragment.newInstance())
					.commit();
				onMaster = 0;
				break;
			case 2:
				fragmentManager.beginTransaction()
					.replace(R.id.container, NoticesFragment.newInstance())
					.commit();
				onMaster = 0;
				break;
			case 3:
				fragmentManager.beginTransaction()
					.replace(R.id.container, BelltimesFragment.newInstance())
					.commit();
				onMaster = 0;
				break;
			case 4:
				if (!isActive) break; // don't do weirdness
				//Log.i("timetableactivity", "isActive = false (launching SettingsActivity)");
				Intent settings = new Intent(this, SettingsActivity.class);
				isActive = false;
				this.startActivity(settings);
				break;
			case 5:
				/*if (ApiAccessor.isLoggedIn()) {
					ApiAccessor.logout(this);
					this.mNavigationDrawerFragment.updateList();
					Toast.makeText(this, "Logged out! (You may need to restart the app to remove all your data)", Toast.LENGTH_SHORT).show();
					//StorageCache.deleteAllCacheFiles(this);
					fragmentManager.beginTransaction().commit();
				} else {
					ApiAccessor.login(this);
				}*/
				break; // HAVE YOU GOT A PLAN BREAK?®
		}
	}

	@Override
	public void onBackPressed() {
		if (mNavigationDrawerFragment.isDrawerOpen()) {
			mNavigationDrawerFragment.closeDrawer();
		} else {
			if (onMaster == 1) {
				finish();
			} else {
				mNavigationDrawerFragment.selectItem(0);
				onMaster = 1;
			}
		}
	}

	@Override
	public void setNavigationStyle(int s) {
		// required
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		if (this.needToRecreate) {
			this.needToRecreate = false;
			this.recreate();
		}
		this.isActive = true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		ApiWrapper.getEventBus().unregister(this.receiver);
	}

	public void updateCachedStatus(Menu m) {

	}

	@SuppressWarnings("unused")
	private class ActivityEventReceiver {
		private TimetableActivity activity;

		public ActivityEventReceiver(TimetableActivity a) {
			this.activity = a;
		}

		public void onEvent(TodayEvent t) {
			if (t.successful()) {
				// TODO
			} else {

			}
		}


	}
}