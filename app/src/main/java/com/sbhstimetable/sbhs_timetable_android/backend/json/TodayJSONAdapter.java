package com.sbhstimetable.sbhs_timetable_android.backend.json;

import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.ClassInfoActivity;
import com.sbhstimetable.sbhs_timetable_android.R;

public class TodayJSONAdapter implements ListAdapter{
    private TodayJson timetable;
    public TodayJSONAdapter(TodayJson tt) {
        this.timetable = tt;
    }

    private TodayJson.Period getEntry(int i) {
       return timetable.getPeriod(i+1);
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
