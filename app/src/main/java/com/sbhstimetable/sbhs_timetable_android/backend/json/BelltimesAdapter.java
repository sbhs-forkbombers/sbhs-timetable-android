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

import android.database.DataSetObserver;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;

import java.util.ArrayList;

public class BelltimesAdapter implements ListAdapter {
	private BelltimesJson b;
	private ArrayList<DataSetObserver> dsos = new ArrayList<DataSetObserver>();
	public BelltimesAdapter(BelltimesJson b) {
		this.b = b;
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
	public void registerDataSetObserver(DataSetObserver dataSetObserver) {
		dsos.add(dataSetObserver);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
		dsos.remove(dataSetObserver);
	}

	private void updateDSOs() {
		for (DataSetObserver i : dsos) {
			i.onChanged();
		}
	}

	public void updateBelltimes(BelltimesJson b) {
		this.b = b;
		updateDSOs();
	}

	@Override
	public int getCount() {
		return b!= null && b.valid() ? b.getMaxIndex() : 1;
	}

	@Override
	public Object getItem(int i) {
		return b.valid() ? b.getIndex(i) : ":(";
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		View r;
		if (b == null || !b.valid()) {
			TextView t = new TextView(viewGroup.getContext());
			t.setText("Couldn't load belltimes!");
			t.setGravity(Gravity.CENTER);
			t.setTextAppearance(viewGroup.getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
			return t;
		}
		if (view instanceof FrameLayout) {
			r = view;
		} else {
			r = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_belltimes_entry, null);
		}
		BelltimesJson.Bell bell = this.b.getIndex(i);
		TextView time = (TextView)r.findViewById(R.id.bell_time);
		TextView label = (TextView)r.findViewById(R.id.bell_name);
		Integer[] bt = bell.getBell();
		String suffix = "am";
		if (bt[0] > 11) {
			bt[0] %= 12;
			if (bt[0] == 0) bt[0] = 12;
			suffix = "pm";
		}
		time.setText(String.format("%02d:%02d %s", new Object[] { bt[0], bt[1], suffix }));
		label.setText(bell.getLabel());
		return r;
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

}
