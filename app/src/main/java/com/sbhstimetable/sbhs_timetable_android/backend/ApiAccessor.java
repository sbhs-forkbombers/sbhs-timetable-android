package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;


import com.sbhstimetable.sbhs_timetable_android.LoginActivity;

import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Map;

/**
 * Access remote API calls and stuff
 */
public class ApiAccessor {
    public static final String baseURL = "http://nodejs.sbhstimetable.tk".toLowerCase(); // ALWAYS LOWER CASE!
    public static final String PREFS_NAME = "timetablePrefs";
    private static String sessionID = null;

    public static void load(Context c) {
        // load stored sessionID and whatnot here
        SharedPreferences s = c.getSharedPreferences(PREFS_NAME, 0);
        sessionID = s.getString("sessionID", null);
        Log.i("timetable", "Loaded sessionID " + sessionID);
    }

    public static boolean isLoggedIn() {
        return sessionID != null;
    }

    public static void login(Context c) {
        c.startActivity(new Intent(c, LoginActivity.class));
    }

    public static void finishedLogin(Context c, String id) {
        sessionID = id;
        SharedPreferences.Editor e = c.getSharedPreferences(PREFS_NAME, 0).edit();
        e.putString("sessionID", sessionID);
        e.commit();
    }

    public static String getToday(Context c) {
        if (!isLoggedIn()) {
            return null;
        }
        try {
            new DownloadFileTask().execute(new URL(baseURL + "/api/today.json"));
        }
        catch (Exception e) {
            Log.e("timetable", "wat", e);
        }

        return null;
    }

    private static class DownloadFileTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            for (URL i : urls) {
                try {
                    // Map<String, Object> m = JSON.std.mapFrom("");
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
                    Log.i("timetable", "Got it: " + result);
                    return l;
                }
                catch (Exception e) {
                    Log.e("timetable", "failed to load " + i.toString(), e);
                }
            }
            return null;
        }
    }


}
