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

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.NavBarFancyAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ScrimInsetsFrameLayout;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

	/**
	 * Remember the position of the selected item.
	 */
	private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

	/**
	 * Per the design guidelines, you should show the drawer on launch until the user manually
	 * expands it. This shared preference tracks this.
	 */
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

	/**
	 * A pointer to the current callbacks instance (the Activity).
	 */
	private NavigationDrawerCallbacks mCallbacks;

	private static final String FMT = "dd/MM hh:mm a";


	/**
	 * Helper component that ties the action bar to the navigation drawer.
	 */
	private ActionBarDrawerToggle mDrawerToggle;

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerListView;
	private View mFragmentContainerView;

	private int mCurrentSelectedPosition = 0;
	private boolean mFromSavedInstanceState;
	private boolean mUserLearnedDrawer;

    private TextView todayStatus;
    private TextView noticesStatus;
    private TextView bellsStatus;

	private CacheStatusUpdater csu;


	private final ArrayList<String> elements = new ArrayList<String>();
	private final ArrayList<NavBarFancyAdapter.DrawerEntry> botElements = new ArrayList<NavBarFancyAdapter.DrawerEntry>();

	public NavigationDrawerFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Read in the flag indicating whether or not the user has demonstrated awareness of the
		// drawer. See PREF_USER_LEARNED_DRAWER for details.
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

		if (savedInstanceState != null) {
			mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
			mFromSavedInstanceState = true;
		}
		// Select either the default item (0) or the last selected item.
		selectItem(mCurrentSelectedPosition);
	}

	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Indicate that this fragment would like to influence the set of actions in the action bar.
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ScrimInsetsFrameLayout l = (ScrimInsetsFrameLayout) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
		this.mDrawerListView = (ListView)l.findViewById(R.id.navdraw_listview);
		mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
			}
		});
		this.elements.addAll(Arrays.asList(getString(R.string.title_countdown),
			getString(R.string.title_timetable),
			getString(R.string.title_notices),
			getString(R.string.title_belltimes)
		));
		mDrawerListView.setAdapter(new ArrayAdapter<>(
			getActivity().getBaseContext(),
			android.R.layout.simple_list_item_activated_1,
			this.elements));
		mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

		ListView smallView = (ListView)l.findViewById(R.id.navdraw_botlistview);
		smallView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
			selectItem(i+elements.size());
			}
		});
		int settings_drawable = (ThemeHelper.isBackgroundDark() ? R.drawable.ic_settings_white_24dp : R.drawable.ic_settings_dark_24dp);
		this.botElements.addAll(Arrays.asList(
			new NavBarFancyAdapter.DrawerEntry(settings_drawable, getString(R.string.action_settings), this.getActivity())
		));
		smallView.setAdapter(new NavBarFancyAdapter<>(
			getActivity().getBaseContext(),
				android.R.layout.simple_list_item_1,
				this.botElements
		));
		long temp = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(ApiAccessor.PREF_BELLTIMES_LAST_UPDATE, 0);
        this.bellsStatus = (TextView)l.findViewById(R.id.navdraw_bellscached);
		this.bellsStatus.setText(temp != 0 ? new SimpleDateFormat(FMT).format(new Date(temp)) : "Never");
        this.todayStatus = (TextView)l.findViewById(R.id.navdraw_todaycached);
		temp = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(ApiAccessor.PREF_TODAY_LAST_UPDATE, 0);
		this.todayStatus.setText(temp != 0 ? new SimpleDateFormat(FMT).format(new Date(temp)) : "Never");
        this.noticesStatus = (TextView)l.findViewById(R.id.navdraw_noticescached);
		temp = PreferenceManager.getDefaultSharedPreferences(getActivity()).getLong(ApiAccessor.PREF_NOTICES_LAST_UPDATE, 0);
		this.noticesStatus.setText(temp != 0 ? new SimpleDateFormat(FMT).format(new Date(temp)) : "Never");
		return l;
	}

	public void updateList() {
		//ArrayAdapter<NavBarFancyAdapter.DrawerEntry> a = (ArrayAdapter<NavBarFancyAdapter.DrawerEntry>)mDrawerListView.getAdapter();
		/*int drawable = (ThemeHelper.isBackgroundDark() ? R.drawable.ic_edit_white_24dp : R.drawable.ic_edit_dark_24dp);
		if (ApiAccessor.isLoggedIn()) {
			this.botElements.remove(1);
			this.botElements.add(new NavBarFancyAdapter.DrawerEntry(drawable, getString(R.string.action_logout), this.getActivity()));
		} else {
			if (a.getCount() < 2) {
				this.botElements.add(new NavBarFancyAdapter.DrawerEntry(drawable, getString(R.string.action_login), this.getActivity()));
			}
		}*/
	}

	public boolean isDrawerOpen() {
		return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
	}

	public void closeDrawer() {
		mDrawerLayout.closeDrawer(mFragmentContainerView);
	}

	/**
	 * Users of this fragment must call this method to set up the navigation drawer interactions.
	 *
	 * @param fragmentId   The android:id of this fragment in its activity's layout.
	 * @param drawerLayout The DrawerLayout containing this fragment's UI.
	 */
	public void setUp(int fragmentId, DrawerLayout drawerLayout) {
		mFragmentContainerView = getActivity().findViewById(fragmentId);
		mDrawerLayout = drawerLayout;

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);	

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the navigation drawer and the action bar app icon.
		mDrawerToggle = new ActionBarDrawerToggle(
			(ActionBarActivity)getActivity(),                    /* host Activity */
			mDrawerLayout,                    /* DrawerLayout object */
			R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
			R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
		) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (!isAdded()) {
					return;
				}

				getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (!isAdded()) {
					return;
				}

				if (!mUserLearnedDrawer) {
					// The user manually opened the drawer; store this flag to prevent auto-showing
					// the navigation drawer automatically in the future.
					mUserLearnedDrawer = true;
					SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
					sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
				}

				getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
			}
		};

		// If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
		// per the navigation drawer design guidelines.
		if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
			mDrawerLayout.openDrawer(mFragmentContainerView);
		}

		// Defer code dependent on restoration of previous instance state.
		mDrawerLayout.post(new Runnable() {
			@Override
			public void run() {
				mDrawerToggle.syncState();
			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	public void selectItem(int position) {
		if (position < elements.size()) {
			mCurrentSelectedPosition = position;
			if (mDrawerListView != null) {
				mDrawerListView.setItemChecked(position, true);
			}
		}
		if (mDrawerLayout != null) {
			mDrawerLayout.closeDrawer(mFragmentContainerView);
		}
		if (mCallbacks != null) {
			mCallbacks.onNavigationDrawerItemSelected(position);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.csu = new CacheStatusUpdater(this);
		IntentFilter i = new IntentFilter();
		i.addAction(ApiAccessor.ACTION_BELLTIMES_JSON);
		i.addAction(ApiAccessor.ACTION_NOTICES_JSON);
		i.addAction(ApiAccessor.ACTION_TODAY_JSON);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(csu, i);
		try {
			mCallbacks = (NavigationDrawerCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
		}
	}

	@Override
	public void onDetach() {
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.csu);
		super.onDetach();
		mCallbacks = null;

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Forward the new configuration the drawer toggle component.
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	/**
	 * Per the navigation drawer design guidelines, updates the action bar to show the global app
	 * 'context', rather than just what's in the current screen.
	 *
	private void showGlobalContextActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setTitle(R.string.app_name);
	}*/

	private ActionBar getActionBar() {
		return ((ActionBarActivity)getActivity()).getSupportActionBar();
	}

	/**
	 * Callbacks interface that all activities using this fragment must implement.
	 */
	public static interface NavigationDrawerCallbacks {
		/**
		 * Called when an item in the navigation drawer is selected.
		 */
		void onNavigationDrawerItemSelected(int position);
	}

	public static class CacheStatusUpdater extends BroadcastReceiver {
		private NavigationDrawerFragment ndf;
		public CacheStatusUpdater(NavigationDrawerFragment f) {
			this.ndf = f;
		}
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(ApiAccessor.ACTION_BELLTIMES_JSON)) {
				this.ndf.bellsStatus.setText(new SimpleDateFormat(FMT).format(new Date(PreferenceManager.getDefaultSharedPreferences(ndf.getActivity()).getLong(ApiAccessor.PREF_BELLTIMES_LAST_UPDATE, 0))));
			}
			else if (intent.getAction().equals(ApiAccessor.ACTION_NOTICES_JSON)) {
				this.ndf.noticesStatus.setText(new SimpleDateFormat(FMT).format(new Date(PreferenceManager.getDefaultSharedPreferences(ndf.getActivity()).getLong(ApiAccessor.PREF_NOTICES_LAST_UPDATE, 0))));
			}
			else if (intent.getAction().equals(ApiAccessor.ACTION_TODAY_JSON)) {
				this.ndf.todayStatus.setText(new SimpleDateFormat(FMT).format(new Date(PreferenceManager.getDefaultSharedPreferences(ndf.getActivity()).getLong(ApiAccessor.PREF_TODAY_LAST_UPDATE, 0))));
			}
		}
	}
}
