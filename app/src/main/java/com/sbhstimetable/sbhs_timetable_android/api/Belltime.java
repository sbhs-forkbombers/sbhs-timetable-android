package com.sbhstimetable.sbhs_timetable_android.api;

import org.joda.time.DateTime;

public interface Belltime {
	String getBellName();
	DateTime getBellTime();
	Belltime getNextBellTime();
	boolean isPeriodStart();
	boolean isNextBellPeriod();
	int getPeriodNumber();

}
