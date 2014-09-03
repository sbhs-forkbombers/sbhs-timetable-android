package com.sbhstimetable.sbhs_timetable_android;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.backend.Compat;
import com.sbhstimetable.sbhs_timetable_android.backend.TodayJson;
import com.sbhstimetable.sbhs_timetable_android.backend.Util;

/**
 * Created by simon on 30/08/2014.
 */
public class TodayJSONAdapter implements ListAdapter{
    private TodayJson timetable;
    public TodayJSONAdapter(TodayJson tt) {
        this.timetable = tt;
    }

    private TodayJson.Period getEntry(int i) {
       return timetable.getPeriod(i);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        // TODO
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        // TODO
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
        final TextView subtitle;
        final ImageView changed;
        if (oldView instanceof RelativeLayout) {
            view = (RelativeLayout)oldView;
        }
        else {
            view = Util.generateSubtextView(viewGroup);
        }
        header = (TextView)view.findViewWithTag("header");
        subtitle = (TextView)view.findViewWithTag("subtitle");
        changed = (ImageView)view.findViewWithTag("changed");

        final TodayJson.Period b = this.getEntry(i);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ClassInfoActivity.class);
                i.putExtra("json", ""+b);
                view.getContext().startActivity(i);
            }
        });
        String room = b.room();
        String teacher = b.fullTeacher();
        if (b.changed()) {
            // variations!
            changed.setVisibility(View.VISIBLE);
        }
        header.setText(b.name());
        subtitle.setText("in " + room + " with " + teacher);
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
