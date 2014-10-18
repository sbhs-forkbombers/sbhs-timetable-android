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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.JsonUtil;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.NoticesDropDownAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesJson;

public class NoticesFragment extends Fragment {

	private CommonFragmentInterface mListener;
	private Menu menu;
	private NoticesAdapter adapter;
	private NoticesDropDownAdapter spinnerAdapter;
	private Handler h;
	private Runnable runnable;
	private SwipeRefreshLayout layout;
	private BroadcastListener listener;
	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment NoticesFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static NoticesFragment newInstance() {
		NoticesFragment fragment = new NoticesFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}
	public NoticesFragment() {
		// Required empty public constructor
	}

	@Override
	@SuppressWarnings("all")
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		super.onCreateOptionsMenu(menu, inflater);
		this.mListener.updateCachedStatus(this.menu);
		this.mListener.setNavigationStyle(ActionBar.NAVIGATION_MODE_LIST);
		this.spinnerAdapter = new NoticesDropDownAdapter();
		((ActionBarActivity)getActivity()).getSupportActionBar().setListNavigationCallbacks(spinnerAdapter, new ActionBar.OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int i, long l) {
			if (adapter == null) {
				return true;
			}
			if (i == 0) {
				adapter.filter(null);
			}
			else {
				adapter.filter(NoticesJson.Year.fromString(spinnerAdapter.getItem(i).replace("Year ", "")));
			}
			return true;
			}
		});
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
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
		h = new Handler();
		res.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
			if (!ApiAccessor.hasInternetConnection(c)) {
				Toast.makeText(c, R.string.refresh_failure, Toast.LENGTH_SHORT).show();
				res.setRefreshing(false);
				return;
			}
			ApiAccessor.getBelltimes(c, false);
			ApiAccessor.getNotices(c, false);
			ApiAccessor.getToday(c, false);
			h.removeCallbacks(runnable);
			runnable = new CountdownFragment.StopSwiping(res);
			h.postDelayed(runnable, 10000);
			}
		});
		Resources r = this.getResources();
		res.setColorSchemeColors(r.getColor(R.color.blue),
			r.getColor(R.color.green),
			r.getColor(R.color.yellow),
			r.getColor(R.color.red));
		JsonObject o = StorageCache.getNotices(getActivity(), DateTimeHelper.getDateString(getActivity()));
		NoticesJson n = NoticesJson.getInstance();
		if (o != null) {
			n = new NoticesJson(o);
		}

		if (n != null) {
			NoticesAdapter a = new NoticesAdapter(n);
			this.adapter = a;
			v.setAdapter(a);
		}

		return res;
	}

	@Override
	public void onPause() {
		super.onPause();
		this.mListener.setNavigationStyle(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		IntentFilter i = new IntentFilter();
		i.addAction(ApiAccessor.ACTION_NOTICES_JSON);
		i.addAction(ApiAccessor.ACTION_BELLTIMES_JSON);
		i.addAction(ApiAccessor.ACTION_TODAY_JSON);
		if (this.listener == null) {
			this.listener = new BroadcastListener(this);
		}
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(this.listener, i);
		try {
			mListener = (CommonFragmentInterface) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(this.listener);
		mListener = null;
	}

	private class BroadcastListener extends BroadcastReceiver {
		private SwipeRefreshLayout f;
		private NoticesFragment frag;
		BroadcastListener(NoticesFragment f) {
			this.f = f.layout;
			this.frag = f;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String act = intent.getAction();
			if (act.equals(ApiAccessor.ACTION_BELLTIMES_JSON) || act.equals(ApiAccessor.ACTION_TODAY_JSON) || act.equals(ApiAccessor.ACTION_NOTICES_JSON)) {
				if (this.f == null) return;
				this.f.setRefreshing(false);
				this.frag.h.removeCallbacks(this.frag.runnable);
				Toast.makeText(context, R.string.refresh_success, Toast.LENGTH_SHORT).show();
				if (act.equals(ApiAccessor.ACTION_NOTICES_JSON)) {
					JsonObject o = JsonUtil.safelyParseJson(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
					if (o.has("notices")) {
						NoticesJson nj = new NoticesJson(o);
						this.frag.adapter.update(nj);
					}
				}
			}
		}
	}
}
