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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

public class BelltimesAdapter implements ListAdapter, AdapterView.OnItemSelectedListener {
	private StorageCache cache;
	private Belltimes bells;


	public BelltimesAdapter(Context c) {
		this.cache = new StorageCache(c);
		this.bells = cache.loadBells();
		if (bells == null || !bells.current()) {
			ApiWrapper.requestBells(c);
			bells = null;
		}
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
	public void registerDataSetObserver(DataSetObserver observer) {

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {

	}

	@Override
	public int getCount() {
		return (bells == null ? 2 : bells.getLength()+1);
	}

	@Override
	public Object getItem(int position) {
		if (bells == null) {
			if (position == 0) {
				return "No bells";
			} else {
				return "Last updated";
			}
		}
		return this.bells.getBellIndex(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (position == getCount()-1) {
			View v = ((LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_last_updated, null);
			TextView t = (TextView)v.findViewById(R.id.last_updated);
			DateTimeFormatter f = new DateTimeFormatterBuilder().appendDayOfWeekShortText().appendLiteral(' ').appendDayOfMonth(2).appendLiteral(' ')
					.appendMonthOfYearShortText().appendLiteral(' ').appendYear(4,4).appendLiteral(' ').appendHourOfDay(2)
					.appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':').appendSecondOfMinute(2).toFormatter();
			t.setText(f.print(this.bells.getFetchTime().toLocalDateTime()));
			return v;
		}
		View r;
		if (bells == null) {
			TextView t = new TextView(parent.getContext());
			t.setText("Couldn't load belltimes!");
			t.setGravity(Gravity.CENTER);
			t.setTextAppearance(parent.getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
			return t;
		}
		if (convertView instanceof FrameLayout) {
			r = convertView;
		} else {
			r = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_belltimes_entry, null);
		}
		Belltimes.Bell bell = this.bells.getBellIndex(position);
		TextView time = (TextView)r.findViewById(R.id.bell_time);
		TextView label = (TextView)r.findViewById(R.id.bell_name);
		String fmt = new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2).appendLiteral(' ').appendHalfdayOfDayText().toFormatter().print(bell.getBellTime().toLocalTime());

		time.setText(fmt);
		label.setText(bell.getBellName());
		return r;
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
