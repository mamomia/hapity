package com.app.hopity.playingActivities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.CaptioningManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.app.hopity.R;
import com.app.hopity.adapters.BroadcastViewersAdapter;
import com.app.hopity.adapters.CommentsListAdapter;
import com.app.hopity.afterStreamActivities.OnlineAfterStreamActivity;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.fragmentDialog.ProfilesDialogFragment;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.ReportResponse;
import com.app.hopity.modelsResponse.SingleComment;
import com.app.hopity.modelsResponse.StatusBroadcastResponse;
import com.app.hopity.player.DashRendererBuilder;
import com.app.hopity.player.DemoPlayer;
import com.app.hopity.player.EventLogger;
import com.app.hopity.player.ExtractorRendererBuilder;
import com.app.hopity.player.HlsRendererBuilder;
import com.app.hopity.player.RendererBuilder;
import com.app.hopity.player.SmoothStreamingRendererBuilder;
import com.app.hopity.player.SmoothStreamingTestMediaDrmCallback;
import com.app.hopity.player.StreamInstance;
import com.app.hopity.player.WidevineTestMediaDrmCallback;
import com.app.hopity.streamActivities.AspectFrameLayout;
import com.app.hopity.utils.ImageUtils;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.google.android.exoplayer.util.Util;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.pkmmte.view.CircularImageView;
import com.pusher.client.Pusher;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionStateChange;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.quist.app.errorreporter.ExceptionReporter;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class OnlineStreamBroadcastActivity extends AppCompatActivity implements
        ConnectionEventListener, SubscriptionEventListener, ChannelEventListener,
        SurfaceHolder.Callback,
        DemoPlayer.Listener,
        DemoPlayer.CaptionListener,
        DemoPlayer.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener {

    private static final CookieManager defaultCookieManager;
    protected static String appToken;
    private static boolean IS_FIRST_READY_STREAM = true;
    private static boolean BUFFERING_THREAD_IS_RUNNING = false;
    private static int BUFFERING_COUNT = 0;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    // pusher and comment elements
    private Pusher pusher;
    private ListView listViewComments;
    private CommentsListAdapter adapter;
    private List<SingleComment> commentsList;
    private EditText etComment;
    private String broadcast_user_id;
    private String broadcast_id;
    private String StreamFileName;
    // testing variables
    private String TAG = "Online Stream Broadcast";
    private RelativeLayout mLoadingView;
    private String broadcast_share_url;
    private TextView tvLoadingView;
    private String appUserId;
    DialogInterface.OnClickListener actionListenerReport = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0: // Report User
                    // Report This User
                    AlertDialog.Builder builderUser = new AlertDialog.Builder(OnlineStreamBroadcastActivity.this)
                            .setTitle("")
                            .setMessage("Are you sure you want to report this user?")
                            .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Report Now
                                    ReportUserTask task = new ReportUserTask();
                                    task.execute(appToken, appUserId, broadcast_user_id);

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
                case 1: // Report Broadcast
                    // Report This Broadcast
                    AlertDialog.Builder builderBroadcast = new AlertDialog.Builder(OnlineStreamBroadcastActivity.this)
                            .setTitle("")
                            .setMessage("Are you sure you want to report this broadcast?")
                            .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Report Now
                                    ReportBroadcastTask task = new ReportBroadcastTask();
                                    task.execute(appToken, appUserId, broadcast_id);

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
                    break;
                default:
                    break;
            }
        }
    };
    private AlertDialog actionsReport;
    private TextView tvNumberLiveViewers;
    private String broadcast_title;
    private String broadcast_image;
    private SweetSheet mViewersSweetSheet;
    private BroadcastViewersAdapter mAdapterListSweetSheet;
    private ArrayList<UserInfo> mListViewers;
    private boolean isFromNotification;
    private EventLogger eventLogger;
    private MediaController mediaController;
    private View shutterView;
    private SurfaceView surfaceView;
    private DemoPlayer player;
    private boolean playerNeedsPrepare;
    private Uri contentUri;
    private int contentType;
    private String contentId;
    private String provider;
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private SubtitleLayout subtitleLayout;
    private boolean isWebCast = false;
    private int mVideoWidth = 0, mVideoHeight = 0;
    private float mPixelWidthAspectRatio = 0;
    private Handler BUFFERING_COUNTER_HANDLER = new Handler();
    private Runnable BUFFERING_COUNTER_RUNNABLE = new Runnable() {
        public void run() {
            Log.e(TAG, "Buffer Count : " + BUFFERING_COUNT);
            if (BUFFERING_COUNT == 15) {
                BUFFERING_THREAD_IS_RUNNING = false;
                BUFFERING_COUNTER_HANDLER.removeCallbacks(BUFFERING_COUNTER_RUNNABLE);
                ShowAfterBroadcastInfoActivity();
            } else {
                BUFFERING_COUNT++;
                BUFFERING_THREAD_IS_RUNNING = true;
                BUFFERING_COUNTER_HANDLER.postDelayed(this, 1000);
            }
        }
    };

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {
        if (!(view instanceof EditText) && !(view instanceof ImageView)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(OnlineStreamBroadcastActivity.this);
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

    private void enableActiveInterface() {
        tvLoadingView.setText("");
        mLoadingView.setVisibility(View.GONE);
    }

    private void enableLoadingInterface(String loading) {
        mLoadingView.setVisibility(View.VISIBLE);
        tvLoadingView.setText(loading);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExceptionReporter.register(this);
        super.onCreate(savedInstanceState);

        // Set up a full-screen black window.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.setBackgroundDrawableResource(android.R.color.black);

        StreamFileName = getIntent().getStringExtra("streamPathFileName");
        broadcast_id = getIntent().getStringExtra("broadcastId");
        broadcast_title = getIntent().getStringExtra("broadcastTitle");
        broadcast_user_id = getIntent().getStringExtra("bUserId");
        broadcast_share_url = getIntent().getStringExtra("bShareUrl");
        broadcast_image = getIntent().getStringExtra("bImageUrl");
        isFromNotification = getIntent().getBooleanExtra("isFromNotification", false);
        isWebCast = StreamFileName.contains("_360p");

        Log.e(TAG, " isWebCast : " + isWebCast);
        Log.e(TAG, "Stream file name : " + StreamFileName);
        if (isWebCast)
            setContentView(R.layout.activity_online_stream_webcast);
        else
            setContentView(R.layout.activity_online_stream_broadcast);

        new GlobalSharedPrefs(this);
        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
        appUserId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        // loading animation
        tvLoadingView = (TextView) findViewById(R.id.tvBufferingUpdateOnlineStream);
        mLoadingView = (RelativeLayout) findViewById(R.id.layoutProgressBarOnlineStream);
        ImageView ivEyes = (ImageView) findViewById(R.id.ivEyesOnlineStream);
        // set loading animation
        Animation animLoading = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.tween);
        ivEyes.startAnimation(animLoading);
        enableLoadingInterface("Initialising ...");

        // initializing player
        if (StreamFileName.isEmpty()) {
            PrettyToast.showError(getApplicationContext(), "Sorry, We cannot play this broadcast. Invalid Stream");
            finish();
        } else {

            // Subscribe to a channel and connecting pusher
            pusher = new Pusher("ed469f4dd7ae71e71eb8");
            pusher.connect(this);

            View root = findViewById(R.id.rootLayoutOnlineStream);

            shutterView = findViewById(R.id.shutterOnline);
            surfaceView = (SurfaceView) findViewById(R.id.surfaceViewOnlineStream);
            surfaceView.getHolder().addCallback(this);
            subtitleLayout = (SubtitleLayout) findViewById(R.id.subtitlesOnline);

            mediaController = new KeyCompatibleMediaController(this);
            mediaController.setAnchorView(root);

            CookieHandler currentHandler = CookieHandler.getDefault();
            if (currentHandler != defaultCookieManager) {
                CookieHandler.setDefault(defaultCookieManager);
            }

            audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
            audioCapabilitiesReceiver.register();

            enableLoadingInterface("Connecting ...");
            String ip = StreamFileName.replace("rtsp", "rtmp");
            onStreamStart(ip);
        }

        // initializing intent and its values
        TextView tvBroadcastShare = (TextView) findViewById(R.id.shareTextOnlineStream);
        CircularImageView profileDp = (CircularImageView) findViewById(R.id.profileDpOnlineStream);

        ImageUtils.loadWebImageIntoCircular(profileDp, getIntent().getStringExtra("bUserProfilePic"), OnlineStreamBroadcastActivity.this, 1024, 1024);
        tvBroadcastShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Watch me Live! now at  " + broadcast_share_url);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share this broadcast with..."));
            }
        });

        // viewers list
        tvNumberLiveViewers = (TextView) findViewById(R.id.tvViewersOnlineStream);
        tvNumberLiveViewers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mViewersSweetSheet.isShow())
                    mViewersSweetSheet.dismiss();
                mViewersSweetSheet.toggle();
            }
        });
        setupBroadcastsSweetSheetView();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        String[] options = {"Report this user", "Report this broadcast"};
        builder.setItems(options, actionListenerReport);
        actionsReport = builder.create();

        etComment = (EditText) findViewById(R.id.etCommentOnlineStream);
        // list view things
        commentsList = new ArrayList<>();
        listViewComments = (ListView) findViewById(R.id.listCommentOnlineStream);
        adapter = new CommentsListAdapter(this, commentsList);
        listViewComments.setAdapter(adapter);
        listViewComments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SingleComment commenter = commentsList.get(position);
                // show profile info
                FragmentManager manager = getSupportFragmentManager();
                ProfilesDialogFragment dialog = new ProfilesDialogFragment("Stream", appToken, appUserId, commenter.commenterUserId);
                dialog.show(manager, "dialog");
            }
        });

        setupUI(findViewById(R.id.rootLayoutOnlineStream));
    }

    //adding broadcasts of a cluster in sweet sheet
    private void setupBroadcastsSweetSheetView() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.rootLayoutOnlineStream);
        mViewersSweetSheet = new SweetSheet(layout);
        CustomDelegate customDelegate = new CustomDelegate(true,
                CustomDelegate.AnimationType.DuangLayoutAnimation);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_broadcast_viewers, null, false);
        customDelegate.setCustomView(view);
        mViewersSweetSheet.setDelegate(customDelegate);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarBroadcastViewers);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitleTextAppearance(getApplicationContext(), R.style.TitleBarText);
        toolbar.setTitle(broadcast_title);

        if (!appUserId.equalsIgnoreCase(broadcast_user_id)) {
            toolbar.inflateMenu(R.menu.menu_stream_broadcast);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    actionsReport.show();
                    return false;
                }
            });
        }

        ListView mListView = (ListView) view.findViewById(R.id.listViewersBroadcastViewers);

        mListViewers = new ArrayList<>();
        mAdapterListSweetSheet = new BroadcastViewersAdapter(this, mListViewers);
        mListView.setAdapter(mAdapterListSweetSheet);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserInfo item = (UserInfo) parent.getItemAtPosition(position);
                FragmentManager manager = getSupportFragmentManager();
                ProfilesDialogFragment dialog = new ProfilesDialogFragment("Stream", appToken, appUserId, item.sid);
                dialog.show(manager, "dialog");
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        if (isWebCast)
            updatePreviewRatioWebcast();
        else
            updatePreviewRatioBroadcast();
    }

    private void updatePreviewRatioBroadcast() {
        if (player == null) {
            return;
        }
        AspectFrameLayout mLayout = (AspectFrameLayout) findViewById(R.id.aspectLayoutOnlineStream);

        Point size = new Point();
        // // Get the dimensions of the video
        int videoWidth = surfaceView.getHolder().getSurfaceFrame().width();
        int videoHeight = surfaceView.getHolder().getSurfaceFrame().height();
        float aspectRatio = (float) videoWidth / videoHeight;
        // Get the SurfaceView layout parameters
        android.view.ViewGroup.LayoutParams surflp = mLayout.getLayoutParams();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            size.x = size.y = 0;
            //Get the width of the screen
            getWindowManager().getDefaultDisplay().getSize(size);
            int screenWidths = size.x;
            surflp.width = screenWidths;
            surflp.height = (int) (aspectRatio * (float) screenWidths);
            // make the screen full if the video is in landscape mode
            if (videoWidth < videoHeight) {
                surflp.height = size.y;
            }
        } else {
            size.x = size.y = 0;
            //Get the width of the screen
            getWindowManager().getDefaultDisplay().getSize(size);
            int screenHeight = size.y;
            surflp.height = screenHeight;
            surflp.width = (int) (aspectRatio * (float) screenHeight);
            // make the screen full if the video is in landscape mode
            if (videoHeight < videoWidth) {
                surflp.width = size.x;
            }
        }
        if (mLayout != null) {
            mLayout.setLayoutParams(surflp);
        }
    }

    private void updatePreviewRatioWebcast() {
        if (player == null) {
            return;
        }
        AspectRatioFrameLayout mVideoFrameLayout = (AspectRatioFrameLayout) findViewById(R.id.aspectLayoutOnlineStream);
        mVideoFrameLayout.setAspectRatio(mVideoHeight == 0 ? 1 : (mVideoWidth * mPixelWidthAspectRatio) / mVideoHeight);
    }

    @Override
    public void onBackPressed() {
        if (mViewersSweetSheet.isShow())
            mViewersSweetSheet.dismiss();
        else
            exitStreamBroadcastActivity();
    }

    private void exitStreamBroadcastActivity() {
        new AlertDialog.Builder(this)
                .setMessage("Would you like to terminate this stream?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ShowAfterBroadcastInfoActivity();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void ShowAfterBroadcastInfoActivity() {
        if (player != null)
            releasePlayer();
        //call after stream dialog
        GlobalSharedPrefs.viewersOfBroadcasts = mListViewers;
        Intent intent = new Intent(this, OnlineAfterStreamActivity.class);
        intent.putExtra("bImageUrl", broadcast_image);
        intent.putExtra("bUserId", broadcast_user_id);
        intent.putExtra("broadcastTitle", broadcast_title);
        intent.putExtra("bShareUrl", broadcast_share_url);
        intent.putExtra("isFromNotification", isFromNotification);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionStateChange(ConnectionStateChange change) {
        System.out.println("Pusher State changed to " + change.getCurrentState() +
                " from " + change.getPreviousState());
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

            if (status.equalsIgnoreCase("close")) {
                ShowAfterBroadcastInfoActivity();
                return;
            }

            SingleComment comment = new SingleComment(status,
                    jsonObject.getString("user_id"),
                    "@" + jsonObject.getString("user_name"),
                    jsonObject.getString("profile_picture"),
                    jsonObject.getString("comment"));

            commentsList.add(comment);
            OnlineStreamBroadcastActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listViewComments.smoothScrollToPosition(listViewComments.getCount() - 1);
                    if (commentsList.size() > 100) {
                        OnlineStreamBroadcastActivity.this.UpdateListViewCommentSize(100);
                    }
                    adapter.notifyDataSetChanged();
                    etComment.setText("");
                }
            });

            if (status.equalsIgnoreCase("join")) {
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
                if (jsonObject.getString("user_id").equalsIgnoreCase(appUserId)) {
                    ShowAfterBroadcastInfoActivity();
                } else {
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
            }

            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String viewerSize = " " + mListViewers.size() + " ";
                    tvNumberLiveViewers.setText(viewerSize);
                    mAdapterListSweetSheet.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSubscriptionSucceeded(String channelName) {
        Log.v("PUSHER Subscription", "Subscribed to " + channelName);
    }

    // keeping only four items in a listview
    private void UpdateListViewCommentSize(int numOfRows) {
        int rowstoRemove = commentsList.size() - numOfRows;
        for (int i = 0; i < rowstoRemove; i++) {
            commentsList.remove(i);
        }
    }

    public void sendComment(View view) {
        String cmnt = etComment.getText().toString().trim();
        etComment.setText("");
        if (!cmnt.isEmpty())
            new SendCommentTask().execute(broadcast_id, appUserId, cmnt);
        cmnt = "";
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (player.getPlaybackState() == DemoPlayer.STATE_ENDED)
            Log.e(TAG, "State is Ended according to get state");

        if (playbackState == ExoPlayer.STATE_ENDED) {
            ShowAfterBroadcastInfoActivity();
            return;
        }
        String text = "";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "Loading ...";

                if (IS_FIRST_READY_STREAM) {
                    IS_FIRST_READY_STREAM = false;
                    new SetViewStatusBroadcastTask().execute(Constants.JOIN_BROADCAST, appToken, appUserId, broadcast_id);
                    pusher.subscribe("Broadcast-" + broadcast_id, this, "Broadcast");
                }

                if (player.getPlaybackState() != DemoPlayer.STATE_PREPARING)
                    findViewById(R.id.layoutOverlayProgressBarOnlineStream).setAlpha(0.3f);

                if (!BUFFERING_THREAD_IS_RUNNING && BUFFERING_COUNT == 0) {
                    BUFFERING_COUNTER_RUNNABLE.run();
                    BUFFERING_THREAD_IS_RUNNING = true;
                }
                break;
            case ExoPlayer.STATE_IDLE:
                text += "Connection is idle, Please try again ...";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "Preparing ...";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                BUFFERING_COUNT = 0;
                BUFFERING_THREAD_IS_RUNNING = false;
                BUFFERING_COUNTER_HANDLER.removeCallbacks(BUFFERING_COUNTER_RUNNABLE);
                enableActiveInterface();
                return;
            default:
                text += "Unknown error, Please start again ...";
                break;
        }
        enableLoadingInterface(text);
    }

    // Internal methods
    @Override
    public void onNewIntent(Intent intent) {
        releasePlayer();
        setIntent(intent);
    }

    private void onStreamStart(String fileUrl) {
        IS_FIRST_READY_STREAM = true;
        StreamInstance mStream = new StreamInstance("", fileUrl, Util.TYPE_OTHER);
        contentUri = Uri.parse(mStream.uri);
        contentType = mStream.type;
        contentId = mStream.contentId;
        provider = mStream.provider;

        configureSubtitleView();
        if (player == null) {
            preparePlayer(true);
        } else {
            player.setBackgrounded(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            onHidden();
        }
        pusher.disconnect();
        audioCapabilitiesReceiver.unregister();
        new SetViewStatusBroadcastTask().execute(Constants.LEFT_BROADCAST, appToken, appUserId, broadcast_id);
        BUFFERING_COUNT = 0;
        BUFFERING_THREAD_IS_RUNNING = false;
        BUFFERING_COUNTER_HANDLER.removeCallbacks(BUFFERING_COUNTER_RUNNABLE);
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            onHidden();
        }
    }

    private void onHidden() {
        releasePlayer();
        shutterView.setVisibility(View.VISIBLE);
    }

    // AudioCapabilitiesReceiver.Listener methods
    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        if (player == null) {
            return;
        }
        boolean backgrounded = player.getBackgrounded();
        boolean playWhenReady = player.getPlayWhenReady();
        releasePlayer();
        preparePlayer(playWhenReady);
        player.setBackgrounded(backgrounded);
    }

    // SurfaceHolder.Callback implementation
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    private void configureSubtitleView() {
        CaptionStyleCompat style;
        float fontScale;
        if (Util.SDK_INT >= 19) {
            style = getUserCaptionStyleV19();
            fontScale = getUserCaptionFontScaleV19();
        } else {
            style = CaptionStyleCompat.DEFAULT;
            fontScale = 1.0f;
        }
        subtitleLayout.setStyle(style);
        subtitleLayout.setFractionalTextSize(SubtitleLayout.DEFAULT_TEXT_SIZE_FRACTION * fontScale);
    }

    @TargetApi(19)
    private float getUserCaptionFontScaleV19() {
        CaptioningManager captioningManager =
                (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
        return captioningManager.getFontScale();
    }

    @TargetApi(19)
    private CaptionStyleCompat getUserCaptionStyleV19() {
        CaptioningManager captioningManager =
                (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
        return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
    }

    private RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        switch (contentType) {
            case Util.TYPE_SS:
                return new SmoothStreamingRendererBuilder(this, userAgent, contentUri.toString(),
                        new SmoothStreamingTestMediaDrmCallback());
            case Util.TYPE_DASH:
                return new DashRendererBuilder(this, userAgent, contentUri.toString(),
                        new WidevineTestMediaDrmCallback(contentId, provider));
            case Util.TYPE_HLS:
                return new HlsRendererBuilder(this, userAgent, contentUri.toString());
            case Util.TYPE_OTHER:
                return new ExtractorRendererBuilder(this, userAgent, contentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
    }

    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);
            player.setCaptionListener(this);
            player.setMetadataListener(this);
            //player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
            enableLoadingInterface("loading ...");
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

    // DemoPlayer.Listener implementation
    @Override
    public void onError(Exception e) {
        String errorString = null;

        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            errorString = getString(Util.SDK_INT < 18 ? R.string.error_drm_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown);
        } else if (e instanceof ExoPlaybackException
                && e.getCause() instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
            // Special case for decoder initialization failures.
            MediaCodecTrackRenderer.DecoderInitializationException decoderInitializationException =
                    (MediaCodecTrackRenderer.DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    errorString = getString(R.string.error_querying_decoders);
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = getString(R.string.error_no_secure_decoder,
                            decoderInitializationException.mimeType);
                } else {
                    errorString = getString(R.string.error_no_decoder,
                            decoderInitializationException.mimeType);
                }
            } else {
                errorString = getString(R.string.error_instantiating_decoder,
                        decoderInitializationException.decoderName);
            }
        }
        if (errorString != null) {
            Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_LONG).show();
        }
        ShowAfterBroadcastInfoActivity();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthAspectRatio) {
        shutterView.setVisibility(View.GONE);
        mPixelWidthAspectRatio = pixelWidthAspectRatio;
        mVideoHeight = height;
        mVideoWidth = width;

        if (isWebCast)
            updatePreviewRatioWebcast();
        else
            updatePreviewRatioBroadcast();
    }

    // Internal methods

    // DemoPlayer.CaptionListener implementation
    @Override
    public void onCues(List<Cue> cues) {
        subtitleLayout.setCues(cues);
    }

    // DemoPlayer.MetadataListener implementation
    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
    }

    //extra for demo player and codec
    private static final class KeyCompatibleMediaController extends MediaController {

        private MediaController.MediaPlayerControl playerControl;

        public KeyCompatibleMediaController(Context context) {
            super(context);
        }

        @Override
        public void setMediaPlayer(MediaController.MediaPlayerControl playerControl) {
            super.setMediaPlayer(playerControl);
            this.playerControl = playerControl;
        }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (playerControl.canSeekForward() && keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() + 15000); // milliseconds
                    show();
                }
                return true;
            } else if (playerControl.canSeekBackward() && keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    playerControl.seekTo(playerControl.getCurrentPosition() - 5000); // milliseconds
                    show();
                }
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
    }

    // Report User Thread
    class SendCommentTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String comment = params[2].replace(" ", "%20");
            String urlParse = Constants.POST_COMMENT
                    + "broadcast_id/" + params[0]
                    + "/token/" + appToken
                    + "/user_id/" + params[1]
                    + "/comment/" + comment;

            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<ReportResponse, ReportResponse>() {
                        @Override
                        public void onRequestSuccess(ReportResponse response) {
                        }

                        @Override
                        public void onRequestError(ReportResponse error) {
                        }
                    }, new HashMap<String, String>()));
            return "";
        }
    }

    // web services classes
    // Report Broadcast Thread
    class ReportBroadcastTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlParse = Constants.REPORT_BROADCAST +
                    "token=" + params[0] +
                    "&reporter_user_id=" + params[1] +
                    "&broadcast_id=" + params[2];

            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<ReportResponse, ReportResponse>() {
                        @Override
                        public void onRequestSuccess(ReportResponse response) {
                            PrettyToast.showSuccess(getApplicationContext(), response.message);
                        }

                        @Override
                        public void onRequestError(ReportResponse error) {
                            PrettyToast.showError(getApplicationContext(), "Something went wrong we could not report.");
                        }
                    }, new HashMap<String, String>()));

            return " ";
        }
    }

    // Report User Thread
    class ReportUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String urlParse = Constants.REPORT_USER +
                    "token=" + params[0] +
                    "&reporter_user_id=" + params[1] +
                    "&reportee_user_id=" + params[2];
            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<ReportResponse, ReportResponse>() {
                        @Override
                        public void onRequestSuccess(ReportResponse response) {
                            PrettyToast.showSuccess(getApplicationContext(), response.message);
                        }

                        @Override
                        public void onRequestError(ReportResponse error) {
                            PrettyToast.showError(getApplicationContext(), "Something went wrong we could not report.");
                        }
                    }, new HashMap<String, String>()));
            return " ";
        }
    }

    class SetViewStatusBroadcastTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String urlParse = params[0] +
                    "token=" + params[1] +
                    "&user_id=" + params[2] +
                    "&broadcast_id=" + params[3];

            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<StatusBroadcastResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(StatusBroadcastResponse response) {
                            if (response.status.equalsIgnoreCase("join")) {
                                // initialize list of peoples
                                Collections.addAll(mListViewers, response.viewer_list);
                                OnlineStreamBroadcastActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String viewerSize = " " + mListViewers.size() + " ";
                                        tvNumberLiveViewers.setText(viewerSize);
                                        mAdapterListSweetSheet.notifyDataSetChanged();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            PrettyToast.showError(getApplicationContext(), "Something went wrong.");
                        }
                    }, new HashMap<String, String>()));
            return " ";
        }
    }
}
