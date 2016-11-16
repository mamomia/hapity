package com.app.hopity.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.app.hopity.R;
import com.app.hopity.SearchPeopleActivity;
import com.app.hopity.adapters.RecyclerPeopleListAdapter;
import com.app.hopity.adapters.SimpleDividerItemDecoration;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.PeopleListResponse;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.util.HashMap;

import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class PeopleFragment extends Fragment {

    protected static String appToken, userId;
    private String TAG = "People Fragment";
    private RecyclerPeopleListAdapter mAdapter;
    private MaterialRefreshLayout mPullRefreshListView;

    public PeopleFragment() {
        // Required empty public constructor
    }

    public static PeopleFragment newInstance() {
        return new PeopleFragment();
    }

    @Override
    public View onCreateView(LayoutInflater _inflater, ViewGroup _container,
                             Bundle _savedInstanceState) {
        // Create and return view for this fragment.
        return _inflater.inflate(R.layout.fragment_people, _container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new GlobalSharedPrefs(getActivity());
        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "empty");
        userId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarPeople);
        toolbar.inflateMenu(R.menu.menu_people);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent au = new Intent(getActivity(), SearchPeopleActivity.class);
                startActivity(au);
                return false;
            }
        });

        mPullRefreshListView = (MaterialRefreshLayout) view.findViewById(R.id.scrollViewPeople);
        mPullRefreshListView.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(MaterialRefreshLayout materialRefreshLayout) {
                new GetPeopleListTask().execute(userId, appToken);
            }
        });

        RecyclerView mRecycler = (RecyclerView) view.findViewById(R.id.peopleRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecycler.setLayoutManager(layoutManager);
        mRecycler.addItemDecoration(new SimpleDividerItemDecoration(getContext()));

        mAdapter = new RecyclerPeopleListAdapter(getActivity(), userId, appToken);
        mRecycler.setAdapter(mAdapter);

        new GetPeopleListTask().execute(userId, appToken);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        System.gc();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // Calling web service
    private class GetPeopleListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String URL_FEED = Constants.GET_PEOPLE_LIST_URL +
                        "&token=" + params[1] +
                        "&user_id=" + params[0];

                RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                        URL_FEED,
                        new RequestHandler<>(new RequestCallbacks<PeopleListResponse, ErrorModel>() {
                            @Override
                            public void onRequestSuccess(PeopleListResponse response) {
                                try {
                                    mPullRefreshListView.finishRefresh();
                                    mAdapter.removeAll();
                                    UserInfo[] respArray = response.users;
                                    int size = respArray.length;
                                    if (size > 0) {
                                        for (int i = size - 1; i >= 0; i--) {
                                            mAdapter.add(respArray[i]);
                                        }
                                    }
                                    mAdapter.notifyDataSetChanged();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    PrettyToast.showError(getContext(), "There was a problem, Please try again");
                                }
                            }

                            @Override
                            public void onRequestError(ErrorModel error) {
                                if (getContext() != null) {
                                    mPullRefreshListView.finishRefresh();
                                    PrettyToast.showError(getContext(), "There was a problem connecting to server, Please try again");
                                }
                            }
                        }, new HashMap<String, String>()));

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "result";
        }
    }
}