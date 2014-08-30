package com.sbhstimetable.sbhs_timetable_android;

import android.database.DataSetObserver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;
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
        Log.i("jsonadapter", "get view" + key);
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
        TextView view;
        if (oldView instanceof TextView) {
            view = (TextView)oldView;
        }
        else {
            view = new TextView(viewGroup.getContext());
        }
        JSONObject b = this.getEntry(i);
        view.setText((String)b.get("fullName"));
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
