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
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class StorageCache {
	private static boolean isOld(File f) {
		// don't touch non-JSONs
		return f.getName().endsWith("json") && (DateTimeHelper.getTimeMillis() - f.lastModified()) > (1000 * 60 * 60 * 24 * 7); // older than a week
	}

	private static boolean isMine(File f) {
		return f.getName().endsWith("json");
	}

	private static boolean writeCacheFile(Context c, String date, String type, String json) {
		File cacheDir = c.getCacheDir();
		File data = new File(cacheDir, date+"_"+type+".json");
		if (json == null) {
			Log.e("storageCache", "not given any data to write!");
			return false;
		}
		try {
			FileWriter w = new FileWriter(data);
			w.write(json);
			w.close();
			return true;
		} catch (IOException e) {
			Log.e("storageCache", "unable to cache json for " + date + "!", e);
		}
		return false;
	}

	private static JsonObject readCacheFile(Context c, String date, String type) {
		File cacheDir = c.getCacheDir();
		File data = new File(cacheDir, date+"_"+type+".json");
		if (data.exists() && data.canRead()) {
			try {
				return new JsonParser().parse(new FileReader(data)).getAsJsonObject();
			} catch (IOException e) {
				Log.v("storageCache","couldn't read cache (which supposedly exists and is cached!)",e);
			} catch (IllegalStateException e) {
				Log.v("storageCache", "wow wek json", e);
			} catch (JsonSyntaxException e) {
				Log.v("storageCache", "wow wek json", e);
			}
		}
		return null;
	}
	@SuppressWarnings("all")
	public static void cleanCache(Context context) {
		File cacheDir = context.getCacheDir();
		for (File f : cacheDir.listFiles()) {
			if (isOld(f)) {
				f.delete();
			}
		}
	}

	@SuppressWarnings("all")
	public static void deleteAllCacheFiles(Context c) {
		File cacheDir = c.getCacheDir();
		for (File f : cacheDir.listFiles()) {
			if (isMine(f)) {
				f.delete();
			}
		}
	}

	public static JsonObject getTodayJson(Context context, String date) {
		JsonObject res = readCacheFile(context, date, "today");
		return res != null && TodayJson.isValid(res) ? res : null;
	}

	public static void cacheTodayJson(Context context, String date, String json) {
		writeCacheFile(context, date, "today", json);
	}

	public static void cacheBelltimes(Context context, String date, String json) {
		writeCacheFile(context, date, "belltimes", json);
	}

	public static JsonObject getBelltimes(Context c, String date) {
		JsonObject res = readCacheFile(c, date, "belltimes");
		return res != null && BelltimesJson.isValid(res) ? res : null;
	}

	public static void cacheNotices(Context c, String date, String json) {
		writeCacheFile(c, date, "notices", json);
	}

	public static JsonObject getNotices(Context c, String date) {
		JsonObject res = readCacheFile(c, date, "notices");
		return res != null && NoticesJson.isValid(res) ? res : null;
	}

	@SuppressWarnings("unused")
	public static boolean hasCachedDate(Context c) {
		return new File(c.getCacheDir(), "date-"+DateTimeHelper.getGuessedDateString()+".json").exists();
	}

	public static String getCachedDate(Context c) {
		File f = new File(c.getCacheDir(), "date-"+DateTimeHelper.getGuessedDateString()+".json");
		if (!f.exists()) {
			return DateTimeHelper.getGuessedDateString();
		}
		try {
			FileReader r = new FileReader(f);
			char[] s = new char[10];
			int max = r.read(s);
			String date = String.valueOf(Arrays.copyOfRange(s, 0, max));
			r.close();
			return date;
		} catch (IOException e) {
			//meh
		}
		return DateTimeHelper.getGuessedDateString();
	}

	public static void writeCachedDate(Context c, String s) {
		File f = new File(c.getCacheDir(), "date-"+DateTimeHelper.getGuessedDateString()+".json");
		try {
			FileWriter w = new FileWriter(f);
			w.write(s);
			w.close();
		} catch (IOException e) {
			Log.e("StorageCache", "failed to write cached date", e);
		}
	}
}
