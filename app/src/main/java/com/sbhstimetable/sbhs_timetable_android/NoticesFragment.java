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
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.authflow.LoginActivity;
import com.sbhstimetable.sbhs_timetable_android.backend.adapter2.NoticesAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.event.RefreshingStateEvent;
import com.sbhstimetable.sbhs_timetable_android.event.RequestReceivedEvent;

public class NoticesFragment extends Fragment {

	private SwipeRefreshLayout layout;
	private EventListener eventListener;
	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment NoticesFragment.
	 */
	public static NoticesFragment newInstance() {
		NoticesFragment fragment = new NoticesFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	public NoticesFragment() {
		// Required empty public constructor
		this.eventListener = new EventListener();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		//this.mListener.updateCachedStatus(this.menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	@SuppressLint("ResourceAsColor")
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		if (!ApiWrapper.isLoggedIn()) {
			View v = inflater.inflate(R.layout.fragment_pls2login, container, false);
			TextView t = (TextView)v.findViewById(R.id.textview);
			t.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent i = new Intent(container.getContext(), LoginActivity.class);
					container.getContext().startActivity(i);
				}
			});
			return v;
		}
		final SwipeRefreshLayout res = (SwipeRefreshLayout)inflater.inflate(R.layout.fragment_notices, container, false);
		this.layout = res;
		final ListView v = (ListView)res.findViewById(R.id.notices_listview);
		v.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int i) {
			}

			@Override
			public void onScroll(AbsListView absListView, int i, int i2, int i3) {
				int topRowVerticalPosition =
					(v == null || v.getChildCount() == 0) ?
					0 : v.getChildAt(0).getTop();
				res.setEnabled(topRowVerticalPosition >= 0);
			}
		});
		final Context c = this.getActivity();
		res.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				ApiWrapper.requestBells(c);
				ApiWrapper.requestNotices(c);
				ApiWrapper.requestToday(c);
			}
		});
		if (ThemeHelper.isBackgroundDark()) {
			res.setProgressBackgroundColor(R.color.background_floating_material_dark);
		} else {
			res.setProgressBackgroundColor(R.color.background_floating_material_light);
		}
		res.setColorSchemeColors(getResources().getColor(R.color.blue),
			getResources().getColor(R.color.green),
			getResources().getColor(R.color.yellow),
			getResources().getColor(R.color.red));

		NoticesAdapter a = new NoticesAdapter(this.getActivity());
		v.setAdapter(a);
		if (ApiWrapper.isLoadingSomething()) {
			TypedValue typed_value = new TypedValue();
			getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
			res.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
			res.setRefreshing(true);
		}
		ApiWrapper.getEventBus().registerSticky(this.eventListener);

		return res;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		ApiWrapper.getEventBus().unregister(this.eventListener);
		this.layout = null;
	}

	private class EventListener {
		public void onEventMainThread(RequestReceivedEvent<?> e) {
			if (!ApiWrapper.isLoadingSomething())
				layout.setRefreshing(false);
			if (!e.successful()) {
				Toast.makeText(layout.getContext(), "Failed to load " + e.getType() + ": " + e.getErrorMessage(), Toast.LENGTH_SHORT).show();
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
