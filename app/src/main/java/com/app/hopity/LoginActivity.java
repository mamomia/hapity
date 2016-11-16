package com.app.hopity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.appdata.SessionManager;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.RegisterResponseModel;
import com.app.hopity.signupwizard.SignupActivity;
import com.cuneytayyildiz.simplegcm.SimpleGcm;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.quist.app.errorreporter.ExceptionReporter;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

@SuppressLint({"NewApi", "SetJavaScriptEnabled"})
public class LoginActivity extends Activity {

    private String TAG = "LogInActivity";
    // UI references.
    private EditText mEmailView, mPasswordView;
    private SessionManager session;
    // twitter api
    private String twitter_id;
    private String twitter_username;
    private String twitter_profile_picture;
    private TwitterAuthClient mTwitterAuthClient;
    // facebook
    private String facebook_id;
    private String facebook_username;
    private String facebook_profile_picture;
    private String facebook_email;
    private SimpleFacebook mSimpleFacebook;
    private OnProfileListener onProfileListener;
    private OnLoginListener onLoginListener;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(LoginActivity.this);
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
        ExceptionReporter.register(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        new GlobalSharedPrefs(this);
        session = new SessionManager(this);
        mSimpleFacebook = SimpleFacebook.getInstance(this);
        SimpleGcm.init(this);

        GlobalSharedPrefs.hapityPref.edit().putString("TWITTER_ACCESS_TOKEN", "").apply();
        GlobalSharedPrefs.hapityPref.edit().putString("TWITTER_ACCESS_TOKEN_SECRET", "").apply();
        if (mTwitterAuthClient == null) {
            try {
                mTwitterAuthClient = new TwitterAuthClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // initializing
        mEmailView = (EditText) findViewById(R.id.etEmailSignin);
        mPasswordView = (EditText) findViewById(R.id.etPasswordSignin);
        mPasswordView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    attemptLoginSimple();
                }
                return false;
            }
        });
        // registering onclick listeners
        findViewById(R.id.btnLoginSignin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLoginSimple();
            }
        });
        findViewById(R.id.tvSignUpNow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(i);
            }
        });
        findViewById(R.id.btnFacebookSignin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSimpleFacebook.login(onLoginListener);
            }
        });
        findViewById(R.id.btnTwitterSignin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInSignUpWithTwitter();
            }
        });
        findViewById(R.id.tvForgetPasswordSignin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.hapity.com/home/forget_password"));
                startActivity(browserIntent);
            }
        });

        // using simple facebook API and initializing call backs
        onLoginListener = new OnLoginListener() {
            @Override
            public void onLogin(String accessToken, List<Permission> acceptedPermissions, List<Permission> declinedPermissions) {
                Profile.Properties properties = new Profile.Properties.Builder()
                        .add(Profile.Properties.ID)
                        .add(Profile.Properties.NAME)
                        .add(Profile.Properties.EMAIL)
                        .add(Profile.Properties.PICTURE)
                        .build();
                mSimpleFacebook.getProfile(properties, onProfileListener);
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "User Canceled");
            }

            @Override
            public void onFail(String reason) {
                Log.i(TAG, "Failed to Login");
            }

            @Override
            public void onException(Throwable throwable) {
                Log.i(TAG, "Exception from facebook");
            }
        };
        onProfileListener = new OnProfileListener() {
            @Override
            public void onComplete(Profile profile) {
                facebook_id = profile.getId();
                facebook_email = profile.getEmail();
                facebook_profile_picture = "https://graph.facebook.com/" + facebook_id + "/picture?type=large";
                facebook_username = generateByDefaultUsername(profile.getName());

                GlobalSharedPrefs.hapityPref.edit().putString("facebook_id", facebook_id).apply();
                socialSigninTask task = new socialSigninTask();
                task.execute("facebook",
                        facebook_id, facebook_username, facebook_email, facebook_profile_picture);
            }
        };

        setupUI(findViewById(R.id.rootLayoutLogin));
    }

    private String generateByDefaultUsername(String name) {
        String gName = name.trim().toLowerCase();
        gName = gName.replace(" ", ".");
        return gName;
    }

    public void attemptLoginSimple() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            GlobalSharedPrefs.hapityPref.edit().putString("type", "simple").apply();
            simpleUserSignInTask task = new simpleUserSignInTask();
            task.execute(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return email.length() >= 1;
    }

    // sig in with twitter
    private void SignInSignUpWithTwitter() {
        mTwitterAuthClient.authorize(LoginActivity.this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {
            @Override
            public void success(final Result<TwitterSession> twitterSessionResult) {
                TwitterSession session =
                        Twitter.getSessionManager().getActiveSession();
                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient(session);

                GlobalSharedPrefs.hapityPref.edit().putString("TWITTER_ACCESS_TOKEN",
                        session.getAuthToken().token)
                        .apply();
                GlobalSharedPrefs.hapityPref.edit().putString("TWITTER_ACCESS_TOKEN_SECRET",
                        session.getAuthToken().secret)
                        .apply();

                twitterApiClient.getAccountService().verifyCredentials(true, true, new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {

                        twitter_id = result.data.id + "";
                        twitter_username = result.data.screenName;
                        twitter_profile_picture = result.data.profileImageUrl;

                        // start Dashboard activity now
                        socialSigninTask task = new socialSigninTask();
                        task.execute("twitter",
                                twitter_id, twitter_username, twitter_profile_picture);
                    }

                    @Override
                    public void failure(TwitterException e) {
                    }
                });
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mSimpleFacebook = SimpleFacebook.getInstance(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 64206) {
            mSimpleFacebook.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == 140) {
            super.onActivityResult(requestCode, resultCode, data);
            mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class simpleUserSignInTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progressSignin;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressSignin = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progressSignin.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progressSignin.setTitleText("Signing In, Please wait..");
            progressSignin.setCancelable(false);
            progressSignin.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(final String... params) {
            String registrationId = "";
            // Check if Google Play Service is installed in Device
            // Play services is needed to handle GCM stuffs
            if (SimpleGcm.isRegistered(getApplicationContext())) {
                // Register Device in GCM Server
                registrationId = SimpleGcm.getRegistrationId(getApplicationContext());
                Log.e(TAG, "reg id : " + registrationId);
            }
            HashMap<String, String> paramsHash = new HashMap<>();
            paramsHash.put("email", params[0]);
            paramsHash.put("password", params[1]);
            paramsHash.put("type", "android");
            paramsHash.put("reg_id", registrationId);
            String url = Constants.SIGN_IN_URL;

            final String finalRegistrationId = registrationId;
            RestClientManager.getInstance().makeJsonRequest(Request.Method.POST,
                    url,
                    new RequestHandler<>(new RequestCallbacks<RegisterResponseModel, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(RegisterResponseModel response) {
                            try {
                                progressSignin.dismiss();
                                String status = response.status;
                                if (status.equalsIgnoreCase("success")) {

                                    GlobalSharedPrefs.hapityPref.edit().putString("facebook_id", facebook_id).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("twitter_id", facebook_id).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putInt("userid", response.user_info[0].user_id).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_EMAIL", response.user_info[0].email).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_NAME", response.user_info[0].username).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_PROFILE_PICTURE", response.user_info[0].profile_picture).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("appToken", response.user_info[0].token).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString(Constants.GCM_REG_KEY, finalRegistrationId).apply();

                                    // creating a session and fire up dashboard
                                    session.createLoginSession(response.user_info[0].username, response.user_info[0].email,
                                            response.user_info[0].token);
                                    session.storeGcmRegId(finalRegistrationId);

                                    Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                                    // Closing all the Activities
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    // Add new Flag to start new Activity
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                    finish();

                                } else if (response.status.equalsIgnoreCase("user not found")) {
                                    PrettyToast.showError(getApplicationContext(), "Incorrect username or password");
                                } else {
                                    PrettyToast.showError(getApplicationContext(), "There was a problem signing in, Please try again");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                PrettyToast.showError(getApplicationContext(), "Something went wrong, Please try again");
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            if (getApplicationContext() != null) {
                                progressSignin.dismiss();
                                PrettyToast.showError(getApplicationContext(), "There was a problem connecting to server, Please try again");
                            }
                        }

                    }, paramsHash));

            return "";
        }//close doInBackground
    }// close validateUserTask

    // facebook or twitter signup
    private class socialSigninTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progressSignin;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressSignin = new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progressSignin.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progressSignin.setTitleText("Signing In, Please wait..");
            progressSignin.setCancelable(false);
            progressSignin.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(String... params) {
            String registrationId = "";
            // Play services is needed to handle GCM stuffs
            if (SimpleGcm.isRegistered(getApplicationContext())) {
                // Register Device in GCM Server
                registrationId = SimpleGcm.getRegistrationId(getApplicationContext());
                Log.e(TAG, "reg id : " + registrationId);
            }
            GlobalSharedPrefs.hapityPref.edit().putString("type", params[0]).apply();
            HashMap<String, String> paramzHash = new HashMap<>();
            String url = "";
            if (params[0].contains("facebook")) {
                url = Constants.SIGN_IN_FACEBOOK_URL;
                paramzHash.put("facebook_id", params[1]);
                paramzHash.put("username", params[2]);
                paramzHash.put("email", params[3]);
                paramzHash.put("profile_picture", params[4]);
            } else if (params[0].contains("twitter")) {
                url = Constants.SIGN_IN_TWITTER_URL;
                paramzHash.put("twitter_id", params[1]);
                paramzHash.put("username", params[2]);
                paramzHash.put("profile_picture", params[3]);
            }
            paramzHash.put("type", "android");
            paramzHash.put("reg_id", registrationId);

            final String finalRegistrationId = registrationId;
            RestClientManager.getInstance().makeJsonRequest(Request.Method.POST,
                    url,
                    new RequestHandler<>(new RequestCallbacks<RegisterResponseModel, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(RegisterResponseModel response) {
                            try {
                                progressSignin.dismiss();
                                String status = response.status;
                                if (status == null || status.isEmpty()) {
                                    PrettyToast.showError(getApplicationContext(), "There was a problem signing in, Please try again");
                                } else {
                                    GlobalSharedPrefs.hapityPref.edit().putInt("userid", response.user_info[0].user_id).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_EMAIL", response.user_info[0].email).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_NAME", response.user_info[0].username).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("USER_PROFILE_PICTURE", response.user_info[0].profile_picture).apply();
                                    GlobalSharedPrefs.hapityPref.edit().putString("appToken", response.user_info[0].token).apply();

                                    // creating a session and fire up dashboard
                                    session.createLoginSession(response.user_info[0].username, response.user_info[0].email,
                                            response.user_info[0].token);
                                    session.storeGcmRegId(finalRegistrationId);

                                    Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
                                    // Closing all the Activities
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    // Add new Flag to start new Activity
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                    finish();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                PrettyToast.showError(getApplicationContext(), "Something went wrong, Please try again");
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            if (getApplicationContext() != null) {
                                progressSignin.dismiss();
                                PrettyToast.showError(getApplicationContext(), "There was a problem connecting to server, Please try again");
                            }
                        }

                    }, paramzHash));

            return "";
        }//close doInBackground
    }// close validateUserTask
}