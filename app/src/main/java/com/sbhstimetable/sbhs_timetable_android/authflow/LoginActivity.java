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

package com.sbhstimetable.sbhs_timetable_android.authflow;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.TimetableActivity;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;

import static com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper.baseURL;

public class LoginActivity extends AppCompatActivity {
	public Toolbar mToolbar;
	public TypedValue mTypedValue;
	public WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeHelper.setTheme(this);
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_login);
		mTypedValue = new TypedValue();
		getTheme().resolveAttribute(R.attr.colorPrimary, mTypedValue, true);
		int colorPrimary = mTypedValue.data;
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setBackgroundColor(colorPrimary);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			getTheme().resolveAttribute(R.attr.colorPrimaryDark, mTypedValue, true);
			int colorPrimaryDark = mTypedValue.data;
			getWindow().setStatusBarColor(colorPrimaryDark);
		}

		mWebView = (WebView) findViewById(R.id.loginview);
		mWebView.setBackgroundColor(Color.parseColor("#000000"));
		mWebView.getSettings().setSaveFormData(true);
		final Activity me = this;
		mWebView.setWebViewClient(new WebViewClient() {
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				//Log.e("LoginActivity", "navigate to " + url);

				if (url.toLowerCase().startsWith(baseURL.replace("https", "http").toLowerCase()) && ((url.endsWith("/") || url.endsWith("loggedIn=true") || url.contains("mobile_loading")))) {
					// this would be our website!
					String[] cookies = CookieManager.getInstance().getCookie(baseURL).split("[;]");
					for (String i : cookies) {
						if (i.contains("SESSID")) {
							String sessionID = i.split("=")[1];
							ApiWrapper.finishedLogin(me, sessionID);

							PreferenceManager.getDefaultSharedPreferences(me).edit().putBoolean(TimetableActivity.PREF_LOGGED_IN_ONCE, true).apply();
							me.onBackPressed();
						}
					}
				}
			}
		});
		mWebView.loadUrl(baseURL + "/try_do_oauth?app=1");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, TimetableActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(i);
		finish();
	}

}
