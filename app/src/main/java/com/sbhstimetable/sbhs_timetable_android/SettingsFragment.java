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

package com.sbhstimetable.sbhs_timetable_android;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.content.LocalBroadcastManager;

import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.Compat;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.service.TodayAppWidget;

public class SettingsFragment extends PreferenceFragment {
	private PreferenceScreen mPreferenceScreen;
	private ListPreference mListPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_notification);
		addPreferencesFromResource(R.xml.pref_widget);
		addPreferencesFromResource(R.xml.pref_appearance);
		String[] prefs = new String[] {
				PrefUtil.WIDGET_TRANSPARENCY_HS,
				PrefUtil.WIDGET_TRANSPARENCY_LS,
				PrefUtil.THEME,
				PrefUtil.COLOUR
		}; // settings to attach listeners to

		// don't offer lockscreen widget options on platforms that don't support them
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
			mPreferenceScreen = getPreferenceScreen();
			mListPreference = (ListPreference) findPreference("widget_transparency_lockscreen");
			mPreferenceScreen.removePreference(mListPreference);
			prefs = new String[] {
					PrefUtil.WIDGET_TRANSPARENCY_HS,
					PrefUtil.THEME,
					PrefUtil.COLOUR
			};

		}
		for (String pref : prefs) {
			Preference thePref = this.findPreference(pref);
			thePref.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
			SharedPreferences p = thePref.getSharedPreferences();
			String defaultVal = ((ListPreference) thePref).getValue();
			thePref.getOnPreferenceChangeListener().onPreferenceChange(thePref, p.getString(pref, defaultVal));
		}
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value. -- TODO
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				// Set the summary to reflect the new value.
				preference.setSummary((index >= 0 ? String.valueOf(listPreference.getEntries()[index]).replace("%", "%%") : null));
				if (preference.getKey().contains("widget")) {
					// update widgets
					Intent i = new Intent(preference.getContext(), TodayAppWidget.class);
					i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
					i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, Compat.getWidgetIds(preference.getContext(), TodayAppWidget.class));
					preference.getContext().sendBroadcast(i);
				}
				else if (preference.getKey().startsWith("app_")) {
					Intent i = new Intent();
					i.setAction(ApiAccessor.ACTION_THEME_CHANGED);
					ThemeHelper.invalidateTheme();
					LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(preference.getContext());
					lbm.sendBroadcast(i);
				}
			}
			return true;
		}
	};
}
