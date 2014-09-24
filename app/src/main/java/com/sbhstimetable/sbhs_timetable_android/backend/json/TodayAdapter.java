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

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;

import java.util.ArrayList;
import java.util.List;

public class TodayAdapter implements ListAdapter{
    private TodayJson timetable;
    private List<DataSetObserver> dsos = new ArrayList<DataSetObserver>();
    public TodayAdapter(TodayJson tt) {
        this.timetable = tt;
    }

    private TodayJson.Period getEntry(int i) {
       return timetable.getPeriod(i+1);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        this.dsos.add(dataSetObserver);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        this.dsos.remove(dataSetObserver);
    }

    public void updateDataSet(TodayJson newValue) {
        this.timetable = newValue;
        for (DataSetObserver i : dsos) {
            i.onChanged();
        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public Object getItem(int i) {
        return this.getEntry(i);
    }

    @Override
    public long getItemId(int i) {
        return (long)i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View oldView, ViewGroup viewGroup) {
        final RelativeLayout view;
        final TextView header;
        final TextView roomText;
        final TextView teacherText;
        final ImageView changed;
        if (oldView instanceof RelativeLayout) {
            view = (RelativeLayout)oldView;
        }
        else {
            view = (RelativeLayout)LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_timetable_classinfo, null);
        }
        header = (TextView)view.findViewById(R.id.timetable_class_header);
        roomText = (TextView)view.findViewById(R.id.timetable_class_room);
        teacherText = (TextView)view.findViewById(R.id.timetable_class_teacher);
        changed = (ImageView)view.findViewById(R.id.timetable_class_changed);

        final TodayJson.Period b = this.getEntry(i);
        /*view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ClassInfoActivity.class);
                i.putExtra("json", ""+b);
                view.getContext().startActivity(i);
            }
        });*/
        String room = b.room();
        String teacher = b.fullTeacher();
        roomText.setTextColor(viewGroup.getResources().getColor(android.R.color.primary_text_dark));
        teacherText.setTextColor(viewGroup.getResources().getColor(android.R.color.primary_text_dark));
        if (b.changed()) {
            // variations!
            changed.setVisibility(View.VISIBLE);
            if (b.roomChanged()) {
                roomText.setTextColor(viewGroup.getResources().getColor(R.color.standout));
            }

            if (b.teacherChanged()) {
                teacherText.setTextColor(viewGroup.getResources().getColor(R.color.standout));
            }

        }
        else {
            changed.setVisibility(View.INVISIBLE);
        }
        header.setText(b.name());
        roomText.setText(room);
        teacherText.setText(teacher);
        //view.setText((String)b.get("fullName"));

        return view;
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
