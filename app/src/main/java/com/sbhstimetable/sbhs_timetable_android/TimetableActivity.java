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

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;
import com.sbhstimetable.sbhs_timetable_android.backend.service.NotificationService;


public class TimetableActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, CommonFragmentInterface {
    private static final String COUNTDOWN_FRAGMENT_TAG = "countdownFragment";
    public static final String BELLTIMES_AVAILABLE = "bellsArePresent";
    public static final String TODAY_AVAILABLE = "todayIsPresent";

    public static final String PREF_DISABLE_DIALOG = "disableFeedbackDialog";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public NavigationDrawerFragment mNavigationDrawerFragment;
    private Menu menu;
    public boolean isActive = false;
    private int navStyle = ActionBar.NAVIGATION_MODE_STANDARD;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_timetable);
        ApiAccessor.load(this);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        IntentFilter interesting = new IntentFilter(ApiAccessor.ACTION_TODAY_JSON);
        interesting.addAction(ApiAccessor.ACTION_BELLTIMES_JSON);
        interesting.addAction(ApiAccessor.ACTION_NOTICES_JSON);
        LocalBroadcastManager.getInstance(this).registerReceiver(new ReceiveBroadcast(this), interesting);
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        // Grab belltimes.json
        ApiAccessor.getBelltimes(this);
        ApiAccessor.getToday(this);
        ApiAccessor.getNotices(this);
        final Context c = this;
        Thread t = new Thread(new Runnable() {
            public void run() {
                StorageCache.cleanCache(c);
            }
        });
        t.start();
        setProgressBarIndeterminateVisibility(true);

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
                        .commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, NoticesFragment.newInstance(), COUNTDOWN_FRAGMENT_TAG)
                        .commit();
                break;
            case 3:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, BelltimesFragment.newInstance(), COUNTDOWN_FRAGMENT_TAG)
                        .commit();
                break;
            case 4:
                Intent settings = new Intent(this, SettingsActivity.class);
                this.startActivity(settings);
                break;
            case 5:
                if (ApiAccessor.isLoggedIn()) {
                    ApiAccessor.logout(this);
                    this.mNavigationDrawerFragment.updateList();
                    Toast.makeText(this, "Logged out! (You may need to restart the app to remove all your data)", Toast.LENGTH_SHORT).show();
                    StorageCache.deleteAllCacheFiles(this);
                    fragmentManager.beginTransaction().commit();
                }
                else {
                    ApiAccessor.login(this);
                }
                break;
            // HAVE YOU GOT A PLAN BREAK?®
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_countdown);
                break;
            case 2:
                mTitle = getString(R.string.title_timetable);
                break;
            case 3:
                mTitle = getString(R.string.title_notices);
                break;
            case 4:
                mTitle = getString(R.string.title_belltimes);
                break;
        }
    }

    @Override
    public void setNavigationStyle(int s) {
        if (getActionBar() != null)
            getActionBar().setNavigationMode(s);
        this.navStyle = s;
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar == null) return;
        actionBar.setNavigationMode(this.navStyle);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.timetable, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.action_cache_status) {
            if (ApiAccessor.isLoggedIn()) {
                ApiAccessor.getNotices(this);
                ApiAccessor.getToday(this);
            }
            else {
                Intent i = new Intent(this, LoginActivity.class);
                this.startActivity(i);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isActive = false;
    }

    public void updateCachedStatus(Menu m) {
        if (this.menu == null) return;
        MenuItem i = this.menu.findItem(R.id.action_cache_status);
        if (i == null) return;
        if (ApiAccessor.noticesLoaded && ApiAccessor.todayLoaded && ApiAccessor.bellsLoaded) {
            setProgressBarIndeterminateVisibility(false);
        }
        else {
            setProgressBarIndeterminateVisibility(true);
        }
        if (ApiAccessor.noticesCached || ApiAccessor.todayCached || ApiAccessor.bellsCached) {
            i.setVisible(true);
        }
        else {
            i.setVisible(false);
        }
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

                if (this.activity.isActive && this.activity.getFragmentManager().findFragmentByTag(COUNTDOWN_FRAGMENT_TAG) instanceof TimetableFragment) {
                    TimetableFragment frag = ((TimetableFragment) this.activity.getFragmentManager().findFragmentByTag(COUNTDOWN_FRAGMENT_TAG));
                    if (frag != null) {
                        frag.doTimetable(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
                    }
                }
                else {
                    JsonObject o = new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject();
                    if (o.has("error")) {
                        // reject it silently/
                        return;
                    }
                    StorageCache.cacheTodayJson(this.activity, DateTimeHelper.getDateString(context), intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));

                    TodayJson j = new TodayJson(o); // set INSTANCE
                    StorageCache.writeCachedDate(context, j.getDate());
                }
                LocalBroadcastManager.getInstance(this.activity).sendBroadcast(new Intent(TimetableActivity.TODAY_AVAILABLE));
            }
            else if (intent.getAction().equals(ApiAccessor.ACTION_BELLTIMES_JSON)) {
                //activity.setProgressBarIndeterminateVisibility(false);
                ApiAccessor.bellsLoaded = true;
                StorageCache.cacheBelltimes(this.activity, DateTimeHelper.getDateString(context), intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
                DateTimeHelper.bells = new BelltimesJson(new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject());
                LocalBroadcastManager.getInstance(this.activity).sendBroadcast(new Intent(TimetableActivity.BELLTIMES_AVAILABLE));
            }
            else if (intent.getAction().equals(ApiAccessor.ACTION_NOTICES_JSON)) {
                //activity.setProgressBarIndeterminateVisibility(false);
                ApiAccessor.noticesLoaded = true;
                StorageCache.cacheNotices(this.activity, DateTimeHelper.getDateString(context), intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
                new NoticesJson(new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject());
            }
            this.activity.updateCachedStatus(this.activity.menu);
        }
    }

}