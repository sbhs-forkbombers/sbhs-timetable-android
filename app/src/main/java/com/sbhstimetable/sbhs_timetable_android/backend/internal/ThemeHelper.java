package com.sbhstimetable.sbhs_timetable_android.backend.internal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.R;

public class ThemeHelper {
	private static boolean isDark = true;
	public static void setTheme(Activity a) {
		String theme = PreferenceManager.getDefaultSharedPreferences(a).getString(PrefUtil.THEME, "mdark");
		Log.i("ThemeHelper", "setting theme to " + theme);
		if (theme.equals("mdark") || theme.equals("hdark")) { // TODO holo themes
			a.setTheme(R.style.AppTheme);
			isDark = true;

		}
		else {
			isDark = false;
			a.setTheme(R.style.AppThemeLight);
		}
	}

	public static boolean isBackgroundDark() {
		return isDark;
	}
}
