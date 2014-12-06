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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.JsonUtil;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesJson;

public class NoticesFragment extends Fragment {

	private CommonFragmentInterface mListener;
	private Menu menu;
	private NoticesAdapter adapter;
	private SwipeRefreshLayout layout;
	private BroadcastListener listener;
	private boolean refreshing = false;
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
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		super.onCreateOptionsMenu(menu, inflater);
		this.mListener.updateCachedStatus(this.menu);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		if (!ApiAccessor.isLoggedIn()) {
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
				refreshing = true;
				ApiAccessor.getBelltimes(c, false);
				ApiAccessor.getNotices(c, false);
				ApiAccessor.getToday(c, false);
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
			if (act.equals(ApiAccessor.ACTION_NOTICES_JSON)) {
				if (this.frag.layout != null) {
					this.frag.layout.setRefreshing(false);
				} else {
					this.frag.onCreate(new Bundle());
				}
                if (refreshing)// show once per refresh cycle
				Toast.makeText(context, R.string.refresh_success, Toast.LENGTH_SHORT).show();
				refreshing = false;
				JsonObject o = JsonUtil.safelyParseJson(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
				if (o.has("notices")) {
					NoticesJson nj = new NoticesJson(o);
					if (this.frag.adapter == null) return;
					this.frag.adapter.update(nj);
				}
			} else if (act.equals(ApiAccessor.ACTION_NOTICES_FAILED)) {
				if (refreshing)
					Toast.makeText(context, intent.getIntExtra(ApiAccessor.EXTRA_ERROR_MESSAGE, R.string.err_noerr), Toast.LENGTH_SHORT).show();
				refreshing = false;
				if (this.frag == null) return;
				this.frag.layout.setRefreshing(false);
			}
		}
	}
}
