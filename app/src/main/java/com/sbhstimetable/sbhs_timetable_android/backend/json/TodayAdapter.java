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

package com.sbhstimetable.sbhs_timetable_android.backend.json;

import android.content.Context;
import android.database.DataSetObserver;
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

import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;

import java.util.ArrayList;
import java.util.List;

public class TodayAdapter implements ListAdapter,AdapterView.OnItemSelectedListener{
	private TodayJson todayJson;
	int todayJsonIndex = 0;
	private IDayType today;
	private TimetableJson timetable;
	private List<DataSetObserver> dsos = new ArrayList<DataSetObserver>();
	private FrameLayout theFilterSelector;
	private int curIndex = 0;
	public TodayAdapter(TodayJson tt, Context c) {
		this.today = tt;
		this.todayJson = tt;

		JsonObject timt = StorageCache.getTimetable(c);
		if (timt != null) {
			Log.i("todayAdapter", "loading timetable.json!");
			this.timetable = new TimetableJson(timt);
			this.todayJsonIndex = this.timetable.getNumForDay(this.todayJson.getDayName());
		} else {
			ApiAccessor.getTimetable(c, true);
		}
	}

	private IDayType.IPeriod getEntry(int i) {
	   return today.getPeriod(i);
	}

	private void notifyDSOs() {
		for (DataSetObserver i : dsos) {
			i.onChanged();
		}
	}

	public void setDay(int dayIndex) {
		if (dayIndex == todayJsonIndex || this.timetable == null) {
			this.today = this.todayJson;
		}
		else {
			this.today = this.timetable.getDayFromNumber(dayIndex);
		}
		this.notifyDSOs();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver dataSetObserver) {
		this.dsos.add(dataSetObserver);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
		this.dsos.remove(dataSetObserver);
	}

	public void updateDataSet(TodayJson newValue) {
		this.today = newValue;
		for (DataSetObserver i : dsos) {
			i.onChanged();
		}
	}

	@Override
	public int getCount() {
		return 6;
	}

	@Override
	public Object getItem(int i) {
		return (i == 0 ? "combo box " : this.getEntry(i-1));
	}

	@Override
	public long getItemId(int i) {
		return (long)i;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(int i, View oldView, ViewGroup viewGroup) {
		if (i == 0) {
			if (this.theFilterSelector != null) {
				Spinner s = (Spinner)theFilterSelector.findViewById(R.id.spinner);
				this.onItemSelected(null, null, s.getSelectedItemPosition(), 0);
				s.setOnItemSelectedListener(this);
				return theFilterSelector;
			}
			FrameLayout f = (FrameLayout)((LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_listview_spinner, null);
			Spinner s = (Spinner)f.findViewById(R.id.spinner);
			String[] entries = new String[15];
			int c = 0;
			for (String j : new String[] { "A", "B", "C"}) {
				for (String k : new String[] {
						"Monday",
						"Tuesday",
						"Wednesday",
						"Thursday",
						"Friday" // yay hardcoded lists!
				}) {
					entries[c++] = k + " " + j;
				}
			}
			ArrayAdapter<String> a = new ArrayAdapter<String>(viewGroup.getContext(), R.layout.textview, entries);
			s.setAdapter(a);
			s.setSelection(this.curIndex);
			s.setOnItemSelectedListener(this);
			this.theFilterSelector = f;
			return f;
		}
		final FrameLayout view;
		final TextView header;
		final TextView roomText;
		final TextView teacherText;
		final ImageView changed;
		if (oldView instanceof FrameLayout) {
			view = (FrameLayout)oldView;
		} else {
			view = (FrameLayout)LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_timetable_classinfo, null);
		}
		header = (TextView)view.findViewById(R.id.timetable_class_header);
		roomText = (TextView)view.findViewById(R.id.timetable_class_room);
		teacherText = (TextView)view.findViewById(R.id.timetable_class_teacher);
		changed = (ImageView)view.findViewById(R.id.timetable_class_changed);

		final IDayType.IPeriod b = this.getEntry(i);
		/*view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i = new Intent(view.getContext(), ClassInfoActivity.class);
				i.putExtra("json", ""+b);
				view.getContext().startActivity(i);
			}
		});*/
		String room = b.room();
		String teacher = b.teacher();
		roomText.setTextColor(viewGroup.getResources().getColor(R.color.primary_text_default_material_dark));
		teacherText.setTextColor(viewGroup.getResources().getColor(R.color.primary_text_default_material_dark));
		if (b.changed()) {
			// variations!
			changed.setVisibility(View.VISIBLE);
			if (b.roomChanged()) {
				roomText.setTextColor(viewGroup.getResources().getColor(R.color.standout));
			}
			if (b.teacherChanged()) {
				teacherText.setTextColor(viewGroup.getResources().getColor(R.color.standout));
			}
		} else {
			changed.setVisibility(View.INVISIBLE);
		}
		header.setText(b.name());
		roomText.setText(room);
		teacherText.setText(teacher);
		//view.setText((String)b.get("fullName"));

		return view;
	}

	@Override
	public int getItemViewType(int i) {
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
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int i) {
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
		this.setDay(i + 1);
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
	}
}
