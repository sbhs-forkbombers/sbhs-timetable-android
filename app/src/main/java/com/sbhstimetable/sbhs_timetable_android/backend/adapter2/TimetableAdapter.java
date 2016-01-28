/*
 * SBHS-Timetable-Android: Countdown and timetable all at once (Android app).
 * Copyright (C) 2015 Simon Shields, James Ye
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
package com.sbhstimetable.sbhs_timetable_android.backend.adapter2;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.FullCycleWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.Lesson;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.ThemeHelper;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.ArrayList;
import java.util.List;

public class TimetableAdapter extends DataSetObserver implements ListAdapter, AdapterView.OnItemSelectedListener {
	private FullCycleWrapper cycle;
	private int currentIndex;
	private FrameLayout theFilterSelector;
	private List<DataSetObserver> watchers = new ArrayList<>();

    private GestureDetectorCompat gesture;

	private int curDayIndex;
	private int curWeekIndex;

	private String[] days = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
	private String[] weeks = new String[] {"A", "B", "C"};

	public TimetableAdapter(Context c) {
		cycle = new FullCycleWrapper(c);
		cycle.addDataSetObserver(this);
		currentIndex = cycle.getCurrentDayInCycle();
        gesture = new GestureDetectorCompat(c, new MyGestureListener());
		int tmp = currentIndex;
		//Log.i("TimetableAdapter", "curIndex = " + currentIndex + ", %5 = " + (tmp % 5) + ", / 5 = " + Math.floor(tmp / 5));
		curDayIndex = (tmp % 5);
		curWeekIndex = (int)Math.floor(tmp / 5);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver dso) {
		this.watchers.add(dso);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver dso) {
		this.watchers.remove(dso);
	}

	private void notifyDSOs() {
		for (DataSetObserver i : this.watchers) {
			i.onInvalidated();
		}
	}

	@Override
	public int getCount() {
		return (cycle.hasFullTimetable() ? 7 : 1);
	}

	@Override
	public Object getItem(int index) {
		if (index == 0 && (!cycle.hasFullTimetable() || cycle.getDayNumber(currentIndex) == null)) {
			return "Loading view";
		} else if (index == 7) {
			return "Last updated view";
		} else if (index == 0) {
			return "AdapterView";
		}
		return cycle.getDayNumber(currentIndex).getPeriod(index);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

    private View inflateLayout(int id, ViewGroup parent, boolean b) {
        return ((LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(id, parent, b);
    }

	@Override
	public View getView(int i, View convertView, ViewGroup parent) {
		if (i == 0) {
            if (!cycle.hasFullTimetable() || cycle.getDayNumber(currentIndex) == null) {
                return inflateLayout(R.layout.view_list_loading, parent, false);
            } else {
                if (this.theFilterSelector != null) {
                    Spinner s = (Spinner)theFilterSelector.findViewById(R.id.spinner_day);
                    this.onItemSelected(s, null, s.getSelectedItemPosition(), 0);
                    s.setOnItemSelectedListener(this);
                    s = (Spinner)theFilterSelector.findViewById(R.id.spinner_week);
                    this.onItemSelected(s, null, s.getSelectedItemPosition(), 0);
                    s.setOnItemSelectedListener(this);
                    return theFilterSelector;
                }
                FrameLayout f = (FrameLayout)inflateLayout(R.layout.layout_today_spinner, parent, false);
                Spinner s = (Spinner)f.findViewById(R.id.spinner_day);
                ArrayAdapter<String> a = new ArrayAdapter<>(parent.getContext(), R.layout.textview, days);
                s.setAdapter(a);
                s.setSelection(this.curDayIndex);
                s.setOnItemSelectedListener(this);
                s = (Spinner)f.findViewById(R.id.spinner_week);
                a = new ArrayAdapter<>(parent.getContext(), R.layout.textview, weeks);
                s.setAdapter(a);
                s.setSelection(this.curWeekIndex);
                s.setOnItemSelectedListener(this);
                this.theFilterSelector = f;
                return f;
            }
        } else if (i == 6) {
			View v = inflateLayout(R.layout.layout_last_updated, parent, false);
			TextView t = (TextView)v.findViewById(R.id.last_updated);
			DateTimeFormatter f = new DateTimeFormatterBuilder().appendDayOfWeekShortText().appendLiteral(' ').appendDayOfMonth(2).appendLiteral(' ')
					.appendMonthOfYearShortText().appendLiteral(' ').appendYear(4,4).appendLiteral(' ').appendHourOfDay(2)
					.appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':').appendSecondOfMinute(2).toFormatter();
			t.setText(f.print(this.cycle.getFetchTime(this.cycle.getCurrentDayInCycle()).toLocalDateTime()));
			return v;
		}
		final FrameLayout view;
		final TextView header;
		final TextView roomText;
		final TextView teacherText;
		final ImageView changed;
		if (convertView instanceof FrameLayout && convertView.findViewById(R.id.timetable_class_header) != null) {
			view = (FrameLayout)convertView;
		} else {
			view = (FrameLayout)inflateLayout(R.layout.layout_timetable_classinfo, parent, false);
		}
		header = (TextView)view.findViewById(R.id.timetable_class_header);
		roomText = (TextView)view.findViewById(R.id.timetable_class_room);
		teacherText = (TextView)view.findViewById(R.id.timetable_class_teacher);
		changed = (ImageView)view.findViewById(R.id.timetable_class_changed);

		final Lesson l = this.cycle.getDayNumber(currentIndex).getPeriod(i);

		header.setText(l.getSubject());
		roomText.setText(l.getRoom());
		teacherText.setText(l.getTeacher());
		if (!l.roomChanged() && !l.teacherChanged() && !l.cancelled()) {
			changed.setVisibility(View.INVISIBLE);
		} else {
			changed.setVisibility(View.VISIBLE);
		}
		int colour = ContextCompat.getColor(roomText.getContext(), R.color.standout);
		if (l.cancelled()) {
			roomText.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
			teacherText.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

		} else {
            roomText.setPaintFlags(0);
            teacherText.setPaintFlags(0);
        }

		if (l.teacherChanged() || l.cancelled()) {
			teacherText.setTextColor(colour);
		} else {
            teacherText.setTextColor(ThemeHelper.getTextColor());
        }

		if (l.roomChanged() || l.cancelled()) {
			roomText.setTextColor(colour);
		} else {
            roomText.setTextColor(ThemeHelper.getTextColor());
        }
		return attachSwipeListener(view);
	}

    private View attachSwipeListener(View v) {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });
        return v;
    }

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}


	/* spinner stuff */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getId() == R.id.spinner_week) {
			if (position == this.curWeekIndex) return;
			this.curWeekIndex = position;
			this.currentIndex = (5*position) + this.curDayIndex;
		} else if (parent.getId() == R.id.spinner_day) {
			if (position == this.curDayIndex) return;
			this.curDayIndex = position;
			this.currentIndex = (5 * this.curWeekIndex) + position;
		}
		//Log.i("TimetableAdapter", "new position wk " + this.curWeekIndex + " day " + this.curDayIndex + " => " + this.currentIndex);
		this.notifyDSOs();
	}

    public GestureDetectorCompat getGestureListener() {
        return gesture;
    }

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onChanged() {
		super.onChanged();
		this.notifyDSOs();
	}

	@Override
	public void onInvalidated() {
		onChanged();
	}

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String TAG = "MEMES";
        boolean isNewGesture = true;
        boolean hasCompletedScroll = false;
        static final int MIN_DISTANCE = 50;
        static final int MAX_DELTA_Y = 300;
        @Override
        public boolean onDown(MotionEvent e) {
            isNewGesture = true;
            hasCompletedScroll = false;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent start, MotionEvent end, float deltaX, float deltaY) {
            if (!isNewGesture && hasCompletedScroll) {
                return true;
            }
            isNewGesture = false;
            try {
                if (Math.abs(start.getY() - end.getY()) > MAX_DELTA_Y) {
                    return false;
                }
                if (start.getX() - end.getX() > MIN_DISTANCE) {
                    // gone left
                    hasCompletedScroll = true;
                    currentIndex--;
                    currentIndex = (currentIndex < 0 ? 15 + currentIndex : currentIndex);
                    int day = currentIndex % 5;
                    int wk = (int)Math.floor(currentIndex / 5);
                    ((Spinner)theFilterSelector.findViewById(R.id.spinner_day)).setSelection(day);
                    ((Spinner)theFilterSelector.findViewById(R.id.spinner_week)).setSelection(wk);
                    return true;
                } else if (end.getX() - start.getX() > MIN_DISTANCE) {
                    // gone right
                    hasCompletedScroll = true;
                    currentIndex++;
                    currentIndex = currentIndex % 15;
                    int day = currentIndex % 5;
                    int wk = (int)Math.floor(currentIndex / 5);
                    ((Spinner)theFilterSelector.findViewById(R.id.spinner_day)).setSelection(day);
                    ((Spinner)theFilterSelector.findViewById(R.id.spinner_week)).setSelection(wk);
                    return true;
                }
            } catch (Exception e) {
                // meh
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent start, MotionEvent end, float velocityX, float velocityY) {
            if (!isNewGesture && hasCompletedScroll) {
                return true;
            }
            isNewGesture = false;
            try {
                if (Math.abs(start.getY() - end.getY()) > MAX_DELTA_Y) {
                    return false;
                }
                if (start.getX() - end.getX() > MIN_DISTANCE) {
                    // gone left
                    hasCompletedScroll = true;
                    currentIndex--;
                    currentIndex = (currentIndex < 0 ? 15 + currentIndex : currentIndex);
                    int day = currentIndex % 5;
                    int wk = (int)Math.floor(currentIndex / 5);
                    ((Spinner)theFilterSelector.findViewById(R.id.spinner_day)).setSelection(day);
                    ((Spinner)theFilterSelector.findViewById(R.id.spinner_week)).setSelection(wk);
                    hasCompletedScroll = true;
                    return true;
                } else if (end.getX() - start.getX() > MIN_DISTANCE) {
                    // gone right
                    hasCompletedScroll = true;
                    currentIndex++;
                    currentIndex = currentIndex % 15;
                    int day = currentIndex % 5;
                    int wk = (int)Math.floor(currentIndex / 5);
                    ((Spinner)theFilterSelector.findViewById(R.id.spinner_day)).setSelection(day);
                    ((Spinner)theFilterSelector.findViewById(R.id.spinner_week)).setSelection(wk);
                    hasCompletedScroll = true;
                    return true;
                }
            } catch (Exception e) {

            }
            return false;
        }
    }
}