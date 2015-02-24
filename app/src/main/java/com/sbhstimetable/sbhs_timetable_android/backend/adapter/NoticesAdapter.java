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

package com.sbhstimetable.sbhs_timetable_android.backend.adapter;

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
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesJson;

import java.util.ArrayList;
import java.util.List;

public class NoticesAdapter implements ListAdapter, AdapterView.OnItemSelectedListener {
    private NoticesJson noticesJson;
    private ArrayList<NoticesJson.Notice> notices;
    private NoticesJson.Year filter = null;
    private List<DataSetObserver> dsos = new ArrayList<DataSetObserver>();
	private static final String[] years = new String[]{
			"All notices",
			"Year 12",
			"Year 11",
			"Year 10",
			"Year 9",
			"Year 8",
			"Year 7"
	};
	private FrameLayout theFilterSelector;
	int curIndex = 0;

    public NoticesAdapter(NoticesJson n) {
        this.noticesJson = n;
        this.notices = n.getNotices();
    }

    public void update(NoticesJson n) {
        this.noticesJson = n;
        this.notices = n.getNotices();
        this.notifyDSOs();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        this.dsos.add(dataSetObserver);
    }

    private void notifyDSOs() {
        for (DataSetObserver i : dsos) {
            i.onChanged();
        }
    }

    public void filter(NoticesJson.Year year) {
		Log.i("notices", "filter to year " + year);
        this.filter = year;
        if (year == null) {
			this.notices = this.noticesJson.getNotices();
		}
		else {
			this.notices = this.noticesJson.getNoticesForYear(year);
		}
        this.notifyDSOs();
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        this.dsos.remove(dataSetObserver);
    }

    @Override
    public int getCount() {
        return notices.size() == 0 ? 1 : notices.size() + 1;
    }

    @Override
    public Object getItem(int i) {
        return i == 0 ? "Spinner" : notices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        if (notices.size() == 0) {
            TextView res = new TextView(viewGroup.getContext());
            res.setTextAppearance(viewGroup.getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
            res.setGravity(Gravity.CENTER);
            res.setText("There are no notices!");
            return res;
        }
		else if (i == 0) {
			Log.i("notices", "getting i = 0");
			if (this.theFilterSelector != null) {
				Log.i("notices", "yay cache");
				Spinner s = (Spinner)theFilterSelector.findViewById(R.id.spinner);
				//this.filter(NoticesJson.Year.fromString())
				//this.onItemSelected(s, null, this.curIndex, 0);
				//s.setOnItemSelectedListener(this);
				curIndex = s.getSelectedItemPosition();
				this.filter(curIndex == 0 ? null : NoticesJson.Year.fromString(this.years[curIndex]));
				return theFilterSelector;
			}
			Log.i("notices", "no cache :(");

			FrameLayout f = (FrameLayout)((LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_listview_spinner, null);
			ArrayAdapter<String> a = new ArrayAdapter<String>(viewGroup.getContext(), R.layout.textview, years);
			Spinner s = (Spinner)f.findViewById(R.id.spinner);
			s.setAdapter(a);
			s.setSelection(this.curIndex);
			s.setOnItemSelectedListener(this);
			//s.setOnItemSelectedListener(this);
			this.theFilterSelector = f;
			return f;
		}
        NoticesJson.Notice n = this.notices.get(i-1);
        View res;
        if (view instanceof FrameLayout && view.findViewById(R.id.notice_title) instanceof TextView) {
            res = view;
        } else {
            res = ((LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.notice_info_view, null);
        }
        TextView v = (TextView)res.findViewById(R.id.notice_body);
        CharSequence s = n.getTextViewNoticeContents();
        if (n.isMeeting()) {
            String meetingDate = n.getMeetingDate();
            String meetingTime = n.getMeetingTime();
            String meetingPlace = n.getMeetingPlace();
            String toSpan = "<span><strong>Meeting Date:</strong> " + meetingDate + " at " + meetingTime + "<br />";
            toSpan += "<span><strong>Meeting Place:</strong> " + meetingPlace + "<br />";
            Spanned s2 = Html.fromHtml(toSpan);
            s = TextUtils.concat(s2, s);
        }
        v.setText(s);
        TextView title = (TextView)res.findViewById(R.id.notice_title);
        title.setText(n.getNoticeTitle());
        TextView author = (TextView)res.findViewById(R.id.notice_author);
        author.setText(n.getNoticeAuthor());
        TextView targ = (TextView)res.findViewById(R.id.notice_target);
        targ.setText("("+n.getNoticeTarget()+")");
        return res;
    }

    @Override
    public int getItemViewType(int i) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
		curIndex = i;
		this.filter(curIndex == 0 ? null : NoticesJson.Year.fromString(this.years[curIndex]));
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
	}
}
