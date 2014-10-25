package com.sbhstimetable.sbhs_timetable_android.backend.json;

/**
 * A generic type used by TodayAdapter to access days
 */
public interface IDayType {
	public String getDayName();

	public IPeriod getPeriod(int num);


	public interface IPeriod {
		public String room();
		public boolean roomChanged();

		public String teacher();
		public boolean teacherChanged();
		public String getShortTeacher();

		public String name();
		public String getShortName();

		public boolean showVariations();

		public boolean changed();
	}

}
