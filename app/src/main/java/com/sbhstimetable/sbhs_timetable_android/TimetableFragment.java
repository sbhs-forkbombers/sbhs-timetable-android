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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimetableFragment} interface
 * to handle interaction events.
 * Use the {@link TimetableFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TimetableFragment extends Fragment {


    private CommonFragmentInterface mListener;
    private TodayJson today;
    private SwipeRefreshLayout layout;
    private Runnable runnable;
    private Handler h;
    private TodayAdapter adapter;
    private BroadcastListener listener;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CountdownFragment.
     */
    public static TimetableFragment newInstance() {
        return new TimetableFragment();
    }
    public TimetableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.h = new Handler();
        //Toast.makeText(getActivity(), "Timetable! Indoor Walking Route in -10 minutes!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mListener.updateCachedStatus(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final SwipeRefreshLayout v =  (SwipeRefreshLayout)inflater.inflate(R.layout.fragment_timetable, container, false);
        this.layout = v;

        final Context c = this.getActivity();
        v.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!ApiAccessor.hasInternetConnection(c)) {
                    Toast.makeText(c, R.string.refresh_failure, Toast.LENGTH_SHORT).show();
                    v.setRefreshing(false);
                    return;
                }
                ApiAccessor.getBelltimes(c, false);
                ApiAccessor.getNotices(c, false);
                ApiAccessor.getToday(c, false);
                h.removeCallbacks(runnable);
                runnable = new CountdownFragment.StopSwiping(v);
                h.postDelayed(runnable, 10000);
            }
        });
        Resources r = this.getResources();
        v.setColorSchemeColors(r.getColor(R.color.green),
                r.getColor(R.color.red),
                r.getColor(R.color.blue),
                r.getColor(R.color.yellow));
        ListView z = (ListView)this.getActivity().findViewById(R.id.timetable_listview);
        if (z != null) {
            ApiAccessor.getToday(this.getActivity());
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (TodayJson.getInstance() != null && TodayJson.getInstance().getPeriod(1) != null) {
            doTimetable(TodayJson.getInstance());
        }
        else {
            JsonObject res = StorageCache.getTodayJson(this.getActivity(), DateTimeHelper.getDateString(getActivity()));
            if (res != null && res.has("timetable")) {
                this.doTimetable(new TodayJson(res));
            }
        }
    }


    public void doTimetable(String b) {
        JsonParser g = new JsonParser();
        JsonObject obj = g.parse(b).getAsJsonObject();
        if (obj.has("timetable")) {
            doTimetable(new TodayJson(obj));
        }
    }

    public void doTimetable(TodayJson o) {
        this.today = o;
        if (this.adapter == null) {
            this.adapter = new TodayAdapter(this.today);
            ListView z = (ListView)this.getActivity().findViewById(R.id.timetable_listview);
            z.setAdapter(this.adapter);
        }
        else {
            this.adapter.updateDataSet(this.today);
        }

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        IntentFilter i = new IntentFilter();
        i.addAction(ApiAccessor.ACTION_TODAY_JSON);
        i.addAction(ApiAccessor.ACTION_BELLTIMES_JSON);
        i.addAction(ApiAccessor.ACTION_NOTICES_JSON);
        if (this.listener == null) {
            this.listener = new BroadcastListener(this);
        }
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(this.listener, i);
        try {
            mListener = (CommonFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(this.listener);
        if (this.today != null) {
            StorageCache.cacheTodayJson(this.getActivity(), this.today.getDate(), this.today.toString());
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class BroadcastListener extends BroadcastReceiver {
        private SwipeRefreshLayout f;
        private TimetableFragment frag;
        BroadcastListener(TimetableFragment f) {
            this.f = f.layout;
            this.frag = f;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if (act.equals(ApiAccessor.ACTION_BELLTIMES_JSON) || act.equals(ApiAccessor.ACTION_TODAY_JSON) || act.equals(ApiAccessor.ACTION_NOTICES_JSON)) {
                if (this.f == null) {
                    this.f = this.frag.layout;
                }
                if (this.f == null) return;
                this.f.setRefreshing(false);
                this.frag.h.removeCallbacks(this.frag.runnable);
                Toast.makeText(context, R.string.refresh_success, Toast.LENGTH_SHORT).show();
                if (act.equals(ApiAccessor.ACTION_TODAY_JSON)) {
                    this.frag.doTimetable(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
                }
            }
        }
    }


}
