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
package com.sbhstimetable.sbhs_timetable_android.api.gson;

import com.sbhstimetable.sbhs_timetable_android.api.Belltime;
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

@SuppressWarnings("unused")
public class Belltimes {
	private String status;
	private boolean bellsAltered;
	private String bellsAlteredReason;

	/**
	 * The date, YYYY-MM-DD
	 */
	private String date;
	/**
	 * The day of the week (Monday, Tuesday, etc)
	 */
	private String day;
	private String term;
	private String week;
	private String weekType;
	private long _fetchTime;

	private Bell[] bells;

	private transient DateTimeFormatter dayParser = new DateTimeFormatterBuilder()
			.appendYear(4, 4).appendLiteral('-').appendMonthOfYear(2).appendLiteral('-').appendDayOfMonth(2).toFormatter();

	public DateTime getSchoolDay() {
		return dayParser.parseDateTime(date);
	}

	public String getWeek() {
		return week + weekType;
	}

	public DateTime getFetchTime() {
		return new DateTime(_fetchTime*1000L);
	}

	public Bell getBellIndex(int i) {
		if (i >= bells.length) {
			return null;
		}
		return bells[i];
	}

	public boolean valid() {
		return !status.equalsIgnoreCase("error");
	}

	// TODO getNextBell(), IPeriod should implement getPeriodNumber() and have better getName(). hasPeriodNext() maybe?

	public class Bell implements Belltime {
		private String name;
		private String time;
		private int index;
		public Bell() {}
		@Override
		public String getBellName() {
			if (this.isPeriodStart()) {
				return "Period " + name;
			}
			return this.name;
		}

		@Override
		public DateTime getBellTime() {
			return DateTimeHelper.getHHMMFormatter().parseDateTime(time);
		}

		@Override
		public boolean isPeriodStart() {
			return Integer.getInteger(this.name, -1) != -1;
		}

		@Override
		public int getPeriodNumber() {
			return Integer.getInteger(this.name, -1);
		}

		public Belltime getNextBellTime() {
			if (index < bells.length-1) {
				return bells[index+1];
			}
			return null;
		}

		@Override
		public boolean isNextBellPeriod() {
			if (index >= bells.length-2) {
				return false;
			} else {
				if (bells[index + 1].isPeriodStart()) {
					return true;
				}
			}
			return false;
		}
	}

}
