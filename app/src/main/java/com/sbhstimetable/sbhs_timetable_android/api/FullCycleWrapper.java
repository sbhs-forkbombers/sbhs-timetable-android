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
package com.sbhstimetable.sbhs_timetable_android.api;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.api.gson.Timetable;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Today;
import com.sbhstimetable.sbhs_timetable_android.event.TimetableEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a full three week cycle (i.e. a Timetable), with information from Today overlayed if it is available
 */
public class FullCycleWrapper {
	private Timetable cycle;
	private Today variationData;
	private int currentDayInCycle = -1;

	private EventListener l;

	private List<DataSetObserver> watchers = new ArrayList<DataSetObserver>();

	public FullCycleWrapper(Context c) {
		StorageCache cache = new StorageCache(c);
		cycle = cache.loadTimetable();
		variationData = cache.loadToday();
		if (variationData != null) {
			currentDayInCycle = variationData.getDayNumber();
		}
		this.l = new EventListener();
		ApiWrapper.getEventBus().register(l);
	}

	public boolean hasFullTimetable() {
		return cycle != null;
	}

	public boolean hasRealTimeInfo() {
		return variationData != null;
	}

	public boolean ready() {
		return cycle != null || variationData != null;
	}

	public Day getDayNumber(int num) {
		if (currentDayInCycle == num) {
			return variationData;
		}
		return cycle.getDayNumber(num);
	}

	public int getCurrentDayInCycle() {
		if (currentDayInCycle != -1)
			return currentDayInCycle;
		else
			return 1; // TODO FIXME
	}

	public void addDataSetObserver(DataSetObserver dso) {
		this.watchers.add(dso);
	}

	public void removeDataSetObserver(DataSetObserver dso) {
		this.watchers.remove(dso);
	}

	private void notifyDSOs() {
		for (DataSetObserver i : this.watchers) {
			i.onInvalidated();
		}
	}

	private void updateTimetable(Timetable t) {
		this.cycle = t;
		this.notifyDSOs();
	}

	private class EventListener {

		public void onEvent(TimetableEvent e) {
			Log.i("EventListener", "got TimetableEvent");
			if (e.successful()) {
				updateTimetable(e.getResponse());
			} else {
				Log.e("EventListener", "Timetable failed - " + e.getErrorMessage());
			}
		}
		// TODO
	}

}
