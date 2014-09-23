package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface} interface
 * to handle interaction events.
 * Use the {@link BelltimesFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class BelltimesFragment extends Fragment {

    private CommonFragmentInterface mListener;
    //private Menu menu;

    public static BelltimesFragment newInstance() {
        return new BelltimesFragment();
    }
    public BelltimesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
        this.mListener.updateCachedStatus(menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_belltimes, container, false);
        ListView lv = (ListView)v.findViewById(R.id.belltimes_listview);
        lv.setAdapter(new BelltimesAdapter(BelltimesJson.getInstance()));
        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CommonFragmentInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CommonFragmentInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
