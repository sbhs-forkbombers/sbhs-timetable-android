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

import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.api.Day;
import com.sbhstimetable.sbhs_timetable_android.api.Lesson;

import org.joda.time.DateTime;

import java.util.HashMap;

@SuppressWarnings("unused")
public class Today implements Day {
	private long _fetchTime;
	private String date;
	private boolean variationsFinalised;
	private int dayNumber;

	private String weekType;
	private String today;
	private String status;
	@SuppressWarnings("all")
	private HashMap<String,ClassEntry> timetable;

	@Override
	public int getDayNumber() {
		return dayNumber;
	}

	@Override
	public String getDayName() {
		return today.split(" ")[0];
	}

	@Override
	public String getWeek() {
		return weekType;
	}

	public boolean isStillCurrent() {
		DateTime d = DateTimeHelper.getYYYYMMDDFormatter().parseDateTime(date).withTimeAtStartOfDay().plusHours(15).plusMinutes(15);
		if (d.isBeforeNow()) {
			return false;
		}
		return true;
	}

	@Override
	public Lesson getPeriod(int number) {
		return this.timetable.get(((Integer)number).toString());
	}

	@Override
	public boolean varies() {
		return false;
	}

	public boolean valid() {
		return status == null;
	}

	public String getStringDate() {
		return date;
	}

	public class ClassEntry implements Lesson.WithBelltime {
		private String fullName;
		private String fullTeacher;
		private String teacher;
		private String room;
		private String roomTo;
		private boolean cancelled; // booleans are false by default
		private String casual;
		private String casualDisplay;
		private boolean hasCasual; // this needs to be nullable
		private boolean varies;

		@SuppressWarnings("all")
		private HashMap<String,String> bell;

		@Override
		public String getSubject() {
			return this.fullName;
		}

		@Override
		public String getRoom() {
			return (roomChanged() ? roomTo : room);
		}

		@Override
		public boolean roomChanged() {
			return roomTo != null && variationsFinalised;
		}

		@Override
		public String getTeacher()	 {
			if (teacherChanged()) {
				return this.casualDisplay;
			}
			String tempShort = teacher.toLowerCase();
			String tempLong = this.fullTeacher.toLowerCase();
			int lastMatch = 0;
			for (char i : tempShort.toCharArray()) {
				if (tempLong.indexOf(i, lastMatch) != -1) {
					lastMatch = tempLong.indexOf(i, lastMatch);
				} else {
					return this.teacher; // split class or something.
				}
			}
			return this.fullTeacher;
		}

		@Override
		public boolean teacherChanged() {
			return varies && variationsFinalised;
		}

		@Override
		public boolean cancelled() {
			return cancelled && variationsFinalised;
		}

		@Override
		public String getName() {
			return this.bell.get("title");
		}

		@Override
		public DateTime getStart() {
			return DateTimeHelper.getHHMMFormatter().parseDateTime(this.bell.get("start"));
		}

		@Override
		public DateTime getEnd() {
			return DateTimeHelper.getHHMMFormatter().parseDateTime(this.bell.get("end"));
		}

		@Override
		public String getNext() {
			return this.bell.get("next");
		}
	}
}
