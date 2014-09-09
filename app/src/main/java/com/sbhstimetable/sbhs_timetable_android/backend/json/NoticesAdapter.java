package com.sbhstimetable.sbhs_timetable_android.backend.json;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.R;

public class NoticesAdapter implements ListAdapter {
    private final NoticesJson notices;
    public NoticesAdapter(NoticesJson n) {
        this.notices = n;
        Log.i("noticesAdapter", "size => " + notices.getNotices().size());
    }
    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

    }

    @Override
    public int getCount() {
        return notices.getNotices().size();
    }

    @Override
    public Object getItem(int i) {
        return notices.getNotices().get(i);
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
        NoticesJson.Notice n = this.notices.getNotices().get(i);
        View res = ((LayoutInflater)viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.notice_info_view, null);
        TextView v = (TextView)res.findViewById(R.id.notice_body);
        v.setText(n.getTextViewNoticeContents());
        TextView title = (TextView)res.findViewById(R.id.notice_title);
        title.setText(n.getNoticeTitle());
        TextView author = (TextView)res.findViewById(R.id.notice_author);
        author.setText(n.getNoticeAuthor());
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
