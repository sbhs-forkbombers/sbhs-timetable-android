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

import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;

import com.google.gson.annotations.SerializedName;
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.api.FetchedObject;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class Notices implements FetchedObject {
	private String date;
	private String term;
	private String week;
	private String status;
	private long _fetchTime;
	private HashMap<String, Notice[]> notices;
	private transient HashMap<String, Notice[]> noticeFiltered;

	public Notice[] getNoticesForWeight(int weight) {
		return getNoticesForWeight(((Integer)weight).toString());
	}

	public Notice[] getNoticesForWeight(String weight) {
		if (noticeFiltered == null) {
			return notices.get(weight);
		}
		return noticeFiltered.get(weight);
	}

	public int getNumberOfNotices() {
		int total = 0;
		for (String i : getWeights()) {
			total += getNoticesForWeight(i).length;
		}
		return total;
	}

	public void filterToYear(String year) {
		if (year == null) {
			noticeFiltered = null;
			return;
		}
		HashMap<String,Notice[]> map = new HashMap<>();
		for (Map.Entry<String,Notice[]> i : notices.entrySet()) {
			List<Notice> res = new ArrayList<>();
			for (Notice j : i.getValue()) {
				if (j.isYearApplicable(year)) {
					res.add(j);
				}
			}
			map.put(i.getKey(), res.toArray(new Notice[0]));
		}
		this.noticeFiltered = map;
	}

	@Override
	public DateTime getFetchTime() {
		return new DateTime(this._fetchTime*1000);
	}

	public Notice getNoticeAtIndex(int idx) {
		int total = 0;
		int iters = 0;
		Comparator<String> comp = new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				try {
					int l = Integer.parseInt(lhs);
					int r = Integer.parseInt(rhs);
					if (l < r) {
						return -1;
					} else if (l > r) {
						return 1;
					}
					return 0;
				} catch (NumberFormatException e) {
					throw new ClassCastException("Not an integer");
				}
			}
		};

		List<String> weights = Arrays.asList(getWeights().toArray(new String[0]));
		Collections.sort(weights, comp);
		Collections.reverse(weights);
		String last = null;
		for (String s : weights) {
			int oldTotal = total;
			total += getNoticesForWeight(s).length;
			if (idx < total) {
				last = s;
				idx -= oldTotal; // now it's the index within the list
				if (idx < 0) idx = 0;
				break;
			}
		}
		if (last == null) return null;
		return getNoticesForWeight(last)[idx];
	}

	public boolean valid() {
		return status == null;
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
		private String id;
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

		public long getID() {
			return Long.parseLong(this.id);
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

		public Spanned getTextViewNoticeContents() {
			return Html.fromHtml(this.text.replace("<p>", "").replace("</p>", "<br />"));
		}

		@Override
		public int compareTo(@NonNull Notice another) {
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
