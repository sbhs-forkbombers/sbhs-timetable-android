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

package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.JsonUtil;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;


public class ClassInfoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_info);
        Intent i = this.getIntent();
        if (!i.hasExtra("json")) {
            throw new IllegalStateException("RUDE I NEED SOME DATA PLOX");
        }
        String json = i.getStringExtra("json");
        TodayJson.Period b = new TodayJson.Period(JsonUtil.safelyParseJson(json), true); // TODO
        TextView subject = (TextView)this.findViewById(R.id.classInfoSubject);
        subject.setText(b.name());
        //subject.setMinimumHeight(50);
        TextView room = (TextView)this.findViewById(R.id.classInfoRoom);
        room.setText(b.room());

        TextView teacher = (TextView)this.findViewById(R.id.classInfoTeacher);
        teacher.setText(b.fullTeacher());

        if (b.roomChanged()) {
            room.setTextColor(getResources().getColor(R.color.standout));
        }
        if (b.teacherChanged()) {
            teacher.setTextColor(getResources().getColor(R.color.standout));
        }
    }
}
