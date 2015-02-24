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

import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Today;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
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
		return new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(':').appendMinuteOfDay(2).toFormatter();
	}

	public static DateTimeFormatter getYYYYMMDDFormatter() {
		return new DateTimeFormatterBuilder().appendYear(4,4).appendLiteral('-').appendMonthOfYear(2).appendLiteral('-').appendDayOfMonth(2).toFormatter();
	}

	private static boolean after315(DateTime t) {
		return t.getHourOfDay() > 15 || (t.getHourOfDay() == 15 && t.getMinuteOfHour() >= 15);
	}

	public DateTimeHelper(Belltimes b, Today t, Context c) {
		this.bells = b;
		this.today = t;
		if (c == null) {
			throw new IllegalArgumentException("Need a context!");
		}
		this.context = c;
		this.cache = new StorageCache(c);
	}

	public DateTimeHelper(Belltimes b, Context c) {
		this(b, null, c);
	}

	public DateTimeHelper(Context c) {
		this(null, null, c);
	}

	public DateTime getNextSchoolDay() {
		if (this.cache.hasCachedDate()) {
			return getYYYYMMDDFormatter().parseDateTime(this.cache.loadDate());
		}
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
		now.plusDays(offset + (goToMidnight ? 1 : 0));
		return now.withTimeAtStartOfDay();
	}

}
