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
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class Notices {
	private String date;
	private String term;
	private String week;
	private HashMap<String, Notice[]> notices;

	public Notice[] getNoticesForWeight(int weight) {
		return notices.get(((Integer)weight).toString());
	}

	public Set<String> getWeights() {
		return notices.keySet();
	}

	public class Notice implements Comparable<Notice> {
		@SerializedName("dTarget")
		private String displayTarget;
		private String[] years;
		private String title;
		private String text;
		private String author;
		private int weight;

		private transient List<String> yearList;

		private boolean isMeeting;
		private String meetingDate;
		private String meetingTime;
		private String meetingPlace;

		public String getDisplayTarget() {
			return displayTarget;
		}

		private void initYearList() {
			if (yearList == null) {
				yearList = Arrays.asList(years);
			}
		}

		public String getTitle() {
			return title;
		}

		public String getText() {
			return text;
		}

		public String getAuthor() {
			return author;
		}

		public boolean isYearApplicable(int year) {
			return isYearApplicable(((Integer)year).toString());
		}

		public boolean isYearApplicable(String year) {
			initYearList();
			return this.yearList.contains(year);
		}

		@Override
		public int compareTo(Notice another) {
			return this.weight - another.weight;
		}

		public boolean isMeeting() {
			return this.isMeeting;
		}

		public String getMeetingDate() {
			DateTime date = DateTimeHelper.getYYYYMMDDFormatter().parseDateTime(this.meetingDate);
			if (date.plusDays(-1).getDayOfMonth() == DateTime.now().getDayOfMonth()) {
				return "Today";
			} else if (date.getDayOfMonth() == DateTime.now().getDayOfMonth()) {
				return "Tomorrow";
			} else if (DateTime.now().plusDays(7).isAfter(date.toInstant()) && DateTime.now().isBefore(date.toInstant())) {
				return date.dayOfMonth().getAsShortText();
			} else {
				return new DateTimeFormatterBuilder().appendDayOfWeekShortText().appendLiteral(' ').appendDayOfMonth(2).appendMonthOfYearShortText().toFormatter().print(date.toInstant());
			}
		}

		public String getMeetingTime() {
			return meetingTime;
		}

		public String getMeetingPlace() {
			return meetingPlace;
		}
	}
}
