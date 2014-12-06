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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;

import static com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor.baseURL;

public class LoginActivity extends ActionBarActivity {
	public Toolbar mToolbar;
	public TypedValue mTypedValue;
	public WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeHelper.setTheme(this);
		super.onCreate(savedInstanceState);
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
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			me.setProgress(newProgress * 1000);
			}
		});
		mWebView.setWebViewClient(new WebViewClient() {
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (url.startsWith(baseURL) && ((url.endsWith("/") || url.contains("mobile_loading")))) {
					// this would be our website!
					String[] cookies = CookieManager.getInstance().getCookie(baseURL).split("[;]");
					for (String i : cookies) {
						if (i.contains("SESSID")) {
							String sessionID = i.split("=")[1];
							ApiAccessor.finishedLogin(me, sessionID);
							view.clearCache(true);
							finish();
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
		finish();
	}
}
