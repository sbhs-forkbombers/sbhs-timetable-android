package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJSONAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.TodayJson;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimetableFragment} interface
 * to handle interaction events.
 * Use the {@link TimetableFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class TimetableFragment extends Fragment {


    private CommonFragmentInterface mListener;
    private TodayJson today;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment CountdownFragment.
     */
    public static TimetableFragment newInstance() {
        return new TimetableFragment();
    }
    public TimetableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //Toast.makeText(getActivity(), "Timetable! Indoor Walking Route in -10 minutes!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mListener.updateCachedStatus(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_timetable, container, false);
        ListView z = (ListView)this.getActivity().findViewById(R.id.timetable_listview);
        if (z != null) {
            ApiAccessor.getToday(this.getActivity());
        }
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (TodayJson.getInstance() != null && TodayJson.getInstance().getPeriod(1) != null) {
            doTimetable(TodayJson.getInstance());
        }
        else {
            JsonObject res = StorageCache.getTodayJson(this.getActivity(), DateTimeHelper.getDateString(getActivity()));
            if (res != null && res.has("timetable")) {
                this.doTimetable(new TodayJson(res));
            }
        }
    }

    public void doTimetable(String b) {
        JsonParser g = new JsonParser();
        JsonObject obj = g.parse(b).getAsJsonObject();
        if (obj.has("timetable")) {
            doTimetable(new TodayJson(obj));
        }
    }

    public void doTimetable(TodayJson o) {
        this.today = o;
        ListView z = (ListView)this.getActivity().findViewById(R.id.timetable_listview);
        z.setAdapter(new TodayJSONAdapter(this.today));
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
    public void onPause() {
        super.onPause();
        if (this.today != null) {
            StorageCache.cacheTodayJson(this.getActivity(), this.today.getDate(), this.today.toString());
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
