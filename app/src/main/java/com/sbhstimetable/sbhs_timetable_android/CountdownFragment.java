package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;

public class CountdownFragment extends Fragment {

    private CommonFragmentInterface mListener;
    private CountDownTimer timeLeft;
    private Menu menu;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TimetableFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CountdownFragment newInstance() {
        CountdownFragment fragment = new CountdownFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public CountdownFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        IntentFilter i = new IntentFilter(TimetableActivity.BELLTIMES_AVAILABLE);
        i.addAction(TimetableActivity.TODAY_AVAILABLE);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(new BroadcastListener(), i);
        //Toast.makeText(getActivity(), "Countdown! School never ends!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final CountdownFragment me = this;
        /*final SwipeRefreshLayout f = (SwipeRefreshLayout)inflater.inflate(R.layout.fragment_countdown, container, false);
        f.setColorSchemeColors(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        f.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ApiAccessor.getNotices(me.getActivity());
                ApiAccessor.getToday(me.getActivity());
                ApiAccessor.getBelltimes(me.getActivity());
            }
        });
        Log.i("countdownFrag", "done");
        f.setRefreshing(true);*/
        return inflater.inflate(R.layout.fragment_countdown, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
        this.mListener.updateCachedStatus(this.menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.updateTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateTimer();
    }

    public void updateTimer() {
        if (this.timeLeft != null) {
            this.timeLeft.cancel();
        }
        final View f = this.getView();
        if (f == null) {
            //Toast.makeText(this.getActivity(),"No view, aborting...", Toast.LENGTH_SHORT).show();
            return;
        }
        RelativeLayout extraData = (RelativeLayout)f.findViewById(R.id.countdown_extraData);
        TextView teacher = (TextView)extraData.findViewById(R.id.countdown_extraData_teacher);
        teacher.setText("…");
        TextView room = (TextView)extraData.findViewById(R.id.countdown_extraData_room);
        room.setText("…");
        TextView subject = (TextView)extraData.findViewById(R.id.countdown_extraData_subject);
        subject.setText("…");
        String label = "Something";
        String connector = "happens in";
        TodayJson.Period p = null;
        if (DateTimeHelper.bells != null) {
            BelltimesJson.Bell next = DateTimeHelper.bells.getNextBell();
            if (next != null) {
                BelltimesJson.Bell now = DateTimeHelper.bells.getIndex(next.getIndex() - 1);
                if (now.isPeriod() && now.getPeriodNumber() < 5) { // in a period, it's not last period.
                    connector = "ends in";
                    if (ApiAccessor.isLoggedIn() && TodayJson.getInstance() != null) {
                        p = TodayJson.getInstance().getPeriod(now.getPeriodNumber());
                        label = p.name();
                        teacher.setText(p.fullTeacher());
                        room.setText(p.room());
                        subject.setText(p.name());
                        extraData.setVisibility(View.VISIBLE);
                    } else {
                        label = now.getLabel();
                        extraData.setVisibility(View.INVISIBLE);
                    }
                } else if (now.isPeriod() && now.getPeriodNumber() == 5) { // last period
                    connector = "in";
                    label = "School ends";
                } else if (now.getIndex() + 1 < DateTimeHelper.bells.getMaxIndex() && DateTimeHelper.bells.getIndex(now.getIndex() + 1).isPeriod()) { // in a break followed by a period - Lunch 2, Recess, Transition.
                    connector = "starts in";
                    if (ApiAccessor.isLoggedIn() && TodayJson.getInstance() != null) {
                        p = TodayJson.getInstance().getPeriod(DateTimeHelper.bells.getIndex(now.getIndex() + 1).getPeriodNumber());
                        label = p.name();
                        teacher.setText(p.fullTeacher());
                        room.setText(p.room());
                        subject.setText(p.name());
                        extraData.setVisibility(View.VISIBLE);
                    } else {
                        label = DateTimeHelper.bells.getIndex(now.getIndex() + 1).getLabel();
                        extraData.setVisibility(View.VISIBLE);
                    }

                }
                else { // There's consecutive non-periods - i.e lunch 1 -> lunch 2
                    label = now.getLabel();
                    connector = "starts in";
                }
            }
            else {
                // end of day
                label = "School starts";
                connector = "in";
                if (TodayJson.getInstance() != null && TodayJson.getInstance().getPeriod(1) != null) {
                    extraData.setVisibility(View.VISIBLE);
                    p = TodayJson.getInstance().getPeriod(1);
                    teacher.setText(p.fullTeacher());
                    room.setText(p.room());
                    subject.setText(p.name());
                }
                else {
                    extraData.setVisibility(View.INVISIBLE);
                }
            }
        }

        if (p != null) {
            if (p.teacherChanged())
                teacher.setTextColor(getActivity().getResources().getColor(R.color.standout));
            else
                teacher.setTextColor(getActivity().getResources().getColor(android.R.color.secondary_text_dark));
            if (p.roomChanged())
                room.setTextColor(getActivity().getResources().getColor(R.color.standout));
            else
                room.setTextColor(getActivity().getResources().getColor(android.R.color.secondary_text_dark));
        }

        final String innerLabel = label;
        ((TextView)f.findViewById(R.id.countdown_name)).setText(label);
        ((TextView)f.findViewById(R.id.countdown_in)).setText(connector);
        final TextView t = (TextView)f.findViewById(R.id.countdown_countdown);
        t.setText("...");
        final CountdownFragment frag = this;
        CountDownTimer timer = new CountDownTimer(DateTimeHelper.milliSecondsUntilNextEvent(), 1000) {
            long lastTime = 10000;
            boolean isLast = innerLabel.equals("School ends");
            @Override
            public void onTick(long l) {
                l = (long)Math.floor(l/1000);
                String sec = "" + (l % 60);
                l -= l % 60;
                l /= 60;
                String mins = "" + (l % 60);
                l -= l % 60;
                l /= 60;
                long hrs = l;
                this.lastTime = l;
                if (sec.length() == 1) {
                    sec = "0" + sec;
                }
                if (mins.length() == 1) {
                    mins = "0" + mins;
                }
                if (hrs != 0) {
                    t.setText(hrs + "h " + mins + "m " + sec + "s");
                }
                else {
                    t.setText(mins + "m " + sec + "s");
                }
            }

            @Override
            public void onFinish() {
                if (this.lastTime <= 1000) {
                    if (this.isLast) {
                        ApiAccessor.getToday(frag.getActivity());
                        ApiAccessor.getBelltimes(frag.getActivity());
                    }
                    final Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateTimer();
                        }
                    }, 1000);
                }
                t.setText("00m 00s");

            }
        };
        timer.start();
        this.timeLeft = timer;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CommonFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private class BroadcastListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TimetableActivity.BELLTIMES_AVAILABLE)) {
                updateTimer();
            }
            else if (intent.getAction().equals(TimetableActivity.TODAY_AVAILABLE)) {
                ApiAccessor.getBelltimes(context);
                updateTimer();
            }
        }
    }

}
