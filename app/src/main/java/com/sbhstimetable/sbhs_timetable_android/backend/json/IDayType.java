/*
 * SBHS-Timetable-Android: Countdown and timetable all at once (Android app).
 * Copyright (C) 2014 Simon Shields, James Ye
 *
 * This file is part of SBHS-Timetable-Android.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
