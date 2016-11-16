package com.app.hopity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.fragmentDialog.BroadcastsListDialogFragment;
import com.app.hopity.fragmentDialog.ProfilesListDialogFragment;
import com.app.hopity.models.BroadcastSingle;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.FollowUnfollowResponse;
import com.app.hopity.modelsResponse.ProfileInfo;
import com.app.hopity.utils.ImageUtils;
import com.pkmmte.view.CircularImageView;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.quist.app.errorreporter.ExceptionReporter;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class ViewProfileActivity extends AppCompatActivity {

    protected static String appToken;
    private static UserInfo[] followers;
    private static UserInfo[] followings;
    private static BroadcastSingle[] broadcasts;
    private static String userIdofViewProfiler;
    private String appUserId;
    private CircularImageView profileDpView;
    private String TAG = "View Profile";
    private Button btnFollowUnfollow;
    private String isFollower;
    private String isBlocked;
    DialogInterface.OnClickListener actionListenerReport = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0: // Report User
                    // Report This User
                    AlertDialog.Builder builderUser = new AlertDialog.Builder(ViewProfileActivity.this)
                            .setTitle("")
                            .setMessage("Are you sure you want to report this user?")
                            .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Report Now
                                    ReportUserTask task = new ReportUserTask();
                                    task.execute(appToken, appUserId, userIdofViewProfiler);

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
                        AlertDialog.Builder builderBroadcast = new AlertDialog.Builder(ViewProfileActivity.this)
                                .setTitle("")
                                .setMessage("Are you sure you want to Block this user?")
                                .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Block Now
                                        BlockUserTask task = new BlockUserTask();
                                        task.execute(appToken, appUserId, userIdofViewProfiler);

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
                        AlertDialog.Builder builderBroadcast = new AlertDialog.Builder(ViewProfileActivity.this)
                                .setTitle("")
                                .setMessage("Are you sure you want to Unblock this user?")
                                .setPositiveButton("Unblock", new Dialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // Unblock Now
                                        UnblockUserTask task = new UnblockUserTask();
                                        task.execute(appToken, appUserId, userIdofViewProfiler);

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
    private Toolbar toolbar;

    private TextView tvNumbersFollowers, tvNumbersFollowing, tvNumbersBroadcasts;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExceptionReporter.register(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        new GlobalSharedPrefs(this);

        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
        userIdofViewProfiler = getIntent().getStringExtra("user_id_ViewProfile");
        appUserId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        toolbar = (Toolbar) findViewById(R.id.toolbarViewProfile);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitleTextAppearance(getApplicationContext(), R.style.TitleBarText);
        toolbar.setNavigationIcon(R.drawable.img_back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.gc();
                finish();
            }
        });
        toolbar.inflateMenu(R.menu.menu_view_profile);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_report_viewprofile) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewProfileActivity.this);
                    String[] options = {"Report this user", ""};

                    if (isBlocked.equalsIgnoreCase("yes"))
                        options[1] = "Unblock this user";
                    else
                        options[1] = "Block this user";

                    builder.setItems(options, actionListenerReport);
                    AlertDialog actionsReport = builder.create();
                    actionsReport.show();
                }
                return false;
            }
        });

        tvNumbersFollowers = (TextView) findViewById(R.id.tvNumberFollowersViewProfile);
        tvNumbersFollowing = (TextView) findViewById(R.id.tvNumberFollowingViewProfile);
        tvNumbersBroadcasts = (TextView) findViewById(R.id.tvNumberBroadcastsViewProfile);

        LinearLayout layoutFollowers = (LinearLayout) findViewById(R.id.loFollowersViewProfile);
        LinearLayout layoutFollowing = (LinearLayout) findViewById(R.id.loFollowingViewProfile);
        LinearLayout layoutBroadcasts = (LinearLayout) findViewById(R.id.loBroadcastsViewProfile);

        layoutFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(tvNumbersFollowers.getText().toString());
                if (count > 0) {
                    FragmentManager manager = getSupportFragmentManager();
                    ProfilesListDialogFragment dialog = new ProfilesListDialogFragment(followers);
                    dialog.show(manager, "dialog");
                }
            }
        });
        layoutFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(tvNumbersFollowing.getText().toString());
                if (count > 0) {
                    FragmentManager manager = getSupportFragmentManager();
                    ProfilesListDialogFragment dialog = new ProfilesListDialogFragment(followings);
                    dialog.show(manager, "dialog");
                }
            }
        });
        layoutBroadcasts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(tvNumbersBroadcasts.getText().toString());
                if (count > 0) {
                    FragmentManager manager = getSupportFragmentManager();
                    BroadcastsListDialogFragment dialog = new BroadcastsListDialogFragment(broadcasts);
                    dialog.show(manager, "dialog");
                }
            }
        });

        profileDpView = (CircularImageView) findViewById(R.id.ivProfilePicViewProfile);

        btnFollowUnfollow = (Button) findViewById(R.id.btnFollowUnfollowViewProfile);
        btnFollowUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFollower.equalsIgnoreCase("no") && btnFollowUnfollow.getText().toString().equalsIgnoreCase("follow")) {
                    String urlToFollow = Constants.FOLLOW_USER_URL
                            + "token=" + appToken
                            + "&follower_id=" + appUserId
                            + "&following_id=" + userIdofViewProfiler;
                    FollowUnfollowTask task = new FollowUnfollowTask();
                    task.execute(urlToFollow);
                } else if (isFollower.equalsIgnoreCase("yes") && btnFollowUnfollow.getText().toString().equalsIgnoreCase("following")) {
                    String urlToUnfollow = Constants.UNFOLLOW_USER_URL
                            + "token=" + appToken
                            + "&follower_id=" + appUserId
                            + "&following_id=" + userIdofViewProfiler;
                    FollowUnfollowTask task = new FollowUnfollowTask();
                    task.execute(urlToUnfollow);
                } else if (isBlocked.equalsIgnoreCase("yes") && btnFollowUnfollow.getText().toString().equalsIgnoreCase("blocked")) {
                    AlertDialog.Builder builderBroadcast = new AlertDialog.Builder(ViewProfileActivity.this)
                            .setTitle("")
                            .setMessage("Are you sure you want to Unblock this user?")
                            .setPositiveButton("Unblock", new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Unblock Now
                                    UnblockUserTask task = new UnblockUserTask();
                                    task.execute(appToken, appUserId, userIdofViewProfiler);

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

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.scrollViewSettingsViewProfile);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String urlToParse = Constants.GET_USER_PROFILE_URL
                        + "user_id=" + userIdofViewProfiler;
                GetProfileInfoTask task = new GetProfileInfoTask();
                task.execute(urlToParse);
            }
        });

        String urlToParse = Constants.GET_USER_PROFILE_URL
                + "user_id=" + userIdofViewProfiler;
        GetProfileInfoTask task = new GetProfileInfoTask();
        task.execute(urlToParse);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        System.gc();
        System.gc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.gc();
        System.gc();
        System.gc();
    }

    // web services background async tasks
    private class GetProfileInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlParse = params[0] + "&token=" + appToken + "&follower_id=" + appUserId;
            ;
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<ProfileInfo, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(ProfileInfo response) {

                            if (getApplicationContext() != null) {
                                // stopping swipe refresh
                                swipeRefreshLayout.setRefreshing(false);
                                try {
                                    String status = response.status;

                                    if (status.equalsIgnoreCase("success")) {

                                        UserInfo[] userInfo = response.profile_info;
                                        followers = response.followers;
                                        broadcasts = response.broadcast;
                                        followings = response.following;

                                        toolbar.setTitle(" " + userInfo[0].username);
                                        tvNumbersFollowers.setText("" + followers.length);
                                        tvNumbersFollowing.setText("" + followings.length);
                                        tvNumbersBroadcasts.setText("" + broadcasts.length);

                                        isFollower = response.is_follower;
                                        isBlocked = response.is_block;
                                        if (isFollower.equalsIgnoreCase("yes")) {
                                            btnFollowUnfollow.setText("Following");
                                        }
                                        if (isFollower.equalsIgnoreCase("no")) {
                                            btnFollowUnfollow.setText("Follow");
                                        }
                                        if (isBlocked.equalsIgnoreCase("yes")) {
                                            btnFollowUnfollow.setText("Blocked");
                                        }

                                        if (appUserId.equalsIgnoreCase(userIdofViewProfiler)) {
                                            btnFollowUnfollow.setVisibility(View.GONE);
                                            toolbar.setEnabled(false);
                                        }

                                        if (!userInfo[0].profile_picture.isEmpty())
                                            ImageUtils.loadWebImageIntoCircular(profileDpView, userInfo[0].profile_picture, ViewProfileActivity.this);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            if (getApplicationContext() != null) {
                                swipeRefreshLayout.setRefreshing(false);
                                PrettyToast.showError(getApplicationContext(), "There was a problem connecting to server, Please try again");
                            }
                        }

                    }, new HashMap<String, String>()));
            return "result";
        }
    }

    private class FollowUnfollowTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new SweetAlertDialog(ViewProfileActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progress.setTitleText("Please wait..");
            progress.setCancelable(false);
            progress.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(final String... params) {

            String url = params[0];
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    url,
                    new RequestHandler<>(new RequestCallbacks<FollowUnfollowResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(FollowUnfollowResponse response) {
                            progress.dismiss();
                            if (response.status.equalsIgnoreCase("success")) {
                                String urlToParse = Constants.GET_USER_PROFILE_URL
                                        + "user_id=" + userIdofViewProfiler;
                                GetProfileInfoTask task = new GetProfileInfoTask();
                                task.execute(urlToParse);
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            progress.dismiss();
                        }

                    }, new HashMap<String, String>()));

            return "";
        }//close doInBackground
    }// close validateUserTask

    private class ReportUserTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new SweetAlertDialog(ViewProfileActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progress.setTitleText("Please wait..");
            progress.setCancelable(false);
            progress.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(final String... params) {

            String urlParse = Constants.REPORT_USER +
                    "token=" + params[0] +
                    "&reporter_user_id=" + params[1] +
                    "&reportee_user_id=" + params[2];
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<FollowUnfollowResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(FollowUnfollowResponse response) {
                            progress.dismiss();
                            if (response.status.equalsIgnoreCase("success")) {
                                PrettyToast.showSuccess(ViewProfileActivity.this, response.message);
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            progress.dismiss();
                            PrettyToast.showSuccess(ViewProfileActivity.this, "Something went wrong we could not report.");
                        }

                    }, new HashMap<String, String>()));

            return "";
        }//close doInBackground
    }// close validateUserTask

    // Block User Thread
    private class BlockUserTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new SweetAlertDialog(ViewProfileActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progress.setTitleText("Please wait..");
            progress.setCancelable(false);
            progress.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(final String... params) {

            String urlParse = Constants.BLOCK_USER +
                    "token=" + params[0] +
                    "&reporter_user_id=" + params[1] +
                    "&reportee_user_id=" + params[2];
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<FollowUnfollowResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(FollowUnfollowResponse response) {
                            progress.dismiss();
                            if (response.status.equalsIgnoreCase("success")) {
                                PrettyToast.showSuccess(ViewProfileActivity.this, response.message);
                                String urlToParse = Constants.GET_USER_PROFILE_URL
                                        + "user_id=" + userIdofViewProfiler;
                                GetProfileInfoTask task = new GetProfileInfoTask();
                                task.execute(urlToParse);
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            progress.dismiss();
                            PrettyToast.showSuccess(ViewProfileActivity.this, "Something went wrong we could not report.");
                        }

                    }, new HashMap<String, String>()));

            return "";
        }//close doInBackground
    }// close validateUserTask

    // Unblock User Thread
    private class UnblockUserTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new SweetAlertDialog(ViewProfileActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progress.setTitleText("Please wait..");
            progress.setCancelable(false);
            progress.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(final String... params) {

            String urlParse = Constants.UNBLOCK_USER +
                    "token=" + params[0] +
                    "&reporter_user_id=" + params[1] +
                    "&reportee_user_id=" + params[2];
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<FollowUnfollowResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(FollowUnfollowResponse response) {
                            progress.dismiss();
                            if (response.status.equalsIgnoreCase("success")) {
                                PrettyToast.showSuccess(ViewProfileActivity.this, response.message);
                                String urlToParse = Constants.GET_USER_PROFILE_URL
                                        + "user_id=" + userIdofViewProfiler;
                                GetProfileInfoTask task = new GetProfileInfoTask();
                                task.execute(urlToParse);
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            progress.dismiss();
                            PrettyToast.showSuccess(ViewProfileActivity.this, "Something went wrong we could not report.");
                        }

                    }, new HashMap<String, String>()));

            return "";
        }//close doInBackground
    }// close validateUserTask


}
