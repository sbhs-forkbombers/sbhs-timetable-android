package com.sbhstimetable.sbhs_timetable_android.backend.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;

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
        return hour > DateTimeHelper.getHour() || (hour == DateTimeHelper.getHour() && minute > DateTimeHelper.getMinute());
    }

    private static boolean isBeforeNow(int hour, int minute) {
        return hour < DateTimeHelper.getHour() || (hour == DateTimeHelper.getHour() && minute <= DateTimeHelper.getMinute());
    }

    public Bell getNextBell() {
        JsonArray belltimes = this.bells.get("bells").getAsJsonArray();
        for (int i = 0; i < belltimes.size(); i++) {
            JsonObject entry = belltimes.get(i).getAsJsonObject();
            Integer[] start = parseTime(entry.get("time").getAsString());
            if (isBeforeNow(start[0], start[1]) && i+1 < belltimes.size() ) {
                JsonObject end = belltimes.get(i+1).getAsJsonObject();
                Integer[] e = parseTime(end.get("time").getAsString());
                if (isAfterNow(e[0], e[1])) {
                    return new Bell(end, i+1);
                }
            }

        }
        return null;
    }

    public Bell getNextPeriod() {
        Bell b = getNextBell();
        JsonArray belltimes = this.bells.get("bells").getAsJsonArray();
        for (int i = b.getIndex(); i < belltimes.size(); i++) {
            if (belltimes.get(i).getAsJsonObject().get("bell").getAsString().matches("^\\d+$")) {
                return new Bell(belltimes.get(i).getAsJsonObject(), i);
            }
        }
        return new Bell(belltimes.get(1).getAsJsonObject(), 1);
    }

    public int getMaxIndex() {
        return this.bells.get("bells").getAsJsonArray().size();
    }
    public Bell getIndex(int i) {
        if (i < this.getMaxIndex()) {
            return new Bell(this.bells.get("bells").getAsJsonArray().get(i).getAsJsonObject(), i);
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

        public boolean isPeriod() {
            return this.data.get("bell").getAsString().matches("^\\d+$");
        }

        public int getPeriodNumber() {
            String num = this.data.get("bell").getAsString();
            if (this.isPeriod()) {
                int b = Integer.valueOf(num);
                return b;
            }
            return -1;
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
