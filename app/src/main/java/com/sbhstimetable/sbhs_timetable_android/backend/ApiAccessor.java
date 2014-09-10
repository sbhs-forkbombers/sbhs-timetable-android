package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.LoginActivity;

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
    public static final String baseURL = "http://sbhstimetable.tk".toLowerCase(); // ALWAYS LOWER CASE!
    public static final String PREFS_NAME = "timetablePrefs";
    public static final String ACTION_TODAY_JSON = "todayData";
    public static final String ACTION_BELLTIMES_JSON = "belltimesData";
    public static final String ACTION_NOTICES_JSON = "noticesData";
    public static final String EXTRA_JSON_DATA = "jsonString";
    private static String sessionID = null;

    public static boolean todayCached = true;
    public static boolean bellsCached = true;
    public static boolean noticesCached = true;

    public static void load(Context c) {
        // load stored sessionID and whatnot here
        SharedPreferences s = c.getSharedPreferences(PREFS_NAME, 0);
        sessionID = s.getString("sessionID", null);
    }

    public static boolean isLoggedIn() {
        return sessionID != null;
    }

    public static void login(Context c) {
        c.startActivity(new Intent(c, LoginActivity.class));
    }

    public static void logOut(Context c) {
        sessionID = null;
        SharedPreferences s = c.getSharedPreferences(PREFS_NAME, 0);
        s.edit().remove("sessionID").commit();
    }

    public static void finishedLogin(Context c, String id) {
        sessionID = id;
        SharedPreferences.Editor e = c.getSharedPreferences(PREFS_NAME, 0).edit();
        e.putString("sessionID", sessionID);
        e.commit();
    }

    public static boolean hasInternetConnection(Context c) {
        ConnectivityManager conn = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return conn.getActiveNetworkInfo() != null && conn.getActiveNetworkInfo().isConnected();
    }

    public static String getToday(Context c) {
        JsonObject obj = StorageCache.getTodayJson(c, DateTimeHelper.getDateString());
        if (obj != null) {
            todayCached = true;
            Intent i = new Intent(ACTION_TODAY_JSON);
            i.putExtra(EXTRA_JSON_DATA, obj.toString());
            Log.i("apiaccessor", "sending broadcast from cache" + obj.toString());
            LocalBroadcastManager.getInstance(c).sendBroadcast(i); // to tide us over - or if there's no internet.
        }
        if (!isLoggedIn() || !hasInternetConnection(c)) {
            return null;
        }
        try {
            new DownloadFileTask(c, DateTimeHelper.getDateString(), ACTION_TODAY_JSON).execute(new URL(baseURL + "/api/today.json"));
        }
        catch (Exception e) {
            Log.e("apiaccessor", "wat", e);
        }

        return null;
    }

    public static void getBelltimes(Context c) {
        JsonObject obj = StorageCache.getBelltimes(c, DateTimeHelper.getDateString());
        if (obj != null) {
            bellsCached = true;
            Intent i = new Intent(ACTION_BELLTIMES_JSON);
            i.putExtra(EXTRA_JSON_DATA, obj.toString());
            LocalBroadcastManager.getInstance(c).sendBroadcast(i);
        }
        if (!hasInternetConnection(c)) return; // TODO fallback bells
        try {
            bellsCached = false;
            new DownloadFileTask(c, DateTimeHelper.getDateString(), ACTION_BELLTIMES_JSON).execute(new URL(baseURL + "/api/belltimes?date=" + DateTimeHelper.getDateString()));
        } catch (Exception e) {
            Log.e("apiaccessor", "belltimes wat", e);
        }
    }

    public static void getNotices(Context c) {
        JsonObject obj = StorageCache.getNotices(c, DateTimeHelper.getDateString());
        if (obj != null) {
            noticesCached = true;
            Intent i = new Intent(ACTION_NOTICES_JSON);
            i.putExtra(EXTRA_JSON_DATA, obj.toString());
            LocalBroadcastManager.getInstance(c).sendBroadcast(i);
        }
        if (!isLoggedIn() || !hasInternetConnection(c)) return;
        try {
            noticesCached = false;
            new DownloadFileTask(c, DateTimeHelper.getDateString(), ACTION_NOTICES_JSON).execute(new URL(baseURL + "/api/notices.json?date=" + DateTimeHelper.getDateString()));
        } catch (Exception e) {
            Log.e("apiaccessor", "notices wat", e);
        }
    }

    private static class DownloadFileTask extends AsyncTask<URL, Void, String> {
        private Context c;
        private final String intentType;
        private final String date;

        public DownloadFileTask(Context c, String date, String type) {
            this.intentType = type;
            this.c = c;
            this.date = date;
            Log.i("downloadfiletask", "download for " + date + " " + type);
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
                    Log.i("downloadfiletask", " got: " + result);
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
            Intent i = new Intent(this.intentType);
            i.putExtra(EXTRA_JSON_DATA, result);
            LocalBroadcastManager.getInstance(this.c).sendBroadcast(i);
        }
    }


}
