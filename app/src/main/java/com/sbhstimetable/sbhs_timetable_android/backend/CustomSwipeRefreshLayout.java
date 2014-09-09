package com.sbhstimetable.sbhs_timetable_android.backend;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    @Override
    public boolean canChildScrollUp() {
        return false;
    }

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
