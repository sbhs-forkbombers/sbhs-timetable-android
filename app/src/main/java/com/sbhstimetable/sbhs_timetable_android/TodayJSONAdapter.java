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

/**
 * Created by simon on 30/08/2014.
 */
public class TodayJSONAdapter implements ListAdapter{
    private JsonObject timetable;
    public TodayJSONAdapter(JsonObject tt) {
        this.timetable = tt;
    }

    private JsonObject getEntry(int i) {
        String key = String.valueOf(i+1);
        JsonElement c = timetable.get(key);
        if (c != null) {
            return c.getAsJsonObject();
        }
        else {
            JsonObject b = new JsonObject();
            b.addProperty("fullName", "Free Period");
            b.addProperty("room", "N/A");
            b.addProperty("fullTeacher", "Nobody");
            b.addProperty("changed", false);
            return b;
        }
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
            header = (TextView)view.findViewWithTag("header");
            subtitle = (TextView)view.findViewWithTag("subtitle");
            changed = (ImageView)view.findViewWithTag("changed");
        }
        else {
            view = new RelativeLayout(viewGroup.getContext());
            RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            view.setMinimumHeight(90);
            RelativeLayout.LayoutParams p1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams p3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(p);
            header = new TextView(viewGroup.getContext());
            header.setTag("header");
            header.setId(Compat.getViewId());
            //header.setHeight(15);
            Context context = viewGroup.getContext();
            header.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Large);
            header.setGravity(Gravity.TOP);
            subtitle = new TextView(viewGroup.getContext());
            subtitle.setTag("subtitle");
            subtitle.setId(Compat.getViewId());
            p2.addRule(RelativeLayout.BELOW, header.getId());
            subtitle.setTextAppearance(context, android.R.style.TextAppearance_DeviceDefault_Small);
            view.setPadding(20, 0, 20, 0);
            view.addView(header, p1);
            view.addView(subtitle,p2);

            changed = new ImageView(viewGroup.getContext());
            changed.setTag("changed");
            //changed.setPadding(0, 5, 0, 0);
            changed.setImageResource(android.R.drawable.ic_dialog_alert);
            changed.setVisibility(View.INVISIBLE);
            p3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            p3.addRule(RelativeLayout.CENTER_VERTICAL);
            view.addView(changed, p3);

        }
        final JsonObject b = this.getEntry(i);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), ClassInfoActivity.class);
                i.putExtra("json", ""+b);
                view.getContext().startActivity(i);
            }
        });
        String room = b.get("room").getAsString();
        String teacher = b.get("fullTeacher").getAsString();
        if (b.get("changed").getAsBoolean()) {
            // variations!
            changed.setVisibility(View.VISIBLE);
            if (b.get("hasCasual").getAsBoolean()) {
                teacher = b.get("casualDisplay").getAsString().trim();
            }
        }
        header.setText(b.get("fullName").getAsString());
        subtitle.setText("in " + b.get("room").getAsString() + " with " + teacher);
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
