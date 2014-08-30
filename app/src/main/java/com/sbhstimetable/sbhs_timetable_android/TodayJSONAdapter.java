package com.sbhstimetable.sbhs_timetable_android;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.simple.JSONObject;

/**
 * Created by simon on 30/08/2014.
 */
public class TodayJSONAdapter implements ListAdapter{
    private JSONObject timetable;
    public TodayJSONAdapter(JSONObject tt) {
        this.timetable = tt;
    }

    private JSONObject getEntry(int i) {
        String key = String.valueOf(i+1);
        return (JSONObject)timetable.get(key);
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
        RelativeLayout view;
        TextView header;
        TextView subtitle;
        if (oldView instanceof RelativeLayout) {
            view = (RelativeLayout)oldView;
            header = (TextView)view.findViewWithTag("header");
            subtitle = (TextView)view.findViewWithTag("subtitle");
        }
        else {
            view = new RelativeLayout(viewGroup.getContext());
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            view.setMinimumHeight(100);
            RelativeLayout.LayoutParams p1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(p);
            header = new TextView(viewGroup.getContext());
            header.setTag("header");
            //header.setHeight(15);
            Context context = viewGroup.getContext();
            header.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Large);
            header.setGravity(Gravity.TOP);
            subtitle = new TextView(viewGroup.getContext());
            subtitle.setTag("subtitle");
            p2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            subtitle.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);
            view.addView(header, p1);
            view.addView(subtitle,p2) ;

        }
        JSONObject b = this.getEntry(i);
        header.setText((String)b.get("fullName"));
        subtitle.setText("in " + (String)b.get("room") + " with " + (String)b.get("fullTeacher"));

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
