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

import com.android.volley.Request;
import com.app.hopity.adapters.RecyclerPeopleListAdapter;
import com.app.hopity.adapters.SimpleDividerItemDecoration;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.PeopleListResponse;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class SearchPeopleActivity extends AppCompatActivity {

    private EditText searchView;
    private String TAG = "Search People";
    private RecyclerPeopleListAdapter mAdapter;
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
                    hideSoftKeyboard(SearchPeopleActivity.this);
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
        setContentView(R.layout.activity_search_people);

        new GlobalSharedPrefs(this);

        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
        userId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        RecyclerView broadcastRecyclerView = (RecyclerView) findViewById(R.id.peopleRecycleViewSearch);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchPeopleActivity.this, LinearLayoutManager.VERTICAL, false);
        broadcastRecyclerView.setLayoutManager(layoutManager);
        broadcastRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));

        mAdapter = new RecyclerPeopleListAdapter(this, userId, appToken);
        broadcastRecyclerView.setAdapter(mAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSearchPeople);
        toolbar.setNavigationIcon(R.drawable.img_back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.gc();
                finish();
            }
        });

        searchView = (EditText) toolbar.findViewById(R.id.searchEditTextPeople);
        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_UNSPECIFIED:
                        String query2 = searchView.getText().toString().trim();
                        new GetPeopleListTask().execute(userId, appToken, query2);
                        break;
                }
                return false;
            }
        });
        searchView.requestFocus();
        setupUI(findViewById(R.id.rootLayoutSearchPeople));
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
    private class GetPeopleListTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progres;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progres = new SweetAlertDialog(SearchPeopleActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progres.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progres.setTitleText("Searching..");
            progres.setCancelable(false);
            progres.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(String... params) {

            String urlParse = Constants.SEARCH_PEOPLE_URL +
                    "user_id=" + params[0] +
                    "&token=" + params[1] +
                    "&search=" + params[2];

            HashMap<String, String> paramsHash = new HashMap<>();
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<PeopleListResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(PeopleListResponse response) {
                            progres.dismiss();
                            try {
                                mAdapter.removeAll();
                                UserInfo[] respArray = response.users;

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

                        @Override
                        public void onRequestError(ErrorModel error) {
                            if (getCurrentFocus() != null) {
                                progres.dismiss();
                                PrettyToast.showError(getApplicationContext(), "There was a problem connecting to server, Please try again");
                            }
                        }
                    }, paramsHash));
            return "result";
        }
    }
}
