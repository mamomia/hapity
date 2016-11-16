package com.app.hopity.appdata;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.hopity.R;
import com.app.hopity.playingActivities.OnlineStreamBroadcastActivity;
import com.cuneytayyildiz.simplegcm.GcmListener;
import com.glidebitmappool.GlideBitmapPool;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import net.lateralview.simplerestclienthandler.RestClientManager;

import io.fabric.sdk.android.Fabric;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

/**
 * Created by Mushi on 3/21/2016.
 */
public class HapityApplication extends Application implements GcmListener {
    private static final String GCM_SENDER_ID = "167720339572";
    private static final String TAG = "GCM HAPITY";
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "f5NMQCZu98csQVvAQcygt3VtD";
    private static final String TWITTER_SECRET = "NpIUCB1NPHHp8DtuQTBHaSjInuh3CyHVK3yLmAL5sAZQqsQLmG";
    private static boolean activityVisible;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GlideBitmapPool.initialize(10 * 1024 * 1024); // 10mb max memory size
        RestClientManager.initialize(getApplicationContext()).enableDebugLog(true);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        PrettyToast.initIcons();
    }

    private void sendNotification(Bundle response) {
        Intent resultIntent = new Intent(this, OnlineStreamBroadcastActivity.class);
        Log.e(TAG, "notification response : " + response);
        resultIntent.putExtra("bUserId", "" + response.getString("user_id"));
        resultIntent.putExtra("bUserProfilePic", "" + response.getString("profile_picture"));
        resultIntent.putExtra("broadcastId", "" + response.getString("broadcast_id"));
        resultIntent.putExtra("bImageUrl", "");
        resultIntent.putExtra("streamPathFileName", "" + response.getString("stream_url"));
        resultIntent.putExtra("broadcastTitle", "" + response.getString("title"));
        resultIntent.putExtra("bShareUrl", "" + response.getString("share_url"));
        resultIntent.putExtra("isFromNotification", true);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String title = "Hapity";
        String message = response.getString("username") + " is live now...";

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setSmallIcon(getNotificationIcon(mBuilder));
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setAutoCancel(true);
        mBuilder.setSound(soundUri); //This sets the sound to play
        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        mBuilder.setDefaults(defaults);
        // Set pending intent
        mBuilder.setContentIntent(resultPendingIntent);
        // Post a notification
        mNotificationManager.notify(0, mBuilder.build());
    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = 0x008000;
            notificationBuilder.setColor(color);
            return R.drawable.img_applogo_transparent;

        } else {
            return R.drawable.img_applogo;
        }
    }

    @Override
    public void onMessage(String from, Bundle data) {
        if (from.equalsIgnoreCase(GCM_SENDER_ID)) {
            sendNotification(data);
        }
    }

    @Override
    public void sendRegistrationIdToBackend(String registrationId) {
        Log.e(TAG, "reg Id : " + registrationId);
    }
}