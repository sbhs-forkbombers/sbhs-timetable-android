package com.sbhstimetable.sbhs_timetable_android.backend;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BelltimesJson {
    private final JsonObject bells;

    public BelltimesJson(JsonObject json) {
        this.bells = json;
    }

    private static Integer[] parseTime(String time) {
        String[] parts = time.split(":");
        Integer[] ans = new Integer[2];
        ans[0] = Integer.valueOf(parts[0]);
        ans[1] = Integer.valueOf(parts[1]);
        return ans;
    }

    private static boolean isAfterNow(int hour, int minute) {
        Log.i("belltimesjson", "is " + DateTimeHelper.getHour() + ":" + DateTimeHelper.getMinute() + " after " + hour + ":" + minute);
        return hour > DateTimeHelper.getHour() || (hour == DateTimeHelper.getHour() && minute > DateTimeHelper.getMinute());
    }

    private static boolean isBeforeNow(int hour, int minute) {
        Log.i("belltimesjson", "is " + DateTimeHelper.getHour() + ":" + DateTimeHelper.getMinute() + " before " + hour + ":" + minute);
        return hour < DateTimeHelper.getHour() || (hour == DateTimeHelper.getHour() && minute <= DateTimeHelper.getMinute());
    }

    public Bell getNextPeriod() {
        JsonArray belltimes = this.bells.get("bells").getAsJsonArray();
        Log.i("belltimesjson", "let's do this.");
        for (int i = 0; i < belltimes.size(); i++) {
            JsonObject entry = belltimes.get(i).getAsJsonObject();
            Integer[] start = parseTime(entry.get("time").getAsString());
            if (isBeforeNow(start[0], start[1]) && i+1 < belltimes.size() ) {
                JsonObject end = belltimes.get(i+1).getAsJsonObject();
                Integer[] e = parseTime(end.get("time").getAsString());
                if (isAfterNow(e[0], e[1])) {
                    Log.i("belltimesjson", "found our period - " + (i+1) + " name => " + end.get("bell").getAsString());
                    return new Bell(end, i+1);
                }
            }

        }
        return null;
    }

    public static class Bell {
        private JsonObject data;
        private int index;

        public Bell(JsonObject obj, int index) {
            this.data = obj;
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }

        public Integer[] getBell() {
            return parseTime(this.data.get("time").getAsString());
        }

        public String getLabel() {
            String res = this.data.get("bell").getAsString();
            if (res.matches("^\\d+$")) {
                return "Period " + res;
            }
            return res;
        }
    }
}
