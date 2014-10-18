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

import android.content.Context;
import android.database.DataSetObserver;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;

import java.util.ArrayList;
import java.util.List;

public class NoticesAdapter implements ListAdapter {
    private NoticesJson noticesJson;
    private ArrayList<NoticesJson.Notice> notices;
    private NoticesJson.Year filter = null;
    private List<DataSetObserver> dsos = new ArrayList<DataSetObserver>();

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
        this.filter = year;
        this.notices = noticesJson.getNotices();
        if (year != null) {
            ArrayList<NoticesJson.Notice> res = new ArrayList<NoticesJson.Notice>();
            for (NoticesJson.Notice i : notices) {
                if (i.isForYear(year)) {
                    res.add(i);
                }
            }
            this.notices = res;
        }
        this.notifyDSOs();
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        this.dsos.remove(dataSetObserver);
    }

    @Override
    public int getCount() {
        return notices.size() == 0 ? 1 : notices.size();
    }

    @Override
    public Object getItem(int i) {
        return notices.get(i);
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (notices.size() == 0) {
            TextView res = new TextView(viewGroup.getContext());
            res.setTextAppearance(viewGroup.getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
            res.setGravity(Gravity.CENTER);
            res.setText("There are no notices!");
            return res;
        }
        NoticesJson.Notice n = this.notices.get(i);
        View res;
        if (view instanceof FrameLayout && view.findViewById(R.id.notice_title) instanceof TextView) {
            res = view;
        }
        else {
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
}
