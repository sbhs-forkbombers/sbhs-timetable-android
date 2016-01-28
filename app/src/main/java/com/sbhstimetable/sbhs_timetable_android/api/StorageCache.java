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

import com.google.gson.Gson;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Notices;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Timetable;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Today;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class StorageCache {
	private static final Gson gson = new Gson();
	private Context c;
	private DateTimeHelper dateTimeHelper;
	public StorageCache(Context c) {
		this.c = c;
		this.dateTimeHelper = new DateTimeHelper(c, false);
		this.cleanCache();
	}

	private void cache(String desc, String json) {
		DateTimeFormatter ymd = DateTimeHelper.getYYYYMMDDFormatter();
		cache(desc, json, ymd.print(dateTimeHelper.getNextSchoolDay()));
	}

	private void cache(String desc, String json, String date) {
		String file = date + desc + ".json";
		if (ApiWrapper.httpDebugging) Log.v("StorageCache", "Cache " + desc + " for " + date + " - " + json);
		File toWrite = new File(c.getCacheDir(), file);
		try {
			FileWriter f = new FileWriter(toWrite);
			f.write(json);
			f.flush();
			f.close();
		} catch (IOException e) {
			Log.w("StorageCache", "Error happened while writing file " + toWrite.getAbsolutePath() + "!", e);
		}
	}

	private boolean shouldReload(String desc) {
		switch (desc) {
			case "timetable":
				Timetable t = loadTimetable();
				return t == null || t.getFetchTime().getMillis() - DateTime.now().getMillis() > 14 * 24 * 60 * 60 * 1000;
			case "today":
				Today t2 = loadToday();
				return t2 == null || !t2.isStillCurrent();
			case "bells":
				Belltimes b = loadBells();
				return b == null || !b.current();
			case "notices":
				Notices n = loadNotices();
				return n == null || n.getFetchTime().getMillis() - DateTime.now().getMillis() >= 60 * 60 * 1000; // one hour
		}
		return false;
	}

	private boolean exists(String desc) {
		return exists(desc, DateTimeHelper.getYYYYMMDDFormatter().print(dateTimeHelper.getNextSchoolDay()));
	}

	private boolean exists(String desc, String date) {
		String file = date + desc + ".json";
		return new File(c.getCacheDir(), file).exists();
	}

	private String load(String desc) {
		return load(desc, DateTimeHelper.getYYYYMMDDFormatter().print(dateTimeHelper.getNextSchoolDay()));
	}

	private String load(String desc, String date) {
		File toRead = new File(c.getCacheDir(), date + desc + ".json");
		//Log.v("StorageCache", "Grab " + desc + " for " + date);
		if (toRead.exists()) {
			//Log.v("StorageCache", "Exists!");
			try {
				BufferedReader b = new BufferedReader(new FileReader(toRead));
				String result = "";
				String last;
				while ((last = b.readLine()) != null) {
					result += "\n" + last;
				}
				//Log.v("StorageCache", "Result: " + result);
				return result;
			} catch (IOException e) {
				Log.w("StorageCache", "Error happened while reading file " + toRead.getAbsolutePath() + "!", e);
			}
		}
		//Log.v("StorageCache", "doesn't exist.");
		return null;
	}

	/* TODAY */
	public void cacheToday(Today t) {
		cache("today", gson.toJson(t));
		DateTime d = DateTime.now();
		d = d.withTimeAtStartOfDay().withDayOfWeek(DateTimeConstants.MONDAY);
		DateTime now = DateTime.now();
		if (now.getDayOfWeek() >= DateTimeConstants.SATURDAY || (now.getDayOfWeek() == DateTimeConstants.FRIDAY && (now.getHourOfDay() == 15 && now.getMinuteOfHour() >= 15) || now.getHourOfDay() > 15)) {
			d = d.plusWeeks(1);
		}
		cache("week", d.getMillis() + " " + t.getWeek(), "");
	}

	public boolean shouldReloadToday() {
		return shouldReload("today");
	}

	public Today loadToday() {
		String res = load("today");
		if (res == null) return null;
		return gson.fromJson(res, Today.class);
	}

	/* BELLS */
	public void cacheBells(Belltimes b) {
		cache("bells", gson.toJson(b));
	}

	public boolean shouldReloadBells() {
		return shouldReload("bells");
	}

	public Belltimes loadBells() {
		String res = load("bells");
		if (res == null) {
			return ApiWrapper.getOfflineBells(c);
		}
		return gson.fromJson(res, Belltimes.class);
	}

	/* NOTICES */
	public void cacheNotices(Notices n) {
		cache("notices", gson.toJson(n));
	}

	public boolean shouldReloadNotices() {
		return shouldReload("notices");
	}

	public Notices loadNotices() {
		String res = load("notices");
		if (res == null) return null;
		return gson.fromJson(res, Notices.class);
	}

	/* TIMETABLE */
	public void cacheTimetable(Timetable t) {
		cache("timetable", gson.toJson(t), "");
	}

	public boolean shouldReloadTimetable() {
		return shouldReload("timetable");
	}

	public Timetable loadTimetable() {
		String res = load("timetable", "");
		if (res == null) return null;
		return gson.fromJson(res, Timetable.class);
	}

	public String loadWeek() {
		String s = load("week", "");
		if (s == null) {
			return null;
		}
		String[] ary = s.replace("\n", "").split(" ");
		if (ary.length < 2) {
			return null;
		}
		long v;
		try {
			v = Long.valueOf(ary[0]);
		} catch (Exception e) {
			Log.v("StorageCache", "Failed to parse long - " + ary[0], e);
			return null;
		}
		DateTime d = new DateTime(v);
		int idx = Arrays.asList("A", "B", "C").indexOf(ary[1].toUpperCase());
		DateTime thisWeek = DateTime.now().withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay();
		DateTime now = DateTime.now();
		if (now.getDayOfWeek() >= DateTimeConstants.SATURDAY || (now.getDayOfWeek() == DateTimeConstants.FRIDAY && (now.getHourOfDay() == 15 && now.getMinuteOfHour() >= 15) || now.getHourOfDay() > 15)) {
			thisWeek = thisWeek.plusWeeks(1);
		}
		Period gap = new Period(d, thisWeek);
		idx += gap.getWeeks();
		idx %= 3;
		return new String[] {"A","B","C"}[idx];
	}

	public void cleanCache() {
		File cacheDir = c.getCacheDir();
		for (File f : cacheDir.listFiles()) {
			if (!f.isFile()) continue;
			if (f.getName().contains("-") && !f.getName().substring(0, 10).equals(DateTimeHelper.getYYYYMMDDFormatter().print(dateTimeHelper.getNextSchoolDay()))) {
				Log.d("StorageCache$Clean", "clean file: " + f.getName() + " for day: " + f.getName().substring(0, 10) + " (next day: " + DateTimeHelper.getYYYYMMDDFormatter().print(dateTimeHelper.getNextSchoolDay()) + ")");
				if (f.delete()) {
					Log.d("StorageCache$Clean", "done");
				} else {
					Log.d("StorageCache$Clean", "failed");
				}
			}
		}
	}

}
