package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CountdownFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CountdownFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CountdownFragment extends Fragment {

    private TimetableActivity mListener;
    private CountDownTimer timeLeft;

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
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(new BroadcastListener(), new IntentFilter(TimetableActivity.BELLTIMES_AVAILABLE));
        Toast.makeText(getActivity(), "Countdown! School never ends!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout f = (FrameLayout)inflater.inflate(R.layout.fragment_countdown, container, false);
        Log.i("countdownFrag", "done");
        return f;
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
        final FrameLayout f = (FrameLayout)this.getView();
        if (f == null) {
            Toast.makeText(this.getActivity(),"No view, aborting...", Toast.LENGTH_SHORT).show();
            return;
        }
        final TextView t = (TextView)f.findViewById(R.id.countdown_countdown);
        CountDownTimer timer = new CountDownTimer(DateTimeHelper.milliSecondsUntilNextEvent(), 1000) {
            long lastTime = 10000;
            @Override
            public void onTick(long l) {
                l = (long)Math.floor(l/1000);
                long sec = l % 60;
                l -= sec;
                l /= 60;
                long mins = l % 60;
                l -= mins;
                l /= 60;
                long hrs = l;
                this.lastTime = l;
                t.setText(hrs + "h " + mins + "m " + sec + "s");
            }

            @Override
            public void onFinish() {
                if (this.lastTime <= 1000) {
                    final Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateTimer();
                        }
                    }, 1000);
                }
                t.setText("RRRRRIIIING");

            }
        };
        timer.start();
        this.timeLeft = timer;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (TimetableActivity) activity;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private class BroadcastListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(TimetableActivity.BELLTIMES_AVAILABLE)) {
                Log.i("broadcastlistener", "belltimes available!");
                updateTimer();
            }
        }
    }

}
