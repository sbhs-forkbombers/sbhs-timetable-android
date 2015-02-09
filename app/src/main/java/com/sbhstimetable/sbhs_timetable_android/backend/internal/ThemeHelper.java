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

import android.app.Activity;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.R;

import java.lang.reflect.Field;

public class ThemeHelper {
	private static boolean isDark = true;
	private static Integer curAppTheme = null;

	public static void setTheme(Activity a) {
		if (curAppTheme != null) {
			a.setTheme(curAppTheme);
		}
		String colour = PreferenceManager.getDefaultSharedPreferences(a).getString(PrefUtil.COLOUR, "AppTheme");
		String theme = PreferenceManager.getDefaultSharedPreferences(a).getString(PrefUtil.THEME, "Dark");

		if (!colour.startsWith("AppTheme")) {
			colour = "AppTheme_" + colour.substring(0, 1).toUpperCase() + colour.substring(1);
		}
		if (theme.equals("Light")) {
			colour += "_Light";
			isDark = false;
		}
		else {
			isDark = true;
		}
		Log.i("ThemeHelper", "Setting colour to " + colour);
		int colourRes = R.style.AppTheme;
		if (!colour.equals("AppTheme")) {

			try {
				Field f = R.style.class.getField(colour);
				colourRes = f.getInt(null);
				a.setTheme(colourRes);
			} catch (Exception e) {
				Log.w("ThemeHelper", "Damn couldn't get the colour '" + colour + "', falling back to blue...", e);
			}
		}

		/*Log.i("ThemeHelper", "setting theme to " + theme);
		if (theme.equals("dark")) {
			isDark = true;
			toChange.applyStyle(R.style.AppTheme, false);
		} else if (theme.equals("light")) {
			isDark = false;
			toChange.applyStyle(R.style.AppTheme_Light, false);
		}*/

		curAppTheme = colourRes;
		//a.getTheme().setTo(curAppTheme);

	}

	public static void invalidateTheme() {
		curAppTheme = null;
	}

	public static boolean isBackgroundDark() {
		return isDark;
	}
}
