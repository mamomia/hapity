package com.app.hopity.afterStreamActivities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.app.hopity.R;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.ProfileInfo;
import com.facebook.FacebookException;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Feed;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnNewPermissionsListener;
import com.sromku.simple.fb.listeners.OnPublishListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.util.HashMap;
import java.util.List;

import de.quist.app.errorreporter.ExceptionReporter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class OfflineAfterStreamActivity extends AppCompatActivity {

    private static final String TAG = "OfflineAfterStream";
    private String appToken, appUserId;
    private SimpleFacebook mSimpleFacebook;
    private OnPublishListener onPublishListener;
    private TextView tvBroadcastTitle, tvBroadcasterUsername, tvNumbersFollowers, tvNumbersFollowing;
    private ImageView ivBroadcastImage;
    private CircularImageView ivBroadcasterProfilePic;
    private boolean isSharedOnFb = false, isSharedOnTw = false;
    private OnLoginListener onLoginListener;
    private OnNewPermissionsListener onNewPermissionListener;
    private TwitterAuthClient mTwitterAuthClient;
    private String broadcastImage, broadcastTitle, broadcastShareUrl, broadcasterUserId;
    private ProgressBar profilePicLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up a full-screen black window.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ExceptionReporter.register(this);
        setContentView(R.layout.activity_offline_after_stream);

        new GlobalSharedPrefs(this);
        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
        appUserId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        mSimpleFacebook = SimpleFacebook.getInstance(this);
        if (mTwitterAuthClient == null) {
            try {
                mTwitterAuthClient = new TwitterAuthClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        broadcastImage = getIntent().getStringExtra("bImageUrl");
        broadcastTitle = getIntent().getStringExtra("broadcastTitle");
        broadcastShareUrl = getIntent().getStringExtra("bShareUrl");
        broadcasterUserId = getIntent().getStringExtra("bUserId");

        // simple facebook
        onPublishListener = new OnPublishListener() {
            @Override
            public void onComplete(String postId) {
                isSharedOnFb = true;
            }
        };

        tvBroadcastTitle = (TextView) findViewById(R.id.tvBroadcastTitleAfterOffline);
        tvBroadcasterUsername = (TextView) findViewById(R.id.tvUsernameOfflineAfter);
        tvNumbersFollowers = (TextView) findViewById(R.id.tvNumberFollowersOfflineAfter);
        tvNumbersFollowing = (TextView) findViewById(R.id.tvNumberFollowingOfflineAfter);

        ivBroadcastImage = (ImageView) findViewById(R.id.ivBroadcastImageAfterOffline);
        ivBroadcasterProfilePic = (CircularImageView) findViewById(R.id.ivProfilePicOfflineAfter);
        profilePicLoader = (ProgressBar) findViewById(R.id.progressBarOfflineAfter);

        tvBroadcastTitle.setText(broadcastTitle);
        if (!broadcastImage.isEmpty()) {
            Picasso.with(this).
                    load(broadcastImage).
                    placeholder(R.drawable.img_default_broadcast).
                    into(ivBroadcastImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                        }
                    });
        }

        findViewById(R.id.actionLocalShareAfterOffline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Watch this broadcast! now at  " + broadcastShareUrl);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share this broadcast with..."));
            }
        });
        findViewById(R.id.actionFBShareAfterOffline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSharedOnFb && checkFacebookConnectivity())
                    StatusUpdateFacebook(
                            "Check this stream on #Hapity",
                            broadcastImage,
                            broadcastShareUrl,
                            broadcastTitle);
                else
                    getFacebookConnectivity();
            }
        });
        findViewById(R.id.actionTWShareAfterOffline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSharedOnTw && checkTwitterConnectivity())
                    StatusUpdateTwitter("Check this stream on #Hapity: " + broadcastTitle + " ", broadcastShareUrl);
                else
                    getTwitterConnectivity();
            }
        });
        findViewById(R.id.btnDoneAfterShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // using simple facebook API and initializing call backs
        onLoginListener = new OnLoginListener() {
            @Override
            public void onLogin(String accessToken, List<Permission> acceptedPermissions, List<Permission> declinedPermissions) {
                // change the state of the button or do whatever you want
                Log.i(TAG, "Logged in");
                if (!mSimpleFacebook.isAllPermissionsGranted()) {
                    Permission[] permissions = new Permission[]{
                            Permission.PUBLISH_ACTION
                    };
                    mSimpleFacebook.requestNewPermissions(permissions, onNewPermissionListener);
                    return;
                } else {
                    if (!isSharedOnFb)
                        StatusUpdateFacebook(
                                "Check this stream on #Hapity",
                                broadcastImage,
                                broadcastShareUrl,
                                broadcastTitle);
                }
            }

            @Override
            public void onCancel() {
                // user canceled the dialog
                Log.i(TAG, "User Canceled");
            }

            @Override
            public void onFail(String reason) {
                // failed to login
                Log.i(TAG, "Failed to Login");
            }

            @Override
            public void onException(Throwable throwable) {
                // exception from facebook
                Log.i(TAG, "Exception from facebook");
            }
        };
        onNewPermissionListener = new OnNewPermissionsListener() {
            @Override
            public void onSuccess(String s, List<Permission> list, List<Permission> list1) {
                Log.i(TAG, "Got Permission");
                if (!isSharedOnFb)
                    StatusUpdateFacebook(
                            "Check this stream on #Hapity",
                            broadcastImage,
                            broadcastShareUrl,
                            broadcastTitle);
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "Cancel Permission");
            }

            @Override
            public void onException(Throwable throwable) {
                Log.i(TAG, "Exception Permission");
            }

            @Override
            public void onFail(String s) {
                Log.i(TAG, "Fail Permission");
            }
        };

        new GetProfileInfoTask().execute(broadcasterUserId);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "ACTIVITY RESULT : " + resultCode + " REQUEST : " + requestCode);
        if (requestCode == 64206) {
            mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == 140) {
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }

    // check and get twitter connectivity methods
    private boolean checkTwitterConnectivity() {
        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();

        if (GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN", "").isEmpty()
                || GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN_SECRET", "").isEmpty())
            return false;
        else if (session.getAuthToken().isExpired())
            return false;
        else
            return true;
    }

    private void getTwitterConnectivity() {
        mTwitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
            @Override
            public void success(final Result<TwitterSession> twitterSessionResult) {

                new GlobalSharedPrefs(getApplicationContext());
                TwitterSession session =
                        Twitter.getSessionManager().getActiveSession();
                GlobalSharedPrefs.hapityPref.edit().putString("TWITTER_ACCESS_TOKEN",
                        session.getAuthToken().token)
                        .apply();
                GlobalSharedPrefs.hapityPref.edit().putString("TWITTER_ACCESS_TOKEN_SECRET",
                        session.getAuthToken().secret)
                        .apply();
                if (!isSharedOnTw)
                    StatusUpdateTwitter("Check this stream on #Hapity: " + broadcastTitle + " ", broadcastShareUrl);
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });
    }

    // check and get facebook connectivity methods
    private boolean checkFacebookConnectivity() {
        if (!mSimpleFacebook.isLogin() || !mSimpleFacebook.isAllPermissionsGranted())
            return false;
        else
            return true;

    }

    private void getFacebookConnectivity() {
        if (!mSimpleFacebook.isLogin()) {
            mSimpleFacebook.login(onLoginListener);
            return;
        }
        if (!mSimpleFacebook.isAllPermissionsGranted()) {
            Permission[] permissions = new Permission[]{
                    Permission.PUBLISH_ACTION
            };
            mSimpleFacebook.requestNewPermissions(permissions, onNewPermissionListener);
            return;
        }
    }

    // twitter
    public void StatusUpdateTwitter(String tweetText, String bLink) {
        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        if (!GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN", "").isEmpty()
                && !GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN_SECRET", "").isEmpty()) {

            if (!session.getAuthToken().isExpired())
                new PostTweet().execute(tweetText, bLink);

        }
    }

    // facebook
    public void StatusUpdateFacebook(String msg, String imageLink, String broadcastLink, String broadcastTitle) {
        if (mSimpleFacebook.isAllPermissionsGranted() && mSimpleFacebook.isLogin()) {
            if (!msg.isEmpty() && !imageLink.isEmpty() && !broadcastLink.isEmpty() && !broadcastTitle.isEmpty()) {
                new PostToFacebook().execute(msg, imageLink, broadcastTitle, broadcastLink);
            }
        }
    }

    private twitter4j.Twitter getTwitter() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey("f5NMQCZu98csQVvAQcygt3VtD");
        builder.setOAuthConsumerSecret("NpIUCB1NPHHp8DtuQTBHaSjInuh3CyHVK3yLmAL5sAZQqsQLmG");
        AccessToken accessToken = new AccessToken(GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN", ""),
                GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN_SECRET", ""));
        twitter4j.Twitter mTwitter = new TwitterFactory(builder.build()).getInstance(accessToken);
        return mTwitter;
    }

    // post to social media
    private class PostTweet extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... arg0) {
            twitter4j.Twitter mTwitter = getTwitter();
            try {
                twitter4j.Status response = mTwitter.updateStatus(arg0[0] + arg0[1]);
                isSharedOnTw = true;
                return response.toString();
            } catch (twitter4j.TwitterException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    private class PostToFacebook extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            try {
                final Feed feed = new Feed.Builder()
                        .setMessage(arg0[0])
                        .setName("" + arg0[2])
                        .setCaption("Hapity Broadcast")
                        .setDescription("This is a link to Hapity broadcast...")
                        .setPicture("" + arg0[1])
                        .setLink("" + arg0[3])
                        .build();
                OfflineAfterStreamActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSimpleFacebook.publish(feed, onPublishListener);
                    }
                });
            } catch (FacebookException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return "";
        }
    }

    // web services background async tasks
    private class GetProfileInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlParse = Constants.GET_USER_PROFILE_URL + "user_id=" + params[0] + "&token=" + appToken;
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<ProfileInfo, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(ProfileInfo response) {

                            if (getApplicationContext() != null) {
                                try {
                                    String status = response.status;
                                    if (status.equalsIgnoreCase("success")) {

                                        UserInfo[] userInfo = response.profile_info;

                                        tvBroadcasterUsername.setText("" + userInfo[0].username);
                                        tvNumbersFollowers.setText(response.followers.length + " Followers");
                                        tvNumbersFollowing.setText(response.following.length + " Following");

                                        profilePicLoader.setVisibility(View.VISIBLE);
                                        if (!userInfo[0].profile_picture.isEmpty()) {
                                            Picasso.with(OfflineAfterStreamActivity.this).
                                                    load(userInfo[0].profile_picture).
                                                    placeholder(R.drawable.com_facebook_profile_picture_blank_square).
                                                    into(ivBroadcasterProfilePic, new Callback() {
                                                        @Override
                                                        public void onSuccess() {
                                                            profilePicLoader.setVisibility(View.INVISIBLE);
                                                        }

                                                        @Override
                                                        public void onError() {
                                                            profilePicLoader.setVisibility(View.INVISIBLE);
                                                        }
                                                    });
                                        } else
                                            profilePicLoader.setVisibility(View.INVISIBLE);

                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            if (getApplicationContext() != null) {
                                PrettyToast.showError(getApplicationContext(), "There was a problem connecting to server, Please try again");
                            }
                        }

                    }, new HashMap<String, String>()));
            return "result";
        }
    }
}
