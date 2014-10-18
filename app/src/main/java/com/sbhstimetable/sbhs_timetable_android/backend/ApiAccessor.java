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

package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.LoginActivity;
import com.sbhstimetable.sbhs_timetable_android.TimetableActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Access remote API calls and stuff
 */
public class ApiAccessor {
    public static final String baseURL = "http://seemslegit.sbhstimetable.tk".toLowerCase(); // ALWAYS LOWER CASE!
    public static final String PREFS_NAME = "timetablePrefs";
    public static final String ACTION_TODAY_JSON = "todayData";
    public static final String ACTION_BELLTIMES_JSON = "belltimesData";
    public static final String ACTION_NOTICES_JSON = "noticesData";
    public static final String EXTRA_JSON_DATA = "jsonString";
    public static final String GLOBAL_ACTION_TODAY_JSON = "com.sbhstimetable.sbhs_timetable_android."+ACTION_TODAY_JSON;
    private static String sessionID = null;

    public static boolean todayCached = true;
    public static boolean bellsCached = true;
    public static boolean noticesCached = true;

    public static boolean todayLoaded = false;
    public static boolean bellsLoaded = false;
    public static boolean noticesLoaded = false;

    public static void load(Context c) {
        // load stored sessionID and whatnot here
        SharedPreferences s = c.getSharedPreferences(PREFS_NAME, 0);
        sessionID = s.getString("sessionID", null);
    }

    public static String getSessionID() {
        return sessionID;
    }

    public static boolean isLoggedIn() {
        return sessionID != null;
    }

    public static void login(Context c) {
        c.startActivity(new Intent(c, LoginActivity.class));
    }

    public static void logout(Context c) {
        sessionID = null;
        SharedPreferences s = c.getSharedPreferences(PREFS_NAME, 0);
        s.edit().remove("sessionID").apply();
    }

    public static void finishedLogin(Context c, String id) {
        sessionID = id;
        SharedPreferences.Editor e = c.getSharedPreferences(PREFS_NAME, 0).edit();
        e.putString("sessionID", sessionID);
        e.apply();
    }

    public static boolean hasInternetConnection(Context c) {
        ConnectivityManager conn = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return conn.getActiveNetworkInfo() != null && conn.getActiveNetworkInfo().isConnected();
    }

    public static void getToday(Context c) {
        getToday(c, true);
    }

    public static void getToday(Context c, boolean tryCache) {
        JsonObject obj = StorageCache.getTodayJson(c, DateTimeHelper.getDateString(c));
        if (obj != null && tryCache) {
            todayCached = true;
            Intent i = new Intent(ACTION_TODAY_JSON);
            i.putExtra(EXTRA_JSON_DATA, obj.toString());
            LocalBroadcastManager.getInstance(c).sendBroadcast(i); // to tide us over - or if there's no internet.
        }
        if (!isLoggedIn() || !hasInternetConnection(c)) {
            todayLoaded  = true;
        }
        try {
            Log.i("ApiAccessor", "Going to get today.json…");
            new DownloadFileTask(c, ACTION_TODAY_JSON).execute(new URL(baseURL + "/api/today.json"));
        }
        catch (Exception e) {
            Log.e("apiaccessor", "today.json dl failed", e);
        }

    }

    public static void getBelltimes(Context c) {
        getBelltimes(c, true);
    }

    public static void getBelltimes(Context c, boolean tryCache) {
        JsonObject obj = StorageCache.getBelltimes(c, DateTimeHelper.getDateString(c));
        if (obj != null && tryCache) {
            bellsCached = true;
            bellsLoaded = true;
            Intent i = new Intent(ACTION_BELLTIMES_JSON);
            i.putExtra(EXTRA_JSON_DATA, obj.toString());
            c.sendBroadcast(i);
        }
        if (!hasInternetConnection(c)) {
            bellsLoaded = true;
            return; // TODO fallback bells
        }
        try {
            new DownloadFileTask(c, ACTION_BELLTIMES_JSON).execute(new URL(baseURL + "/api/belltimes?date=" + DateTimeHelper.getDateString(c)));
        } catch (Exception e) {
            Log.e("apiaccessor", "belltimes failed", e);
        }
    }

    public static void getNotices(Context c) {
        getNotices(c, true);
    }

    public static void getNotices(Context c, boolean tryCache) {
        JsonObject obj = StorageCache.getNotices(c, DateTimeHelper.getDateString(c));
        if (obj != null && tryCache) {
            noticesCached = true;
            Intent i = new Intent(ACTION_NOTICES_JSON);
            i.putExtra(EXTRA_JSON_DATA, obj.toString());
            LocalBroadcastManager.getInstance(c).sendBroadcast(i);
        }
        if (!isLoggedIn() || !hasInternetConnection(c)) {
            noticesLoaded = true;
            return;
        }
        try {
            new DownloadFileTask(c, ACTION_NOTICES_JSON).execute(new URL(baseURL + "/api/notices.json?date=" + DateTimeHelper.getDateString(c)));
        } catch (Exception e) {
            Log.e("apiaccessor", "notices wat", e);
        }
    }

    private static class DownloadFileTask extends AsyncTask<URL, Void, String> {
        private Context c;
        private final String intentType;

        public DownloadFileTask(Context c, String type) {
            this.intentType = type;
            this.c = c;
        }

        @Override
        protected String doInBackground(URL... urls) {
            for (URL i : urls) {
                try {
                    HttpURLConnection con = (HttpURLConnection) i.openConnection();
                    con.setRequestMethod("GET");
                    con.setRequestProperty("Cookie", "SESSID="+sessionID);
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String result = "";
                    String l = br.readLine();
                    while (l != null) {
                        result += l;
                        l = br.readLine();
                    }
                    return result;
                }
                catch (Exception e) {
                    Log.e("apiaccessor", "failed to load " + i.toString(), e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Log.e("downloadfiletask","failed to download a result for " + this.intentType);
                return;
            }

            if (intentType.equals(ACTION_BELLTIMES_JSON)) {
                bellsCached = false;
            }
            else if (intentType.equals(ACTION_NOTICES_JSON)) {
                noticesCached = false;
            }
            else if (intentType.equals(ACTION_TODAY_JSON)) {
                todayCached = false;
            }

            Intent i = new Intent(this.intentType);
            i.putExtra(EXTRA_JSON_DATA, result);
            try {
                JsonObject o = new JsonParser().parse(result).getAsJsonObject();
                if (o.has("status") && o.get("status").getAsString().equals("401")) {
                    // need to log in
                }
            }
            catch (Exception e) {
                // whatever
                Log.d("ApiAccessor", "failed to parse json", e);
            }
            if (this.c instanceof TimetableActivity) {
                TimetableActivity a = (TimetableActivity)c;
                a.mNavigationDrawerFragment.lastTimestamp.setText("Last updated: " + new SimpleDateFormat("h:mm:ss a").format(new Date()));
            }


            if (intentType.equals(GLOBAL_ACTION_TODAY_JSON)) {
                this.c.sendBroadcast(i);
            }
            else {
                LocalBroadcastManager.getInstance(this.c).sendBroadcast(i);
            }


        }
    }


}
