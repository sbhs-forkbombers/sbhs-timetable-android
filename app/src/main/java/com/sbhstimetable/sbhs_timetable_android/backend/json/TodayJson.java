package com.sbhstimetable.sbhs_timetable_android.backend.json;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TodayJson {
    private final JsonObject today;
    private Period periods[] = new Period[5];
    private static TodayJson INSTANCE;

    public static TodayJson getInstance() {
        return INSTANCE;
    }

    public TodayJson(JsonObject obj) {
         this.today = obj;
         for (int i = 0; i < 5; i++) {

             JsonElement j = today.get("timetable").getAsJsonObject().get(String.valueOf(i+1));
             if (j != null) {
                 periods[i] = new Period(j.getAsJsonObject());
             }
             else {
                 JsonObject b = new JsonObject();
                 b.addProperty("fullName", "Free Period");
                 b.addProperty("room", "N/A");
                 b.addProperty("fullTeacher", "Nobody");
                 b.addProperty("changed", false);
                 periods[i] = new Period(b);
             }
         }
        INSTANCE = this;
    }

    public String getDate() {
        return this.today.get("date").getAsString();
    }

    public Period getPeriod(int num) {
        return periods[num-1];
    }

    @Override
    public String toString() {
        return this.today.toString();
    }

    public static class Period {
        private final JsonObject period;
        public Period(JsonObject obj) {
            this.period = obj;
        }

        public boolean changed() {
            return period.get("changed").getAsBoolean();
        }

        public String fullTeacher() {
            if (this.changed() && period.get("hasCasual").getAsBoolean()) {
                return period.get("casualDisplay").getAsString().trim();
            }
            return period.get("fullTeacher").getAsString();
        }

        public String room() {
            if (period.has("roomFrom")) {
                return period.get("roomTo").getAsString();
            }
            return period.get("room").getAsString();
        }

        public String name() {
            return period.get("fullName").getAsString();
        }

        @Override
        public String toString() {
            return period.toString();
        }

    }
}
