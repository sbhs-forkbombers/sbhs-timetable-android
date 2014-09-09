package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimetableFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimetableFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TimetableFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_SECTION_NUMBER = "1";

    // TODO: Rename and change types of parameters
    private int mSectionNumber;

    private OnFragmentInteractionListener mListener;
    private TodayJson today;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CountdownFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TimetableFragment newInstance() {
        TimetableFragment fragment = new TimetableFragment();
        return fragment;
    }
    public TimetableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        Log.i("timetable", "My tag is " + this.getTag());
        Toast.makeText(getActivity(), "Timetable! Indoor Walking Route in -10 minutes!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_timetable, container, false);
        ListView z = (ListView)this.getActivity().findViewById(R.id.timetable_listview);
        if (z != null) {
            String b = ApiAccessor.getToday(this.getActivity());
            if (b != null) {
                this.doTimetable(b);
            }
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences p = this.getActivity().getSharedPreferences(ApiAccessor.PREFS_NAME, 0);
        if (p.contains("todayJsonCache")) {
            String json = p.getString("todayJsonCache", "");
            this.doTimetable(json);
        }
    }

    public void doTimetable(String b) {
        ListView z = (ListView)this.getActivity().findViewById(R.id.timetable_listview);
        JsonParser g = new JsonParser();
        JsonObject obj = g.parse(b).getAsJsonObject();
        if (!obj.has("timetable")) {
        }
        else {
            this.today = new TodayJson(obj);
            TodayJSONAdapter adapter = new TodayJSONAdapter(this.today);
            z.setAdapter(adapter);
        }
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
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.today != null) {
            SharedPreferences p = this.getActivity().getSharedPreferences(ApiAccessor.PREFS_NAME, 0);
            SharedPreferences.Editor e = p.edit();
            e.putString("todayJsonCache", this.today.toString());
            e.commit();
            StorageCache.cacheTodayJson(this.getActivity(), this.today.getDate(), this.today.toString());
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
