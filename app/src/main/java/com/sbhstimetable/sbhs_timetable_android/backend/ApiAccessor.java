package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.sbhstimetable.sbhs_timetable_android.LoginActivity;

/**
 * Access remote API calls and stuff
 */
public class ApiAccessor {
    public static final String baseURL = "http://edragyz:8080".toLowerCase(); // ALWAYS LOWER CASE!
    public static final String PREFS_NAME = "timetablePrefs";
    private static String sessionID = null;

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

    public static void finishedLogin(Context c, String id) {
        sessionID = id;
        SharedPreferences.Editor e = c.getSharedPreferences(PREFS_NAME, 0).edit();
        e.putString("sessionID", sessionID);
        e.commit();
    }
}
