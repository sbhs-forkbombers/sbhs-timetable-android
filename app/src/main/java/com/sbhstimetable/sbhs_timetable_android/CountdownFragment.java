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
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;
import com.sbhstimetable.sbhs_timetable_android.debug.DebugActivity;

public class CountdownFragment extends Fragment {

	private CommonFragmentInterface mListener;
	private static CountDownTimer timeLeft;
	private static boolean cancelling = false;
	private SwipeRefreshLayout mainView;
	private BroadcastListener listener;
	private int tapCount = 0;
	private boolean refreshing = false;
	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @return A new instance of fragment TimetableFragment.
	 */
	public static CountdownFragment newInstance() {
		CountdownFragment fragment = new CountdownFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public CountdownFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final CountdownFragment me = this;
		final SwipeRefreshLayout f = (SwipeRefreshLayout)inflater.inflate(R.layout.fragment_countdown, container, false);
		f.findViewById(R.id.countdown_name).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (++tapCount == 7) {
					Toast.makeText(getActivity(), R.string.toast_debug_entry, Toast.LENGTH_SHORT).show();
					tapCount = 0;
					Intent i = new Intent(getActivity(), DebugActivity.class);
					getActivity().startActivity(i);
				}
			}
		});
		f.setColorSchemeColors(getResources().getColor(R.color.blue),
			getResources().getColor(R.color.green),
			getResources().getColor(R.color.yellow),
			getResources().getColor(R.color.red));
		f.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				refreshing = true;
				ApiAccessor.getNotices(me.getActivity(), false);
				ApiAccessor.getToday(me.getActivity(), false);
				ApiAccessor.getBelltimes(me.getActivity(), false);
			}
		});
		this.mainView = f;
		return f;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		this.mListener.updateCachedStatus(menu);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (timeLeft != null) {
			cancelling = true;
			timeLeft.cancel();
			cancelling = false;
		}
		this.updateTimer();
	}

	public void updateTimer() {
		final View f = this.getView();
		if (f == null) {
			return;
		}

		if (timeLeft != null) {
			cancelling = true;
			timeLeft.cancel();
			cancelling = false;
		}

		RelativeLayout extraData = (RelativeLayout)f.findViewById(R.id.countdown_extraData);
		TextView teacher = (TextView)extraData.findViewById(R.id.countdown_extraData_teacher);
		teacher.setText("…");
		TextView room = (TextView)extraData.findViewById(R.id.countdown_extraData_room);
		room.setText("…");
		TextView subject = (TextView)extraData.findViewById(R.id.countdown_extraData_subject);
		subject.setText("…");
		String label = "Something";
		String connector = "happens in";
		TodayJson.Period p = null;
		if (DateTimeHelper.bells != null) {
			BelltimesJson.Bell next = DateTimeHelper.bells.getNextBell();
			if (next != null) {
				BelltimesJson.Bell now = DateTimeHelper.bells.getIndex(next.getIndex() - 1);
				if (now.isPeriod() && now.getPeriodNumber() < 5) { // in a period, it's not last period.
					connector = "ends in";
					if (ApiAccessor.isLoggedIn() && TodayJson.getInstance() != null) {
						p = TodayJson.getInstance().getPeriod(now.getPeriodNumber());
						label = p.name();
						teacher.setText(p.teacher());
						room.setText(p.room());
						subject.setText(p.name());
						extraData.setVisibility(View.VISIBLE);
					} else {
						label = now.getLabel();
						extraData.setVisibility(View.INVISIBLE);
					}
				} else if (now.isPeriod() && now.getPeriodNumber() == 5) { // last period
					connector = "in";
					label = "School ends";
                    extraData.setVisibility(View.INVISIBLE);
				} else if (now.getIndex() + 1 < DateTimeHelper.bells.getMaxIndex() && DateTimeHelper.bells.getIndex(now.getIndex() + 1).isPeriod()) { // in a break followed by a period - Lunch 2, Recess, Transition.
					connector = "starts in";
					if (ApiAccessor.isLoggedIn() && TodayJson.getInstance() != null) {
						p = TodayJson.getInstance().getPeriod(DateTimeHelper.bells.getIndex(now.getIndex() + 1).getPeriodNumber());
						label = p.name();
						teacher.setText(p.teacher());
						room.setText(p.room());
						subject.setText(p.name());
						extraData.setVisibility(View.VISIBLE);
					} else {
						label = DateTimeHelper.bells.getIndex(now.getIndex() + 1).getLabel();
						extraData.setVisibility(View.VISIBLE);
					}
				} else { // There's consecutive non-periods - i.e lunch 1 -> lunch 2
					label = now.getLabel();
					connector = "starts in";
				}
			} else {
				// end of day
				label = "School starts";
				connector = "in";
				if (TodayJson.getInstance() != null && TodayJson.getInstance().getPeriod(1) != null) {
					extraData.setVisibility(View.VISIBLE);
					p = TodayJson.getInstance().getPeriod(1);
					teacher.setText(p.teacher());
					room.setText(p.room());
					subject.setText(p.name());
				} else {
					extraData.setVisibility(View.INVISIBLE);
				}
			}
		}

		if (p != null) {
			if (p.teacherChanged()) {
				teacher.setTextColor(getActivity().getResources().getColor(R.color.standout));
			} else {
				//teacher.setTextColor(getActivity().getResources().getColor(R.color.secondary_text_dark));
			}
			if (p.roomChanged()) {
				room.setTextColor(getActivity().getResources().getColor(R.color.standout));
			} else {
				//room.setTextColor(getActivity().getResources().getColor(android.R.color.secondary_text_dark));
			}
		}

		final String innerLabel = label;
		((TextView)f.findViewById(R.id.countdown_name)).setText(label);
		((TextView)f.findViewById(R.id.countdown_in)).setText(connector);
		final TextView t = (TextView)f.findViewById(R.id.countdown_countdown);
		final CountdownFragment frag = this;
		CountDownTimer timer = new CountDownTimer(DateTimeHelper.milliSecondsUntilNextEvent(), 1000) {
			long lastTime = 10000;
			boolean isLast = innerLabel.equals("School ends");
			@Override
			public void onTick(long l) {
				lastTime = l;
				t.setText(DateTimeHelper.formatToCountdown(l));
			}

			@Override
			public void onFinish() {
				if (this.lastTime <= 1000 && !cancelling) {
					if (this.isLast) {
						ApiAccessor.getToday(frag.getActivity());
						ApiAccessor.getBelltimes(frag.getActivity());
					}
					final Handler h = new Handler();
					h.postDelayed(new Runnable() {
						@Override
						public void run() {
							updateTimer();
						}
					}, 1000);
				}
			}
		};
		timer.start();
		timeLeft = timer;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		IntentFilter i = new IntentFilter(TimetableActivity.BELLTIMES_AVAILABLE);
		i.addAction(TimetableActivity.TODAY_AVAILABLE);
		i.addAction(ApiAccessor.ACTION_BELLTIMES_JSON);
		i.addAction(ApiAccessor.ACTION_TODAY_JSON);
		i.addAction(ApiAccessor.ACTION_TODAY_FAILED);
		if (this.listener == null) {
			this.listener = new BroadcastListener();
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
		@Override
		public void onReceive(Context context, Intent intent) {
			updateTimer();
			if (intent.getAction().equals(ApiAccessor.ACTION_BELLTIMES_JSON)) {
				if (mainView != null)
					mainView.setRefreshing(false);
				if (refreshing)
					Toast.makeText(getActivity(), R.string.refresh_success, Toast.LENGTH_SHORT).show();
				refreshing = false;
			}
			if (intent.getAction().equals(TimetableActivity.TODAY_AVAILABLE) && BelltimesJson.getInstance() != null) {
				ApiAccessor.getBelltimes(context);

			}
			else if (intent.getAction().equals(ApiAccessor.ACTION_TODAY_FAILED)) {
				if (refreshing)
					Toast.makeText(context, intent.getIntExtra(ApiAccessor.EXTRA_ERROR_MESSAGE, R.string.err_noerr), Toast.LENGTH_SHORT).show();
				refreshing = false;
			}
		}
	}

}
