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

import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Today;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

public class DateTimeHelper {
	private Belltimes bells;
	private Today today;

	// useful things
	public static DateTimeFormatter getHHMMFormatter() {
		return new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(':').appendMinuteOfDay(2).toFormatter();
	}

	public static DateTimeFormatter getYYYYMMDDFormatter() {
		return new DateTimeFormatterBuilder().appendYear(4,4).appendLiteral('-').appendMonthOfYear(1).appendLiteral('-').appendDayOfMonth(1).toFormatter();
	}

	public DateTimeHelper(Belltimes b, Today t) {
		this.bells = b;
		this.today = t;
	}

	public DateTimeHelper(Belltimes b) {
		this(b, null);
	}

}
