package com.sbhstimetable.sbhs_timetable_android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sbhstimetable.sbhs_timetable_android.backend.ApiAccessor;
import com.sbhstimetable.sbhs_timetable_android.backend.internal.CommonFragmentInterface;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesAdapter;
import com.sbhstimetable.sbhs_timetable_android.backend.json.BelltimesJson;
import com.sbhstimetable.sbhs_timetable_android.backend.json.NoticesJson;

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
    private Runnable runnable;
    private SwipeRefreshLayout layout;
    private Handler h;
    private BelltimesAdapter adapter;
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
        final SwipeRefreshLayout v = (SwipeRefreshLayout)inflater.inflate(R.layout.fragment_belltimes, container, false);
        this.layout = v;
        final ListView lv = (ListView)v.findViewById(R.id.belltimes_listview);
        IntentFilter i = new IntentFilter();
        i.addAction(ApiAccessor.ACTION_NOTICES_JSON);
        i.addAction(ApiAccessor.ACTION_BELLTIMES_JSON);
        i.addAction(ApiAccessor.ACTION_TODAY_JSON);
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(new BroadcastListener(this), i);
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                int topRowVerticalPosition =
                        (lv == null || v.getChildCount() == 0) ?
                                0 : v.getChildAt(0).getTop();
                v.setEnabled(topRowVerticalPosition >= 0);
            }
        });
        final Context c = this.getActivity();
        h = new Handler();
        v.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!ApiAccessor.hasInternetConnection(c)) {
                    Toast.makeText(c, R.string.refresh_failure, Toast.LENGTH_SHORT).show();
                    v.setRefreshing(false);
                    return;
                }
                ApiAccessor.getBelltimes(c, false);
                ApiAccessor.getNotices(c, false);
                ApiAccessor.getToday(c, false);
                h.removeCallbacks(runnable);
                runnable = new CountdownFragment.StopSwiping(v);
                h.postDelayed(runnable, 10000);
            }
        });
        Resources r = this.getResources();
        v.setColorSchemeColors(r.getColor(R.color.green),
                r.getColor(R.color.red),
                r.getColor(R.color.blue),
                r.getColor(R.color.yellow));
        this.adapter = new BelltimesAdapter(BelltimesJson.getInstance());
        lv.setAdapter(this.adapter);
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


    private class BroadcastListener extends BroadcastReceiver {
        private SwipeRefreshLayout f;
        private BelltimesFragment frag;
        BroadcastListener(BelltimesFragment f) {
            this.f = f.layout;
            this.frag = f;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if (act.equals(ApiAccessor.ACTION_BELLTIMES_JSON) || act.equals(ApiAccessor.ACTION_TODAY_JSON) || act.equals(ApiAccessor.ACTION_NOTICES_JSON)) {
                if (this.f == null) return;
                this.f.setRefreshing(false);
                this.frag.h.removeCallbacks(this.frag.runnable);
                Toast.makeText(context, R.string.refresh_success, Toast.LENGTH_SHORT).show();
                if (act.equals(ApiAccessor.ACTION_BELLTIMES_JSON)) {
                    JsonObject o = new JsonParser().parse(intent.getStringExtra(ApiAccessor.EXTRA_JSON_DATA)).getAsJsonObject();
                    if (o.has("bells")) {
                        BelltimesJson b = new BelltimesJson(o);
                        this.frag.adapter.updateBelltimes(b);
                        //this.frag.adapter.update(nj);
                    }

                }
            }
        }
    }
}
