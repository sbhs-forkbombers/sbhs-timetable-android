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

package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.Context;

import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DateTimeHelper {
	private static final String TAG = "DateTimeHelper";
	private static Calendar cal() {
		return Calendar.getInstance();
	}
	public static BelltimesJson bells;
	public static int getDateOffset() { // TODO holidays - these work when done
		int day = getDay();
		int hour = getHour();
		int minute = getMinute();
		int offset = 0;
		if (day == Calendar.SATURDAY) {
			// push to sunday afternoon.
			offset++;
		} else if (day == Calendar.FRIDAY && (hour > 15 || hour == 15 && minute >= 15)) {
			offset += 2;
		}
		return offset;
	}

	public static String getGuessedDateString() {
		return getYear() + "-" + (getMonth() + 1) + "-" + (getDate() + getDateOffset() + (needsMidnightCountdown() ? 1 : 0));
	}

	/**
	 * get the next school day's date string in YYYY-MM-DD
	 * @return a date in format YYYY-MM-DD
	 */
	public static String getDateString(Context optionalCon) {
		if (!ApiAccessor.todayLoaded || TodayJson.getInstance() == null) {
			if (optionalCon != null) {
				String s = StorageCache.getCachedDate(optionalCon);
				if (!s.equals("1970-01-01"))
					return s;
			}
			String s = getGuessedDateString();
			return s;
		} else {
			String s = TodayJson.getInstance().getDate();
			return s;
		}
	}

	public static boolean needsMidnightCountdown() {
		int offset = getDateOffset();
		return offset > 0 || getDay() == Calendar.SUNDAY || (getHour() > 15 || (getHour() == 15 && getMinute() >= 15));
	}

	public static long milliSecondsUntilNextEvent() {
		long time;
		GregorianCalendar d = new GregorianCalendar(getYear(), getMonth(), getDate() + getDateOffset() + (needsMidnightCountdown() ? 1 : 0), 9, 5);
		if (bells == null || bells.getNextBell() == null || getDateOffset() > 0 || needsMidnightCountdown()) {
			d.set(Calendar.HOUR_OF_DAY, 9);
			if (d.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
				d.set(Calendar.MINUTE, 30);
			} else {
				d.set(Calendar.MINUTE, 5);
			}
		} else {
			BelltimesJson.Bell b = bells.getNextBell();
			Integer[] els = b.getBell();
			d.set(Calendar.HOUR_OF_DAY, els[0]);
			d.set(Calendar.MINUTE, els[1]);
		}
		time = d.getTimeInMillis() - cal().getTimeInMillis();
		return time;
	}

	public static int getDay() {
		return cal().get(Calendar.DAY_OF_WEEK);
	}
	public static int getHour() {
		return cal().get(Calendar.HOUR_OF_DAY);
	}
	public static int getMinute() {
		return cal().get(Calendar.MINUTE);
	}
	public static int getYear() {
		return cal().get(Calendar.YEAR);
	}
	public static int getMonth() {
		return cal().get(Calendar.MONTH);
	}
	public static int getDate() {
		return cal().get(Calendar.DAY_OF_MONTH);
	}
	public static long getTimeMillis() { return System.currentTimeMillis(); }

	public static String formatToCountdown(long millis) {
		millis = (long)Math.floor(millis/1000);
		String sec = "" + (millis % 60);
		millis -= millis % 60;
		millis /= 60;
		String mins = "" + (millis % 60);
		millis -= millis % 60;
		millis /= 60;
		long hrs = millis;
		if (sec.length() == 1) {
			sec = "0" + sec;
		}
		if (mins.length() == 1) {
			mins = "0" + mins;
		}
		if (hrs != 0) {
			return hrs + "h " + mins + "m " + sec + "s";
		} else {
			return mins + "m " + sec + "s";
		}
	}
}
