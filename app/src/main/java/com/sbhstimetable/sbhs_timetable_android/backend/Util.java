package com.sbhstimetable.sbhs_timetable_android.backend;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by simon on 3/09/2014.
 */
public class Util {
    public static RelativeLayout generateSubtextView(ViewGroup viewGroup) {
        RelativeLayout layout;
        TextView header;
        TextView subtitle;
        ImageView changed;

        layout = new RelativeLayout(viewGroup.getContext());
        layout.setMinimumHeight(90);
        layout.setPadding(20, 0, 20, 0);

        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams p1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams p2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams p3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(p);

        header = new TextView(viewGroup.getContext());
        header.setTag("header");
        header.setId(Compat.getViewId());
        header.setTextAppearance(viewGroup.getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
        header.setGravity(Gravity.TOP);

        subtitle = new TextView(viewGroup.getContext());
        subtitle.setTag("subtitle");
        subtitle.setId(Compat.getViewId());
        p2.addRule(RelativeLayout.BELOW, header.getId());
        subtitle.setTextAppearance(viewGroup.getContext(), android.R.style.TextAppearance_DeviceDefault_Small);

        changed = new ImageView(viewGroup.getContext());
        changed.setTag("changed");
        changed.setImageResource(android.R.drawable.ic_dialog_alert);
        changed.setVisibility(View.INVISIBLE);
        p3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        p3.addRule(RelativeLayout.CENTER_VERTICAL);

        layout.addView(header, p1);
        layout.addView(subtitle, p2);
        layout.addView(changed, p3);
        return layout;
    }
}
