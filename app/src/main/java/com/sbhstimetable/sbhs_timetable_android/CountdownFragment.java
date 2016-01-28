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
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.api.FullCycleWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.Lesson;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.debug.DebugActivity;
import com.sbhstimetable.sbhs_timetable_android.debug.GoogleApiActivity;
import com.sbhstimetable.sbhs_timetable_android.event.BellsEvent;
import com.sbhstimetable.sbhs_timetable_android.event.RefreshingStateEvent;
import com.sbhstimetable.sbhs_timetable_android.event.RequestReceivedEvent;

import org.joda.time.DateTime;

public class CountdownFragment extends Fragment {

	private SwipeRefreshLayout mainView;
	//private BroadcastListener listener;
	private int tapCount = 0;
	private int gapiTapCount = 0;
	private DateTimeHelper dth;
	private StorageCache cache;
	private FullCycleWrapper cycle;
	private CountDownTimer mTimer;
	private DataWatcherEventListener evListener;
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
	@SuppressLint("ResourceAsColor")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
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
		f.findViewById(R.id.countdown_countdown).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (++gapiTapCount == 7) {
					Toast.makeText(getActivity(), R.string.toast_debug_entry, Toast.LENGTH_SHORT).show();
					tapCount = 0;
					Intent i = new Intent(getActivity(), GoogleApiActivity.class);
					getActivity().startActivity(i);
				}
			}
		});
		if (ThemeHelper.isBackgroundDark()) {
			f.setProgressBackgroundColorSchemeResource(R.color.background_floating_material_dark);
		} else {
			f.setProgressBackgroundColorSchemeResource(R.color.background_floating_material_light);
		}
		f.setColorSchemeResources(R.color.blue, R.color.green, R.color.yellow, R.color.red);
		final Context c = inflater.getContext();
		f.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				ApiWrapper.requestNotices(c);
				ApiWrapper.requestBells(c);
				ApiWrapper.requestToday(c);
			}
		});
		if (ApiWrapper.isLoadingSomething()) {
			TypedValue typed_value = new TypedValue();
			getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
			f.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
			f.setRefreshing(true);
		}
		Log.i("CountdownFragment", "====> onCreateView");
		this.mainView = f;
		setup(f);
		return f;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		cleanup();
		Log.i("CountdownFragment", "<==== onDestroyView");
	}

	private void setup(View f) {
		if (this.dth == null) this.dth = new DateTimeHelper(getActivity());
		if (this.cache == null)	this.cache = new StorageCache(getActivity());
		if (this.cycle == null) this.cycle = new FullCycleWrapper(getActivity());
		if (this.evListener == null) {
			this.evListener = new DataWatcherEventListener();
			this.cycle.addDataSetObserver(this.evListener);
			ApiWrapper.getEventBus().registerSticky(this.evListener);
		}
		if (f != null) {
			this.updateTimer(f);
		} else {
			this.updateTimer();
		}
	}

	private void cleanup() {
		if (this.mTimer != null) {
			this.mTimer.cancel();
		}
		if (this.evListener != null) {
			ApiWrapper.getEventBus().unregister(this.evListener);
			this.cycle.removeDataSetObserver(this.evListener);
		}
		this.dth = null;
		this.cache = null;
		this.cycle = null;
		this.evListener = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private void debug(String s) {
		Log.d("CountdownFrag", s);
	}


	public void updateTimer() {
		final View f = this.getView();
		if (f == null) {
			Log.wtf("CountdownFragment", "getView() == NULL!"); // this should never happen. If it happens, the timer has been left running after the fragment has gone.
			if (mTimer != null) mTimer.cancel();
			mTimer = null;
			return;
		}
		updateTimer(f);
	}

	public void updateTimer(final View f) {

		RelativeLayout extraData = (RelativeLayout)f.findViewById(R.id.countdown_extraData);
		TextView teacher = (TextView)extraData.findViewById(R.id.countdown_extraData_teacher);
		teacher.setText("…");
		TextView room = (TextView)extraData.findViewById(R.id.countdown_extraData_room);
		room.setText("…");
		TextView subject = (TextView)extraData.findViewById(R.id.countdown_extraData_subject);
		subject.setText("…");
		String label = "Something";
		String connector = "happens in";
		Lesson p = null;
		if (!dth.hasBells()) {
			Belltimes bells = cache.loadBells();
			dth.setBells(bells);
		}
		debug("has bells - " + dth.hasBells());
		if (dth.hasBells()) {
			Belltimes.Bell next = dth.getNextBell();
			if (next != null && next.getPreviousBellTime() != null) {
				debug("next - " + next + " " + next.getBellDisplay() + " " + next.getBellName());
				Belltimes.Bell now = next.getPreviousBellTime();
				debug("period start - period => " + now.getBellName() + "(" + now.getPeriodNumber() + ") is ps? " + now.isPeriodStart());
				if (now.isPeriodStart() && now.getPeriodNumber() < 5) { // in a period, it's not last period.
					connector = "ends in";
					if (ApiWrapper.isLoggedIn() && cycle.ready()) {
						p = cycle.getToday().getPeriod(now.getPeriodNumber());
						debug("has today - " + p);
						label = p.getSubject();
						teacher.setText(p.getTeacher());
						room.setText(p.getRoom());
						subject.setText(p.getSubject());
						extraData.setVisibility(View.VISIBLE);
					} else {
						label = now.getBellName();
						extraData.setVisibility(View.INVISIBLE);
					}
				} else if (now.isPeriodStart() && now.getPeriodNumber() == 5) { // last period
					connector = "in";
					label = "School ends";
					extraData.setVisibility(View.INVISIBLE);
				} else if (!now.isPeriodStart() && next.isPeriodStart()) { // in a break followed by a period - Lunch 2, Recess, Transition.
					connector = "starts in";
					if (ApiWrapper.isLoggedIn() && cycle.ready()) {
						p = cycle.getToday().getPeriod(next.getPeriodNumber());
						label = p.getSubject();
						teacher.setText(p.getTeacher());
						room.setText(p.getRoom());
						subject.setText(p.getSubject());
						extraData.setVisibility(View.VISIBLE);
					} else {
						label = next.getBellName();
						extraData.setVisibility(View.INVISIBLE);
					}
				} else { // There's consecutive non-periods - i.e lunch 1 -> lunch 2
					label = next.getBellName();
					connector = "starts in";
					extraData.setVisibility(View.INVISIBLE);
				}
			} else {
				// end of day
				label = "School starts";
				connector = "in";
				if (cycle.hasFullTimetable()) {
					extraData.setVisibility(View.VISIBLE);
					p = cycle.getToday().getPeriod(1);
					teacher.setText(p.getTeacher());
					room.setText(p.getRoom());
					subject.setText(p.getSubject());
				} else {
					extraData.setVisibility(View.INVISIBLE);
				}
			}
		} else {
			ApiWrapper.requestBells(this.getActivity());
			Log.i("CountdownFragment", "Requesting bells to make up for my lameness");
		}

		if (p != null) {
			if (p.teacherChanged()) {
				teacher.setTextColor(ContextCompat.getColor(getActivity(), R.color.standout));
			} else {
				teacher.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text_default_material_dark));
				//teacher.setTextColor(getActivity().getResources().getColor(R.color.secondary_text_dark));
			}
			if (p.roomChanged()) {
				room.setTextColor(ContextCompat.getColor(getActivity(), R.color.standout));
			} else {
				room.setTextColor(ContextCompat.getColor(getActivity(), R.color.primary_text_default_material_dark));
				//room.setTextColor(getActivity().getResources().getColor(android.R.color.secondary_text_dark));
			}
		}


		((TextView)f.findViewById(R.id.countdown_name)).setText(label);
		((TextView)f.findViewById(R.id.countdown_in)).setText(connector);
		final TextView t = (TextView)f.findViewById(R.id.countdown_countdown);
		//final TextView j = (TextView)f.findViewById(R.id.countdown_name);
		/*if (mTimer != null) {
			return;
		}*/
		long millisToCountfor = dth.getNextEvent().toDateTime().getMillis() - DateTime.now().getMillis();
		millisToCountfor += 1000; // show 00:00
		Log.i("CountdownFragment", "Will count for " + millisToCountfor + "ms");
		CountDownTimer timer = new CountDownTimer(millisToCountfor, 1000) {
			@Override
			public void onTick(long millisUntilFinished) {
				//t.setText(new DateTimeFormatterBuilder().append(DateTimeHelper.getHHMMFormatter()).appendLiteral(':').appendSecondOfMinute(2).appendMillisOfSecond(4).toFormatter().print(DateTime.now().toLocalTime()))
				int secondsLeft;
				if (dth == null) {
					Log.i("CountdownFragment", "No DTH!");
					if (getActivity() != null) {
						Log.i("CountdownFragment", "I'll make a DTH");
						setup(f);
					}
					// probably been destroyed or something
					this.cancel();
					return;
				}
				if (dth.getNextEvent() != null) {
					secondsLeft = (int) Math.floor((dth.getNextEvent().toDateTime().getMillis() - DateTime.now().getMillis()) / 1000);
				} else {
					Log.w("CountdownFragment", "No next event...");
					secondsLeft = 0;
				}
				int seconds = secondsLeft % 60;
				secondsLeft -= seconds;
				secondsLeft /= 60;
				int minutes = secondsLeft % 60;
				secondsLeft -= minutes;
				secondsLeft /= 60;
				if (secondsLeft == 0) {
					t.setText(String.format("%02dm %02ds", new Object[] {minutes, seconds}));
				} else {
					t.setText(String.format("%02dh %02dm %02ds", new Object[]{secondsLeft, minutes, seconds}));
				}
			}

			@Override
			public void onFinish() {
				//j.setText(new DateTimeFormatterBuilder().append(DateTimeHelper.getHHMMFormatter()).appendLiteral(':').appendSecondOfMinute(2).appendMillisOfSecond(4).toFormatter().print(DateTime.now().toLocalTime()));
				mTimer = null;
				t.setText(R.string.no_time_left);
				updateTimer();

			}
		};
		mTimer = timer;
		timer.start();
	}

	@Override
	public void onAttach(Context a) {
		super.onAttach(a);
		Log.i("CountdownFragment", "ATTACHED ===> " + this.getView());
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i("CountdownFragment", "DETACHED <===");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i("CountdownFragment", "<==== RESUMING: " + mTimer);
		/*if (timeLeft != null) {
			cancelling = true;
			timeLeft.cancel();
			cancelling = false;}*/
		setup(null);
		Log.i("CountdownFragment", "<==== RESUMED");

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i("CountdownFragment", "===> PAUSING");
		Log.i("CountdownFragment", "===> PAUSED");
		cleanup();
		//LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(this.listener);
		//mListener = null;
	}

	@SuppressWarnings("unused")
	private class DataWatcherEventListener extends DataSetObserver {
		@Override
		public void onChanged() {
			super.onChanged();
			//Log.i("CountdownFrag$DWatcher", "DSO changed! Updating labels");
			updateTimer();
		}

		@Override
		public void onInvalidated() {
			//super.onInvalidated();
			onChanged();
		}

		public void onEventMainThread(BellsEvent b) {
			//Log.i("CountdownFrag$DWatcher", "Got bellsevent. Error: " + b.getErrorMessage());
			if (b.successful() && !b.getResponse().isStatic()) {
				updateTimer();
			}
		}

		public void onEventMainThread(RequestReceivedEvent<?> e) {
			if (!ApiWrapper.isLoadingSomething()) {
				mainView.setRefreshing(false);
			}
			if (!e.successful()) {
				Toast.makeText(mainView.getContext(), "Failed to load " + e.getType() + ": " + e.getErrorMessage(), Toast.LENGTH_SHORT).show();
			}
		}

		public void onEventMainThread(RefreshingStateEvent e) {
			if (e.refreshing && !mainView.isRefreshing()) {
				TypedValue typed_value = new TypedValue();
				getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
				mainView.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
				mainView.setRefreshing(true);
			}
		}
	}

}
