package com.sbhstimetable.sbhs_timetable_android.backend.json;

import android.text.Html;
import android.text.Spanned;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class NoticesJson {
    private JsonObject notices;
    public NoticesJson(JsonObject obj) {
        this.notices = obj;
    }

    public static class NoticeList {
        private JsonArray notices;
        private int level;
        public NoticeList(JsonArray ary, int importance) {
            this.notices = ary;
            this.level = importance;
        }

        public int length() {
            return notices.size();
        }

        public Notice get(int i) {
            if (i < length()) {
                return new Notice(this.notices.get(i).getAsJsonObject());
            }
            else {
                throw new ArrayIndexOutOfBoundsException("Nope");
            }
        }
    }

    public static class Notice {
        private JsonObject notice;
        private List<Year> years;
        public Notice(JsonObject obj) {
            this.notice = obj;
            JsonArray a = this.notice.get("years").getAsJsonArray();
            years = new ArrayList<Year>(a.size());
            for (int i = 0; i < a.size(); i++) {
                years.add(i, Year.fromString(a.get(i).getAsString()));
            }

        }

        public boolean isForYear(Year y) {
            return years.contains(y);
        }

        public Spanned getTextViewNoticeContents() {
            return Html.fromHtml(this.notice.get("text").getAsString().replace("<p>", "").replace("</p>", "<br />"));
        }

        public String getNoticeTitle() {
            return this.notice.get("title").getAsString();
        }

        public String getNoticeAuthor() {
            return this.notice.get("author").getAsString();
        }

        public String getNoticeTarget() {
            return this.notice.get("dTarget").getAsString();
        }
    }

    public enum Year {
        SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), ELEVEN("11"), TWELVE("12"), STAFF("Staff");
        private String ident;
        private Year(String s) {
            this.ident = s;
        }
        public String toString() {
            return this.ident;
        }

        public static Year fromString(String s) {
            if (!s.startsWith("Staff")) {
                int i = Integer.valueOf(s);
                switch (i) {
                    case 7:
                        return SEVEN;
                    case 8:
                        return EIGHT;
                    case 9:
                        return NINE;
                    case 10:
                        return TEN;
                    case 11:
                        return ELEVEN;
                    case 12:
                        return TWELVE;
                    default:
                        throw new IllegalArgumentException("Must be a number 7-12 or \"Staff\"");
                }
            }
            return STAFF;
        }
    }
}
