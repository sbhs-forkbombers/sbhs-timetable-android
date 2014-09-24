package com.sbhstimetable.sbhs_timetable_android.backend.internal;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonUtil {
    public static JsonObject safelyParseJson(String s) {
        try {
            return new JsonParser().parse(s).getAsJsonObject();
        }
        catch (JsonSyntaxException e) {
            Log.v("jsonUtil","failed to parse json string!", e);
        }
        return new JsonObject();
    }
}
