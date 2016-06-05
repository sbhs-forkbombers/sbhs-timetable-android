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

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.backend.adapter2.BelltimesAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.event.RefreshingStateEvent;
import com.sbhstimetable.sbhs_timetable_android.event.RequestReceivedEvent;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must be activities
 * to handle interaction events.
 * Use the {@link BelltimesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BelltimesFragment extends Fragment {

    private SwipeRefreshLayout layout;


    private EventListener eventListener;

    /**
     * are we refreshing
     * in the UI?
     */
    public boolean refreshing = false;
    //private Menu menu;

    public static BelltimesFragment newInstance() {
        return new BelltimesFragment();
    }

    public BelltimesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    @SuppressLint("ResourceAsColor")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final SwipeRefreshLayout v = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_belltimes, container, false);
        this.layout = v;

        final Context c = this.getActivity();
        v.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;
                ApiWrapper.requestBells(c);
                ApiWrapper.requestNotices(c);
                ApiWrapper.requestToday(c);
            }
        });
        if (ThemeHelper.isBackgroundDark()) {
            v.setProgressBackgroundColorSchemeResource(R.color.background_floating_dark);
        } else {
            v.setProgressBackgroundColorSchemeResource(R.color.background_floating_light);
        }
        v.setColorSchemeResources(R.color.blue, R.color.green, R.color.yellow, R.color.red);

        final ListView lv = (ListView) v.findViewById(R.id.belltimes_listview);

		/*lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
			public void onScrollStateChanged(AbsListView absListView, int i) {
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i2, int i3) {
				int topRowVerticalPosition =
					(lv == null || v.getChildCount() == 0) ?
					0 : v.getChildAt(0).getTop();
				Log.i("belltimesFragment", "scroll? COMPUTER SAYS " + topRowVerticalPosition);
				v.setEnabled(topRowVerticalPosition >= -100);
			}
		});*/
        if (ApiWrapper.isLoadingSomething()) {
            // this is a workaround - see https://code.google.com/p/android/issues/detail?id=77712
            TypedValue typed_value = new TypedValue();
            getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
            v.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
            v.setRefreshing(true);
        }
        BelltimesAdapter adapter = new BelltimesAdapter(getActivity());
        lv.setAdapter(adapter);

        this.eventListener = new EventListener(getActivity());
        ApiWrapper.getEventBus().registerSticky(this.eventListener);
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ApiWrapper.getEventBus().unregister(this.eventListener);
        eventListener = null;
    }

    @SuppressWarnings("unused")
    private class EventListener {
        private Context c;

        public EventListener(Context c) {
            this.c = c;
        }

        public void onEventMainThread(RequestReceivedEvent<?> e) {
            if (!ApiWrapper.isLoadingSomething()) {
                layout.setRefreshing(false);
            }
            if (!e.successful()) {
                Toast.makeText(c, "Failed to load " + e.getType() + ": " + e.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        public void onEventMainThread(RefreshingStateEvent e) {
            if (e.refreshing && !layout.isRefreshing()) {
                TypedValue typed_value = new TypedValue();
                getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
                layout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
                layout.setRefreshing(true);
            }
        }
    }

}
