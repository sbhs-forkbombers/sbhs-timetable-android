package com.sbhstimetable.sbhs_timetable_android.backend.json;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;

public class BelltimesAdapter implements ListAdapter {
    private BelltimesJson b;
    public BelltimesAdapter(BelltimesJson b) {
        this.b = b;
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
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return b.getMaxIndex()-1;
    }

    @Override
    public Object getItem(int i) {
        BelltimesJson.Bell bell = b.getIndex(i);

        return b.getIndex(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        RelativeLayout r;
        if (view instanceof RelativeLayout) {
            r = (RelativeLayout)view;
        }
        else {
            r = (RelativeLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_belltimes_entry, null);
        }
        Integer[] a = b.getIndex(i+1).getBell();
        String e = String.format("%02d:%02d", a);
        BellWithEnd bell = new BellWithEnd(b.getIndex(i),e);
        TextView start = (TextView)r.findViewById(R.id.bell_start);
        TextView end = (TextView)r.findViewById(R.id.bell_end);
        TextView label = (TextView)r.findViewById(R.id.bell_name);
        start.setText(bell.getStart());
        end.setText(bell.getEnd());
        label.setText(bell.getLabel());
        return r;
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

    private class BellWithEnd {
        private String end;
        private BelltimesJson.Bell start;
        public BellWithEnd(BelltimesJson.Bell b, String end) {
            this.end = end;
            this.start = b;
        }

        public String getStart() {
            return String.format("%02d:%02d", this.start.getBell());
        }

        public String getEnd() {
            return this.end;
        }

        public String getLabel() {
            return this.start.getLabel();
        }
    }
}
