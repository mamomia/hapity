package com.app.hopity.fragmentDialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.app.hopity.R;
import com.app.hopity.appdata.Constants;
import com.app.hopity.extra.GsonRequest;
import com.app.hopity.extra.VolleySingleton;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.FollowUnfollowResponse;
import com.app.hopity.modelsResponse.ProfileInfo;
import com.app.hopity.modelsResponse.ReportResponse;
import com.app.hopity.utils.ImageUtils;
import com.pkmmte.view.CircularImageView;

/**
 * Created by Mushi on 3/4/2016.
 */
public class ProfilesDialogFragment extends DialogFragment {

    private String appToken;
    private String appUserId;
    private String viewersUserId;
    private TextView tvUsername;
    private Button btnFollow;
    private TextView tvFollowers;
    private TextView tvFollowing;
    private TextView tvBroadcasts;
    private CircularImageView dpDialogBox;
    private String TAG = "Profile Dialog";
    private String isFollower = "no";
    private String isBlocked = "no";
    private String CurrentActivity;

    DialogInterface.OnClickListener actionListenerReport = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0: // Report User
                    // Report This User
                    AlertDialog.Builder builderUser = new AlertDialog.Builder(getDialog().getContext())
                            .setTitle("")
                            .setMessage("Are you sure you want to report this user?")
                            .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Report Now
                                    new ReportBlockUnblockUserTask().execute(Constants.REPORT_USER, appToken, appUserId, viewersUserId);
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Close the activity as they have declined the EULA
                                    dialog.dismiss();
                                }
                            });
                    builderUser.create().show();
                    break;
                case 1:
                    // Block Unblock User
                    if (isBlocked.equalsIgnoreCase("no")) {
                        AlertDialog.Builder builderBroadcast = new AlertDialog.Builder(getDialog().getContext())
                                .setTitle("")
                                .setMessage("Are you sure you want to Block this user?")
                                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Block Now
                                        if (CurrentActivity.equalsIgnoreCase("broadcaster"))
                                            new ReportBlockUnblockUserTask().execute(Constants.BROADCASTER_BLOCK_USER, appToken, appUserId, viewersUserId);
                                        else
                                            new ReportBlockUnblockUserTask().execute(Constants.BLOCK_USER, appToken, appUserId, viewersUserId);
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Close the activity as they have declined the EULA
                                        dialog.dismiss();
                                    }
                                });
                        builderBroadcast.create().show();
                    } else {
                        AlertDialog.Builder builderBroadcast = new AlertDialog.Builder(getDialog().getContext())
                                .setTitle("")
                                .setMessage("Are you sure you want to Unblock this user?")
                                .setPositiveButton("Unblock", new Dialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Unblock Now
                                        new ReportBlockUnblockUserTask().execute(Constants.UNBLOCK_USER, appToken, appUserId, viewersUserId);
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Close the activity as they have declined the EULA
                                        dialog.dismiss();
                                    }
                                });
                        builderBroadcast.create().show();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @SuppressLint("ValidFragment")
    public ProfilesDialogFragment(String tag, String token, String appUserid, String viewerUserId) {
        CurrentActivity = tag;
        appToken = token;
        appUserId = appUserid;
        viewersUserId = viewerUserId;
    }

    public ProfilesDialogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_fragment_dialoge, null, false);

        // set the custom dialog components - text, image and button
        dpDialogBox = (CircularImageView) view.findViewById(R.id.ivProfileDpProfileDialog);
        tvUsername = (TextView) view.findViewById(R.id.tvUsernameProfileDialog);
        tvFollowers = (TextView) view.findViewById(R.id.tvNumberFollowersProfileDialog);
        tvFollowing = (TextView) view.findViewById(R.id.tvNumberFollowingProfileDialog);
        tvBroadcasts = (TextView) view.findViewById(R.id.tvNumberBroadcastsProfileDialog);
        btnFollow = (Button) view.findViewById(R.id.btnFollowBroadcasterProfileDialog);
        btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollower.equalsIgnoreCase("yes") && isBlocked.equalsIgnoreCase("no")) {

                    String urlToUnfollow = Constants.UNFOLLOW_USER_URL
                            + "token=" + appToken
                            + "&follower_id=" + appUserId
                            + "&following_id=" + viewersUserId;
                    new FollowUnfollowBroadcasterTask().execute(urlToUnfollow, viewersUserId);

                }
                if (isFollower.equalsIgnoreCase("no") && isBlocked.equalsIgnoreCase("no")) {
                    String urlToFollow = Constants.FOLLOW_USER_URL
                            + "token=" + appToken
                            + "&follower_id=" + appUserId
                            + "&following_id=" + viewersUserId;
                    new FollowUnfollowBroadcasterTask().execute(urlToFollow, viewersUserId);
                }
                if (isBlocked.equalsIgnoreCase("yes")) {

                    AlertDialog.Builder builderBroadcast = new AlertDialog.Builder(getDialog().getContext())
                            .setTitle("")
                            .setMessage("Are you sure you want to Unblock this user?")
                            .setPositiveButton("Unblock", new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Unblock Now
                                    new ReportBlockUnblockUserTask().execute(Constants.UNBLOCK_USER, appToken, appUserId, viewersUserId);
                                    dialogInterface.dismiss();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Close the activity as they have declined the EULA
                                    dialog.dismiss();
                                }
                            });
                    builderBroadcast.create().show();

                }
            }
        });
        ImageView btnExit = (ImageView) view.findViewById(R.id.btnExitProfileDialog);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.gc();
                dismiss();
            }
        });
        ImageView btnSettings = (ImageView) view.findViewById(R.id.btnSettingsProfileDialog);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.gc();
                AlertDialog.Builder builder = new AlertDialog.Builder(getDialog().getContext());
                String[] options = {"Report this user", ""};

                if (isBlocked.equalsIgnoreCase("yes"))
                    options[1] = "Unblock this user";
                else
                    options[1] = "Block this user";
                builder.setItems(options, actionListenerReport);
                AlertDialog actionsReport = builder.create();
                actionsReport.show();
            }
        });

        String urlToParse = Constants.GET_USER_PROFILE_URL + "user_id=" + viewersUserId;
        new GetProfileInfoTask().execute(urlToParse);

        if (appUserId.equalsIgnoreCase(viewersUserId)) {
            btnSettings.setVisibility(View.INVISIBLE);
            btnFollow.setVisibility(View.INVISIBLE);
        }
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // web services clases
    class GetProfileInfoTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            try {
                String urlParse = params[0] + "&token=" + appToken + "&follower_id=" + appUserId;
                Log.e(TAG, " getting info url : " + urlParse);

                GsonRequest<ProfileInfo> myReq = new GsonRequest<>(
                        Request.Method.GET,
                        urlParse,
                        ProfileInfo.class,
                        null,
                        null,
                        SuccessListener(),
                        ErrorListener());

                myReq.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(getActivity()).addToRequestQueue(myReq);

                return " ";

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private Response.Listener<ProfileInfo> SuccessListener() {
            return new Response.Listener<ProfileInfo>() {
                @Override
                public void onResponse(ProfileInfo response) {
                    if (getShowsDialog()) {
                        try {
                            String status = response.status;
                            UserInfo[] userInfo = response.profile_info;

                            if (status.equalsIgnoreCase("success")) {

                                tvUsername.setText("" + userInfo[0].username);
                                tvFollowers.setText("" + response.followers.length);
                                tvFollowing.setText("" + response.following.length);
                                tvBroadcasts.setText("" + response.broadcast.length);

                                isFollower = response.is_follower;
                                isBlocked = response.is_block;

                                if (isFollower.equalsIgnoreCase("yes")) {
                                    btnFollow.setText("Following");
                                }
                                if (isFollower.equalsIgnoreCase("no")) {
                                    btnFollow.setText("Follow");
                                }
                                if (isBlocked.equalsIgnoreCase("yes")) {
                                    btnFollow.setText("Blocked");
                                }

                                ImageUtils.loadWebImageIntoCircular(dpDialogBox, userInfo[0].profile_picture, getContext());

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }

        private Response.ErrorListener ErrorListener() {
            return new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (getShowsDialog()) {
                        Toast.makeText(getContext(), "We were unable to connect to our service please try again.", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            };
        }
    }

    class FollowUnfollowBroadcasterTask extends AsyncTask<String, String, String> {
        private String userIdOfTarget = "";

        protected String doInBackground(String... params) {
            try {
                String urlParse = params[0];
                userIdOfTarget = params[1];
                Log.e(TAG, " getting info url : " + urlParse);

                GsonRequest<FollowUnfollowResponse> myReq = new GsonRequest<>(
                        Request.Method.GET,
                        urlParse,
                        FollowUnfollowResponse.class,
                        null,
                        null,
                        SuccessListener(),
                        ErrorListener());
                myReq.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(getActivity()).addToRequestQueue(myReq);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "res";
        }

        private Response.Listener<FollowUnfollowResponse> SuccessListener() {
            return new Response.Listener<FollowUnfollowResponse>() {
                @Override
                public void onResponse(FollowUnfollowResponse response) {
                    if (getShowsDialog()) {
                        try {
                            if (response.status.equalsIgnoreCase("success")) {

                                String urlToParse = Constants.GET_USER_PROFILE_URL + "user_id="
                                        + userIdOfTarget;
                                new GetProfileInfoTask().execute(urlToParse);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }

        private Response.ErrorListener ErrorListener() {
            return new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (getShowsDialog()) {
                        Toast.makeText(getContext(), "We were unable to perform this request please try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            };
        }
    }

    // Unblock User Thread
    class ReportBlockUnblockUserTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            try {
                String urlParse = params[0] +
                        "token=" + params[1] +
                        "&reporter_user_id=" + params[2] +
                        "&reportee_user_id=" + params[3];
                Log.e(TAG, " unblock user url : " + urlParse);

                GsonRequest<ReportResponse> myReq = new GsonRequest<>(
                        Request.Method.GET,
                        urlParse,
                        ReportResponse.class,
                        null,
                        null,
                        SuccessListener(),
                        ErrorListener());

                myReq.setRetryPolicy(new DefaultRetryPolicy(3000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                VolleySingleton.getInstance(getActivity()).addToRequestQueue(myReq);

                return " ";

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        private Response.Listener<ReportResponse> SuccessListener() {
            return new Response.Listener<ReportResponse>() {
                @Override
                public void onResponse(ReportResponse response) {
                    if (getShowsDialog() && response.status.equalsIgnoreCase("success")) {
                        Toast.makeText(getContext(), response.message, Toast.LENGTH_LONG).show();
                        String urlToParse = Constants.GET_USER_PROFILE_URL
                                + "user_id=" + viewersUserId;
                        new GetProfileInfoTask().execute(urlToParse);
                    }
                }
            };
        }

        private Response.ErrorListener ErrorListener() {
            return new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "" + error.toString());
                    if (getShowsDialog()) {
                        Toast.makeText(getContext(), "Something went wrong we could not report.", Toast.LENGTH_LONG).show();
                    }
                }
            };
        }
    }

}
