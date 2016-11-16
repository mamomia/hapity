package com.app.hopity.appdata;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Mushi on 8/30/2015.
 */
public class SessionManager {
    // User name (make variable public to access from outside)
    public static final String KEY_USERNAME = "session_username";
    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "session_email";
    public static final String KEY_TOKEN = "session_token";
    public static final String KEY_REG_ID = "regIdGcm";
    // Sharedpref file name
    private static final String PREF_NAME = "HapityPreferences";
    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";
    // Shared Preferences
    SharedPreferences pref;
    // Editor for Shared preferences
    SharedPreferences.Editor editor;
    // Context
    Context _context;
    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Constructor
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String username, String email, String token) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_USERNAME, username);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // Storing email in pref
        editor.putString(KEY_TOKEN, token);

        // commit changes
        editor.commit();
    }

    /**
     * Save Gcm Registration id in shared prefrences
     */
    public void storeGcmRegId(String regId) {
        // Storing email in pref
        editor.putString(KEY_REG_ID, regId);
        // commit changes
        editor.commit();
    }

    public String getGcmRegId() {
        return pref.getString(KEY_REG_ID, null);
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public boolean checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            return false;
        }
        return true;
    }

    /**
     * Clear session details
     */
    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.putBoolean(IS_LOGIN, false);
        editor.commit();
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
