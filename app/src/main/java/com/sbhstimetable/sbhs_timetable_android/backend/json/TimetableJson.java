package com.sbhstimetable.sbhs_timetable_android.backend.json;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TimetableJson {
    private JsonObject json;
	private Day[] days = new Day[15];
	private HashMap<String,Integer> dayNameToNum = new HashMap<String, Integer>();
	private HashMap<String,SubjectInfo> subjs = new HashMap<String, SubjectInfo>();
    public TimetableJson(JsonObject o) {
        this.json = o;

		JsonObject subj = o.get("subjInfo").getAsJsonObject();
		Set<Map.Entry<String,JsonElement>> entries = subj.entrySet();

		for (Map.Entry<String,JsonElement> i : entries) {
			subjs.put(i.getKey(), new SubjectInfo(i.getValue().getAsJsonObject()));
		}

		JsonObject days = o.get("days").getAsJsonObject();
		for (int i = 1; i < 16; i++) {

			this.days[i-1] = new Day(i, days.get(i+"").getAsJsonObject(), this);
			this.dayNameToNum.put(this.days[i-1].getDayName(), i);
		}
    }

	public SubjectInfo getInfoForSubject(String year, String shortName) {
		return this.getInfoForSubject(year + shortName);
	}

	public SubjectInfo getInfoForSubject(String yShortName) {
		return this.subjs.get(yShortName);
	}

	public Day getDayByName(String name) {
		return this.days[this.dayNameToNum.get(name)];
	}

	public int getNumForDay(String name) {
		return this.dayNameToNum.get(name);
	}

	public Day getDayFromNumber(int idx) {
		return this.days[idx - 1];
	}

	public class SubjectInfo {
		private JsonObject json;

		public SubjectInfo(JsonObject data) {
			this.json = data;
		}

		public String getTitle() {
			return this.json.get("title").getAsString();
		}

		public String getShortTitle() {
			return this.json.get("shortTitle").getAsString();
		}

		public String getShortTeacher() {
			return this.json.get("teacher").getAsString();
		}

		public String getSubject() {
			return this.json.get("subject").getAsString();
		}

		public String getTeacher() {
			String full = this.json.get("fullTeacher").getAsString();
			String[] parts = full.split(" ");
			String res = parts[0];
			for (int i = 2; i < parts.length; i++) {
				res += " " + parts[i];
			}
			return res;
		}

		public String getYear() {
			return this.json.get("year").getAsString();
		}
	}

	public class Day implements IDayType {
		private int num;
		private JsonObject json;
		private Period[] periods = new Period[5];
		public Day(int idx, JsonObject o, TimetableJson parent) {
			num = idx;
			json = o;
			JsonObject q = o.get("periods").getAsJsonObject();
			for (int i = 1; i < 6; i++) {
				if (!q.has(i+"")) {
					Log.i("timetable", "!o.has(" + i + "+'') :(");
					JsonObject j = new JsonObject();
					j.addProperty("title", "Free");
					j.addProperty("teacher", "Nobody");
					j.addProperty("room", "N/A");
					periods[i - 1] = new Period(j, i, null);

				}
				else {
					JsonObject p = q.get(i+"").getAsJsonObject();
					Log.i("timetable", p.get("year").getAsString() + p.get("title").getAsString());
					periods[i - 1] = new Period(p, i, parent.getInfoForSubject(p.get("year").getAsString(), p.get("title").getAsString()));
				}
			}
		}

		public Period getPeriod(int idx) {
			return this.periods[idx-1];
		}

		public String getDayName() {
			return this.json.get("dayname").getAsString();
		}
	}

	public class Period implements IDayType.IPeriod {
		private JsonObject json;
		private int num;
		private SubjectInfo myInfo;

		public Period(JsonObject o, int idx, SubjectInfo si) {
			this.num = idx;
			this.json = o;
			this.myInfo = si;
		}

		public String name() {
			if (myInfo != null) {
				return myInfo.getTitle();
			}
			else {
				return "Nothing";
			}
		}

		public boolean teacherChanged() {
			return false;
		}

		public boolean roomChanged() {
			return false;
		}

		public boolean changed() {
			return false;
		}

		public String getShortName() {
			return json.get("title").getAsString();
		}

		public String teacher() {
			if (myInfo != null) {
				return this.myInfo.getTeacher();
			}
			else {
				return "Mr X";
			}
		}
		public String getShortTeacher() {
			return json.get("teacher").getAsString();
		}

		public String room() {
			return json.get("room").getAsString();
		}

		public int getNumber() {
			return num;
		}

		public boolean showVariations() {
			return false;
		}

	}
}
