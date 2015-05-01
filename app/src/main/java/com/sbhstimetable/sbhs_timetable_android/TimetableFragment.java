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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.authflow.LoginActivity;
import com.sbhstimetable.sbhs_timetable_android.backend.adapter2.TimetableAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.event.RefreshingStateEvent;
import com.sbhstimetable.sbhs_timetable_android.event.RequestReceivedEvent;


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


	private SwipeRefreshLayout layout;
	private TimetableAdapter adapter;
	private EventListener listener;

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
		this.listener = new EventListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
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
		final SwipeRefreshLayout v = (SwipeRefreshLayout)inflater.inflate(R.layout.fragment_timetable, container, false);
		this.layout = v;

		final Context c = this.getActivity();
		v.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				/*ApiAccessor.getBelltimes(c, false);
				ApiAccessor.getNotices(c, false);
				ApiAccessor.getToday(c, false);*/
				ApiWrapper.requestBells(c);
				ApiWrapper.requestNotices(c);
				ApiWrapper.requestToday(c);
				ApiWrapper.requestTimetable(c);
			}
		});
		if (ApiWrapper.isLoadingSomething()) {
			TypedValue typed_value = new TypedValue();
			getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
			v.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
			v.setRefreshing(true);
		}
		if (ThemeHelper.isBackgroundDark()) {
			// ignore these errors
			v.setProgressBackgroundColor(R.color.background_floating_material_dark);
		} else {
			v.setProgressBackgroundColor(R.color.background_floating_material_light);
		}
		v.setColorSchemeColors(getResources().getColor(R.color.blue),
			getResources().getColor(R.color.green),
			getResources().getColor(R.color.yellow),
			getResources().getColor(R.color.red));

		ListView z = (ListView)this.getActivity().findViewById(R.id.timetable_listview);
		//if (this.getActivity() == null || this.getActivity().findViewById(R.id.timetable_listview) == null) return v;

		ApiWrapper.getEventBus().registerSticky(this.listener);
		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		ListView z = (ListView)this.getActivity().findViewById(R.id.timetable_listview);
		this.adapter = new TimetableAdapter(this.getActivity());
		z.setAdapter(this.adapter);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		/*if (this.listener == null) {
			this.listener = new BroadcastListener(this);
		}
		LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(this.listener, i);*/
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		//LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(this.listener);

	}

	@Override
	public void onDetach() {
		super.onDetach();
		ApiWrapper.getEventBus().unregister(this.listener);
	}

	private class EventListener {
		private TimetableFragment t;
		public EventListener(TimetableFragment t) {
			this.t = t;
		}
		public void onEvent(RequestReceivedEvent<?> e) {
			if (!ApiWrapper.isLoadingSomething()) {
				t.layout.setRefreshing(false);
				t.layout.clearAnimation();
			}
			if (!e.successful()) {
				Toast.makeText(t.layout.getContext(), "Failed to load " + e.getType() + ": " + e.getErrorMessage(), Toast.LENGTH_SHORT).show();
			}
		}
		public void onEventMainThread(RefreshingStateEvent e) {
			if (e.refreshing && !t.layout.isRefreshing()) {
				TypedValue typed_value = new TypedValue();
				getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
				t.layout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
				t.layout.setRefreshing(true);
			}
		}
	}

	/*private class BroadcastListener extends BroadcastReceiver {
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
				if (this.f == null) {
					return;
				}

				if (act.equals(ApiAccessor.ACTION_TODAY_JSON)) {
					if (refreshing)
                    	Toast.makeText(context, R.string.refresh_success, Toast.LENGTH_SHORT).show();
					this.f.setRefreshing(false);
					refreshing = false;
					this.frag.doTimetable(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA));
				}
			}
			if (act.equals(ApiAccessor.ACTION_TODAY_FAILED)) {
				if (refreshing)
					Toast.makeText(context, intent.getIntExtra(ApiAccessor.EXTRA_ERROR_MESSAGE, R.string.err_noerr), Toast.LENGTH_SHORT).show();
				refreshing = false;
				if (this.frag == null) return;
				this.frag.layout.setRefreshing(false);
			}
		}
	}*/
}
