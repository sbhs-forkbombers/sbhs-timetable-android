package com.sbhstimetable.sbhs_timetable_android.backend.internal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sbhstimetable.sbhs_timetable_android.R;

import java.lang.reflect.Field;

public class ThemeHelper {
	private static boolean isDark = true;
	private static Resources.Theme curAppTheme = null;
	public static void setTheme(Activity a) {
		if (curAppTheme != null) {
			a.getTheme().setTo(curAppTheme);
		}
		String theme = PreferenceManager.getDefaultSharedPreferences(a).getString(PrefUtil.THEME, "mdark");
		String colour = PreferenceManager.getDefaultSharedPreferences(a).getString(PrefUtil.COLOUR, "blue");
		Resources.Theme toChange = a.getResources().newTheme();
		Log.i("ThemeHelper", "setting theme to " + theme);
		if (theme.equals("dark")) {
			isDark = true;
			toChange.applyStyle(R.style.AppTheme, true);
		} else if (theme.equals("light")) {
			isDark = false;
			toChange.applyStyle(R.style.AppTheme_Light, true);
		}
		if (!colour.equals("Blue")) {
			try {
				Field f = R.style.class.getField(colour);
				int colourRes = f.getInt(null);
				toChange.applyStyle(colourRes, true);
			} catch (Exception e) {
				Log.w("ThemeHelper", "Damn couldn't get the colour '" + colour + "', falling back to blue...", e);
			}
		}

		curAppTheme = toChange;
		a.getTheme().setTo(curAppTheme);
	}

	public static boolean isBackgroundDark() {
		return isDark;
	}
}
