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

package com.sbhstimetable.sbhs_timetable_android.backend.internal;

public class PrefUtil {
    public static final String WIDGET_TRANSPARENCY_HS = "widget_transparency_homescreen";
    public static final String WIDGET_TRANSPARENCY_LS = "widget_transparency_lockscreen";
	public static final String THEME = "app_theme";
	public static final String COLOUR = "app_colour";
	public static final String NOTIFICATIONS_ENABLED = "notifications_enable";
	public static final String NOTIFICATION_INCLUDE_BREAKS = "notifications_only_periods";
	public static final String NOTIFICATIONS_PERSISTENT = "notification_persist";
	public static final String BELLTIMES_DAY_TESTING = "bell_day_testing";
	public static final String PREF_DISABLE_DIALOG = "disableFeedbackDialog";
	public static final String PREF_LOGGED_IN_ONCE = "hasLoggedInBefore";
}
