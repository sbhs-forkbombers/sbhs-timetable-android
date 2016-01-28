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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.sbhstimetable.sbhs_timetable_android.backend.internal.Compat;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.service.NotificationService;
import com.sbhstimetable.sbhs_timetable_android.backend.service.TodayAppWidget;

public class SettingsFragment extends PreferenceFragment {

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
				PrefUtil.COLOUR,
				PrefUtil.NOTIFICATION_INCLUDE_BREAKS,
				PrefUtil.NOTIFICATIONS_ENABLED,
				PrefUtil.NOTIFICATIONS_PERSISTENT
		}; // settings to attach listeners to

		// don't offer lock screen widget options on platforms that don't support them
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
			PreferenceScreen s = getPreferenceScreen();
			ListPreference p = (ListPreference) findPreference("widget_transparency_lockscreen");
			s.removePreference(p);
			prefs = new String[] {
					PrefUtil.WIDGET_TRANSPARENCY_HS,
					PrefUtil.THEME,
					PrefUtil.COLOUR,
					PrefUtil.NOTIFICATION_INCLUDE_BREAKS,
					PrefUtil.NOTIFICATIONS_ENABLED,
					PrefUtil.NOTIFICATIONS_PERSISTENT
			};

		}
		for (String pref : prefs) {
			Preference thePref = this.findPreference(pref);
			thePref.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
			SharedPreferences p = thePref.getSharedPreferences();
			if (thePref instanceof ListPreference) {
				String defaultVal = ((ListPreference) thePref).getValue();
				thePref.getOnPreferenceChangeListener().onPreferenceChange(thePref, p.getString(pref, defaultVal));
			} else if (thePref instanceof CheckBoxPreference) {
				CheckBoxPreference checkbox = (CheckBoxPreference)thePref;
				checkbox.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
				checkbox.getOnPreferenceChangeListener().onPreferenceChange(checkbox, checkbox.getSharedPreferences().getBoolean(checkbox.getKey(), false));
			}
		}


	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
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
				} else if (preference.getKey().startsWith("app_")) { // new theme!
					ThemeHelper.invalidateTheme();
				}
			} else if (preference instanceof CheckBoxPreference) {
				if (preference.getKey().equals(PrefUtil.NOTIFICATIONS_ENABLED)) {
					Intent i = new Intent(preference.getContext(), NotificationService.class);
					if ((boolean)value) {
						i.setAction(NotificationService.ACTION_INITIALISE);
						preference.getContext().startService(i);
						preference.setSummary("Showing notifications for next class.");
					} else {
						preference.getContext().stopService(i);
						preference.setSummary("Not showing notifications for next class.");
					}
				} else if (preference.getKey().equals(PrefUtil.NOTIFICATION_INCLUDE_BREAKS)) {
					Intent i = new Intent(preference.getContext(), NotificationService.class);
					i.setAction(NotificationService.ACTION_INITIALISE);
					preference.getContext().startService(i);
					if ((boolean)value) {
						preference.setSummary(R.string.pref_desc_notification_periods_positive);
					} else {
						preference.setSummary(R.string.pref_desc_notification_periods_negative);
					}
				} else if (preference.getKey().equals(PrefUtil.NOTIFICATIONS_PERSISTENT)) {
					Intent i = new Intent(preference.getContext(), NotificationService.class);
					i.setAction(NotificationService.ACTION_INITIALISE);
					preference.getContext().startService(i);
					if ((boolean)value) {
						preference.setSummary(R.string.pref_desc_notification_persisting);
					} else {
						preference.setSummary(R.string.pref_desc_notification_persist_off);
					}
				}
			}
			return true;
		}
	};
}
