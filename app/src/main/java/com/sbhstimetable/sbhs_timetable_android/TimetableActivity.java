package com.sbhstimetable.sbhs_timetable_android;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.readystatesoftware.systembartint.SystemBarTintManager;
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
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setTintColor(Color.parseColor("#455ede"));
        tintManager.setStatusBarTintEnabled(true);
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
        NotificationService.startUpdatingNotification(this, ApiAccessor.getSessionID());
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
                if (ApiAccessor.isLoggedIn()) {
                    ApiAccessor.logOut(this);
                    this.mNavigationDrawerFragment.updateList();
                    Toast.makeText(this, "Logged out! (You may need to restart the app to remove all your data)", Toast.LENGTH_SHORT).show();
                    StorageCache.deleteAllCacheFiles(this);
                    fragmentManager.beginTransaction().commit();
                }
                else {
                    ApiAccessor.login(this);
                }
                break;
            case 5:
                Intent settings = new Intent(this, SettingsActivity.class);
                this.startActivity(settings);
                break;
            default:
                fragmentManager.beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                        .commit();
                break;
            // HAVE YOU GOT A PLAN BREAK?®
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
            case 5:
                mTitle = getString(R.string.action_login);
                break;
        }
    }

    @Override
    public void setNavigationStyle(int s) {
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
        Log.i("timetableactivity", "cachedness: " + ApiAccessor.noticesCached + " " + ApiAccessor.todayCached + " " + ApiAccessor.bellsCached);
        if (ApiAccessor.noticesCached || ApiAccessor.todayCached || ApiAccessor.bellsCached) {
            i.setVisible(true);
        }
        else {
            i.setVisible(false);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((TimetableActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
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
                   } else {
                       Log.i("timetable", "oops");
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
                DateTimeHelper.bells = new BelltimesJson(new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject(), context);
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
