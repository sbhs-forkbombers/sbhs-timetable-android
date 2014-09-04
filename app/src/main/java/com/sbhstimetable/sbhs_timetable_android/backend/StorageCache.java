package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StorageCache {
    private Context context;
    public StorageCache(Context c) {
        this.context = c;
    }

    public JsonObject getTodayJson(String date) {
        File cacheDir = context.getCacheDir();
        File data = new File(cacheDir, date+"_today.json");
        if (data.exists() && data.canRead()) {
            try {
                JsonObject today = new JsonParser().parse(new FileReader(data)).getAsJsonObject();
                return today;
            }
            catch (IOException e) {
                Log.e("storageCache","couldn't read cache (which supposedly exists and is cached!)",e);
            }
        }

        return null;
    }

    public void cacheTodayJson(String date, String json) {
        File cacheDir = context.getCacheDir();
        File data = new File(cacheDir, date+"_today.json");
        try {
            FileWriter w = new FileWriter(data);
            w.write(json);
            w.close();
        }
        catch (IOException e) {
            Log.e("storageCache", "unable to cache json for " + date + "!", e);
        }
    }
}
