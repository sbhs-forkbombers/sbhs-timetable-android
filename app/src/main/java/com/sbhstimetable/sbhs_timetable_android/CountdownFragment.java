package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;


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
        Toast.makeText(getActivity(), "Countdown! School never ends!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FrameLayout f = (FrameLayout)inflater.inflate(R.layout.fragment_countdown, container, false);
        final TextView t = (TextView)f.findViewById(R.id.countdown_countdown);
        CountDownTimer timer = new CountDownTimer(DateTimeHelper.milliSecondsUntilNextEvent(), 1000) {
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

                t.setText("Time remaining: " + hrs + "h " + mins + "m " + sec + "s");
            }

            @Override
            public void onFinish() {
                t.setText("Time up!");

            }
        };
        timer.start();
        Log.i("countdownFrag", "done");
        return f;
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

}
