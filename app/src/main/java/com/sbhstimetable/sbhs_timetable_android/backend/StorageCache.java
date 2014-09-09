package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class StorageCache {
    private static boolean isOld(File f) {
        return (DateTimeHelper.getTimeMillis() - f.lastModified()) > (1000 * 60 * 60 * 24 * 7); // older than a week
    }

    private static boolean writeCacheFile(Context c, String date, String type, String json) {
        File cacheDir = c.getCacheDir();
        File data = new File(cacheDir, date+"_"+type+".json");
        try {
            FileWriter w = new FileWriter(data);
            w.write(json);
            w.close();
            return true;
        }
        catch (IOException e) {
            Log.e("storageCache", "unable to cache json for " + date + "!", e);
        }
        return false;
    }

    private static JsonObject readCacheFile(Context c, String date, String type) {
        File cacheDir = c.getCacheDir();
        File data = new File(cacheDir, date+"_"+type+".json");
        if (data.exists() && data.canRead()) {
            try {
                JsonObject res = new JsonParser().parse(new FileReader(data)).getAsJsonObject();
                return res;
            }
            catch (IOException e) {
                Log.e("storageCache","couldn't read cache (which supposedly exists and is cached!)",e);
            }
        }
        return null;
    }

    public static void cleanCache(Context context) {
        File cacheDir = context.getCacheDir();
        for (File f : cacheDir.listFiles()) {
            if (isOld(f)) {
                f.delete();
            }
        }
    }

    public static JsonObject getTodayJson(Context context, String date) {
        return readCacheFile(context, date, "today");
    }

    public static void cacheTodayJson(Context context, String date, String json) {
        writeCacheFile(context, date, "today", json);
    }

    public static void cacheBelltimes(Context context, String date, String json) {
        writeCacheFile(context, date, "belltimes", json);
    }

    public static JsonObject getBelltimes(Context c, String date) {
        return readCacheFile(c, date, "belltimes");
    }
}
