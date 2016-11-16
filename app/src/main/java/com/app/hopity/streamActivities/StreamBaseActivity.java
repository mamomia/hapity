package com.app.hopity.streamActivities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.app.hopity.R;
import com.app.hopity.adapters.BroadcastViewersAdapter;
import com.app.hopity.adapters.CommentsListAdapter;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.fragmentDialog.ProfilesDialogFragment;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.BroadcastImageResp;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.SingleComment;
import com.app.hopity.modelsResponse.SuccessModel;
import com.app.hopity.utils.StorageUtils;
import com.facebook.FacebookException;
import com.michael.easydialog.EasyDialog;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.pusher.client.Pusher;
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
import com.wmspanel.libstream.Streamer;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.quist.app.errorreporter.ExceptionReporter;
import twitter4j.StatusUpdate;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Mushi on 8/30/2016.
 */
public class StreamBaseActivity extends AppCompatActivity implements Streamer.Listener {

    private static int mRetries = 0;
    // sharing and ui variables
    protected ImageButton btnShareFacebook, btnShareTwitter, btnShareLocation, btnSwitchCameraStepOne, btnCustomImageStepOne;
    protected Button btnStartBroadcast;
    protected TwitterAuthClient mTwitterAuthClient;
    protected SimpleFacebook mSimpleFacebook;
    protected OnLoginListener onLoginListener;
    protected OnNewPermissionsListener onNewPermissionListener;
    protected OnPublishListener onPublishListener;
    protected EditText mBroadCastTitle;
    protected String broadcastImageBase64, broadCastTitle, Longitude, Latitude, isSensitiveFlagValue,
            broadcastStreamUrl, broadcastShareUrl, broadcastId, broadcastImageReturnedUrl, mCustomImageUrl;
    protected String appToken, appUserId;

    //values for step two
    //previous activity data
    protected boolean connectFacebookFlag, connectTwitterFlag, shareLocationFlag, customImageFlag;

    // previous relieved data
    protected Pusher pusher;
    protected TextView tvNumberLiveViewers;
    protected RelativeLayout loadingLayout;
    protected TextView tvLoadingLayout;
    protected Button mButtonCross;
    protected ImageView mButtonCamera, mButtonFlash, imageRecording;
    // comments data
    protected ListView listViewComments;
    protected CommentsListAdapter mCommentAdapter;
    protected List<SingleComment> commentsList;
    protected SweetSheet mViewersSweetSheet;
    protected BroadcastViewersAdapter mAdapterListSweetSheet;
    protected ArrayList<UserInfo> mListViewers;
    protected Streamer mStreamer;
    protected boolean mBroadcastOn;
    protected int mConnectionId = -1;
    protected LocationManager mLocationManager;
    protected boolean isGPSEnabled;
    protected Handler mHandlerBroadcastStatus;
    // repeating tasks for showing broadcast duration
    protected final Runnable broadcastRunnable = new Runnable() {
        public void run() {
            try {
                String url = Constants.TIMESTAMP_BROADCAST_URL
                        + "token/" + appToken
                        + "/broadcast_id/" + broadcastId;
                if (mBroadcastOn)
                    new BroadcastStatusTask().execute(url, "online");
            } catch (Exception e) {
                e.printStackTrace();
            }
            mHandlerBroadcastStatus.postDelayed(broadcastRunnable, 60000);
        }
    };
    Streamer.CAPTURE_STATE mVideoCaptureState = Streamer.CAPTURE_STATE.FAILED;
    Streamer.CAPTURE_STATE mAudioCaptureState = Streamer.CAPTURE_STATE.FAILED;
    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if (location != null) {
                Latitude = "" + location.getLatitude();
                Longitude = "" + location.getLongitude();
                shareLocationFlag = true;
                btnShareLocation.setImageResource(R.drawable.location_icon_enable);
            } else {
                shareLocationFlag = false;
                btnShareLocation.setImageResource(R.drawable.location_icon_disable);
                Latitude = "0.0";
                Longitude = "0.0";
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            shareLocationFlag = false;
            btnShareLocation.setImageResource(R.drawable.location_icon_disable);
            Latitude = "0.0";
            Longitude = "0.0";
        }
    };
    private String TAG = "StreamBaseActivity";
    //streamer variables
    private Handler mHandler;
    private Streamer.CONNECTION_STATE mConnectionState;
    private String mLocationProvider;

    protected static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    protected void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(StreamBaseActivity.this);
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
        ExceptionReporter.register(this);

        // Set up a full-screen black window.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSimpleFacebook = SimpleFacebook.getInstance(this);
        if (mTwitterAuthClient == null) {
            try {
                mTwitterAuthClient = new TwitterAuthClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        new GlobalSharedPrefs(this);
        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
        appUserId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);
        broadcastImageBase64 = "";
        broadCastTitle = "Untitled";
        Longitude = "0";
        Latitude = "0";
        isSensitiveFlagValue = "";
        broadcastStreamUrl = "";
        broadcastShareUrl = "";
        broadcastId = "";
        broadcastImageReturnedUrl = "";
        mCustomImageUrl = "";

        if (getIntent().getBooleanExtra("isSensitive", false))
            isSensitiveFlagValue = "yes";
        else
            isSensitiveFlagValue = "no";

        mHandler = new Handler(Looper.getMainLooper());
        mHandlerBroadcastStatus = new Handler(Looper.getMainLooper());

        connectFacebookFlag = GlobalSharedPrefs.hapityPref.getBoolean("facebook_share_settings", true);
        connectTwitterFlag = GlobalSharedPrefs.hapityPref.getBoolean("twitter_share_settings", true);
        shareLocationFlag = GlobalSharedPrefs.hapityPref.getBoolean("location_share_settings", false);
        customImageFlag = false;

        //for location
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();
        mLocationProvider = mLocationManager.getBestProvider(criteria, false);
        // simple facebook
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
                    connectFacebookFlag = true;
                    btnShareFacebook.setImageResource(R.drawable.fb_icon_enable);
                    new EasyDialog(StreamBaseActivity.this)
                            .setLayoutResourceId(R.layout.layout_pop_up_fb_on)
                            .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                            .setLocationByAttachedView(btnShareFacebook)
                            .setGravity(EasyDialog.GRAVITY_TOP)
                            .setAnimationAlphaShow(600, 0.0f, 1.0f)
                            .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                            .setTouchOutsideDismiss(true)
                            .setMatchParent(false)
                            .setMarginLeftAndRight(24, 24)
                            .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                            .show();
                }
            }

            @Override
            public void onCancel() {
                // user canceled the dialog
                Log.i(TAG, "User Canceled");
                connectFacebookFlag = false;
                btnShareFacebook.setImageResource(R.drawable.fb_icon_disable);
                new EasyDialog(StreamBaseActivity.this)
                        .setLayoutResourceId(R.layout.layout_pop_up_fb_off)
                        .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                        .setLocationByAttachedView(btnShareFacebook)
                        .setGravity(EasyDialog.GRAVITY_TOP)
                        .setAnimationAlphaShow(600, 0.0f, 1.0f)
                        .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                        .setTouchOutsideDismiss(true)
                        .setMatchParent(false)
                        .setMarginLeftAndRight(24, 24)
                        .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                        .show();
            }

            @Override
            public void onFail(String reason) {
                // failed to login
                Log.i(TAG, "Failed to Login");
                connectFacebookFlag = false;
                btnShareFacebook.setImageResource(R.drawable.fb_icon_disable);
                new EasyDialog(StreamBaseActivity.this)
                        .setLayoutResourceId(R.layout.layout_pop_up_fb_off)
                        .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                        .setLocationByAttachedView(btnShareFacebook)
                        .setGravity(EasyDialog.GRAVITY_TOP)
                        .setAnimationAlphaShow(600, 0.0f, 1.0f)
                        .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                        .setTouchOutsideDismiss(true)
                        .setMatchParent(false)
                        .setMarginLeftAndRight(24, 24)
                        .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                        .show();
            }

            @Override
            public void onException(Throwable throwable) {
                // exception from facebook
                Log.i(TAG, "Exception from facebook");
                connectFacebookFlag = false;
                btnShareFacebook.setImageResource(R.drawable.fb_icon_disable);
                new EasyDialog(StreamBaseActivity.this)
                        .setLayoutResourceId(R.layout.layout_pop_up_fb_off)
                        .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                        .setLocationByAttachedView(btnShareFacebook)
                        .setGravity(EasyDialog.GRAVITY_TOP)
                        .setAnimationAlphaShow(600, 0.0f, 1.0f)
                        .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                        .setTouchOutsideDismiss(true)
                        .setMatchParent(false)
                        .setMarginLeftAndRight(24, 24)
                        .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                        .show();
            }
        };

        onNewPermissionListener = new OnNewPermissionsListener() {
            @Override
            public void onSuccess(String s, List<Permission> list, List<Permission> list1) {
                Log.i(TAG, "Got Permission");
                connectFacebookFlag = true;
                btnShareFacebook.setImageResource(R.drawable.fb_icon_enable);
                new EasyDialog(StreamBaseActivity.this)
                        .setLayoutResourceId(R.layout.layout_pop_up_fb_on)
                        .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                        .setLocationByAttachedView(btnShareFacebook)
                        .setGravity(EasyDialog.GRAVITY_TOP)
                        .setAnimationAlphaShow(600, 0.0f, 1.0f)
                        .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                        .setTouchOutsideDismiss(true)
                        .setMatchParent(false)
                        .setMarginLeftAndRight(24, 24)
                        .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                        .show();
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "Cancel Permission");
                connectFacebookFlag = false;
                btnShareFacebook.setImageResource(R.drawable.fb_icon_disable);
                new EasyDialog(StreamBaseActivity.this)
                        .setLayoutResourceId(R.layout.layout_pop_up_fb_off)
                        .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                        .setLocationByAttachedView(btnShareFacebook)
                        .setGravity(EasyDialog.GRAVITY_TOP)
                        .setAnimationAlphaShow(600, 0.0f, 1.0f)
                        .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                        .setTouchOutsideDismiss(true)
                        .setMatchParent(false)
                        .setMarginLeftAndRight(24, 24)
                        .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                        .show();
            }

            @Override
            public void onException(Throwable throwable) {
                Log.i(TAG, "Exception Permission");
                connectFacebookFlag = false;
                btnShareFacebook.setImageResource(R.drawable.fb_icon_disable);
                new EasyDialog(StreamBaseActivity.this)
                        .setLayoutResourceId(R.layout.layout_pop_up_fb_off)
                        .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                        .setLocationByAttachedView(btnShareFacebook)
                        .setGravity(EasyDialog.GRAVITY_TOP)
                        .setAnimationAlphaShow(600, 0.0f, 1.0f)
                        .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                        .setTouchOutsideDismiss(true)
                        .setMatchParent(false)
                        .setMarginLeftAndRight(24, 24)
                        .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                        .show();
            }

            @Override
            public void onFail(String s) {
                Log.i(TAG, "Fail Permission");
                connectFacebookFlag = false;
                btnShareFacebook.setImageResource(R.drawable.fb_icon_disable);
                new EasyDialog(StreamBaseActivity.this)
                        .setLayoutResourceId(R.layout.layout_pop_up_fb_off)
                        .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                        .setLocationByAttachedView(btnShareFacebook)
                        .setGravity(EasyDialog.GRAVITY_TOP)
                        .setAnimationAlphaShow(600, 0.0f, 1.0f)
                        .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                        .setTouchOutsideDismiss(true)
                        .setMatchParent(false)
                        .setMarginLeftAndRight(24, 24)
                        .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                        .show();
            }
        };

        // for step two
    }

    //step one utility methods and calls

    @Override
    protected void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Applications should release the camera immediately in onPause()
        // https://developer.android.com/guide/topics/media/camera.html#release-camera
        releaseStreamer();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "ACTIVITY RESULT : " + resultCode + " REQUEST : " + requestCode);
        if (requestCode == 64206) {
            mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == 140) {
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected String convertBitmapToBase64(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] byteArrayImage = baos.toByteArray();
        return Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
    }

    protected void setupSharingIcons() {

        btnCustomImageStepOne.setImageResource(R.drawable.image_icon_custom_disable);

        if (connectFacebookFlag)
            btnShareFacebook.setImageResource(R.drawable.fb_icon_enable);
        else
            btnShareFacebook.setImageResource(R.drawable.fb_icon_disable);

        if (connectTwitterFlag)
            btnShareTwitter.setImageResource(R.drawable.twitter_icon_enble);
        else
            btnShareTwitter.setImageResource(R.drawable.twitter_icon_disable);

        if (shareLocationFlag)
            btnShareLocation.setImageResource(R.drawable.location_icon_enable);
        else
            btnShareLocation.setImageResource(R.drawable.location_icon_disable);
    }

    // check if all conditions meet then start broadcast
    protected boolean startBroadcastCheck() {
        if (connectTwitterFlag) {
            if (!checkTwitterConnectivity()) {
                getTwitterConnectivity();
                return false;
            }
        }
        if (connectFacebookFlag) {
            if (!checkFacebookConnectivity()) {
                getFacebookConnectivity();
                return false;
            }
        }
        if (shareLocationFlag) {
            startLocation();
        }
        return true;
    }

    // check the title entered is valid
    protected boolean checkTitleValid(String title) {
        if (title.isEmpty()) {
            broadCastTitle = "Untitled";
        } else
            broadCastTitle = title;
        return true;
    }

    //getting sharing options tasks
    protected void startLocation() {
        Log.e(TAG, "ShareLocation startLocation");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // getting GPS status
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // check if GPS enabled
        if (isGPSEnabled) {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                Latitude = "" + location.getLatitude();
                Longitude = "" + location.getLongitude();
                shareLocationFlag = true;
                btnShareLocation.setImageResource(R.drawable.location_icon_enable);
            } else {
                mLocationManager.requestLocationUpdates(mLocationProvider, 0, 0, locationListener);
            }
        }
    }

    // check and get twitter connectivity methods
    protected boolean checkTwitterConnectivity() {
        TwitterSession session =
                Twitter.getSessionManager().getActiveSession();

        if (GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN", "").isEmpty()
                || GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN_SECRET", "").isEmpty())
            return false;
        else return !session.getAuthToken().isExpired();
    }

    protected void getTwitterConnectivity() {
        mTwitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
            @Override
            public void success(final Result<TwitterSession> twitterSessionResult) {

                new GlobalSharedPrefs(StreamBaseActivity.this);

                TwitterSession session =
                        Twitter.getSessionManager().getActiveSession();
                GlobalSharedPrefs.hapityPref.edit().putString("TWITTER_ACCESS_TOKEN",
                        session.getAuthToken().token)
                        .apply();
                GlobalSharedPrefs.hapityPref.edit().putString("TWITTER_ACCESS_TOKEN_SECRET",
                        session.getAuthToken().secret)
                        .apply();

                connectTwitterFlag = true;
                btnShareTwitter.setImageResource(R.drawable.twitter_icon_enble);
                new EasyDialog(StreamBaseActivity.this)
                        .setLayoutResourceId(R.layout.layout_pop_up_tw_on)
                        .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                        .setLocationByAttachedView(btnShareTwitter)
                        .setGravity(EasyDialog.GRAVITY_TOP)
                        .setAnimationAlphaShow(600, 0.0f, 1.0f)
                        .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                        .setTouchOutsideDismiss(true)
                        .setMatchParent(false)
                        .setMarginLeftAndRight(24, 24)
                        .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                        .show();
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
                connectTwitterFlag = false;
                btnShareTwitter.setImageResource(R.drawable.twitter_icon_disable);
                new EasyDialog(StreamBaseActivity.this)
                        .setLayoutResourceId(R.layout.layout_pop_up_tw_off)
                        .setBackgroundColor(StreamBaseActivity.this.getResources().getColor(R.color.white))
                        .setLocationByAttachedView(btnShareTwitter)
                        .setGravity(EasyDialog.GRAVITY_TOP)
                        .setAnimationAlphaShow(600, 0.0f, 1.0f)
                        .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                        .setTouchOutsideDismiss(true)
                        .setMatchParent(false)
                        .setMarginLeftAndRight(24, 24)
                        .setOutsideColor(getApplicationContext().getResources().getColor(android.R.color.transparent))
                        .show();
            }
        });
    }

    // check and get facebook connectivity methods
    protected boolean checkFacebookConnectivity() {
        return !(!mSimpleFacebook.isLogin() || !mSimpleFacebook.isAllPermissionsGranted());

    }

    protected void getFacebookConnectivity() {
        Log.i(TAG, "inside not facebook login and permission");
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

    // comment and ui methods
    protected void enableActiveInterface() {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.tween);
        imageRecording.startAnimation(anim);
        tvLoadingLayout.setText("");
        loadingLayout.setVisibility(View.INVISIBLE);
    }

    // comment and ui methods
    protected void enableLoadingInterface(String loading) {
        imageRecording.setVisibility(View.GONE);
        tvLoadingLayout.setText(loading);
        loadingLayout.setVisibility(View.VISIBLE);
    }

    //adding broadcasts of a cluster in sweet sheet
    protected void setupBroadcastsSweetSheetView() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.rootLayoutStepTwoStartBroadcast);
        mViewersSweetSheet = new SweetSheet(layout);
        CustomDelegate customDelegate = new CustomDelegate(true,
                CustomDelegate.AnimationType.DuangLayoutAnimation);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_broadcast_viewers, null, false);
        customDelegate.setCustomView(view);
        mViewersSweetSheet.setDelegate(customDelegate);

        Toolbar toolbarViewers = (Toolbar) view.findViewById(R.id.toolbarBroadcastViewers);
        toolbarViewers.setVisibility(View.INVISIBLE);
        ListView mListView = (ListView) view.findViewById(R.id.listViewersBroadcastViewers);

        mListViewers = new ArrayList<>();
        mAdapterListSweetSheet = new BroadcastViewersAdapter(this, mListViewers);
        mListView.setAdapter(mAdapterListSweetSheet);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserInfo item = (UserInfo) parent.getItemAtPosition(position);
                FragmentManager manager = getSupportFragmentManager();
                ProfilesDialogFragment dialog = new ProfilesDialogFragment("Broadcaster", appToken, appUserId, "" + item.user_id);
                dialog.show(manager, "dialog");
            }
        });
    }

    protected void UpdateListViewCommentSize(int numOfRows) {
        int rowsToRemove = commentsList.size() - numOfRows;
        for (int i = 0; i < rowsToRemove; i++) {
            commentsList.remove(i);
        }
    }

    //web services and social media and broadcast functions
    // twitter
    protected void StatusUpdateTwitter(String tweetText, String bLink) {
        TwitterSession session = Twitter.getSessionManager().getActiveSession();
        if (!GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN", "").isEmpty()
                && !GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN_SECRET", "").isEmpty()) {

            if (!session.getAuthToken().isExpired())
                new PostTweet().execute(tweetText, bLink);
        }
    }

    // facebook
    protected void StatusUpdateFacebook(String msg, String imageLink, String broadcastLink, String broadcastTitle) {
        if (mSimpleFacebook.isAllPermissionsGranted() && mSimpleFacebook.isLogin()) {
            if (!msg.isEmpty() && !imageLink.isEmpty() && !broadcastLink.isEmpty() && !broadcastTitle.isEmpty()) {
                new PostToFacebook().execute(msg, imageLink, broadcastTitle, broadcastLink);
            }
        }
    }

    protected twitter4j.Twitter getTwitter() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey("f5NMQCZu98csQVvAQcygt3VtD");
        builder.setOAuthConsumerSecret("NpIUCB1NPHHp8DtuQTBHaSjInuh3CyHVK3yLmAL5sAZQqsQLmG");
        AccessToken accessToken = new AccessToken(GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN", ""),
                GlobalSharedPrefs.hapityPref.getString("TWITTER_ACCESS_TOKEN_SECRET", ""));
        twitter4j.Twitter mTwitter = new TwitterFactory(builder.build()).getInstance(accessToken);
        return mTwitter;
    }

    // step two larix methods
    // Applications should release the camera immediately in onPause()
    // https://developer.android.com/guide/topics/media/camera.html#release-camera
    protected void releaseStreamer() {
        // check if Streamer instance exists
        if (mStreamer == null) {
            return;
        }
        // stop broadcast
        releaseConnection(true);
        // stop mp4 recording
        mStreamer.stopRecord();
        // cancel audio and video capture
        mStreamer.stopAudioCapture();
        mStreamer.stopVideoCapture();
        // finally release streamer, after release(), the object is no longer available
        // if a Streamer is in released state, all methods will throw an IllegalStateException
        mStreamer.release();
        // sanitize Streamer object holder
        mStreamer = null;
    }

    protected boolean createConnection(String conn0_uri) {

        if (mStreamer == null) {
            enableLoadingInterface("Stream is not initialized ...");
            return false;
        }
        if (conn0_uri.isEmpty()) {
            Log.e(TAG, "connection uri is empty");
            enableLoadingInterface("Connection settings are incorrect...");
            return false;
        }
        boolean isStreamerReady =
                (mVideoCaptureState == Streamer.CAPTURE_STATE.STARTED &&
                        mAudioCaptureState == Streamer.CAPTURE_STATE.STARTED);

        if (!isStreamerReady) {
            Log.e(TAG, "AudioCaptureState=" + mAudioCaptureState);
            Log.e(TAG, "VideoCaptureState=" + mVideoCaptureState);
            enableLoadingInterface("Initiating Broadcast ...");
            return false;
        }

        mConnectionId = mStreamer.createConnection(conn0_uri, Streamer.MODE.AUDIO_VIDEO, this);
        startRecord();
        if (mConnectionId != -1) {
            enableLoadingInterface("Connecting ...");
            return true;
        }
        return false;
    }

    protected void releaseConnection(boolean stopBroadcast) {
        if (stopBroadcast) {
            mBroadcastOn = false;
        }
        if (mStreamer != null && mConnectionId != -1) {
            mStreamer.stopRecord();
            mStreamer.releaseConnection(mConnectionId);
        }
    }

    protected void startRecord() {
        boolean started = false;
        File f = StorageUtils.newMp4File();
        if (f != null && mStreamer != null) {
            started = mStreamer.startRecord(f);
        }
        if (!started) {
            Toast.makeText(this, "Unable to record stream to local storage.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionStateChanged(int connectionId, Streamer.CONNECTION_STATE state, Streamer.STATUS status) {
        Log.d(TAG, "onConnectionStateChanged, state=" + state);

        Runnable mBroadcastConnectionLostRunnable = new Runnable() {
            @Override
            public void run() {
                if (mBroadcastOn) {
                    if (mRetries < 1) {
                        mRetries++;
                        createConnection(broadcastStreamUrl);
                    } else {
                        mHandler.removeCallbacks(this);
                        String BROADCAST_OFFLINE_URL = Constants.OFFLINE_BROADCAST_URL
                                + "token/" + appToken
                                + "/broadcast_id/" + broadcastId;
                        new BroadcastStatusTask().execute(BROADCAST_OFFLINE_URL, "offline");
                        stopRepeatingTask();
                        pusher.disconnect();
                        releaseStreamer();
                        finish();
                    }
                }
            }
        };

        if (mStreamer == null) {
            return;
        }

        if (mConnectionId == connectionId) {
            mConnectionState = state;
        }

        switch (state) {
            case INITIALIZED:
                break;
            case CONNECTED:
                //server is connected now and is streaming
                mHandler.removeCallbacks(mBroadcastConnectionLostRunnable);
                mRetries = 0;
                mBroadcastOn = true;
                enableActiveInterface();
                startRepeatingTask();
                break;
            case SETUP:
                break;
            case RECORD:
                break;
            case DISCONNECTED:
                if (connectionId != mConnectionId) {
                    Log.e(TAG, "unregistered connection");
                    break;
                }
                mConnectionId = -1;

                if (mBroadcastOn) {
                    //stream was running when connection dropped
                    mBroadcastOn = false;
                    enableLoadingInterface("Connection Lost Retrying...");
                    stopRepeatingTask();
                }

                // do not try to reconnect in case of wrong credentials
                final boolean stop = (status == Streamer.STATUS.AUTH_FAIL);
                if (stop) {
                    String BROADCAST_OFFLINE_URL = Constants.OFFLINE_BROADCAST_URL
                            + "token/" + appToken
                            + "/broadcast_id/" + broadcastId;
                    new BroadcastStatusTask().execute(BROADCAST_OFFLINE_URL, "offline");
                    stopRepeatingTask();
                    pusher.disconnect();
                    releaseStreamer();
                    finish();
                }
                mHandler.postDelayed(mBroadcastConnectionLostRunnable, 3000);
                break;
        }
    }

    @Override
    public void onVideoCaptureStateChanged(Streamer.CAPTURE_STATE state) {
        Log.d(TAG, "onVideoCaptureStateChanged, state=" + state);
        mVideoCaptureState = state;
        switch (state) {
            case STARTED:
                // can start broadcasting video
                // mVideoCaptureState will be checked in createConnection()
                break;
            case STOPPED:
                // stop confirmation
                break;
            case ENCODER_FAIL:
            case FAILED:
            default:
                if (mStreamer != null) {
                    mStreamer.stopRecord();
                    mStreamer.stopVideoCapture();
                }
                Log.e(TAG, state == Streamer.CAPTURE_STATE.ENCODER_FAIL
                        ? "video encoder failed" : "video capture failed");
                finish();
                break;
        }
    }

    @Override
    public void onAudioCaptureStateChanged(Streamer.CAPTURE_STATE state) {
        Log.d(TAG, "onAudioCaptureStateChanged, state=" + state);
        mAudioCaptureState = state;
        switch (state) {
            case STARTED:
                // can start broadcasting audio
                // mAudioCaptureState will be checked in createConnection()
                break;
            case STOPPED:
                // stop confirmation
                break;
            case ENCODER_FAIL:
            case FAILED:
            default:
                if (mStreamer != null) {
                    mStreamer.stopRecord();
                    mStreamer.stopAudioCapture();
                }
                Log.e(TAG, state == Streamer.CAPTURE_STATE.ENCODER_FAIL
                        ? "audio encoder failed" : "audio capture failed");
                finish();
                break;
        }
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    protected void stopRepeatingTask() {
        if (mHandlerBroadcastStatus != null && broadcastRunnable != null) {
            mHandlerBroadcastStatus.removeCallbacks(broadcastRunnable);
        }
    }

    protected void startRepeatingTask() {
        broadcastRunnable.run();
    }

    // web services background async tasks
    protected class BroadcastStatusTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlParse = params[0];
            final String status = params[1];
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<SuccessModel, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(SuccessModel response) {
                            //upload new picture now
                            if (status.equalsIgnoreCase("online") && mBroadcastOn) {
                                // now send broadcast image to server
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                        }

                    }, new HashMap<String, String>()));
            return "result";
        }
    }

    protected class UploadBroadcastImageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            Log.e("UploadImageTask", "doInBackground");
            String urlParse = Constants.INSERT_BROADCAST_IMAGE_URL;
            HashMap<String, String> paramsHash = new HashMap<>();
            paramsHash.put("broadcast_id", params[0]);
            paramsHash.put("broadcast_image", params[1]);
            paramsHash.put("token", params[2]);
            final String isFirstRun = params[3];

            RestClientManager.getInstance().makeJsonRequest(Request.Method.POST,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<BroadcastImageResp, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(BroadcastImageResp response) {
                            broadcastImageReturnedUrl = response.img_path;
                            if (isFirstRun.equalsIgnoreCase("yes")) {
                                if (connectFacebookFlag) {
                                    StatusUpdateFacebook(
                                            "LIVE on #Hapity",
                                            broadcastImageReturnedUrl,
                                            broadcastShareUrl,
                                            broadCastTitle);
                                }
                                if (connectTwitterFlag) {
                                    StatusUpdateTwitter("LIVE on #Hapity: " + broadCastTitle + " ", broadcastShareUrl);
                                }
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                        }
                    }, paramsHash));
            return "result";
        }
    }

    // post to social media
    protected class PostTweet extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... arg0) {
            twitter4j.Twitter mTwitter = getTwitter();
            try {
                StatusUpdate st = new StatusUpdate(arg0[0] + arg0[1]);
                if (customImageFlag)
                    st.setMedia(new File(mCustomImageUrl));
                twitter4j.Status response = mTwitter.updateStatus(st);
                return response.toString();
            } catch (twitter4j.TwitterException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    protected class PostToFacebook extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... arg0) {
            try {
                final Feed feed = new Feed.Builder()
                        .setMessage(arg0[0])
                        .setName("" + arg0[2])
                        .setCaption("Hapity Live Broadcast")
                        .setDescription("This is a link to my Hapity broadcast...")
                        .setPicture("" + arg0[1])
                        .setLink("" + arg0[3])
                        .build();
                StreamBaseActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSimpleFacebook.publish(feed, onPublishListener);
                    }
                });
            } catch (FacebookException e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}