package com.app.hopity.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.app.hopity.AboutUsActivity;
import com.app.hopity.AppSettingsActivity;
import com.app.hopity.DashboardActivity;
import com.app.hopity.LoginActivity;
import com.app.hopity.R;
import com.app.hopity.TermsConditionsActivity;
import com.app.hopity.UserSettingsActivity;
import com.app.hopity.ViewListActivity;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.appdata.SessionManager;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.ProfileInfo;
import com.app.hopity.utils.ImageUtils;
import com.pkmmte.view.CircularImageView;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLogoutListener;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyProfileFragment extends Fragment implements View.OnClickListener, DashboardActivity.OnBackPressedListener {

    protected static String appToken;
    private static int userId;

    public SimpleFacebook mSimpleFacebook;
    private CircularImageView profileUserDp;
    private String TAG = "My Profile Fragment";

    private TextView tvNumbersBlocked, tvNumbersBroadcasts, tvNumbersFollowers, tvNumbersFollowing;
    private TextView tv_userUsername;
    private int countFollow, countFollowing, countBlock, countBroad;

    // newInstance constructor for creating fragment with arguments
    public static MyProfileFragment newInstance() {
        return new MyProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        ((DashboardActivity) getActivity()).setOnBackPressedListener(this);

        new GlobalSharedPrefs(getActivity());
        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
        userId = GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        ImageButton profileEditButton = (ImageButton) rootView.findViewById(R.id.btnUserSettingsProfile);
        profileEditButton.setOnClickListener(this);

        profileUserDp = (CircularImageView) rootView.findViewById(R.id.profile_info_dp);
        tv_userUsername = (TextView) rootView.findViewById(R.id.profile_info_username);

        String username = GlobalSharedPrefs.hapityPref.getString("USER_NAME", "Username");
        tv_userUsername.setText("" + username);

        // user info variables initializes
        tvNumbersFollowers = (TextView) rootView.findViewById(R.id.tvNumberFollowers);
        tvNumbersFollowing = (TextView) rootView.findViewById(R.id.tvNumberFollowing);
        tvNumbersBlocked = (TextView) rootView.findViewById(R.id.tvNumberBlocked);
        tvNumbersBroadcasts = (TextView) rootView.findViewById(R.id.tvNumberBroadcasts);

        rootView.findViewById(R.id.lo_followers_profile_list).setOnClickListener(this);
        rootView.findViewById(R.id.lo_following_profile_list).setOnClickListener(this);
        rootView.findViewById(R.id.lo_blocked_profile_list).setOnClickListener(this);
        rootView.findViewById(R.id.lo_broadcasts_profile_list).setOnClickListener(this);
        //adding up settings layouts
        rootView.findViewById(R.id.lo_settings_profile_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // App Settings
                Intent aSet = new Intent(getActivity(), AppSettingsActivity.class);
                startActivity(aSet);
            }
        });
        rootView.findViewById(R.id.lo_tac_profile_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // terms and condition
                Intent aSet = new Intent(getActivity(), TermsConditionsActivity.class);
                startActivity(aSet);
            }
        });
        rootView.findViewById(R.id.lo_au_profile_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // About us
                Intent aSet = new Intent(getActivity(), AboutUsActivity.class);
                startActivity(aSet);
            }
        });
        rootView.findViewById(R.id.lo_lo_profile_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout Settings
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setMessage("Are you sure?");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String regId = GlobalSharedPrefs.hapityPref.getString(Constants.GCM_REG_KEY, "");
                                String appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
                                String userId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

                                new SignOutTask().execute(appToken, "" + userId, regId);
                                dialog.cancel();
                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        });


        String urlImage = GlobalSharedPrefs.hapityPref.getString("USER_PROFILE_PICTURE",
                "http://api.hapity.com/assets/images/null.jpg");

        if (!urlImage.isEmpty()) {
            ImageUtils.loadWebImageIntoCircular(profileUserDp, urlImage, getContext());
        }

        String urlToParse = Constants.GET_USER_PROFILE_URL
                + "user_id=" + userId;
        new GetProfileInfoTask().execute(urlToParse);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 64206) {
            mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mSimpleFacebook = SimpleFacebook.getInstance(getActivity());

        String urlToParse = Constants.GET_USER_PROFILE_URL
                + "user_id=" + userId;
        GetProfileInfoTask task = new GetProfileInfoTask();
        task.execute(urlToParse);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lo_followers_profile_list:
                // list of followers
                if (countFollow > 0) {
                    Intent ifollow = new Intent(getActivity(), ViewListActivity.class);
                    // inserting data
                    ifollow.putExtra("listToShow", "followers");
                    startActivityForResult(ifollow, 0);
                    getActivity().overridePendingTransition(R.anim.lefttoright, R.anim.stable);
                }
                break;
            case R.id.lo_following_profile_list:
                // list of following
                if (countFollowing > 0) {
                    Intent ifollowing = new Intent(getActivity(), ViewListActivity.class);
                    // inserting data
                    ifollowing.putExtra("listToShow", "following");
                    startActivityForResult(ifollowing, 0);
                    getActivity().overridePendingTransition(R.anim.lefttoright, R.anim.stable);
                }
                break;
            case R.id.lo_blocked_profile_list:
                // list of Blocked
                if (countBlock > 0) {
                    Intent iblock = new Intent(getActivity(), ViewListActivity.class);
                    // inserting data
                    iblock.putExtra("listToShow", "blocked");
                    startActivityForResult(iblock, 0);
                    getActivity().overridePendingTransition(R.anim.lefttoright, R.anim.stable);
                }
                break;
            case R.id.lo_broadcasts_profile_list:
                // list of Broadcasts
                if (countBroad > 0) {
                    Intent ibroadcasts = new Intent(getActivity(), ViewListActivity.class);
                    // inserting data
                    ibroadcasts.putExtra("listToShow", "broadcasts");
                    startActivityForResult(ibroadcasts, 0);
                    getActivity().overridePendingTransition(R.anim.lefttoright, R.anim.stable);
                }
                break;

            case R.id.btnUserSettingsProfile:
                // user settings
                Intent uSet = new Intent(getActivity(), UserSettingsActivity.class);
                startActivity(uSet);
                break;
        }
    }

    @Override
    public void doBack() {
        //BackPressed in activity will call this
    }

    // web services background async tasks
    class GetProfileInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlParse = params[0] + "&token=" + appToken;
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<ProfileInfo, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(ProfileInfo response) {

                            try {
                                String status = response.status;
                                UserInfo[] userInfo = response.profile_info;
                                GlobalSharedPrefs.followers = response.followers;
                                GlobalSharedPrefs.following = response.following;
                                GlobalSharedPrefs.blocked = response.blocked;
                                GlobalSharedPrefs.broadcasts = response.broadcast;

                                if (status.equalsIgnoreCase("success")) {

                                    tv_userUsername.setText("" + userInfo[0].username);
                                    // grab the Piece at position 1
                                    tvNumbersFollowers.setText(" " + response.followers.length + " ");
                                    tvNumbersFollowing.setText(" " + response.following.length + " ");
                                    tvNumbersBlocked.setText(" " + response.blocked.length + " ");
                                    tvNumbersBroadcasts.setText(" " + response.broadcast.length + " ");

                                    countFollow = response.followers.length;
                                    countFollowing = response.following.length;
                                    countBroad = response.broadcast.length;
                                    countBlock = response.blocked.length;

                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_PROFILE_PICTURE", userInfo[0].profile_picture).commit();
                                    ImageUtils.loadWebImageIntoCircular(profileUserDp, userInfo[0].profile_picture, getContext());
                                }
                            } catch (Exception e) {
                                PrettyToast.showError(getContext(), "There was a problem fetching profile information, Please try again");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            if (getContext() != null) {
                                PrettyToast.showError(getContext(), "There was a problem connecting to server, Please try again");
                            }
                        }

                    }, new HashMap<String, String>()));
            return "result";
        }
    }

    class SignOutTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
            progress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progress.setTitleText("Signing Out, Please wait..");
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String urlParse = Constants.SIGN_OUT_URL + "token=" + params[0] +
                    "&user_id=" + params[1] +
                    "&reg_id=" + params[2] +
                    "&type=android";
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<ProfileInfo, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(ProfileInfo response) {
                            progress.dismiss();

                            final SessionManager s = new SessionManager(getContext());
                            // logout listener
                            OnLogoutListener onLogoutListener = new OnLogoutListener() {
                                public String TAG = "";

                                @Override
                                public void onLogout() {
                                    Log.e(TAG, "You are logged out");
                                    if (s.isLoggedIn()) {

                                        SharedPreferences.Editor edit = GlobalSharedPrefs.hapityPref.edit();
                                        edit.putString("ACCESS_TOKEN", "");
                                        edit.putString("ACCESS_TOKEN_SECRET", "");
                                        edit.apply();
                                        s.logoutUser();

                                        Intent i = new Intent(getActivity(), LoginActivity.class);
                                        // Closing all the Activities
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        // Staring Login Activity
                                        startActivity(i);
                                        getActivity().finish();
                                    }
                                }
                            };
                            SimpleFacebook mSimpleFacebook = SimpleFacebook.getInstance();
                            mSimpleFacebook.logout(onLogoutListener);
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            progress.dismiss();
                            if (getActivity() != null) {
                                PrettyToast.showError(getContext(), "Failed to Logout, Please try again");
                            }
                        }

                    }, new HashMap<String, String>()));
            return "result";
        }
    }
}