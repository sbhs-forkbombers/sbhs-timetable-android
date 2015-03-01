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
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;
import com.sbhstimetable.sbhs_timetable_android.api.ApiWrapper;
import com.sbhstimetable.sbhs_timetable_android.api.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.api.gson.Notices;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesJson;
import com.sbhstimetable.sbhs_timetable_android.event.NoticesEvent;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.ArrayList;
import java.util.List;

public class NoticesAdapter implements ListAdapter, AdapterView.OnItemSelectedListener {
	private StorageCache cache;
	private Notices notices;
	private List<DataSetObserver> dsos = new ArrayList<>();
	private EventListener eventListener;
	private int curIndex;
	private FrameLayout theFilterSelector;

	private static final String[] years = new String[] {"All Notices", "Year 7", "Year 8", "Year 9", "Year 10", "Year 11", "Year 12", "Staff"};

	public NoticesAdapter(Context c) {
		this.cache = new StorageCache(c);
		this.notices = cache.loadNotices();
		this.eventListener = new EventListener();
		ApiWrapper.getEventBus().register(this.eventListener);
		if (this.notices == null) {
			ApiWrapper.requestNotices(c);
		}
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
	public void registerDataSetObserver(DataSetObserver observer) {
		this.dsos.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		this.dsos.remove(observer);
	}

	private void notifyDSOs() {
		for (DataSetObserver i : dsos) {
			i.onChanged();
		}
	}

	private void filter(String year) {
		if (year == null || year.equals(years[0])) {
			this.notices.filterToYear(null);
		} else {
			this.notices.filterToYear(year.replace("Year ", ""));
		}
		this.notifyDSOs();
	}

	@Override
	public int getCount() {
		return notices.getNumberOfNotices()+2;
	}

	@Override
	public Object getItem(int position) {
		if (position == 0) {
			return "Selector thingy";
		}
		return notices.getNoticeAtIndex(position);
	}

	@Override
	public long getItemId(int position) {
		return notices.getNoticeAtIndex(position).getID();
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (notices.getNumberOfNotices() == 0 && position == 0) {
			TextView res = new TextView(parent.getContext());
			res.setTextAppearance(parent.getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
			res.setGravity(Gravity.CENTER);
			res.setText("There are no notices!");
			return res;
		} else if (position == 0) {
			if (this.theFilterSelector != null) {
				Spinner s = (Spinner)theFilterSelector.findViewById(R.id.spinner);
				curIndex = s.getSelectedItemPosition();
				this.filter(curIndex == 0 ? null : this.years[curIndex]);
				return theFilterSelector;
			}

			FrameLayout f = (FrameLayout)((LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_listview_spinner, null);
			ArrayAdapter<String> a = new ArrayAdapter<String>(parent.getContext(), R.layout.textview, years);
			Spinner s = (Spinner)f.findViewById(R.id.spinner);
			s.setAdapter(a);
			s.setSelection(this.curIndex);
			s.setOnItemSelectedListener(this);
			this.theFilterSelector = f;
			return f;
		} else if (position == getCount() -1 ) {
			View v = ((LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_last_updated, null);
			TextView t = (TextView)v.findViewById(R.id.last_updated);
			DateTimeFormatter f = new DateTimeFormatterBuilder().appendDayOfWeekShortText().appendLiteral(' ').appendDayOfMonth(2).appendLiteral(' ')
					.appendMonthOfYearShortText().appendLiteral(' ').appendYear(4,4).appendLiteral(' ').appendHourOfDay(2)
					.appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':').appendSecondOfMinute(2).toFormatter();
			t.setText(f.print(this.notices.getFetchTime().toLocalDateTime()));
			return v;
		}
		Notices.Notice n = notices.getNoticeAtIndex(position-1);
		View res;
		if (convertView instanceof FrameLayout && convertView.findViewById(R.id.notice_title) instanceof TextView) {
			res = convertView;
		} else {
			res = ((LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.notice_info_view, null);
		}
		TextView v = (TextView)res.findViewById(R.id.notice_body);
		CharSequence s = n.getTextViewNoticeContents();
		if (n.isMeeting()) {
			String meetingDate = n.getMeetingDate();
			String meetingTime = n.getMeetingTime();
			String meetingPlace = n.getMeetingPlace();
			String toSpan = "<span><strong>Meeting Date:</strong> " + meetingDate + " at " + meetingTime + "<br />";
			toSpan += "<span><strong>Meeting Place:</strong> " + meetingPlace + "<br /><br />";
			Spanned s2 = Html.fromHtml(toSpan);
			s = TextUtils.concat(s2, s);
		}
		v.setText(s);
		TextView title = (TextView)res.findViewById(R.id.notice_title);
		title.setText(n.getTitle());
		TextView author = (TextView)res.findViewById(R.id.notice_author);
		author.setText(n.getAuthor());
		TextView targ = (TextView)res.findViewById(R.id.notice_target);
		targ.setText("("+n.getDisplayTarget()+")");
		return res;
	}

	@Override
	public int getItemViewType(int position) {
		return (position == 0 ? 0 : 1);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		curIndex = position;
		this.filter(curIndex == 0 ? null : this.years[curIndex]);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private void updateNotices(Notices n) {
		this.notices = n;
		this.curIndex = 0;
		this.notifyDSOs();
	}

	private class EventListener {
		public void onEvent(NoticesEvent e) {
			if (e.successful()) {
				updateNotices(e.getResponse());
			} else {
				Log.e("NoticesAdapter$EVL", "request failed - " + e.getErrorMessage());
			}
		}
	}
}
