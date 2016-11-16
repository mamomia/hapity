package com.app.hopity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.app.hopity.appIntro.HapityIntro;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.appdata.SessionManager;
import com.app.hopity.extra.LoadingTask;
import com.robohorse.gpversionchecker.GPVersionChecker;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import net.lateralview.simplerestclienthandler.RestClientManager;

import de.quist.app.errorreporter.ExceptionReporter;
import io.fabric.sdk.android.Fabric;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class SplashActivity extends Activity implements LoadingTask.LoadingTaskFinishedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "f5NMQCZu98csQVvAQcygt3VtD";
    private static final String TWITTER_SECRET = "NpIUCB1NPHHp8DtuQTBHaSjInuh3CyHVK3yLmAL5sAZQqsQLmG";
    private String TAG = "SplashActivity";
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExceptionReporter.register(this);
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        new GlobalSharedPrefs(this);
        session = new SessionManager(this);
        RestClientManager.initialize(getApplicationContext()).enableDebugLog(true);
        PrettyToast.initIcons();
        new GPVersionChecker.Builder(this).create();

        setContentView(R.layout.activity_splash);

        Permission[] permissions = new Permission[]{
                Permission.PUBLIC_PROFILE,
                Permission.EMAIL,
                Permission.PUBLISH_ACTION
        };

        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId("536816569790573")
                .setNamespace("egnexthapity")
                .setPermissions(permissions)
                .build();
        SimpleFacebook.setConfiguration(configuration);

        if (GlobalSharedPrefs.hapityPref.getBoolean("isInitialAppLaunch", false)) {
            new LoadingTask(this).execute();
        } else {
            //First Time App launched, you are putting isInitialAppLaunch to false and calling create password activity.
            GlobalSharedPrefs.hapityPref.edit().putBoolean("isInitialAppLaunch", true).commit();
            Intent i = new Intent(SplashActivity.this, HapityIntro.class);
            startActivity(i);
            finish();
        }

    }

    // This is the callback for when your async task has finished
    @Override
    public void onTaskFinished() {
        if (session.checkLogin()) {
            //navigate to Login
            Intent i = new Intent(SplashActivity.this, DashboardActivity.class);
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }
//    public static String printKeyHash(Activity context) {
//        PackageInfo packageInfo;
//        String key = null;
//        try {
//            //getting application package name, as defined in manifest
//            String packageName = context.getApplicationContext().getPackageName();
//
//            //Retriving package info
//            packageInfo = context.getPackageManager().getPackageInfo(packageName,
//                    PackageManager.GET_SIGNATURES);
//
//            Log.e("Package Name=", context.getApplicationContext().getPackageName());
//            for (Signature signature : packageInfo.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                key = new String(Base64.encode(md.digest(), 0));
//
//                //String key = new String(Base64.encodeBytes(md.digest()));
//                Log.e("Key Hash=", key);
//            }
//        } catch (PackageManager.NameNotFoundException e1) {
//            Log.e("Name not found", e1.toString());
//        } catch (NoSuchAlgorithmException e) {
//            Log.e("No such an algorithm", e.toString());
//        } catch (Exception e) {
//            Log.e("Exception", e.toString());
//        }
//
//        return key;
//    }
}