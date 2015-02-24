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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Notices;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Timetable;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Today;
import com.sbhstimetable.sbhs_timetable_android.event.BellsEvent;
import com.sbhstimetable.sbhs_timetable_android.event.NoticesEvent;
import com.sbhstimetable.sbhs_timetable_android.event.TimetableEvent;
import com.sbhstimetable.sbhs_timetable_android.event.TodayEvent;

import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.client.Response;

public class ApiWrapper {
	private static SbhsTimetableService api;
	private static RestAdapter adapter;
	private static final EventBus EVENT_BUS = new EventBus();
	private static boolean initialised = false;
	private static String sessID;
	static {
		adapter = new RestAdapter.Builder()
				.setEndpoint("https://sbhstimetable.tk")
				.setLog(new AndroidLog("http"))
				.setLogLevel(RestAdapter.LogLevel.BASIC)
				.build();

		api = adapter.create(SbhsTimetableService.class);
	}

	public static SbhsTimetableService getApi() {
		return api;
	}

	public static void initialise(Context c) {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);
		if (p.contains("sessionID")) {
			sessID = p.getString("sessionID", "");
		} else {
			p.edit().putString("sessionID", c.getSharedPreferences("timetablePrefs", 0).getString("sessionID", "")).commit();
			sessID = p.getString("sessionID", "");
		}
		initialised = true;
	}

	// only used on functions which need sessID.
	private static boolean errIfNotReady() {
		if (!initialised) {
			Log.e("ApiWrapper", "Method called before initialise()!", new IllegalStateException());
			return true;
		}
		return false;
	}

	public static boolean isLoggedIn() {
		if (errIfNotReady()) return false;
		return sessID != null && !sessID.equals("");
	}

	public static void requestToday(Context c) {
		if (errIfNotReady()) return;
		api.getTodayJson(sessID, new Callback<Today>() {
			@Override
			public void success(Today today, Response response) {
				TodayEvent t;
				if (today.valid()) {
					t = new TodayEvent(today);
				} else {
					t = new TodayEvent(true);
				}
				getEventBus().post(t);
			}

			@Override
			public void failure(RetrofitError error) {
				TodayEvent t = new TodayEvent(error);
				getEventBus().post(t);
			}
		});
	}

	public static void requestBells(Context c) {
		if (errIfNotReady()) return;
		String s = DateTimeHelper.getYYYYMMDDFormatter().print(new DateTimeHelper(c).getNextSchoolDay().toInstant());
		api.getBelltimes(s, new Callback<Belltimes>() {
			@Override
			public void success(Belltimes belltimes, Response response) {
				BellsEvent b;
				if (belltimes.valid()) {
					b = new BellsEvent(belltimes);
				} else {
					b = new BellsEvent(true);
				}
				getEventBus().post(b);
			}

			@Override
			public void failure(RetrofitError error) {
				getEventBus().post(new BellsEvent(error));
			}
		});
	}

	public static void requestNotices(Context c) {
		if (errIfNotReady()) return;
		api.getNotices(sessID, new Callback<Notices>() {
			@Override
			public void success(Notices notices, Response response) {
				NoticesEvent t;
				if (notices.valid()) {
					t = new NoticesEvent(notices);
				} else {
					t = new NoticesEvent(true);
				}
				getEventBus().post(t);
			}

			@Override
			public void failure(RetrofitError error) {
				NoticesEvent t = new NoticesEvent(error);
				getEventBus().post(t);
			}
		});
	}

	public static void requestTimetable(Context c) {
		if (errIfNotReady()) return;
		api.getTimetable(sessID, new Callback<Timetable>() {
			@Override
			public void success(Timetable timetable, Response response) {
				TimetableEvent t;
				if (timetable.valid()) {
					t = new TimetableEvent(timetable);
				} else {
					t = new TimetableEvent(true);
				}
				getEventBus().post(t);
			}

			@Override
			public void failure(RetrofitError error) {
				TimetableEvent t = new TimetableEvent(error);
				getEventBus().post(t);
			}
		});
	}

	public static EventBus getEventBus() {
		return EVENT_BUS;
	}
}
