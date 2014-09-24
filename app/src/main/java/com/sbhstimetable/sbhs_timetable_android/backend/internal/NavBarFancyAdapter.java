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

package com.sbhstimetable.sbhs_timetable_android.backend.internal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;

import java.util.List;

public class NavBarFancyAdapter<T> extends ArrayAdapter<T> {
    public NavBarFancyAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NavBarFancyAdapter.DrawerEntry e = (NavBarFancyAdapter.DrawerEntry)this.getItem(position);
        if (convertView instanceof RelativeLayout && convertView.findViewById(R.id.navbar_label) != null) {
            return e.getView(convertView);
        }
        return e.getView();
    }

    public static class DrawerEntry {
        private int drawableID;
        private String name;
        private Context c;
        public DrawerEntry(int drawable, String name, Context c) {
            this.c = c;
            this.name = name;
            this.drawableID = drawable;
        }

        public View getView() {
            View res = LayoutInflater.from(c).inflate(R.layout.layout_navbar_entry,null);
            return this.getView(res);
        }

        public View getView(View res) {
            ImageView v = (ImageView)res.findViewById(R.id.navbar_icon);
            v.setImageResource(this.drawableID);
            TextView t = (TextView)res.findViewById(R.id.navbar_label);
            t.setText(name);
            return res;
        }
    }
}
