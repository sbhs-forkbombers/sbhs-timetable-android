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

package com.sbhstimetable.sbhs_timetable_android.backend.json;

import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NoticesJson {
	private static NoticesJson INSTANCE;
	public static NoticesJson getInstance() {
		return INSTANCE;
	}

	public static boolean isValid(JsonObject n) {
		return n.has("notices");
	}

	@SuppressWarnings("all")
	private JsonObject notices;
	private ArrayList<Notice> n = new ArrayList<Notice>();
	private HashMap<Year,ArrayList<Notice>> noticesByYear = new HashMap<Year, ArrayList<Notice>>();
	public NoticesJson(JsonObject obj) {
		this.notices = obj;
		ArrayList<NoticeList> why = new ArrayList<NoticeList>();
		Year[] years = Year.values();
		for (int k = 0; k < years.length; k++) {
			this.noticesByYear.put(years[k], new ArrayList<Notice>());
		}
		for (Map.Entry<String, JsonElement> i : this.notices.get("notices").getAsJsonObject().entrySet()) {
			NoticeList l = new NoticeList(i.getValue().getAsJsonArray(), Integer.valueOf(i.getKey()));
			for (int j = 0; j < l.length(); j++) {
				Notice temp = l.get(j);
				this.n.add(temp);
				for (Year y : temp.years) {
					this.noticesByYear.get(y).add(temp);
				}
			}
		}

		INSTANCE = this;
	}

	public ArrayList<Notice> getNotices() {
		return this.n;
	}

	public ArrayList<Notice> getNoticesForYear(Year y) {
		return this.noticesByYear.get(y);
	}

	public static class NoticeList {
		private JsonArray notices;
		private int level;
		private List<Notice> mine = new ArrayList<Notice>();
		public NoticeList(JsonArray ary, int importance) {
			this.notices = ary;
			this.level = importance;
			for (int i = 0; i < length(); i++) {
				mine.add(new Notice(this.notices.get(i).getAsJsonObject(), this.level));
			}
		}

		public int length() {
			return notices.size();
		}
		@SuppressWarnings("unused")
		public Notice get(int i) {
			if (i < length()) {
				return mine.get(i);
			} else {
				throw new ArrayIndexOutOfBoundsException("Nope");
			}
		}

		public List<Notice> getAllNotices() {
			return mine;
		}
	}

	public static class Notice implements Comparable<Notice> {
		private JsonObject notice;
		private List<Year> years;
		private int weight;
		public Notice(JsonObject obj, int weight) {
			this.notice = obj;
			JsonArray a = this.notice.get("years").getAsJsonArray();
			years = new ArrayList<Year>(a.size());
			for (int i = 0; i < a.size(); i++) {
				years.add(i, Year.fromString(a.get(i).getAsString()));
			}
			this.weight = weight;

		}
		@SuppressWarnings("unused")
		public int getWeight() {
			return this.weight;
		}

		public boolean isForYear(Year y) {
			return years.contains(y);
		}

		public Spanned getTextViewNoticeContents() {
			return Html.fromHtml(this.notice.get("text").getAsString().replace("<p>", "").replace("</p>", "<br />"));
		}

		public String getNoticeTitle() {
			return this.notice.get("title").getAsString();
		}

		public String getNoticeAuthor() {
			return this.notice.get("author").getAsString();
		}

		public String getNoticeTarget() {
			return this.notice.get("dTarget").getAsString();
		}

		public boolean isMeeting() {
			return this.notice.has("isMeeting") && this.notice.get("isMeeting").getAsBoolean();
		}

		public String getMeetingDate() {
			SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
			try {
				Date d = fmt.parse(this.notice.get("meetingDate").getAsString());
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(d.getTime());
				int DAY = Calendar.DAY_OF_YEAR;
				Calendar now = Calendar.getInstance();
				if (c.get(DAY) == now.get(DAY)) {
					return "Today";
				} else if (c.get(DAY) - 1 == now.get(DAY)) {
					return "Tomorrow";
				} else if (c.get(DAY) - 7 >= now.get(DAY)) { // within the next week
					return c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
				} else {
					return c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()) + " " + DateFormat.format("dd", d).toString() + " " + c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
				}
			} catch (ParseException e) {
				Log.e("NoticesJson", "Failed to parse meeting date: " + this.notice.get("meetingDate").getAsString());
				return this.notice.get("meetingDate").getAsString();
			}
		}

		public String getMeetingTime() {
			return this.notice.get("meetingTime").getAsString();
		}

		public String getMeetingPlace() {
			return this.notice.get("meetingPlace").getAsString();
		}

		@Override
		public int compareTo(Notice n) {
			return this.getWeight() - n.getWeight();
		}
	}

	public enum Year {
		SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), ELEVEN("11"), TWELVE("12"), STAFF("Staff");
		private String ident;
		private Year(String s) {
			this.ident = s;
		}
		public String toString() {
			return this.ident;
		}

		public static Year fromString(String s) {
			s = s.toLowerCase();
			if (s.startsWith("year ")) s = s.replace("year ", "");
			if (!s.startsWith("staff")) {
				int i = Integer.valueOf(s);
				switch (i) {
					case 7:
						return SEVEN;
					case 8:
						return EIGHT;
					case 9:
						return NINE;
					case 10:
						return TEN;
					case 11:
						return ELEVEN;
					case 12:
						return TWELVE;
					default:
						throw new IllegalArgumentException("Must be a number 7-12 or \"Staff\", not " + i);
				}
			}
			return STAFF;
		}
	}
}
