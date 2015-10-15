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
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import static org.joda.time.DateTimeConstants.FRIDAY;
import static org.joda.time.DateTimeConstants.SATURDAY;
import static org.joda.time.DateTimeConstants.SUNDAY;

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

	public static String toCountdown(int seconds) {
		int sec = seconds % 60;
		seconds = (seconds - sec) / 60;
		int min = seconds % 60;
		seconds = (seconds - min) / 60;
		if (seconds > 0) {
			return String.format("%02d:%02d:%02d", seconds, min, sec);
		}
		return String.format("%02d:%02d", min, sec);

	}

	private static boolean after315(DateTime t) {
		return after(t.toLocalDateTime(), 15, 15);
	}

	public static boolean after(LocalDateTime t, int hrs, int minutes) {
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
			if (this.bells == null) {
				this.bells = cache.loadBells();
			}
			if (this.today == null) {
				this.today = cache.loadToday();
			}
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
		if (this.bells != null) {
			return getYYYYMMDDFormatter().parseLocalDateTime(bells.date);
		}
		return getNextSchoolDayStatic();
	}

	public static LocalDateTime getNextSchoolDayStatic() {
		int offset = 0;
		DateTime now = DateTime.now();
		if (now.getDayOfWeek() == SATURDAY) {
			offset = 2;
		} else if (now.getDayOfWeek() == FRIDAY && after315(now)) {
			offset = 3;
		} else if (after315(now) || now.getDayOfWeek() == SUNDAY) {
			offset = 1;
		}
		now = now.plusDays(offset);
		return now.withTimeAtStartOfDay().toLocalDateTime();
	}

	public void setBells(Belltimes b) {
		this.bells = b;
	}

	public void setToday(Today t) {
		this.today = t;
	}

	public Belltimes.Bell getNextBell() {
		if (bells == null || bells.getSchoolDay().withTime(15, 15, 0, 0).isBefore(DateTime.now())) {
			return null;
		}
		int len = bells.getLength();
		for (int i = 0; i < len; i++) {
			DateTime t = bells.getBellIndex(i).getBellTime();
			t = t.withDate(getNextSchoolDay().toLocalDate());
			//Log.i("dth", ""+t);
			if (t.isAfterNow()) {
				return bells.getBellIndex(i);
			}
		}
		return null;
	}

	public Belltimes.Bell getNextPeriod() {
		if (bells == null) {
			return null;
		}
		if (getNextSchoolDay().isAfter(DateTime.now().toLocalDateTime())) {
			return null;
		}
		int len = bells.getLength();
		for (int i = 0; i < len; i++) {
			if (bells.getBellIndex(i).isPeriodStart() && bells.getBellIndex(i).getBellTime().withDate(getNextSchoolDay().toLocalDate()).isAfterNow()) {
				return bells.getBellIndex(i);
			}
		}
		return null;
	}

	public boolean hasBells() {
		if (this.bells == null) {
			this.bells = ApiWrapper.getOfflineBells(this.context);
			Log.i("DateTimeHelper", "Using this.bells: " + this.bells);
			if (this.bells != null) return true; // good enough
		}
		Log.i("DateTimeHelper", "bells: " + this.bells + " " + (this.bells == null ? "" : (this.bells.isStatic() + " " + this.bells.current())));
		return this.bells != null && (this.bells.current() || this.bells.isStatic());
	}

	public boolean hasToday() {
		return this.today != null && this.today.isStillCurrent();
	}

	public Today getToday() {
		return this.today;
	}

	public Belltimes getBells() {
		return this.bells;
	}

	public LocalDateTime getNextEvent() {
		//Log.i("dth", "bells => " + this.bells);
		Belltimes.Bell next = getNextBell();
		if (next == null) { // count to start of next school day. TODO show a notification or something
			DateTime day = getNextSchoolDay().toDateTime();
			if (after(DateTime.now().toLocalDateTime(), 9, 5) && day.equals(DateTime.now().withTimeAtStartOfDay())) {
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
		return next.getBellTime().withDate(getNextSchoolDay().toLocalDate()).toLocalDateTime();
	}

}
