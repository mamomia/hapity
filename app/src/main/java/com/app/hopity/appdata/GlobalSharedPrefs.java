package com.app.hopity.appdata;

import android.content.Context;
import android.content.SharedPreferences;

import com.app.hopity.models.BroadcastSingle;
import com.app.hopity.models.UserInfo;

import java.util.ArrayList;

/**
 * Created by Mushi on 8/28/2015.
 */
public class GlobalSharedPrefs {
    // sharedPrefrence
    private static final String PREFS_NAME = "HapityPreferences";
    public static SharedPreferences hapityPref;

    public static UserInfo[] followers;
    public static UserInfo[] following;
    public static UserInfo[] blocked;
    public static ArrayList<UserInfo> viewersOfBroadcasts;
    public static BroadcastSingle[] broadcasts;

    public GlobalSharedPrefs(Context con) {
        hapityPref = con.getSharedPreferences(PREFS_NAME, 0);
    }
}
