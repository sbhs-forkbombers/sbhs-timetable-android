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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;

import com.sbhstimetable.sbhs_timetable_android.backend.internal.Compat;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.PrefUtil;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.service.NotificationService;
import com.sbhstimetable.sbhs_timetable_android.backend.service.TodayAppWidget;
import com.sbhstimetable.sbhs_timetable_android.gapis.GoogleApiHelper;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsFragment extends PreferenceFragment {

	private boolean initDone = false;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.pref_notification);
		addPreferencesFromResource(R.xml.pref_widget);
		addPreferencesFromResource(R.xml.pref_appearance);
		addPreferencesFromResource(R.xml.pref_location);
		ArrayList<String> prefs = new ArrayList<>(Arrays.asList(
				PrefUtil.WIDGET_TRANSPARENCY_HS,
				PrefUtil.WIDGET_TRANSPARENCY_LS,
				PrefUtil.THEME,
				PrefUtil.COLOUR,
				PrefUtil.NOTIFICATION_INCLUDE_BREAKS,
				PrefUtil.NOTIFICATIONS_ENABLED,
				PrefUtil.NOTIFICATIONS_PERSISTENT,
				PrefUtil.GEOFENCING_ACTIVE,
				PrefUtil.GEOFENCE_SOUND,
				PrefUtil.GEOFENCE_VIBRATE
		)); // settings to attach listeners to
		PreferenceScreen s = getPreferenceScreen();
		// don't offer lock screen widget options on platforms that don't support them
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
			ListPreference p = (ListPreference) findPreference("widget_transparency_lockscreen");
			s.removePreference(p);
			prefs.remove(PrefUtil.WIDGET_TRANSPARENCY_LS);
			/*prefs = new String[] {
					PrefUtil.WIDGET_TRANSPARENCY_HS,
					PrefUtil.THEME,
					PrefUtil.COLOUR,
					PrefUtil.NOTIFICATION_INCLUDE_BREAKS,
					PrefUtil.NOTIFICATIONS_ENABLED,
					PrefUtil.NOTIFICATIONS_PERSISTENT
			};*/

		}
		if (!((Vibrator)s.getContext().getSystemService(Context.VIBRATOR_SERVICE)).hasVibrator()) {
			prefs.remove(PrefUtil.GEOFENCE_VIBRATE);
			s.removePreference(findPreference(PrefUtil.GEOFENCE_VIBRATE));
		}
		for (String pref : prefs) {
			Preference thePref = this.findPreference(pref);
			thePref.setOnPreferenceChangeListener(mPreferenceChangeListener);
			SharedPreferences p = thePref.getSharedPreferences();
			if (thePref instanceof ListPreference) {
				String defaultVal = ((ListPreference) thePref).getValue();
				thePref.getOnPreferenceChangeListener().onPreferenceChange(thePref, p.getString(pref, defaultVal));
			} else if (thePref instanceof CheckBoxPreference) {
				CheckBoxPreference checkbox = (CheckBoxPreference)thePref;
				checkbox.setOnPreferenceChangeListener(mPreferenceChangeListener);
				checkbox.getOnPreferenceChangeListener().onPreferenceChange(checkbox, checkbox.getSharedPreferences().getBoolean(checkbox.getKey(), false));
			} else if (thePref instanceof RingtonePreference) {
				RingtonePreference rp = (RingtonePreference)thePref;
				rp.setOnPreferenceChangeListener(mPreferenceChangeListener);
				rp.getOnPreferenceChangeListener().onPreferenceChange(rp, rp.getSharedPreferences().getString(rp.getKey(), ""));
			}
		}

		initDone = true;

	}

	public void onLocationPermissionDenied() {
		((CheckBoxPreference)findPreference(PrefUtil.GEOFENCING_ACTIVE)).setChecked(false);
		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putBoolean(PrefUtil.GEOFENCING_ACTIVE, false).apply();

	}

	@Override
	public void onActivityCreated(Bundle b) {
		super.onActivityCreated(b);
		((SettingsFragmentListener)getActivity()).setSettingsFragment(this);
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private Preference.OnPreferenceChangeListener mPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
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
				} else if (preference.getKey().equals(PrefUtil.GEOFENCING_ACTIVE)) {
					PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(PrefUtil.GEOFENCE_LAST_NOTIFIED_DATE, "1970-01-01").apply();

					if ((boolean)value && initDone) {
						if (!GoogleApiHelper.checkPermission(preference.getContext(), null)) {
							// need to request permission
							GoogleApiHelper.checkPermission(preference.getContext(), getActivity());
						} else {
							GoogleApiHelper.initialise(preference.getContext());
						}
					} else if (initDone && !(boolean)value) {
						GoogleApiHelper.disableApiClient();
					}
				}
			} else if (preference instanceof RingtonePreference) {
				RingtonePreference rp = (RingtonePreference) preference;
				if (value.toString().equals("")) {
					rp.setSummary("No sound");
				} else {
					Uri ringtoneUri = Uri.parse(value.toString());
					Ringtone r = RingtoneManager.getRingtone(rp.getContext(), ringtoneUri);
					rp.setSummary(r.getTitle(rp.getContext()));
				}
			}
			return true;
		}
	};

	public interface SettingsFragmentListener {
		public void setSettingsFragment(SettingsFragment frag);
	}
}
