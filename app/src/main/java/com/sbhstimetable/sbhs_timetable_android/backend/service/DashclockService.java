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

package com.sbhstimetable.sbhs_timetable_android.backend.service;

import android.content.Intent;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.TimetableActivity;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.api.Day;
import com.sbhstimetable.sbhs_timetable_android.api.FullCycleWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.Lesson;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Belltimes;

import org.joda.time.format.DateTimeFormatterBuilder;

public class DashclockService extends DashClockExtension {
    private FullCycleWrapper cycle;
	private DateTimeHelper dth;
	private StorageCache cache;
    @Override
    protected void onInitialize(boolean isReconnect) {
        super.onInitialize(isReconnect);
        setUpdateWhenScreenOn(true);
        cycle = new FullCycleWrapper(this);
        dth = new DateTimeHelper(this);
		cache = new StorageCache(this);

		dth.setBells(cache.loadBells());
		if (!dth.hasBells()) {
			ApiWrapper.requestBells(this);
		}

    }

    @Override
    protected void onUpdateData(int reason) {
        int num;
        boolean summary = false;
        if (dth.hasBells()) {

		}
        else {
            publishUpdate(new ExtensionData().visible(false));
            return;
        }
		Day t = null;
		if (cycle.ready()) {
			t = cycle.getToday();
		}
		if (!cycle.hasRealTimeInfo()) {
			ApiWrapper.requestToday(this);
		}
		if (this.dth.getNextPeriod() == null || this.dth.getNextBell().getBellName().startsWith("Roll")) summary = true;
		ExtensionData res = new ExtensionData().icon(R.mipmap.ic_notification_icon).clickIntent(new Intent(this, TimetableActivity.class));
        if (t != null && !summary) {
			Belltimes.Bell next = dth.getNextPeriod();
			num = next.getPeriodNumber();
            publishUpdate(res.status(t.getPeriod(num).getShortName() + " - " + t.getPeriod(num).getRoom())
                            .expandedTitle(t.getPeriod(num).getSubject())
                            .expandedBody("in " + t.getPeriod(num).getRoom() + " with " + t.getPeriod(num).getTeacher())
                            .visible(true)
            );
        }
        else if (summary && t != null) {
            String subjects = "";
            for (int i : new int[] { 1, 2, 3, 4, 5}) {
                Lesson p = t.getPeriod(i);
                if (i == 5) {
                    if (p != null) {
                        subjects += "and " + p.getSubject().replace(" Period", "");
                    }
                    else {
                        subjects += "and free!";
                    }
                    continue;
                }
                if (p != null) {
                    subjects += p.getSubject().replace(" Period", "") + ", ";
                }
                else {
                    subjects += "Free, ";
                }
            }
            if (!cycle.ready()) {
                subjects = "I need to reload!";
                ApiWrapper.requestToday(this);
				ApiWrapper.requestTimetable(this);
            }
            String shortTitle;
			String day = new DateTimeFormatterBuilder().appendDayOfWeekText().toFormatter().print(dth.getNextSchoolDay());
			if (day.length() > 3) {
                shortTitle = day.substring(0, 3);
				if (cycle.getToday().getWeek().isEmpty()) {
					shortTitle += " " + cache.loadWeek();
				} else {
					shortTitle += " " + cycle.getToday().getWeek();
				}
            }
            else {
                shortTitle = "TMR";
            }
            publishUpdate(res.status(shortTitle)
							.expandedTitle(day)
							.expandedBody(subjects)
							.visible(true)
			);
        }
    }
}
