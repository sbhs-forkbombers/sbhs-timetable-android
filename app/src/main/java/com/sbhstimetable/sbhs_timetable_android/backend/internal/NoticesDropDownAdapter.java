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

package com.sbhstimetable.sbhs_timetable_android.backend.internal;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class NoticesDropDownAdapter implements SpinnerAdapter {
	private final String[] elements = new String[]{
		"All notices",
		"Year 12",
		"Year 11",
		"Year 10",
		"Year 9",
		"Year 8",
		"Year 7"
	};

	@Override
	public View getDropDownView(int i, View view, ViewGroup viewGroup) {
		TextView res;
		if (view instanceof TextView) {
			res = (TextView) view;
		}
		else {
			res = new TextView(viewGroup.getContext());
		}
		res.setTextAppearance(viewGroup.getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
		res.setPadding(8, 8, 8, 8);
		res.setText(this.getItem(i));
		return res;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver dataSetObserver) {

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

	}

	@Override
	public int getCount() {
		return this.elements.length;
	}

	@Override
	public String getItem(int i) {
		return this.elements[i];
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		return this.getDropDownView(i, view, viewGroup);
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
