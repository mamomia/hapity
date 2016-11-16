package com.app.hopity.afterStreamActivities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.hopity.DashboardActivity;
import com.app.hopity.R;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.models.UserInfo;
import com.app.hopity.utils.ImageUtils;
import com.facebook.FacebookException;
import com.pkmmte.view.CircularImageView;
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

import java.util.ArrayList;
import java.util.List;

import de.quist.app.errorreporter.ExceptionReporter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class OnlineAfterStreamActivity extends AppCompatActivity {

    private static final String TAG = "OnlineAfterStream";
    private String appToken, appUserId;
    private SimpleFacebook mSimpleFacebook;
    private OnPublishListener onPublishListener;
    private TextView tvBroadcastTitle;
    private ImageView ivBroadcastImage;
    private boolean isSharedOnFb = false, isSharedOnTw = false;
    private OnLoginListener onLoginListener;
    private OnNewPermissionsListener onNewPermissionListener;
    private TwitterAuthClient mTwitterAuthClient;
    private String broadcastImage;
    private String broadcastTitle;
    private String broadcastShareUrl;
    private boolean isFromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set up a full-screen black window.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ExceptionReporter.register(this);
        setContentView(R.layout.activity_online_after_stream);

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
        isFromNotification = getIntent().getBooleanExtra("isFromNotification", false);
//        intent.putExtra("bUserId", broadcast_user_id);

        // simple facebook
        onPublishListener = new OnPublishListener() {
            @Override
            public void onComplete(String postId) {
                isSharedOnFb = true;
            }
        };

        tvBroadcastTitle = (TextView) findViewById(R.id.tvBroadcastTitleAfterOnline);
        ivBroadcastImage = (ImageView) findViewById(R.id.ivBroadcastImageAfterOnline);
        ImageUtils.loadWebImageIntoSimple(ivBroadcastImage, broadcastImage, OnlineAfterStreamActivity.this);
        tvBroadcastTitle.setText(broadcastTitle);

        findViewById(R.id.actionLocalShareAfterOnline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Watch this broadcast! now at  " + broadcastShareUrl);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share this broadcast with..."));
            }
        });
        findViewById(R.id.actionFBShareAfterOnline).setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.actionTWShareAfterOnline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSharedOnTw && checkTwitterConnectivity())
                    StatusUpdateTwitter("Check this stream on #Hapity: " + broadcastTitle + " ", broadcastShareUrl);
                else
                    getTwitterConnectivity();
            }
        });
        findViewById(R.id.btnDoneAfterOnline).setOnClickListener(new View.OnClickListener() {
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
        ListView mylist = (ListView) findViewById(R.id.listViewViewersAfterOnlinePlay);
        mylist.setAdapter(new ViewersAdapter(this, GlobalSharedPrefs.viewersOfBroadcasts));
    }

    @Override
    protected void onDestroy() {
        if (isFromNotification) {
            Intent i = new Intent(OnlineAfterStreamActivity.this, DashboardActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
        super.onDestroy();
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
    protected boolean checkTwitterConnectivity() {
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

    protected void getTwitterConnectivity() {
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
    protected boolean checkFacebookConnectivity() {
        if (!mSimpleFacebook.isLogin() || !mSimpleFacebook.isAllPermissionsGranted())
            return false;
        else
            return true;

    }

    protected void getFacebookConnectivity() {
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
                OnlineAfterStreamActivity.this.runOnUiThread(new Runnable() {
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

    // Followers Followers ListView Adaptors
    public class ViewersAdapter extends BaseAdapter {
        ArrayList<UserInfo> result;
        Context context;
        private LayoutInflater inflater = null;

        public ViewersAdapter(Activity mainActivity, ArrayList<UserInfo> data) {
            result = data;
            context = mainActivity;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return result.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder = new Holder();
            View rowView;
            UserInfo item = result.get(position);

            if (convertView == null) {
                rowView = (View) inflater.inflate(R.layout.followerfollowing_list_item, null);
                // Do some initialization
            } else {
                rowView = convertView;
            }

            holder.tv = (TextView) rowView.findViewById(R.id.tvUsernameFFItem);
            holder.img = (CircularImageView) rowView.findViewById(R.id.ivProfilePicFFItem);

            holder.tv.setText(item.username);
            if (!item.profile_picture.isEmpty())
                ImageUtils.loadWebImageIntoCircular(holder.img, item.profile_picture, OnlineAfterStreamActivity.this);

            return rowView;
        }

        public class Holder {
            TextView tv;
            CircularImageView img;
        }
    }
}
