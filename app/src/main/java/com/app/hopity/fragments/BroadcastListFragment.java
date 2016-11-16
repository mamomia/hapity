package com.app.hopity.fragments;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.app.hopity.R;
import com.app.hopity.SearchBroadcastActivity;
import com.app.hopity.adapters.RecyclerBroadcastListAdapter;
import com.app.hopity.adapters.SimpleDividerItemDecoration;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.models.BroadcastItemListModel;
import com.app.hopity.models.BroadcastSingle;
import com.app.hopity.modelsResponse.ErrorModel;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.util.HashMap;

import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class BroadcastListFragment extends Fragment {

    protected static String appToken, appUserId;
    private String TAG = "Broadcast List Fragment";
    private MaterialRefreshLayout mPullRefreshListView;
    private RecyclerBroadcastListAdapter mAdapter;

    public BroadcastListFragment() {
        // Required empty public constructor
    }

    public static BroadcastListFragment newInstance() {
        return new BroadcastListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater _inflater, ViewGroup _container,
                             Bundle _savedInstanceState) {
        // Create and return view for this fragment.
        return _inflater.inflate(R.layout.fragment_broadcast_list, _container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new GlobalSharedPrefs(getActivity());
        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "empty");
        appUserId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarBroadcastList);
        toolbar.inflateMenu(R.menu.menu_broadcast_list);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent au = new Intent(getActivity(), SearchBroadcastActivity.class);
                startActivity(au);
                return false;
            }
        });

        RecyclerView mRecycler = (RecyclerView) view.findViewById(R.id.recyclerBroadcasts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        mAdapter = new RecyclerBroadcastListAdapter(getActivity());
        mRecycler.setAdapter(mAdapter);

        mPullRefreshListView = (MaterialRefreshLayout) view.findViewById(R.id.scrollViewGlobalBroadcasts);
        mPullRefreshListView.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {
                //refreshing...
                new GetBroadcastListTask().execute(appToken, appUserId);
            }

            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                //load more refreshing...
            }
        });
        new GetBroadcastListTask().execute(appToken, appUserId);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        System.gc();
    }


    // web services background async tasks
    class GetBroadcastListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            System.out.println("DataListLoader.loadInBackground");

            HashMap<String, String> paramsHash = new HashMap<>();
            paramsHash.put("token", params[0]);
            paramsHash.put("user_id", params[1]);

            RestClientManager.getInstance().makeJsonRequest(Request.Method.POST,
                    Constants.GET_ALL_BROADCASTS,
                    new RequestHandler<>(new RequestCallbacks<BroadcastItemListModel, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(BroadcastItemListModel response) {
                            try {
                                mPullRefreshListView.finishRefresh();
                                mAdapter.removeAll();
                                BroadcastSingle[] respArray = response.broadcast;
                                int size = respArray.length;
                                if (size > 0) {
                                    for (int i = size - 1; i >= 0; i--) {
                                        mAdapter.add(respArray[i]);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Exception in My Broadcast Successful Response");
                                PrettyToast.showError(getContext(), "There was a problem fetching broadcasts, Please try again");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            if (getContext() != null) {
                                mPullRefreshListView.finishRefresh();
                                PrettyToast.showError(getContext(), "There was a problem connecting to server, Please try again");
                            }
                        }
                    }, paramsHash));
            return "result";
        }
    }
}