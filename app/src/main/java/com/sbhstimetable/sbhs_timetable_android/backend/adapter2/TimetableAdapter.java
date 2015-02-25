/*
 * SBHS-Timetable-Android: Countdown and timetable all at once (Android app).
 * Copyright (C) 2015 Simon Shields, James Ye
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
package com.sbhstimetable.sbhs_timetable_android.backend.adapter2;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.FullCycleWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.Lesson;

import java.util.ArrayList;
import java.util.List;

public class TimetableAdapter extends DataSetObserver implements ListAdapter, AdapterView.OnItemSelectedListener {
	private FullCycleWrapper cycle;
	private int currentIndex;
	private FrameLayout theFilterSelector;
	private List<DataSetObserver> watchers = new ArrayList<DataSetObserver>();

	private int curDayIndex;
	private int curWeekIndex;

	private String[] days = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
	private String[] weeks = new String[] {"A", "B", "C"};

	public TimetableAdapter(Context c) {
		cycle = new FullCycleWrapper(c);
		ApiWrapper.requestTimetable(c);
		cycle.addDataSetObserver(this);
		currentIndex = cycle.getCurrentDayInCycle();
		int tmp = currentIndex - 1;
		Log.i("TimetableAdapter", "curIndex = " + currentIndex + ", %5 = " + (tmp % 5) + ", / 5 = " + Math.floor(tmp / 5));
		curDayIndex = (tmp % 5);
		curWeekIndex = (int)Math.floor(tmp / 5);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver dso) {
		this.watchers.add(dso);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver dso) {
		this.watchers.remove(dso);
	}

	private void notifyDSOs() {
		for (DataSetObserver i : this.watchers) {
			i.onInvalidated();
		}
	}

	@Override
	public int getCount() {
		return (cycle.hasFullTimetable() ? 6 : 1);
	}

	@Override
	public Object getItem(int index) {
		if (index == 0 && !cycle.hasFullTimetable()) {
			return "Loading view";
		} else if (index == 0) {
			return "AdapterView";
		}
		return cycle.getDayNumber(currentIndex).getPeriod(index);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(int i, View convertView, ViewGroup parent) {
		if (i == 0 && !cycle.hasFullTimetable()) {
			return ((LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_list_loading, null);
		}
		if (i == 0) {
			if (this.theFilterSelector != null) {
				Spinner s = (Spinner)theFilterSelector.findViewById(R.id.spinner_day);
				this.onItemSelected(s, null, s.getSelectedItemPosition(), 0);
				s.setOnItemSelectedListener(this);
				s = (Spinner)theFilterSelector.findViewById(R.id.spinner_week);
				this.onItemSelected(s, null, s.getSelectedItemPosition(), 0);
				s.setOnItemSelectedListener(this);
				return theFilterSelector;
			}
			FrameLayout f = (FrameLayout)((LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_today_spinner, null);
			Spinner s = (Spinner)f.findViewById(R.id.spinner_day);
			ArrayAdapter<String> a = new ArrayAdapter<String>(parent.getContext(), R.layout.textview, days);
			s.setAdapter(a);
			s.setSelection(this.curDayIndex);
			s.setOnItemSelectedListener(this);
			s = (Spinner)f.findViewById(R.id.spinner_week);
			a = new ArrayAdapter<>(parent.getContext(), R.layout.textview, weeks);
			s.setAdapter(a);
			s.setSelection(this.curWeekIndex);
			s.setOnItemSelectedListener(this);
			this.theFilterSelector = f;
			return f;
		}
		final FrameLayout view;
		final TextView header;
		final TextView roomText;
		final TextView teacherText;
		final ImageView changed;
		if (convertView instanceof FrameLayout && convertView.findViewById(R.id.timetable_class_header) != null) {
			view = (FrameLayout)convertView;
		} else {
			view = (FrameLayout)LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_timetable_classinfo, null);
		}
		header = (TextView)view.findViewById(R.id.timetable_class_header);
		roomText = (TextView)view.findViewById(R.id.timetable_class_room);
		teacherText = (TextView)view.findViewById(R.id.timetable_class_teacher);
		changed = (ImageView)view.findViewById(R.id.timetable_class_changed);

		final Lesson l = this.cycle.getDayNumber(currentIndex).getPeriod(i);

		header.setText(l.getSubject());
		roomText.setText(l.getRoom());
		teacherText.setText(l.getTeacher());
		if (!l.roomChanged() && !l.teacherChanged() && !l.cancelled()) {
			changed.setVisibility(View.INVISIBLE);
		} else {
			changed.setVisibility(View.VISIBLE);
		}
		int colour = roomText.getContext().getResources().getColor(R.color.standout);
		if (l.cancelled()) {
			roomText.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
			teacherText.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

		}

		if (l.teacherChanged() || l.cancelled()) {
			teacherText.setTextColor(colour);
		}

		if (l.roomChanged() || l.cancelled()) {
			roomText.setTextColor(colour);
		}
		return view;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.spinner_week) {
			if (position == this.curWeekIndex) return;
			this.curWeekIndex = position;
			this.currentIndex = (5*position) + this.curDayIndex;
		} else if (parent.getId() == R.id.spinner_day) {
			position++;
			if (position == this.curDayIndex) return;
			this.curDayIndex = position;
			this.currentIndex = (5 * this.curWeekIndex) + position;
		}
		Log.i("TimetableAdapter", "new position wk " + this.curWeekIndex + " day " + this.curDayIndex + " => " + this.currentIndex);
		this.notifyDSOs();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onChanged() {
		super.onChanged();
		this.notifyDSOs();
	}

	@Override
	public void onInvalidated() {
		//super.onInvalidated();
		onChanged();
	}
}
