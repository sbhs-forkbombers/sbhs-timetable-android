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
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StorageCache {
	private static final Gson gson = new Gson();
	private Context c;
	private DateTimeHelper dateTimeHelper;
	public StorageCache(Context c) {
		this.c = c;
		this.dateTimeHelper = new DateTimeHelper(c, false);
	}

	private void cache(String desc, String json) {
		DateTimeFormatter ymd = DateTimeHelper.getYYYYMMDDFormatter();
		cache(desc, json, ymd.print(dateTimeHelper.getNextSchoolDay()));
	}

	private void cache(String desc, String json, String date) {
		String file = date + desc + ".json";
		Log.i("StorageCache", "Cache " + desc + " for " + date + " - " + json);
		File toWrite = new File(c.getCacheDir(), file);
		try {
			FileWriter f = new FileWriter(toWrite);
			f.write(json);
			f.flush();
			f.close();
		} catch (IOException e) {
			Log.e("StorageCache", "Error happened while writing file " + toWrite.getAbsolutePath() + "!", e);
		}
	}

	public void cacheDate(String s) {
		cache("date", s);
	}

	public void cacheToday(Today t) {
		cache("today", gson.toJson(t));
	}

	public void cacheBells(Belltimes b) {
		cache("bells", gson.toJson(b));
	}

	public void cacheNotices(Notices n) {
		cache("notices", gson.toJson(n));
	}

	public void cacheTimetable(Timetable t) {
		cache("timetable", gson.toJson(t), "");
	}

	private boolean exists(String desc) {
		return exists(desc, DateTimeHelper.getYYYYMMDDFormatter().print(dateTimeHelper.getNextSchoolDay()));
	}

	private boolean exists(String desc, String date) {
		String file = date + desc + ".json";
		return new File(c.getCacheDir(), file).exists();
	}

	private DateTime getFetchTime(String desc) {
		return getFetchTime(desc, DateTimeHelper.getYYYYMMDDFormatter().print(dateTimeHelper.getNextSchoolDay()));
	}

	private DateTime getFetchTime(String desc, String date) {
		if (!exists(desc, date)) {
			return null;
		}
		File f = new File(c.getCacheDir(), date + desc + ".json");
		return new DateTime(f.lastModified());
	}

	private boolean shouldReload(String desc) {
		DateTime now = DateTime.now();
		DateTime lastUpdate = getFetchTime(desc);
		if (lastUpdate == null) return true;
		if (now.getYear() != lastUpdate.getYear()) return true; // different year

		if (desc.equals("timetable")) {
			if (now.getDayOfYear() - lastUpdate.getDayOfYear() >= 14) {
				return true;
			} else {
				return false;
			}
		}

		if (now.getDayOfYear() != lastUpdate.getDayOfYear()) return true; // different day in year
		if (desc.equals("bells") || desc.equals("notices")) {
			if ((lastUpdate.getHourOfDay()) > 9 || (lastUpdate.getHourOfDay() == 9 && lastUpdate.getMinuteOfHour() >= 5)) {
				return false;
			}
		}
		if (now.getMinuteOfDay() - lastUpdate.getMinuteOfDay() > 30) { // > 30 minutes since last update
			return true;
		}
		if (desc.equals("today")) {
			if ((now.getHourOfDay() > 8 || (now.getHourOfDay() == 8 && now.getMinuteOfHour() > 30))
					&& (lastUpdate.getHourOfDay() < 8 || (lastUpdate.getHourOfDay() == 8 && now.getMinuteOfHour() < 30))) { // after 8:30, last update was before 8:30
				return true;
			}
		}

		return false;
	}

	public boolean shouldReloadToday() {
		return shouldReload("today");
	}

	public boolean shouldReloadBells() {
		return shouldReload("bells");
	}

	public boolean shouldReloadNotices() {
		return shouldReload("notices");
	}

	public boolean shouldReloadTimetable() {
		return shouldReload("timetable");
	}

	public boolean hasCachedDate() {
		return exists("date");
	}

	private String load(String desc) {
		return load(desc, DateTimeHelper.getYYYYMMDDFormatter().print(dateTimeHelper.getNextSchoolDay()));
	}

	private String load(String desc, String date) {
		File toRead = new File(c.getCacheDir(), date + desc + ".json");
		Log.v("StorageCache", "Grab " + desc + " for " + date);
		if (toRead.exists()) {
			Log.v("StorageCache", "Exists!");
			try {
				BufferedReader b = new BufferedReader(new FileReader(toRead));
				String result = "";
				String last;
				while ((last = b.readLine()) != null) {
					result += "\n" + last;
				}
				Log.v("StorageCache", "Result: " + result);
				return result;
			} catch (IOException e) {
				Log.e("StorageCache", "Error happened while reading file " + toRead.getAbsolutePath() + "!", e);
			}
		}
		Log.v("StorageCache", "doesn't exist.");
		return null;
	}

	public String loadDate() {
		return load("date");
	}

	public Today loadToday() {
		String res = load("today");
		if (res == null) return null;
		Today t = gson.fromJson(res, Today.class);
		return t;
	}

	public Belltimes loadBells() {
		String res = load("bells");
		if (res == null) return null;
		Belltimes t = gson.fromJson(res, Belltimes.class);
		return t;
	}

	public Notices loadNotices() {
		String res = load("notices");
		if (res == null) return null;
		Notices t = gson.fromJson(res, Notices.class);
		return t;
	}

	public Timetable loadTimetable() {
		String res = load("timetable", "");
		if (res == null) return null;
		Timetable t = gson.fromJson(res, Timetable.class);
		return t;
	}
}
