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
import android.preference.PreferenceManager;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Timetable;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Today;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.event.BellsEvent;
import com.sbhstimetable.sbhs_timetable_android.event.TimetableEvent;
import com.sbhstimetable.sbhs_timetable_android.event.TodayEvent;

import org.joda.time.DateTime;
import static org.joda.time.DateTimeConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents a full three week cycle (i.e. a Timetable), with information from Today overlayed if it is available
 */
public class FullCycleWrapper {
	private Timetable cycle;
	private Today variationData;
	private Belltimes todayBells;
	private DateTimeHelper dth;
	private int currentDayInCycle = -1;
	private static final List<String> weeks = Arrays.asList("A", "B", "C");

	private EventListener l;

	private List<DataSetObserver> watchers = new ArrayList<DataSetObserver>();

	public FullCycleWrapper(Context c) {
		StorageCache cache = new StorageCache(c);
		cycle = cache.loadTimetable();
		variationData = cache.loadToday();
		todayBells = cache.loadBells();
		this.dth = new DateTimeHelper(c, false);
		if (variationData != null && variationData.isStillCurrent()) {
			currentDayInCycle = variationData.getDayNumber();
		} else if (todayBells != null && todayBells.current()) {
			currentDayInCycle = todayBells.getDayNumber();
		}

		if (currentDayInCycle == -1) {
			String week = cache.loadWeek();
			if (week != null) {
				int wk = weeks.indexOf(week.toUpperCase());
				int day = dth.getNextSchoolDay().toDateTime().getDayOfWeek();
				Log.i("FullCycleWrapper", "Guessing that currentDayInCycle will be week " + week + " (5*"+wk+"+"+day+")");
				currentDayInCycle = 5*wk + day;
				Log.i("FullCycleWrapper", "=> " + currentDayInCycle);
			} else {
				Log.i("FullCycleWrapper", "I have no idea what the week is.");
			}
		}
		if (variationData == null) {
			ApiWrapper.requestToday(c);
		}
		if (cycle == null) {
			ApiWrapper.requestTimetable(c);
		}
		if (todayBells == null) {
			ApiWrapper.requestBells(c);
		}
		this.l = new EventListener();
		ApiWrapper.getEventBus().register(l);
	}

	public DateTime getFetchTime(int index) {
		if (index == currentDayInCycle && variationData != null) {
			return variationData.getFetchTime();
		} else {
			return cycle.getFetchTime();
		}
	}

	public Today getVariationData() {
		return variationData;
	}

	public boolean hasFullTimetable() {
		return cycle != null;
	}

	public boolean hasRealTimeInfo() {
		return variationData != null;
	}

	public boolean ready() {
		return (cycle != null || variationData != null) && currentDayInCycle != -1;
	}

	public Day getDayNumber(int num) {
		if (currentDayInCycle == num && variationData != null) {
			return variationData;
		}
		return cycle.getDayNumber(num);
	}

	public int getCurrentDayInCycle() {
		if (currentDayInCycle != -1) {

			return currentDayInCycle;
		}
		else {
			Log.i("FullCycleWrapper", "No day available", new Exception());
			return -1; // TODO FIXME
		}
	}

	public Day getToday() {
		return getDayNumber(currentDayInCycle);
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

	private void updateToday(Today t) {
		this.variationData = t;
		currentDayInCycle = variationData.getDayNumber();
		this.notifyDSOs();
	}

	private void updateBells(Belltimes b) {
		if (b.getDayNumber() != -1) currentDayInCycle = b.getDayNumber();
		this.todayBells = b;
		this.notifyDSOs();
	}

	private class EventListener {

		public void onEvent(TimetableEvent e) {
			Log.i("FCW$EventListener", "got TimetableEvent");
			if (e.successful()) {
				updateTimetable(e.getResponse());
			} else {
				Log.e("FCW$EventListener", "Timetable failed - " + e.getErrorMessage());
			}
		}

		public void onEvent(TodayEvent e) {
			Log.i("FCW$EventListener", "got TodayEvent");
			if (e.successful()) {
				updateToday(e.getResponse());
			} else {
				Log.e("FCW$EventListener", "Today failed - " + e.getErrorMessage());
			}
		}

		public void onEvent(BellsEvent e) {
			Log.i("FCW$EventListener", "got BellsEvent");
			if (e.successful()) {
				if (e.getResponse().isStatic() && todayBells != null) return;
				updateBells(e.getResponse());
			} else {
				Log.e("FCW$EventListener", "Bells failed - " + e.getErrorMessage());
			}
		}
	}

}
