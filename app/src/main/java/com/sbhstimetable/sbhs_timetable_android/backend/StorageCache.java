package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class StorageCache {
    private static boolean isOld(File f) {
        // don't touch non-JSONs
        return f.getName().endsWith("json") && (DateTimeHelper.getTimeMillis() - f.lastModified()) > (1000 * 60 * 60 * 24 * 7); // older than a week
    }

    private static boolean isMine(File f) {
        return f.getName().endsWith("json");
    }

    private static boolean writeCacheFile(Context c, String date, String type, String json) {
        File cacheDir = c.getCacheDir();
        File data = new File(cacheDir, date+"_"+type+".json");
        if (json == null) {
            Log.e("storageCache", "not given any data to write!");
            return false;
        }
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
                return new JsonParser().parse(new FileReader(data)).getAsJsonObject();
            }
            catch (IOException e) {
                Log.e("storageCache","couldn't read cache (which supposedly exists and is cached!)",e);
            }
            catch (IllegalStateException e) {
                Log.e("storageCache", "wow wek json", e);
            }
        }
        return null;
    }
    @SuppressWarnings("all")
    public static void cleanCache(Context context) {
        File cacheDir = context.getCacheDir();
        for (File f : cacheDir.listFiles()) {
            if (isOld(f)) {
                f.delete();
            }
        }
    }

    @SuppressWarnings("all")
    public static void deleteAllCacheFiles(Context c) {
        File cacheDir = c.getCacheDir();
        for (File f : cacheDir.listFiles()) {
            if (isMine(f)) {
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

    public static void cacheNotices(Context c, String date, String json) {
        writeCacheFile(c, date, "notices", json);
    }

    public static JsonObject getNotices(Context c, String date) {
        return readCacheFile(c, date, "notices");
    }

    @SuppressWarnings("unused")
    public static boolean hasCachedDate(Context c) {
        return new File(c.getCacheDir(), "date-"+DateTimeHelper.getGuessedDateString()+".json").exists();
    }

    public static String getCachedDate(Context c) {
        File f = new File(c.getCacheDir(), "date-"+DateTimeHelper.getGuessedDateString()+".json");
        if (!f.exists()) {
            return DateTimeHelper.getGuessedDateString();
        }
        try {
            FileReader r = new FileReader(f);
            char[] s = new char[10];
            int max = r.read(s);
            String date = String.valueOf(Arrays.copyOfRange(s, 0, max));
            r.close();
            return date;
        }
        catch (IOException e) {
            //meh
        }
        return DateTimeHelper.getGuessedDateString();
    }

    public static void writeCachedDate(Context c, String s) {
        File f = new File(c.getCacheDir(), "date-"+DateTimeHelper.getGuessedDateString()+".json");
        try {
            FileWriter w = new FileWriter(f);
            w.write(s);
            w.close();
        }
        catch (IOException e) {
            Log.e("StorageCache", "failed to write cached date", e);
        }
    }
}
