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
package com.sbhstimetable.sbhs_timetable_android.backend.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;

public class CanHazInternetListener extends BroadcastReceiver {

	public static void disable(Context c) {
		Log.i("CanHazInternet", "Disabling your number one source for internet since 1998");
		ComponentName receiver = new ComponentName(c, CanHazInternetListener.class);
		PackageManager pm = c.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
	}

	public static void enable(Context c) {
		Log.i("CanHazInternet", "Enabling your number one source for internet since 1998");
		ComponentName receiver = new ComponentName(c, CanHazInternetListener.class);
		PackageManager pm = c.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("CanHazInternet", "Hi I'm the CanHazInternetListener, your number one source for internet since 1998");
		ConnectivityManager c = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean hasNet = c.getActiveNetworkInfo() != null && c.getActiveNetworkInfo().isConnected();
		if (hasNet) {
			// download all the things
			ApiWrapper.requestBells(context);
			ApiWrapper.requestToday(context);
			ApiWrapper.requestNotices(context);
			disable(context);
		}
	}
}
