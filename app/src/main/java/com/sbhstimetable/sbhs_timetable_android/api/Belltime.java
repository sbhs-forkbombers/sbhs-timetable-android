package com.sbhstimetable.sbhs_timetable_android.api;

import org.joda.time.DateTime;

public interface Belltime {
	public String getBellName();
	public DateTime getBellTime();
	public Belltime getNextBellTime();
	public boolean isPeriodStart();
	public boolean isNextBellPeriod();
	public int getPeriodNumber();

}
