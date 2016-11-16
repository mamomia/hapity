package com.app.hopity;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.app.hopity.adapters.RecyclerBroadcastListAdapter;
import com.app.hopity.adapters.SimpleDividerItemDecoration;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.extra.GsonRequest;
import com.app.hopity.extra.VolleySingleton;
import com.app.hopity.models.BroadcastItemListModel;
import com.app.hopity.models.BroadcastSingle;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class SearchBroadcastActivity extends AppCompatActivity {

    private EditText searchView;
    private String TAG = "Search Broadcast";
    private RecyclerBroadcastListAdapter mAdapter;
    private String userId;
    private String appToken;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(SearchBroadcastActivity.this);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_broadcast);

        new GlobalSharedPrefs(this);

        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
        userId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        RecyclerView broadcastRecyclerView = (RecyclerView) findViewById(R.id.broadcastRecyclerViewSearch);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchBroadcastActivity.this, LinearLayoutManager.VERTICAL, false);
        broadcastRecyclerView.setLayoutManager(layoutManager);
        broadcastRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        mAdapter = new RecyclerBroadcastListAdapter(this);
        broadcastRecyclerView.setAdapter(mAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSearchBroadcast);
        toolbar.setNavigationIcon(R.drawable.img_back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.gc();
                finish();
            }
        });

        searchView = (EditText) toolbar.findViewById(R.id.searchEditTextBroadcast);
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_UNSPECIFIED:
                        String query2 = searchView.getText().toString().trim();
                        SearchBroadcastListTask fl2 = new SearchBroadcastListTask();
                        fl2.execute(userId, appToken, query2);
                        break;

                }
                return false;
            }
        });
        searchView.requestFocus();
        setupUI(findViewById(R.id.rootLayoutSearchBroadcast));
    }

    @Override
    protected void onDestroy() {
        hideSoftKeyboard(this);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        hideSoftKeyboard(this);
        super.onPause();
    }

    // Calling web service
    private class SearchBroadcastListTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progres;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progres = new SweetAlertDialog(SearchBroadcastActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progres.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progres.setTitleText("Searching..");
            progres.setCancelable(false);
            progres.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(String... params) {
            try {

                String URL_SEARCH = Constants.SEARCH_BROADCASTS_LIST
                        + "user_id=" + params[0]
                        + "&token=" + params[1]
                        + "&search=" + params[2];

                GsonRequest<BroadcastItemListModel[]> myReq = new GsonRequest<>(
                        Request.Method.GET,
                        URL_SEARCH,
                        BroadcastItemListModel[].class,
                        null,
                        null,
                        SuccessListener(),
                        ErrorListener());
                myReq.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(myReq);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "result";
        }

        private Response.ErrorListener ErrorListener() {
            return new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (getCurrentFocus() != null) {
                        progres.dismiss();
                        PrettyToast.showError(getApplicationContext(), "There was a problem connecting to server, Please try again");
                    }
                }
            };
        }

        private Response.Listener<BroadcastItemListModel[]> SuccessListener() {
            return new Response.Listener<BroadcastItemListModel[]>() {
                @Override
                public void onResponse(BroadcastItemListModel[] response) {
                    if (getApplicationContext() != null) {
                        progres.dismiss();
                        try {
                            mAdapter.removeAll();
                            BroadcastSingle[] respArray = response[1].broadcast;

                            if (respArray == null) {
                                PrettyToast.showSuccess(getApplicationContext(), "Nothing found");
                            } else {
                                int size = respArray.length;
                                for (int i = size - 1; i >= 0; i--) {
                                    mAdapter.add(respArray[i]);
                                }
                            }
                            mAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }
    }

}
