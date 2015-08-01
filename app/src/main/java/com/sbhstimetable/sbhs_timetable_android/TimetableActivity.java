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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.authflow.TokenExpiredActivity;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.service.NotificationService;
import com.sbhstimetable.sbhs_timetable_android.event.TodayEvent;


public class TimetableActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {
	private static final String COUNTDOWN_FRAGMENT_TAG = "countdownFragment";
	public static final String BELLTIMES_AVAILABLE = "bellsArePresent";
	public static final String TODAY_AVAILABLE = "todayIsPresent";
	public static final String PREF_DISABLE_DIALOG = "disableFeedbackDialog";
	public static final String PREF_LOGGED_IN_ONCE = "hasLoggedInBefore";

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	public DrawerLayout mDrawerLayout;
	public Toolbar mToolbar;
	public NavigationView mNavigationView;
	public TypedValue mTypedValue;
	public ActionBarDrawerToggle mDrawerToggle;
	private Menu menu;
	public boolean isActive = false;
	private boolean needToRecreate = false;
	private int onMaster = 1;
	private ActivityEventReceiver receiver;
	private StorageCache cache;
	public int mNavItemId;
	private Runnable mPendingRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeHelper.setTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timetable);

		mNavItemId = R.id.nav_countdown;
		mTypedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.colorPrimary, mTypedValue, true);
		int colorPrimary = mTypedValue.data;
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setBackgroundColor(colorPrimary);
		setSupportActionBar(mToolbar);

		mDrawerLayout = (DrawerLayout) getWindow().findViewById(R.id.drawer_layout);

		mNavigationView = (NavigationView) findViewById(R.id.navigation);
		mNavigationView.setNavigationItemSelectedListener(this);
		mNavigationView.getMenu().findItem(mNavItemId).setChecked(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerToggle.syncState();

		mNavigationView.getMenu().getItem(4).setIcon(ContextCompat.getDrawable(getApplicationContext(), ThemeHelper.isBackgroundDark() ? R.drawable.ic_settings_dark_24dp : R.drawable.ic_settings_white_24dp));
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
		if (!NotificationService.running) {
			Intent i = new Intent(this, NotificationService.class);
			i.setAction(NotificationService.ACTION_INITIALISE);
			this.startService(i);
		}

		navigate(mNavItemId);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_DISABLE_DIALOG, false)) {
			DialogFragment f = new FeedbackDialogFragment();
			f.show(this.getFragmentManager(), "dialog");
		}
		if (ThemeHelper.themeNeedsRevalidating()) {
			//this.
			this.recreate();
		}
		if (this.cache.shouldReloadBells()) ApiWrapper.requestBells(this);
		if (this.cache.shouldReloadNotices()) ApiWrapper.requestNotices(this);
		if (this.cache.shouldReloadToday()) ApiWrapper.requestToday(this);
		if (this.cache.shouldReloadTimetable()) ApiWrapper.requestTimetable(this);
		ApiWrapper.getEventBus().register(this.receiver);
		//NotificationService.startUpdatingNotification(this);
		this.isActive = true;
	}

	private void navigate(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		SwipeRefreshLayout v = (SwipeRefreshLayout)this.findViewById(R.id.swrl);
		if (v != null) {
			v.setRefreshing(false);
			v.clearAnimation();
		}
		switch (position) { // USE THE BREAK, LUKE!
			case R.id.nav_countdown:
				fragmentManager.beginTransaction()
						.replace(R.id.container, CountdownFragment.newInstance())
						.commit();
				onMaster = 1;
				break;
			case R.id.nav_timetable:
				fragmentManager.beginTransaction()
						.replace(R.id.container, TimetableFragment.newInstance())
						.commit();
				onMaster = 0;
				break;
			case R.id.nav_notices:
				fragmentManager.beginTransaction()
						.replace(R.id.container, NoticesFragment.newInstance())
						.commit();
				onMaster = 0;
				break;
			case R.id.nav_belltimes:
				fragmentManager.beginTransaction()
						.replace(R.id.container, BelltimesFragment.newInstance())
						.commit();
				onMaster = 0;
				break;
			case R.id.nav_settings:
				if (!isActive) break; // don't do weirdness
				//Log.i("timetableactivity", "isActive = false (launching SettingsActivity)");
				Intent settings = new Intent(this, SettingsActivity.class);
				isActive = false;
				this.startActivity(settings);
				break;
		}
	}

	@Override
	public boolean onNavigationItemSelected(final MenuItem item) {
		item.setChecked(true);
		mNavItemId = item.getItemId();
		if (item.getItemId() == R.id.nav_settings) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					navigate(item.getItemId());
				}
			}, 0);
			mDrawerLayout.closeDrawer(GravityCompat.START);
		} else {
			navigate(item.getItemId());
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					mDrawerLayout.closeDrawer(GravityCompat.START);
				}
			}, 0);
		}

		return true;
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
