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
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Today;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import static org.joda.time.DateTimeConstants.*;

public class DateTimeHelper {
	private Belltimes bells;
	private Today today;
	private Context context;
	private StorageCache cache;
	// useful things
	public static DateTimeFormatter getHHMMFormatter() {
		return new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2).toFormatter();
	}

	public static DateTimeFormatter getYYYYMMDDFormatter() {
		return new DateTimeFormatterBuilder().appendYear(4,4).appendLiteral('-').appendMonthOfYear(2).appendLiteral('-').appendDayOfMonth(2).toFormatter();
	}

	private static boolean after315(DateTime t) {
		return after(t, 15, 15);
	}

	private static boolean after(DateTime t, int hrs, int minutes) {
		return t.getHourOfDay() > hrs || (t.getHourOfDay() == hrs && t.getMinuteOfHour() >= minutes);
	}

	public DateTimeHelper(Belltimes b, Today t, Context c, boolean createCache) {
		this.bells = b;
		this.today = t;
		if (c == null) {
			throw new IllegalArgumentException("Need a context!");
		}
		this.context = c;
		if (createCache) {
			this.cache = new StorageCache(c);
		}
	}

	public DateTimeHelper(Belltimes b, Context c) {
		this(b, null, c, true);
	}

	public DateTimeHelper(Context c) {
		this(null, null, c, true);
	}

	public DateTimeHelper(Context c, boolean createCache) {
		this(null, null, c, createCache);
	}

	public LocalDateTime getNextSchoolDay() {
		/*if (this.cache != null && this.cache.hasCachedDate()) {
			return getYYYYMMDDFormatter().parseDateTime(this.cache.loadDate());
		}*/
		int offset = 0;
		boolean goToMidnight = false;
		DateTime now = DateTime.now();
		if (now.getDayOfWeek() == SATURDAY) {
			offset = 1;
			goToMidnight = true;
		} else if (now.getDayOfWeek() == FRIDAY && after315(now)) {
			goToMidnight = true;
			offset = 2;
		} else if (after315(now)) {
			goToMidnight = true;
		}
		now = now.plusDays(offset + (goToMidnight ? 1 : 0));
		return now.withTimeAtStartOfDay().plusMinutes(1).toLocalDateTime();
	}

	public void setBells(Belltimes b) {
		Log.i("dth", "set bells to " + b);
		this.bells = b;
	}

	public void setToday(Today t) {
		this.today = t;
	}

	public Belltimes.Bell getNextLesson() {
		if (bells == null) {
			return null;
		}
		int len = bells.getLength();
		for (int i = 0; i < len; i++) {
			DateTime t = bells.getBellIndex(i).getBellTime();
			t = t.withDate(DateTime.now().toLocalDate());
			//Log.i("dth", ""+t);
			if (t.isAfterNow()) {
				return bells.getBellIndex(i);
			}
		}
		return null;
	}

	public boolean hasBells() {
		return this.bells != null;// && this.bells.current();
	}

	public boolean hasToday() {
		return this.today != null && this.today.isStillCurrent();
	}

	public Today getToday() {
		return this.today;
	}

	public LocalDateTime getNextEvent() {
		//Log.i("dth", "bells => " + this.bells);
		Belltimes.Bell next = getNextLesson();
		if (next == null) { // count to start of next school day. TODO show a notification or something
			DateTime day = getNextSchoolDay().toDateTime();
			if (after(DateTime.now(), 9, 5) && day.equals(DateTime.now().withTimeAtStartOfDay())) {
				day = day.plusDays(1);
				if (day.getDayOfWeek() == SATURDAY) day = day.plusDays(2);
				if (day.getDayOfWeek() == SUNDAY) day = day.plusDays(1);
				if (day.getDayOfWeek() != FRIDAY) {
					return day.plusHours(9).toLocalDateTime();
				} else {
					return day.plusHours(9).plusMinutes(25).toLocalDateTime();
				}
			} else {
				if (day.getDayOfWeek() != FRIDAY) {
					return day.plusHours(9).toLocalDateTime();
				} else {
					return day.plusHours(9).plusMinutes(25).toLocalDateTime();
				}
			}
		}
		return next.getBellTime().withDate(DateTime.now().toLocalDate()).toLocalDateTime();
	}

}
