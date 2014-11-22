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
		if (theme.equals("dark")) {
			isDark = true;
			a.setTheme(R.style.AppTheme);
		} else if (theme.equals("light")) {
			isDark = false;
			a.setTheme(R.style.AppTheme_Light);
		} else if (theme.equals("darkgr")) {
			isDark = true;
			a.setTheme(R.style.AppTheme_Green);
		} else if (theme.equals("lightgr")) {
			isDark = false;
			a.setTheme(R.style.AppTheme_Green_Light);
		} else if (theme.equals("darkrd")) {
			isDark = true;
			a.setTheme(R.style.AppTheme_Red);
		} else if (theme.equals("lightrd")) {
			isDark = false;
			a.setTheme(R.style.AppTheme_Red_Light);
		}
	}

	public static boolean isBackgroundDark() {
		return isDark;
	}
}
