package com.app.hopity.streamActivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.camera2.CaptureRequest;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.app.hopity.R;
import com.app.hopity.adapters.CommentsListAdapter;
import com.app.hopity.afterStreamActivities.OnlineAfterStreamActivity;
import com.app.hopity.appdata.AppConfig;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.fragmentDialog.ProfilesDialogFragment;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.NewBroadcastResponse;
import com.app.hopity.modelsResponse.SingleComment;
import com.app.hopity.utils.StreamUtils;
import com.michael.easydialog.EasyDialog;
import com.pusher.client.Pusher;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;
import com.soundcloud.android.crop.Crop;
import com.sromku.simple.fb.listeners.OnPublishListener;
import com.wmspanel.libstream.SnapshotWriter;
import com.wmspanel.libstream.Streamer;
import com.wmspanel.libstream.StreamerGL;
import com.wmspanel.libstream.StreamerGLBuilder;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.quist.app.errorreporter.ExceptionReporter;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

/**
 * Created by Mushi on 8/30/2016.
 */
public class StreamBroadcastActivity extends StreamBaseActivity implements SurfaceHolder.Callback,
        ConnectionEventListener,
        SubscriptionEventListener,
        ChannelEventListener {

    private static String TAG = "StreamBroadcastActivity";
    // Step One Stream
    protected SnapshotWriter.SnapshotCallback jpegCallback = new SnapshotWriter.SnapshotCallback() {
        @Override
        public void onSnapshotTaken(Bitmap broadcastImageBitmap) {
            Log.e(TAG, "onSnapTaken");
            if (customImageFlag) {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(mCustomImageUrl, bmOptions);
                broadcastImageBase64 = convertBitmapToBase64(bitmap);
                bitmap.recycle();
            } else {
                broadcastImageBase64 = convertBitmapToBase64(broadcastImageBitmap);
            }
            if (!mBroadcastOn) {
                enableStepTwoUI();
                startTheBroadcastStream();
            } else {
                new UploadBroadcastImageTask().execute(broadcastId, broadcastImageBase64,
                        appToken, "no");
            }
        }
    };
    // camera variables
    private String mConnectionProfileType;
    private SurfaceHolder mSurfaceHolder;
    private String mCameraId = "0";
    private StreamerGL mStreamerGL;
    private boolean mUseCamera2;
    private boolean mLockOrientation;
    private boolean mVerticalVideo;
    //connection testing
    private String mURL = "http://www.hapity.com/images/test.jpg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExceptionReporter.register(this);
        setContentView(R.layout.activity_start_broadcast_base);
        enableStepOneUI();

        //initialising step one ui elements
        // top buttons
        mBroadCastTitle = (EditText) findViewById(R.id.etBroadcastTitleStepOneStartBroadcast);
        btnStartBroadcast = (Button) findViewById(R.id.btnStartStepOneStartBroadcast);
        btnShareFacebook = (ImageButton) findViewById(R.id.btnFBShareStepOneStartBroadcast);
        btnShareTwitter = (ImageButton) findViewById(R.id.btnTwitterShareStepOneStartBroadcast);
        btnShareLocation = (ImageButton) findViewById(R.id.btnLocationShareStepOneStartBroadcast);
        btnSwitchCameraStepOne = (ImageButton) findViewById(R.id.btnSwitchCameraStepOneStartBroadcast);
        btnCustomImageStepOne = (ImageButton) findViewById(R.id.btnCustomImageStepOneStartBroadcast);

        btnCustomImageStepOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customImageFlag) {
                    mCustomImageUrl = "";
                    customImageFlag = false;
                    btnCustomImageStepOne.setImageResource(R.drawable.image_icon_custom_disable);
                    new EasyDialog(StreamBroadcastActivity.this)
                            .setLayoutResourceId(R.layout.layout_pop_up_image_off)
                            .setBackgroundColor(ContextCompat.getColor(StreamBroadcastActivity.this, R.color.white))
                            .setLocationByAttachedView(btnCustomImageStepOne)
                            .setGravity(EasyDialog.GRAVITY_TOP)
                            .setAnimationAlphaShow(600, 0.0f, 1.0f)
                            .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                            .setTouchOutsideDismiss(true)
                            .setMatchParent(false)
                            .setMarginLeftAndRight(24, 24)
                            .setOutsideColor(ContextCompat.getColor(StreamBroadcastActivity.this, android.R.color.transparent))
                            .show();
                } else {
                    Crop.pickImage(StreamBroadcastActivity.this);
                }
            }
        });

        btnShareFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!connectFacebookFlag) {
                    // turn facebook sharing on
                    connectFacebookFlag = true;
                    if (!checkFacebookConnectivity()) {
                        getFacebookConnectivity();
                    } else {
                        btnShareFacebook.setImageResource(R.drawable.fb_icon_enable);
                        new EasyDialog(StreamBroadcastActivity.this)
                                .setLayoutResourceId(R.layout.layout_pop_up_fb_on)
                                .setBackgroundColor(ContextCompat.getColor(StreamBroadcastActivity.this, R.color.white))
                                .setLocationByAttachedView(btnShareFacebook)
                                .setGravity(EasyDialog.GRAVITY_TOP)
                                .setAnimationAlphaShow(600, 0.0f, 1.0f)
                                .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                                .setTouchOutsideDismiss(true)
                                .setMatchParent(false)
                                .setMarginLeftAndRight(24, 24)
                                .setOutsideColor(ContextCompat.getColor(StreamBroadcastActivity.this, android.R.color.transparent))
                                .show();
                    }

                } else {
                    connectFacebookFlag = false;
                    btnShareFacebook.setImageResource(R.drawable.fb_icon_disable);
                    new EasyDialog(StreamBroadcastActivity.this)
                            .setLayoutResourceId(R.layout.layout_pop_up_fb_off)
                            .setBackgroundColor(ContextCompat.getColor(StreamBroadcastActivity.this, R.color.white))
                            .setLocationByAttachedView(btnShareFacebook)
                            .setGravity(EasyDialog.GRAVITY_TOP)
                            .setAnimationAlphaShow(600, 0.0f, 1.0f)
                            .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                            .setTouchOutsideDismiss(true)
                            .setMatchParent(false)
                            .setMarginLeftAndRight(24, 24)
                            .setOutsideColor(ContextCompat.getColor(StreamBroadcastActivity.this, android.R.color.transparent))
                            .show();
                }
            }
        });
        btnShareTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!connectTwitterFlag) {
                    connectTwitterFlag = true;
                    if (!checkTwitterConnectivity()) {
                        getTwitterConnectivity();
                    } else {
                        btnShareTwitter.setImageResource(R.drawable.twitter_icon_enble);
                        new EasyDialog(StreamBroadcastActivity.this)
                                .setLayoutResourceId(R.layout.layout_pop_up_tw_on)
                                .setBackgroundColor(ContextCompat.getColor(StreamBroadcastActivity.this, R.color.white))
                                .setLocationByAttachedView(btnShareTwitter)
                                .setGravity(EasyDialog.GRAVITY_TOP)
                                .setAnimationAlphaShow(600, 0.0f, 1.0f)
                                .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                                .setTouchOutsideDismiss(true)
                                .setMatchParent(false)
                                .setMarginLeftAndRight(24, 24)
                                .setOutsideColor(ContextCompat.getColor(StreamBroadcastActivity.this, android.R.color.transparent))
                                .show();
                    }
                } else {
                    connectTwitterFlag = false;
                    btnShareTwitter.setImageResource(R.drawable.twitter_icon_disable);
                    new EasyDialog(StreamBroadcastActivity.this)
                            .setLayoutResourceId(R.layout.layout_pop_up_tw_off)
                            .setBackgroundColor(ContextCompat.getColor(StreamBroadcastActivity.this, R.color.white))
                            .setLocationByAttachedView(btnShareTwitter)
                            .setGravity(EasyDialog.GRAVITY_TOP)
                            .setAnimationAlphaShow(600, 0.0f, 1.0f)
                            .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                            .setTouchOutsideDismiss(true)
                            .setMatchParent(false)
                            .setMarginLeftAndRight(24, 24)
                            .setOutsideColor(ContextCompat.getColor(StreamBroadcastActivity.this, android.R.color.transparent))
                            .show();
                }
            }
        });
        btnShareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!shareLocationFlag) {
                    // Location permission not granted
                    Log.e(TAG, "ShareLocation False");
                    isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    // check if enabled and if not send user to the GSP settings
                    // Better solution would be to display a dialog and suggesting to
                    // go to the settings
                    if (!isGPSEnabled) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                        return;
                    }
                    startLocation();
                } else {
                    shareLocationFlag = false;
                    btnShareLocation.setImageResource(R.drawable.location_icon_disable);
                    Latitude = "0.0";
                    Longitude = "0.0";
                }
            }
        });
        btnSwitchCameraStepOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStreamer == null) {
                    return;
                }
                mStreamerGL.flip();
                if (mCameraId.equalsIgnoreCase("1"))
                    mCameraId = "0";
                else
                    mCameraId = "1";
                // camera is changed, so update aspect ratio to actual value
                updatePreviewRatio();
            }
        });
        btnStartBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onBroadcastStartClicked");
                if (startBroadcastCheck()) {
                    if (checkTitleValid(mBroadCastTitle.getText().toString().trim())) {
                        mStreamerGL.takeSnapshot(jpegCallback);
                    }
                }
            }
        });

        btnStartBroadcast.setText("Initialising...");
        btnStartBroadcast.setEnabled(false);

        //initialise ui objects and loading
        ImageView ivLightning = (ImageView) findViewById(R.id.ivLightningStepTwoStartBroadcast);
        loadingLayout = (RelativeLayout) findViewById(R.id.layoutLoadingStepTwoStartBroadcast);
        tvLoadingLayout = (TextView) findViewById(R.id.tvLoadingStatusStepTwoStartBroadcast);
        // set loading animation
        Animation animLoading = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.tween);
        ivLightning.startAnimation(animLoading);

        mButtonFlash = (ImageView) findViewById(R.id.btnFlashStepTwoStartBroadcast);
        mButtonCamera = (ImageView) findViewById(R.id.btnSwitchCameraStepTwoStartBroadcast);
        mButtonCross = (Button) findViewById(R.id.btnCrossStepTwoStartBroadcast);
        imageRecording = (ImageView) findViewById(R.id.ivRecordingStepTwoStartBroadcast);

        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStreamer == null) {
                    return;
                }
                mStreamerGL.flip();
                if (mCameraId.equalsIgnoreCase("1"))
                    mCameraId = "0";
                else
                    mCameraId = "1";
                // camera is changed, so update aspect ratio to actual value
                updatePreviewRatio();
            }
        });
        mButtonCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitByBackKey();
            }
        });
        mButtonFlash.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.e(TAG, "onClickFlashButton");
                if (mStreamer != null && mVideoCaptureState == Streamer.CAPTURE_STATE.STARTED) {
                    mStreamer.toggleTorch();
                }
            }
        });

        // initializing instances for comments
        // list view things
        commentsList = new ArrayList<>();
        listViewComments = (ListView) findViewById(R.id.listViewCommentsStepTwoStartBroadcast);
        mCommentAdapter = new CommentsListAdapter(this, commentsList);
        listViewComments.setAdapter(mCommentAdapter);
        listViewComments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SingleComment commenter = commentsList.get(position);
                // show profile info
                FragmentManager manager = getSupportFragmentManager();
                ProfilesDialogFragment dialog = new ProfilesDialogFragment("Broadcaster", appToken, appUserId, commenter.commenterUserId);
                dialog.show(manager, "dialog");
            }
        });
        // viewers list
        tvNumberLiveViewers = (TextView) findViewById(R.id.tvViewersStepTwoStartBroadcast);
        tvNumberLiveViewers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewersSweetSheet.isShow())
                    mViewersSweetSheet.dismiss();
                mViewersSweetSheet.toggle();
            }
        });
        setupBroadcastsSweetSheetView();
        // simple facebook
        onPublishListener = new OnPublishListener() {
            @Override
            public void onComplete(String postId) {
            }
        };
        // initializing pusher instances
        // registering pusher listener
        pusher = new Pusher("ed469f4dd7ae71e71eb8");
        pusher.connect(this);

        //initializing camera
        mCameraId = "0";
        mConnectionProfileType = "high";
        mLockOrientation = false;
        final int orientation = getResources().getConfiguration().orientation;
        mVerticalVideo = orientation == Configuration.ORIENTATION_PORTRAIT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mUseCamera2 = StreamUtils.allowCamera2Support(this);
        }

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceViewStartBroadcast);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        setupSharingIcons();
        setupUI(findViewById(R.id.rootLayoutStartBroadcast));
    }

    @Override
    public void onBackPressed() {
        if (mBroadcastOn) {
            exitByBackKey();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBroadcastOn) {
            pusher.disconnect();
            stopRepeatingTask();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        // it is transition from onPause() to onResume(), we already have surface
//        if (mSurfaceHolder != null) {
//            Log.d(TAG, "Resuming after pause");
//            createStreamer(mSurfaceHolder);
//        }
        // else it is transition
        // onCreate() -> onStart() -> onResume()
        // or
        // onPause() -> surfaceDestroyed() -> onStop() -> onRestart() -> onStart() -> onResume()
        // in both scenarios we should wait for surfaceCreated callback
    }

    private void enableStepTwoUI() {
        StreamBroadcastActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.rootLayoutStepTwoStartBroadcast).setVisibility(View.VISIBLE);
                findViewById(R.id.rootLayoutStepOneStartBroadcast).setVisibility(View.INVISIBLE);
            }
        });
    }

    private void enableStepOneUI() {
        StreamBroadcastActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.rootLayoutStepOneStartBroadcast).setVisibility(View.VISIBLE);
                findViewById(R.id.rootLayoutStepTwoStartBroadcast).setVisibility(View.INVISIBLE);
            }
        });
    }

    //Step two utility methods and services
    private void showAfterBroadcastShareDialog() {
        //call after stream dialog
        GlobalSharedPrefs.viewersOfBroadcasts = mListViewers;
        Intent intent = new Intent(this, OnlineAfterStreamActivity.class);
        intent.putExtra("bImageUrl", broadcastImageReturnedUrl);
        intent.putExtra("bUserId", broadcastId);
        intent.putExtra("broadcastTitle", broadCastTitle);
        intent.putExtra("bShareUrl", broadcastShareUrl);
        startActivity(intent);

        // releasing connections
        releaseStreamer();
        pusher.disconnect();
        finish();
    }

    private void exitByBackKey() {
        new AlertDialog.Builder(this)
                .setMessage("Would you like to terminate this stream?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (mBroadcastOn) {
                            stopRepeatingTask();
                            String BROADCAST_OFFLINE_URL = Constants.OFFLINE_BROADCAST_URL
                                    + "token/" + appToken
                                    + "/broadcast_id/" + broadcastId;
                            new BroadcastStatusTask().execute(BROADCAST_OFFLINE_URL, "offline");
                            showAfterBroadcastShareDialog();
                        } else {
                            releaseConnection(true);
                            pusher.disconnect();
                            finish();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                    }
                })
                .show();
    }

    //Step two methods and interfaces
    //pusher methods
    @Override
    public void onSubscriptionSucceeded(String channelName) {
    }

    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {
    }

    @Override
    public void onError(String message, String code, Exception e) {
        pusher.connect();
    }

    @Override
    public void onEvent(String channelName, String eventName, String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            String status = jsonObject.getString("status");

            SingleComment comment = new SingleComment(status,
                    jsonObject.getString("user_id"),
                    "@" + jsonObject.getString("user_name"),
                    jsonObject.getString("profile_picture"),
                    jsonObject.getString("comment"));
            Log.e(TAG, comment.toString());
            commentsList.add(comment);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listViewComments.smoothScrollToPosition(listViewComments.getCount() - 1);
                    if (commentsList.size() > 50) {
                        UpdateListViewCommentSize(50);
                    }
                    mCommentAdapter.notifyDataSetChanged();
                }
            });

            if (status.equalsIgnoreCase("join")) {
                Log.e(TAG, "list response joined ");
                JSONArray viewerJsonArray = jsonObject.getJSONArray("viewer_list");
                mListViewers.clear();

                for (int i = 0; i < viewerJsonArray.length(); i++) {
                    JSONObject job = viewerJsonArray.getJSONObject(i);
                    UserInfo user = new UserInfo();
                    user.user_id = Integer.parseInt(job.getString("sid"));
                    user.username = job.getString("username");
                    user.profile_picture = job.getString("profile_picture");
                    mListViewers.add(user);
                }
            }

            if (status.equalsIgnoreCase("left")) {
                Log.e(TAG, "list response joined ");
                JSONArray viewerJsonArray = jsonObject.getJSONArray("viewer_list");
                mListViewers.clear();

                for (int i = 0; i < viewerJsonArray.length(); i++) {
                    JSONObject job = viewerJsonArray.getJSONObject(i);
                    UserInfo user = new UserInfo();
                    user.user_id = Integer.parseInt(job.getString("sid"));
                    user.username = job.getString("username");
                    user.profile_picture = job.getString("profile_picture");

                    mListViewers.add(user);
                }
            }

            if (status.equalsIgnoreCase("block")) {
                JSONArray viewerJsonArray = jsonObject.getJSONArray("viewer_list");
                mListViewers.clear();

                for (int i = 0; i < viewerJsonArray.length(); i++) {
                    JSONObject job = viewerJsonArray.getJSONObject(i);
                    UserInfo user = new UserInfo();
                    user.user_id = Integer.parseInt(job.getString("sid"));
                    user.username = job.getString("username");
                    user.profile_picture = job.getString("profile_picture");
                    mListViewers.add(user);
                }
            }

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvNumberLiveViewers.setText(" " + mListViewers.size() + " ");
                    mAdapterListSweetSheet.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated()");
        mSurfaceHolder = holder;
        // We got surface to draw on, start streamer creation
        createStreamer(mSurfaceHolder);
        btnStartBroadcast.setText("Start Streaming");
        btnStartBroadcast.setEnabled(true);
    }

    //720p bitrate="2000000"
    //360p bitrate="800000"
    //240p bitrate="300000"
    private void createStreamer(SurfaceHolder holder) {
        Log.e(TAG, "createStreamer()");
        StreamerGLBuilder builder = new StreamerGLBuilder();
        List<Streamer.CameraInfo> cameraList = builder.getCameraList(this, mUseCamera2);

        if (cameraList == null || cameraList.size() == 0) {
            return;
        }

        Streamer.CameraInfo cameraInfo = StreamUtils.getActiveCameraInfo(cameraList, mCameraId);
        // common
        builder.setContext(this);
        // Streamer.Listener implementation, see MainActivityBase class for details
        builder.setListener(this);
        builder.setUserAgent("Hapity" + "/" + Streamer.VERSION_NAME);
        // audio
        int sampleRate = Integer.parseInt("44100");
        builder.setSampleRate(sampleRate);
        int channelCount = Integer.parseInt("1");
        builder.setChannelCount(channelCount);
        int audioSrc = StreamUtils.audioSource(2);
        builder.setAudioSource(audioSrc);
        // video
        builder.setCamera2(mUseCamera2);
        // focus mode
        if (mUseCamera2) {
            builder.setFocusMode(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
        } else {
            builder.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        // preview size will use resolution set by builder.setVideoSize(...)
        builder.setCameraId(mCameraId);
        // Streamer is limited to same resolution for stream and preview
        Streamer.Size videoSize = StreamUtils.getVideoSize(cameraInfo);
        builder.setVideoSize(mVerticalVideo ? new Streamer.Size(videoSize.height, videoSize.width) : videoSize);
        float fps = Float.parseFloat("10");
        builder.setFrameRate(fps);
        int keyFrameInterval = Integer.parseInt("2");
        builder.setKeyFrameInterval(keyFrameInterval);
        int videoBitRate = Integer.parseInt("2000000");
        builder.setBitRate(videoBitRate);
        builder.setSurface(holder.getSurface());
        SurfaceView sv = (SurfaceView) findViewById(R.id.surfaceViewStartBroadcast);
        builder.setSurfaceSize(new Streamer.Size(sv.getWidth(), sv.getHeight()));
        final int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            builder.setVideoOrientation(StreamerGL.ORIENTATIONS.PORTRAIT);
        } else {
            builder.setVideoOrientation(StreamerGL.ORIENTATIONS.LANDSCAPE);
        }
        builder.setDisplayRotation(getWindowManager().getDefaultDisplay().getRotation());
        // start adding cameras from default camera, then add second camera
        // larix uses same resolution for camera preview and stream to simplify setup
        Streamer.Size camVideoSize = videoSize;
        // add first camera to flip list, make sure you called setVideoSize(...) before
        builder.addCamera(mCameraId, camVideoSize);
        // set start position in flip list to camera id
        builder.setCameraId(mCameraId);
        Log.e(TAG, "Camera #" + mCameraId + " size: " + Integer.toString(camVideoSize.width) + "x" + Integer.toString(camVideoSize.height));
        // Set the same video size for both cameras
        // If not possible (for example front camera has no FullHD support)
        // try to find video size with the same aspect ratio
        String flipCameraId = null;
        if (cameraList.size() > 1) {
            Streamer.Size flipSize = null;
            boolean found = false;
            for (int i = 0; i < cameraList.size(); i++) {
                cameraInfo = cameraList.get(i);
                if (cameraInfo.cameraId.equals(mCameraId)) {
                    continue;
                } else {
                    flipCameraId = cameraInfo.cameraId;
                }
                // If secondary camera supports same resolution, use it
                for (int ii = 0; ii < cameraInfo.recordSizes.length; ii++) {
                    if (cameraInfo.recordSizes[ii].width == videoSize.width &&
                            cameraInfo.recordSizes[ii].height == videoSize.height) {
                        flipSize = new Streamer.Size(cameraInfo.recordSizes[ii].width, cameraInfo.recordSizes[ii].height);
                        found = true;
                        break;
                    }
                }
                // If same resolution not found, search for same aspect ratio
                if (!found) {
                    for (int ii = 0; ii < cameraInfo.recordSizes.length; ii++) {
                        if (cameraInfo.recordSizes[ii].width < videoSize.width) {
                            double mTargetAspect = (double) videoSize.width / videoSize.height;
                            double viewAspectRatio = (double) cameraInfo.recordSizes[ii].width / cameraInfo.recordSizes[ii].height;
                            double aspectDiff = mTargetAspect / viewAspectRatio - 1;
                            if (Math.abs(aspectDiff) < 0.01) {
                                flipSize = new Streamer.Size(cameraInfo.recordSizes[ii].width, cameraInfo.recordSizes[ii].height);
                                found = true;
                                break;
                            }
                        }
                    }
                }
                // Same aspect ratio not found, search for less or similar frame sides
                if (!found) {
                    for (int ii = 0; ii < cameraInfo.recordSizes.length; ii++) {
                        if (cameraInfo.recordSizes[ii].height <= videoSize.height &&
                                cameraInfo.recordSizes[ii].width <= videoSize.width) {
                            flipSize = new Streamer.Size(cameraInfo.recordSizes[ii].width, cameraInfo.recordSizes[ii].height);
                            found = true;
                            break;
                        }
                    }
                }
                // Nothing found, use default
                if (!found) {
                    flipSize = new Streamer.Size(cameraInfo.recordSizes[0].width, cameraInfo.recordSizes[0].height);
                }
            }
            // flipSize == null should never happen
            if (flipSize != null) {
                // add second camera to flip list
                builder.addCamera(flipCameraId, flipSize);
                Log.e(TAG, "Camera #" + flipCameraId + " size: " + Integer.toString(flipSize.width) + "x" + Integer.toString(flipSize.height));
            }
            //enable switch camera
            mButtonCamera.setVisibility(flipSize != null ? View.VISIBLE : View.INVISIBLE);
        } else {
            //disable switch camera
            mButtonCamera.setVisibility(View.INVISIBLE);
        }
        mStreamerGL = builder.build();
        if (mStreamerGL != null) {
            mStreamer = mStreamerGL;
            // Streamer build succeeded, can start Video/Audio capture
            // call startVideoCapture, wait for onVideoCaptureStateChanged callback
            mStreamerGL.startVideoCapture();
            // call startAudioCapture, wait for onAudioCaptureStateChanged callback
            mStreamerGL.startAudioCapture();
            // Deal with preview's aspect ratio
            updatePreviewRatio();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged() " + width + "x" + height);
        if (mStreamer != null) {
            mStreamerGL.setSurfaceSize(new Streamer.Size(width, height));
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "MainActivity: surfaceDestroyed()");
        mSurfaceHolder = null;
        releaseStreamer();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (mStreamer == null) {
            return;
        }

        int orientation = getResources().getConfiguration().orientation;
        if (!mLockOrientation) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mStreamerGL.setVideoOrientation(StreamerGL.ORIENTATIONS.PORTRAIT);
            } else {
                mStreamerGL.setVideoOrientation(StreamerGL.ORIENTATIONS.LANDSCAPE);
            }
        }

        // Set display rotation to flip image correctly, should be called always
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        mStreamerGL.setDisplayRotation(rotation);

        updatePreviewRatio();

        String degrees = Integer.toString(Streamer.rotationToDegrees(rotation));
        Log.d(TAG, "onConfigurationChanged; orientation: " + Integer.toString(orientation) + ", rotation is: " + degrees);
    }

    private void updatePreviewRatio() {
        Streamer.Size preview_size = mStreamerGL.getActiveCameraVideoSize();
        if (preview_size == null) {
            return;
        }

        AspectFrameLayout layout = (AspectFrameLayout) findViewById(R.id.aspectLayoutStartBroadcast);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layout.setAspectRatio(preview_size.getRatio());
        } else {
            // Vertical video, so reverse aspect ratio
            layout.setAspectRatio(preview_size.getVerticalRatio());
        }
    }

    //stream connection methods
    private void startTheBroadcastStream() {
        GlobalSharedPrefs.hapityPref.edit().putBoolean("facebook_share_settings", connectFacebookFlag).apply();
        GlobalSharedPrefs.hapityPref.edit().putBoolean("twitter_share_settings", connectTwitterFlag).apply();
        GlobalSharedPrefs.hapityPref.edit().putBoolean("location_share_settings", shareLocationFlag).apply();
        // make the broadcast url and start broadcast
        broadcastStreamUrl = "rtsp://52.18.194.183:1935/testStream/myStreamOne";
        String urlStartBroadcast = null;
        String time = Calendar.HOUR + "" + Calendar.MINUTE + "" + Calendar.SECOND;
        String date = Calendar.DAY_OF_MONTH + "" + Calendar.MONTH + "" + Calendar.YEAR;
        String partialBroadcastID = time + date + System.currentTimeMillis() + ".stream";
        try {
            if (shareLocationFlag) {
                urlStartBroadcast = Constants.START_BROADCAST_URL
                        + "title=" + URLEncoder.encode(broadCastTitle, "UTF-8")
                        + "&geo_location=" + Latitude + "," + Longitude
                        + "&allow_user_messages=No"
                        + "&is_sensitive=" + isSensitiveFlagValue
                        + "&user_id=" + appUserId
                        + "&token=" + appToken
                        + "&stream_url=" + partialBroadcastID;
            } else {
                urlStartBroadcast = Constants.START_BROADCAST_URL
                        + "title=" + URLEncoder.encode(broadCastTitle, "UTF-8")
                        + "&geo_location=0,0"
                        + "&allow_user_messages=No"
                        + "&is_sensitive=" + isSensitiveFlagValue
                        + "&user_id=" + appUserId
                        + "&token=" + appToken
                        + "&stream_url=" + partialBroadcastID;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new StartBroadcastTask().execute(urlStartBroadcast);
    }

    private void StartStreamingConnection(String streamPath) {
        if (createConnection(streamPath)) {
            mBroadcastOn = true;
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            mStreamerGL.setDisplayRotation(rotation);
            // Don't try to set landscape/reverse_landscape orientation manually
            // With API 18+ just need to set SCREEN_ORIENTATION_LOCKED
            if (mLockOrientation) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            }
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                mStreamerGL.setVideoOrientation(StreamerGL.ORIENTATIONS.PORTRAIT);
            } else {
                mStreamerGL.setVideoOrientation(StreamerGL.ORIENTATIONS.LANDSCAPE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mSurfaceHolder != null) {
            Log.d(TAG, "onResult after pause");
            createStreamer(mSurfaceHolder);
        }
        if (requestCode == Crop.REQUEST_PICK && resultCode == Activity.RESULT_OK && data != null) {
            String path = getPath(this, data.getData());
            Log.e(TAG, "Result Received by Crop : No crop needed, path : " + path);
            processCustomImageSelected(path);
        }
    }

    private void processCustomImageSelected(String path) {
        mCustomImageUrl = path;
        customImageFlag = true;
        btnCustomImageStepOne.setImageResource(R.drawable.image_icon_custom_enable);
        new EasyDialog(StreamBroadcastActivity.this)
                .setLayoutResourceId(R.layout.layout_pop_up_image_on)
                .setBackgroundColor(ContextCompat.getColor(StreamBroadcastActivity.this, R.color.white))
                .setLocationByAttachedView(btnCustomImageStepOne)
                .setGravity(EasyDialog.GRAVITY_TOP)
                .setAnimationAlphaShow(600, 0.0f, 1.0f)
                .setAnimationAlphaDismiss(600, 1.0f, 0.0f)
                .setTouchOutsideDismiss(true)
                .setMatchParent(false)
                .setMarginLeftAndRight(24, 24)
                .setOutsideColor(ContextCompat.getColor(StreamBroadcastActivity.this, android.R.color.transparent))
                .show();
    }

    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKatOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKatOrAbove && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    // web services background async tasks
    protected class StartBroadcastTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlParse = params[0];
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<NewBroadcastResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(NewBroadcastResponse response) {
                            try {
                                if (response.status.equalsIgnoreCase("start")) {

                                    enableLoadingInterface("Starting a Session...");
                                    tvNumberLiveViewers.setText(" " + mListViewers.size() + " ");

                                    broadcastId = "" + response.broadcast_id;
                                    broadcastShareUrl = response.share_url;

                                    // setting wowza server credentials here and client connecting
                                    String ip, port, path;
                                    Pattern uri = Pattern.compile("rtsp://(.+):(\\d*)/(.+)");
                                    Matcher m = uri.matcher(response.stream_url);
                                    m.find();
                                    ip = m.group(1);
                                    port = m.group(2);
                                    path = m.group(3);

                                    broadcastStreamUrl = "rtsp://" + AppConfig.PUBLISHER_USERNAME + ":" + AppConfig.PUBLISHER_PASSWORD +
                                            "@" + ip + ":" + port +
                                            "/" +
                                            path;
                                    Log.e(TAG, broadcastStreamUrl);
                                    StartStreamingConnection(broadcastStreamUrl.replace("_360p", ""));
                                    // Subscribe to a channel
                                    pusher.subscribe("Broadcast-" + response.broadcast_id,
                                            StreamBroadcastActivity.this, "Broadcast");
                                    // now start handler
                                    startRepeatingTask();
                                    // now send broadcast image to server
                                    new UploadBroadcastImageTask().execute(response.broadcast_id + "",
                                            broadcastImageBase64,
                                            appToken, "yes");
                                } else {
                                    PrettyToast.showError(getApplicationContext(),
                                            "We are sorry there was an error starting your broadcast please try again later.");
                                    finish();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            PrettyToast.showError(getApplicationContext(),
                                    "We are sorry there was an error starting your broadcast please try again later.");
                            finish();
                        }

                    }, new HashMap<String, String>()));
            return "result";
        }
    }
}
