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
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.CaptioningManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.app.hopity.R;
import com.app.hopity.afterStreamActivities.OfflineAfterStreamActivity;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.fragmentDialog.ProfilesDialogFragment;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.NewBroadcastResponse;
import com.app.hopity.modelsResponse.ReportResponse;
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
import com.google.android.exoplayer.metadata.id3.GeobFrame;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.PrivFrame;
import com.google.android.exoplayer.metadata.id3.TxxxFrame;
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.google.android.exoplayer.util.Util;
import com.pkmmte.view.CircularImageView;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.List;

import de.quist.app.errorreporter.ExceptionReporter;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class OfflineStreamBroadcastActivity extends AppCompatActivity implements
        SurfaceHolder.Callback,
        DemoPlayer.Listener,
        DemoPlayer.CaptionListener,
        DemoPlayer.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener {

    private static final CookieManager defaultCookieManager;
    protected static String appToken;
    private static boolean BUFFERING_THREAD_IS_RUNNING = false;
    private static int BUFFERING_COUNT = 0;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    //    private Timer updateTimer;
    private String recordedStreamFileName = " ";
    // pusher and comment elements
    private String broadcast_image;
    private String broadcast_title;
    private String broadcast_user_id;
    private String broadcast_user_profilepic;
    private String broadcast_username = "";
    private String broadcast_id;
    private String broadcast_share_url;
    // testing variables
    private String TAG = "Offline Stream Broadcast";
    private RelativeLayout mLoadingView;
    private TextView tvLoadingView;
    private String appUserId;
    DialogInterface.OnClickListener actionListenerReport = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0: // Report User
                    // Report This User
                    AlertDialog.Builder builderUser = new AlertDialog.Builder(OfflineStreamBroadcastActivity.this)
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
                    AlertDialog.Builder builderBroadcast = new AlertDialog.Builder(OfflineStreamBroadcastActivity.this)
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
    private EventLogger eventLogger;
    private MediaController mediaController;
    private View shutterView;
    private SurfaceView surfaceView;
    private DemoPlayer player;
    private boolean playerNeedsPrepare;
    private long playerPosition;
    private Uri contentUri;
    private int contentType;
    private String contentId;
    private String provider;
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private SubtitleLayout subtitleLayout;
    private int mVideoWidth = 0, mVideoHeight = 0;
    private float mPixelWidthAspectRatio = 0;
    private Handler BUFFERING_COUNTER_HANDLER = new Handler();
    private Runnable BUFFERING_COUNTER_RUNNABLE = new Runnable() {
        public void run() {
            if (BUFFERING_COUNT == 10) {
                BUFFERING_THREAD_IS_RUNNING = false;
                BUFFERING_COUNTER_HANDLER.removeCallbacks(BUFFERING_COUNTER_RUNNABLE);
                ShowAfterBroadcastInfoActivity();
                return;
            } else {
                BUFFERING_COUNT++;
            }
            BUFFERING_THREAD_IS_RUNNING = true;
            BUFFERING_COUNTER_HANDLER.postDelayed(this, 1000);
        }
    };

    private boolean isWebCast = false;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
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

        recordedStreamFileName = getIntent().getStringExtra("streamPathFileName");
        broadcast_id = getIntent().getStringExtra("broadcastId");
        broadcast_title = getIntent().getStringExtra("broadcastTitle");
        broadcast_image = getIntent().getStringExtra("bImageUrl");
        broadcast_user_id = getIntent().getStringExtra("bUserId");
        broadcast_username = getIntent().getStringExtra("bUserName");
        broadcast_user_profilepic = getIntent().getStringExtra("bUserProfilePic");
        broadcast_share_url = getIntent().getStringExtra("bShareUrl");
        isWebCast = recordedStreamFileName.contains("_360p");

        if (isWebCast)
            setContentView(R.layout.activity_offline_stream_webcast);
        else
            setContentView(R.layout.activity_offline_stream_broadcast);

        new GlobalSharedPrefs(this);
        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
        appUserId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);
        hideSoftKeyboard(OfflineStreamBroadcastActivity.this);

        // loading animation
        tvLoadingView = (TextView) findViewById(R.id.tvBufferingUpdateOfflineStream);
        mLoadingView = (RelativeLayout) findViewById(R.id.layoutProgressBarOfflineStream);
        ImageView ivEyes = (ImageView) findViewById(R.id.ivEyesOfflineStream);
        // set loading animation
        Animation animLoading = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.tween);
        ivEyes.startAnimation(animLoading);
        enableLoadingInterface("Initialising ...");

        // initializing intent and its values
        TextView tvBroadcastShare = (TextView) findViewById(R.id.shareTextOfflineStream);
        CircularImageView profileDp = (CircularImageView) findViewById(R.id.profileDpOfflineStream);

        ImageUtils.loadWebImageIntoCircular(profileDp, broadcast_user_profilepic, OfflineStreamBroadcastActivity.this, 100, 100);
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarOfflineStream);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitleTextAppearance(getApplicationContext(), R.style.TitleBarText);
        toolbar.setTitle(broadcast_title);
        toolbar.setNavigationIcon(R.drawable.img_back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.gc();
                exitStreamBroadcastActivity();
            }
        });

        if (!appUserId.equalsIgnoreCase(broadcast_user_id)) {
            profileDp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getSupportFragmentManager();
                    ProfilesDialogFragment dialog = new ProfilesDialogFragment("Stream", appToken, appUserId, broadcast_user_id);
                    dialog.show(manager, "dialog");
                }
            });
            toolbar.inflateMenu(R.menu.menu_stream_broadcast);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    actionsReport.show();
                    return false;
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        String[] options = {"Report this user", "Report this broadcast"};
        builder.setItems(options, actionListenerReport);
        actionsReport = builder.create();

        // initializing player
        if (recordedStreamFileName.isEmpty()) {
            PrettyToast.showError(this, "Cannot play this broadcast, Its unavailable or deleted.");
            finish();
        } else {

            View root = findViewById(R.id.root_layout_OfflineStream);

            shutterView = findViewById(R.id.shutter);
            surfaceView = (SurfaceView) findViewById(R.id.surfaceViewOfflineStream);
            surfaceView.getHolder().addCallback(this);
            subtitleLayout = (SubtitleLayout) findViewById(R.id.subtitles);

            mediaController = new KeyCompatibleMediaController(this);
            mediaController.setAnchorView(root);

            CookieHandler currentHandler = CookieHandler.getDefault();
            if (currentHandler != defaultCookieManager) {
                CookieHandler.setDefault(defaultCookieManager);
            }

            audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);
            audioCapabilitiesReceiver.register();

            enableLoadingInterface("Connecting ...");
            new GetWowzaServerIpTask().execute();
        }
    }

    private void ShowAfterBroadcastInfoActivity() {
        if (player != null)
            releasePlayer();
        //call after stream dialog
        Intent intent = new Intent(this, OfflineAfterStreamActivity.class);
        intent.putExtra("bImageUrl", broadcast_image);
        intent.putExtra("bUserId", broadcast_user_id);
        intent.putExtra("bUserName", broadcast_username);
        intent.putExtra("bUserProfilePic", broadcast_user_profilepic);
        intent.putExtra("broadcastTitle", broadcast_title);
        intent.putExtra("bShareUrl", broadcast_share_url);
        startActivity(intent);
        System.gc();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
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
        AspectFrameLayout mLayout = (AspectFrameLayout) findViewById(R.id.aspectLayoutOfflineStream);

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
        AspectRatioFrameLayout mVideoFrameLayout = (AspectRatioFrameLayout) findViewById(R.id.aspectLayoutOfflineStream);
        mVideoFrameLayout.setAspectRatio(mVideoHeight == 0 ? 1 : (mVideoWidth * mPixelWidthAspectRatio) / mVideoHeight);
    }

    @Override
    public void onBackPressed() {
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

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            ShowAfterBroadcastInfoActivity();
            return;
        }
        String text = "";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "Loading ...";
                if (player.getPlaybackState() != DemoPlayer.STATE_PREPARING)
                    findViewById(R.id.layoutOverlayProgressBarOfflineStream).setAlpha(0.3f);

                if (!BUFFERING_THREAD_IS_RUNNING && BUFFERING_COUNT == 0) {
                    BUFFERING_COUNTER_RUNNABLE.run();
                    BUFFERING_THREAD_IS_RUNNING = true;
                }
                break;
            case ExoPlayer.STATE_IDLE:
                text += "Connection is idle ...";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "Loading ...";
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
        playerPosition = 0;
        setIntent(intent);
    }

    private void onStreamStart(String fileUrl) {

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
        audioCapabilitiesReceiver.unregister();
        BUFFERING_COUNT = 0;
        BUFFERING_THREAD_IS_RUNNING = false;
        BUFFERING_COUNTER_HANDLER.removeCallbacks(BUFFERING_COUNTER_RUNNABLE);
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
            player.seekTo(playerPosition);
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
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

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

    // Internal methods

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

    // DemoPlayer.CaptionListener implementation
    @Override
    public void onCues(List<Cue> cues) {
        subtitleLayout.setCues(cues);
    }

    // DemoPlayer.MetadataListener implementation
    @Override
    public void onId3Metadata(List<Id3Frame> id3Frames) {
        for (Id3Frame id3Frame : id3Frames) {
            if (id3Frame instanceof TxxxFrame) {
                TxxxFrame txxxFrame = (TxxxFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s", txxxFrame.id,
                        txxxFrame.description, txxxFrame.value));
            } else if (id3Frame instanceof PrivFrame) {
                PrivFrame privFrame = (PrivFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s", privFrame.id, privFrame.owner));
            } else if (id3Frame instanceof GeobFrame) {
                GeobFrame geobFrame = (GeobFrame) id3Frame;
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        geobFrame.id, geobFrame.mimeType, geobFrame.filename, geobFrame.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", id3Frame.id));
            }
        }
    }

    // DemoPlayer.Listener implementation

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

    // web services classes
    class GetWowzaServerIpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            String urlParse = Constants.GET_WOWZA_SERVER_IP;

            HashMap<String, String> paramsGetIp = new HashMap<>();
            paramsGetIp.put("token", appToken);

            RestClientManager.getInstance().makeJsonRequest(Request.Method.POST,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<NewBroadcastResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(NewBroadcastResponse response) {
                            try {
                                String status = response.status;
                                if (status.equalsIgnoreCase("success")) {
//                                    http://52.17.132.36:1935/vod/1012135211474564379442.stream.mp4/playlist.m3u8
                                    // now start the media player and connect to wowza server
                                    String ip = response.IP.replace("rtsp", "rtmp");
                                    String pathToFileOrUrl = ip + recordedStreamFileName;
                                    Log.e(TAG, "the returned ip is : " + pathToFileOrUrl);
                                    onStreamStart(pathToFileOrUrl);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            PrettyToast.showError(getApplicationContext(), "We are Sorry, we were unable to connect to server.");
                            finish();
                        }
                    }, paramsGetIp));
            return "result";
        }
    }

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
}
