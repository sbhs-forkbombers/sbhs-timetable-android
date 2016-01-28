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

import com.google.gson.annotations.SerializedName;
import com.sbhstimetable.sbhs_timetable_android.api.Day;
import com.sbhstimetable.sbhs_timetable_android.api.FetchedObject;
import com.sbhstimetable.sbhs_timetable_android.api.FreePeriod;
import com.sbhstimetable.sbhs_timetable_android.api.Lesson;

import org.joda.time.DateTime;

import java.util.HashMap;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection", "ConstantConditions"})
public class Timetable implements FetchedObject {
	private HashMap<String, TimetableDay> days;
	private HashMap<String, SubjectInfo> subjInfo;
	private String status;
	private long _fetchTime;

	public boolean valid() {
		return status == null;
	}

	public TimetableDay getDayNumber(String num) {
		TimetableDay res = days.get(num);
		res.setDayNumber(Integer.valueOf(num));
		return res;
	}

	public TimetableDay getDayNumber(int num) {
		num++;
		TimetableDay res = days.get(((Integer)num).toString());
		if (res == null) return res;
		res.setDayNumber(num-1);
		res._subjInfo = subjInfo;
		return res;
	}

	@Override
	public DateTime getFetchTime() {
		return new DateTime(_fetchTime*1000);
	}

	public class TimetableDay implements Day {
		@SerializedName("dayname")
		private String dayName;
		private HashMap<String, FixedLesson> periods;
		private int dayNumber;
		private HashMap<String, SubjectInfo> _subjInfo;

		protected void setDayNumber(int num) {
			dayNumber = num;
		}

		@Override
		public int getDayNumber() {
			return dayNumber;
		}

		@Override
		public String getDayName() {
			return dayName.split(" ")[0];
		}

		@Override
		public String getWeek() {
			return dayName.split(" ")[1];
		}

		@Override
		public Lesson getPeriod(int number) {
			String key = ((Integer)number).toString();
			if (!this.periods.containsKey(key)) {
				return new FreePeriod();
			}
			FixedLesson l = this.periods.get(key);
			l.subjInfo = this._subjInfo.get(l.year + l.title);
			return l;
		}

		@Override
		public boolean varies() {
			return false;
		}

		public class FixedLesson implements Lesson {
			private String title;
			private String teacher;
			private String room;
			private String year;
			public SubjectInfo subjInfo;
			@Override
			public String getSubject() {
				if (subjInfo == null) {
					return "Unknown";
				}
				return subjInfo.getSubjectName();
				//return subjInfo.get(year + title).getSubjectName();
			}

			@Override
			public String getShortName() {
				return title;
			}

			@Override
			public String getRoom() {
				return room;
			}

			@Override
			public boolean roomChanged() {
				return false;
			}

			@Override
			public String getTeacher() {
				if (subjInfo == null) {
					return "someone";
				}
				String[] parts = subjInfo.getNormalTeacher().split(" ");
				String teacherLastName = "";
				for (int i = 2; i < parts.length; i++) {
					teacherLastName += parts[i];
				}
				String ltemp = teacherLastName.toLowerCase();
				String stemp = teacher.toLowerCase();
				int prevIndex = 0;
				for (char i : stemp.toCharArray()) {
					if (ltemp.indexOf(i, prevIndex) != -1) {
						prevIndex = ltemp.indexOf(i, prevIndex);
					} else {
						return teacher;
					}
				}
				return parts[0] + " " + teacherLastName;
			}

			@Override
			public boolean teacherChanged() {
				return false;
			}

			@Override
			public boolean cancelled() {
				return false;
			}

			@Override
			public boolean isTimetabledFree() {
				return false;
			}

		}
	}

	public class SubjectInfo {
		private String title;
		private String shortTitle;
		private String teacher;
		private String subject;
		private String fullTeacher;
		private String year;

		public String getFullSubjectName() {
			return subject;
		}

		public String getSubjectName() {
			return title;
		}

		public String getShortSubjectName() {
			return shortTitle;
		}

		public String getNormalTeacher() {
			return fullTeacher;
		}

		public String getShortTeacher() {
			return teacher;
		}

		public String getYear() {
			return year;
		}

	}
}
