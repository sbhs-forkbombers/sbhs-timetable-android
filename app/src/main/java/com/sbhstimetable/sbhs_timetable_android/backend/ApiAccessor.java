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
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.LoginActivity;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.TimetableActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Access remote API calls and stuff
 */
public class ApiAccessor {
	public static final String baseURL = "http://sbhstimetable.tk".toLowerCase(); // ALWAYS LOWER CASE!
	public static final String PREFS_NAME = "timetablePrefs";
	public static final String ACTION_TODAY_JSON = "todayData";
	public static final String ACTION_BELLTIMES_JSON = "belltimesData";
	public static final String ACTION_NOTICES_JSON = "noticesData";
	public static final String ACTION_TIMETABLE_JSON = "timetableData";
	public static final String EXTRA_JSON_DATA = "jsonString";
	public static final String EXTRA_CACHED = "isCached";
	public static final String PREF_TODAY_LAST_UPDATE = "todayUpdate";
	public static final String PREF_NOTICES_LAST_UPDATE = "noticesUpdate";
	public static final String PREF_BELLTIMES_LAST_UPDATE = "bellsUpdate";
	public static final String GLOBAL_ACTION_TODAY_JSON = "com.sbhstimetable.sbhs_timetable_android."+ACTION_TODAY_JSON;

	private static String sessionID = null;


	public static int noticesStatus = R.string.desc_failed;
	public static int bellsStatus = R.string.desc_failed;
	public static int todayStatus = R.string.desc_failed;

	public static boolean todayCached = true;
	public static boolean bellsCached = true;
	public static boolean noticesCached = true;
	public static boolean timetableCached = true;

	public static boolean todayLoaded = false;
	public static boolean bellsLoaded = false;
	public static boolean noticesLoaded = false;
	public static boolean timetableLoaded = false;


	public static void load(Context c) {
		// load stored sessionID and whatnot here
		Log.i("ApiAccessor", "loading...");
		SharedPreferences s = c.getSharedPreferences(PREFS_NAME, 0);
		sessionID = s.getString("sessionID", null);
	}

	public static String getSessionID() {
		return sessionID;
	}

	public static boolean isLoggedIn() {
		return sessionID != null;
	}

	public static void login(Context c) {
		c.startActivity(new Intent(c, LoginActivity.class));
	}

	public static void logout(Context c) {
		sessionID = null;
		SharedPreferences s = c.getSharedPreferences(PREFS_NAME, 0);
		s.edit().remove("sessionID").apply();
	}

	public static void finishedLogin(Context c, String id) {
		sessionID = id;
		SharedPreferences.Editor e = c.getSharedPreferences(PREFS_NAME, 0).edit();
		e.putString("sessionID", sessionID);
		e.apply();
	}

	public static boolean hasInternetConnection(Context c) {
		ConnectivityManager conn = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
		return conn.getActiveNetworkInfo() != null && conn.getActiveNetworkInfo().isConnected();
	}

	public static void getToday(Context c) {
		getToday(c, true);
	}

	public static void getToday(Context c, boolean tryCache) {
		String ds = DateTimeHelper.getDateString(c);
		JsonObject obj = StorageCache.getTodayJson(c, ds);
		if (obj != null && tryCache) {
			todayLoaded = true;
			todayCached = StorageCache.isStale(StorageCache.getFile(ds, StorageCache.TYPE_TODAY, c));
			todayStatus = todayCached ? R.string.desc_cached : R.string.desc_current;
			Intent i = new Intent(ACTION_TODAY_JSON);
			i.putExtra(EXTRA_JSON_DATA, obj.toString());
			i.putExtra(EXTRA_CACHED, todayCached);
			LocalBroadcastManager.getInstance(c).sendBroadcast(i); // to tide us over - or if there's no internet.
			if (!todayCached) return; // no point wasting data!
		}
		if (!isLoggedIn() || !hasInternetConnection(c)) {
			todayLoaded  = true;
		}
		try {
			Log.i("ApiAccessor", "Going to get today.json…");
			new DownloadFileTask(c, ACTION_TODAY_JSON).execute(new URL(baseURL + "/api/today.json"));
		} catch (Exception e) {
			Log.e("apiaccessor", "today.json dl failed", e);
		}

	}

	public static void getTimetable(Context c, boolean tryCache) {
		JsonObject obj = StorageCache.getTimetable(c);
		if (obj != null && tryCache) {
			timetableCached = StorageCache.isStale(StorageCache.getFile("", StorageCache.TYPE_TIMETABLE, c));
			timetableLoaded = true;
			Intent i = new Intent(ACTION_TIMETABLE_JSON);
			i.putExtra(EXTRA_JSON_DATA, obj.toString());
			i.putExtra(EXTRA_CACHED, timetableCached);
			LocalBroadcastManager.getInstance(c).sendBroadcast(i);
			if (!timetableCached) return;
		}
		if (!hasInternetConnection(c) || !ApiAccessor.isLoggedIn()) {
			timetableLoaded = true;
			return;
		}
		try {
			new DownloadFileTask(c, ACTION_TIMETABLE_JSON).execute(new URL(baseURL + "/api/bettertimetable.json"));
		} catch (Exception e) {
			Log.e("apiaccessor", "timetable failed", e);
		}
	}

	public static void getBelltimes(Context c) {
		getBelltimes(c, true);
	}

	public static void getBelltimes(Context c, boolean tryCache) {
		String ds = DateTimeHelper.getDateString(c);
		JsonObject obj = StorageCache.getBelltimes(c, ds);
		if (obj != null && tryCache) {
			bellsCached = StorageCache.isStale(StorageCache.getFile(ds, StorageCache.TYPE_BELLTIMES, c));
			bellsLoaded = true;
			bellsStatus = bellsCached ? R.string.desc_cached : R.string.desc_current;
			Intent i = new Intent(ACTION_BELLTIMES_JSON);
			i.putExtra(EXTRA_JSON_DATA, obj.toString());
			i.putExtra(EXTRA_CACHED, bellsCached);
			LocalBroadcastManager.getInstance(c).sendBroadcast(i);
			if (!bellsCached) return; // no point wasting data!
		}
		if (!hasInternetConnection(c)) {
			bellsLoaded = true;
			return; // TODO fallback bells
		}
		try {
			new DownloadFileTask(c, ACTION_BELLTIMES_JSON).execute(new URL(baseURL + "/api/belltimes?date=" + DateTimeHelper.getDateString(c)));
		} catch (Exception e) {
			Log.e("apiaccessor", "belltimes failed", e);
		}
	}

	public static void getNotices(Context c) {
		getNotices(c, true);
	}

	public static void getNotices(Context c, boolean tryCache) {
		String ds = DateTimeHelper.getDateString(c);
		JsonObject obj = StorageCache.getNotices(c, ds);
		if (obj != null && tryCache) {
			noticesCached = StorageCache.isStale(StorageCache.getFile(ds, StorageCache.TYPE_NOTICES, c));
			noticesLoaded = true;
			noticesStatus = noticesCached ? R.string.desc_cached : R.string.desc_current;
			Intent i = new Intent(ACTION_NOTICES_JSON);
			i.putExtra(EXTRA_JSON_DATA, obj.toString());
			i.putExtra(EXTRA_CACHED, noticesCached);
			LocalBroadcastManager.getInstance(c).sendBroadcast(i);
			if (!noticesCached) return; // no point wasting data!
		}
		if (!isLoggedIn() || !hasInternetConnection(c)) {
			noticesLoaded = true;
			return;
		}
		try {
			new DownloadFileTask(c, ACTION_NOTICES_JSON).execute(new URL(baseURL + "/api/notices.json?date=" + DateTimeHelper.getDateString(c)));
		} catch (Exception e) {
			Log.e("apiaccessor", "notices wat", e);
		}
	}

	private static class DownloadFileTask extends AsyncTask<URL, Void, String> {
		private Context c;
		private final String intentType;

		public DownloadFileTask(Context c, String type) {
			this.intentType = type;
			this.c = c;
		}

		@Override
		protected String doInBackground(URL... urls) {
			for (URL i : urls) {
				try {
					HttpURLConnection con = (HttpURLConnection) i.openConnection();
					con.setRequestMethod("GET");
					con.setRequestProperty("Cookie", "SESSID="+sessionID);
					BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
					String result = "";
					String l = br.readLine();
					while (l != null) {
						result += l;
						l = br.readLine();
					}
					return result;
				} catch (Exception e) {
					Log.e("apiaccessor", "failed to load " + i.toString(), e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				Log.e("downloadfiletask", "failed to download a result for " + this.intentType);
				return;
			}
			try {
				JsonObject o = new JsonParser().parse(result).getAsJsonObject();
				if (o.has("error") || (o.has("status") && !o.get("status").getAsString().equals("OK"))) {
					Log.e("downloadfiletask", "something's wrong with the json we got, ignoring...");
					return;
				}
			}
			catch (Exception e) {
				Log.e("downloadfiletask", "received invalid json");
				return;
			}
			SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
			SharedPreferences.Editor ed = p.edit();
			if (intentType.equals(ACTION_BELLTIMES_JSON)) {
				bellsStatus = R.string.desc_current;
				bellsCached = false;
				ed.putLong(PREF_BELLTIMES_LAST_UPDATE, DateTimeHelper.getTimeMillis());
			} else if (intentType.equals(ACTION_NOTICES_JSON)) {
				noticesStatus = R.string.desc_current;
				noticesCached = false;
				ed.putLong(PREF_NOTICES_LAST_UPDATE, DateTimeHelper.getTimeMillis());
			} else if (intentType.equals(ACTION_TODAY_JSON)) {
				todayStatus = R.string.desc_current;
				todayCached = false;
				ed.putLong(PREF_TODAY_LAST_UPDATE, DateTimeHelper.getTimeMillis());
			}

			ed.commit();

			Intent i = new Intent(this.intentType);
			i.putExtra(EXTRA_JSON_DATA, result);
			try {
				JsonObject o = new JsonParser().parse(result).getAsJsonObject();
				if (o.has("status") && o.get("status").getAsString().equals("401")) {
					// need to log in
				}
			} catch (Exception e) {
				// whatever
				Log.d("ApiAccessor", "failed to parse json", e);
			}
			if (this.c instanceof TimetableActivity) {
				TimetableActivity a = (TimetableActivity)c;
				//a.mNavigationDrawerFragment.lastTimestamp.setText("Last updated: " + new SimpleDateFormat("h:mm:ss a").format(new Date()));
			}
			i.putExtra(EXTRA_CACHED, false);

			if (intentType.equals(GLOBAL_ACTION_TODAY_JSON)) {
				this.c.sendBroadcast(i);
			} else {
				LocalBroadcastManager.getInstance(this.c).sendBroadcast(i);
			}
		}
	}
}
