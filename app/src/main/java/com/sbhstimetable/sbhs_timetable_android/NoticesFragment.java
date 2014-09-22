package com.sbhstimetable.sbhs_timetable_android;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.DateTimeHelper;
import com.sbhstimetable.sbhs_timetable_android.backend.StorageCache;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.NoticesDropDownAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesJson;



public class NoticesFragment extends Fragment {

    private CommonFragmentInterface mListener;
    private Menu menu;
    private NoticesAdapter adapter;
    private NoticesDropDownAdapter spinnerAdapter;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NoticesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NoticesFragment newInstance() {
        NoticesFragment fragment = new NoticesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public NoticesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
        this.mListener.updateCachedStatus(this.menu);
        this.mListener.setNavigationStyle(ActionBar.NAVIGATION_MODE_LIST);
        this.spinnerAdapter = new NoticesDropDownAdapter();
        getActivity().getActionBar().setListNavigationCallbacks(spinnerAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int i, long l) {
                Log.i("onNavListener", "getting - " + i + " (that's " + spinnerAdapter.getItem(i) + ")");
                if (i == 0) {
                    adapter.filter(null);
                }
                else {
                    adapter.filter(NoticesJson.Year.fromString(spinnerAdapter.getItem(i).replace("Year ", "")));
                }
                return true;
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View res = inflater.inflate(R.layout.fragment_notices, container, false);
        ListView v = (ListView)res.findViewById(R.id.notices_listview);
        JsonObject o = StorageCache.getNotices(getActivity(), DateTimeHelper.getDateString(getActivity()));
        NoticesJson n = NoticesJson.getInstance();
        if (n == null && o != null) {
            n = new NoticesJson(o);
        }
        if (n == null) {
            Log.i("noticesFrag", "FOILED AGAIN :'(");
            Toast.makeText(getActivity(), "Not ready yet!", Toast.LENGTH_SHORT);
        }
        else {
            NoticesAdapter a = new NoticesAdapter(n);
            this.adapter = a;
            v.setAdapter(a);
            Toast.makeText(getActivity(), "WEW", Toast.LENGTH_SHORT);
        }

        return res;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.mListener.setNavigationStyle(ActionBar.NAVIGATION_MODE_STANDARD);
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


}
