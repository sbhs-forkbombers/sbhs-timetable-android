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

import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Notices;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Timetable;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Today;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface SbhsTimetableService {
	@GET("/api/today.json")
	void getTodayJson(@Query("SESSID") String sessID, Callback<Today> r);

	@GET("/api/belltimes")
	void getBelltimes(@Query("date") String date, Callback<Belltimes> r);

	@GET("/api/notices.json")
	void getNotices(@Query("SESSID") String sessID, Callback<Notices> r);

	@GET("/api/bettertimetable.json")
	void getTimetable(@Query("SESSID") String sessID, Callback<Timetable> r);

}
