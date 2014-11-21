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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface} interface
 * to handle interaction events.
 * Use the {@link BelltimesFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class BelltimesFragment extends Fragment {

	private CommonFragmentInterface mListener;
	private SwipeRefreshLayout layout;
	private BelltimesAdapter adapter;
	private BroadcastListener listener;

	/** are we refreshing
	 *  in the UI?
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
		this.mListener.updateCachedStatus(menu);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final SwipeRefreshLayout v = (SwipeRefreshLayout)inflater.inflate(R.layout.fragment_belltimes, container, false);
		this.layout = v;
		final ListView lv = (ListView)v.findViewById(R.id.belltimes_listview);

		lv.setOnScrollListener(new AbsListView.OnScrollListener() {
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
		});
		final Context c = this.getActivity();
		v.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				ApiAccessor.getBelltimes(c, false);
				ApiAccessor.getNotices(c, false);
				ApiAccessor.getToday(c, false);
			}
		});
		Resources r = this.getResources();
		v.setColorSchemeColors(r.getColor(R.color.blue),
			r.getColor(R.color.green),
			r.getColor(R.color.yellow),
			r.getColor(R.color.red));
		this.adapter = new BelltimesAdapter(BelltimesJson.getInstance());
		lv.setAdapter(this.adapter);
		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		IntentFilter i = new IntentFilter();
		i.addAction(ApiAccessor.ACTION_NOTICES_JSON);
		i.addAction(ApiAccessor.ACTION_BELLTIMES_JSON);
		i.addAction(ApiAccessor.ACTION_TODAY_JSON);
		i.addAction(ApiAccessor.ACTION_BELLTIMES_FAILED);
		Log.i("belltimesFragment", "I want " + i.getAction(3));

		if (this.listener == null) {
			this.listener = new BroadcastListener(this);
		}
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(this.listener, i);
		try {
			mListener = (CommonFragmentInterface) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement CommonFragmentInterface");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(this.listener);
		mListener = null;
	}

	private class BroadcastListener extends BroadcastReceiver {
		private BelltimesFragment frag;
		BroadcastListener(BelltimesFragment f) {
			this.frag = f;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String act = intent.getAction();
			if (act.equals(ApiAccessor.ACTION_BELLTIMES_JSON) || act.equals(ApiAccessor.ACTION_TODAY_JSON) || act.equals(ApiAccessor.ACTION_NOTICES_JSON)) {
				if (this.frag == null) return;
				this.frag.layout.setRefreshing(false);
				if (act.equals(ApiAccessor.ACTION_BELLTIMES_JSON)) {
					Toast.makeText(context, R.string.refresh_success, Toast.LENGTH_SHORT).show();
					JsonObject o = new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject();
					if (o.has("bells")) {
						BelltimesJson b = new BelltimesJson(o);
						this.frag.adapter.updateBelltimes(b);
					}
				}
			}
			else if (act.equals(ApiAccessor.ACTION_BELLTIMES_FAILED)) {
				Toast.makeText(context, intent.getIntExtra(ApiAccessor.EXTRA_ERROR_MESSAGE, R.string.err_noerr), Toast.LENGTH_SHORT).show();
				if (this.frag == null) return;
				this.frag.layout.setRefreshing(false);
			}
		}
	}
}
