package com.app.hopity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.JsonResponse;
import com.app.hopity.utils.ImageUtils;
import com.pkmmte.view.CircularImageView;
import com.soundcloud.android.crop.Crop;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.quist.app.errorreporter.ExceptionReporter;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class UserSettingsActivity extends Activity implements View.OnFocusChangeListener {

    boolean updateInfoStatus = false;
    private String appToken;
    private EditText mUsernameView, mEmailView, mPassView, mPassConView;
    private CircularImageView profilePic;
    private String TAG = "Settings Activity";
    private String appUserId;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(UserSettingsActivity.this);
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
        setContentView(R.layout.activity_user_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarUserSetting);
        toolbar.setNavigationIcon(R.drawable.img_back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.gc();
                if (updateInfoStatus)
                    new AlertDialog.Builder(UserSettingsActivity.this)
                            .setMessage("Are you sure you want to exit? any unsaved changes will be discarded.")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                else
                    finish();
            }
        });

        new GlobalSharedPrefs(this);
        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "token empty");
        appUserId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        mUsernameView = (EditText) findViewById(R.id.settings_username);
        mEmailView = (EditText) findViewById(R.id.settings_email);
        mPassView = (EditText) findViewById(R.id.settings_password);
        mPassConView = (EditText) findViewById(R.id.settings_password_confirm);

        mUsernameView.setOnFocusChangeListener(this);
        mEmailView.setOnFocusChangeListener(this);
        mPassView.setOnFocusChangeListener(this);
        mPassConView.setOnFocusChangeListener(this);

        profilePic = (CircularImageView) findViewById(R.id.profile_settings_dp);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crop.pickImage(UserSettingsActivity.this);
            }
        });

        Button updateButton = (Button) findViewById(R.id.settings_btn_updateprofile);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptUpdateInfo();
            }
        });

        updateInformationInStart();
        eraseFocus();
        setupUI(findViewById(R.id.rootLayoutSettings));
    }

    private void eraseFocus() {
        mEmailView.clearFocus();
        mUsernameView.clearFocus();
        mPassView.clearFocus();
        mPassConView.clearFocus();

        mPassConView.setVisibility(View.GONE);
        updateInfoStatus = false;
    }

    private void updateInformationInStart() {

        mUsernameView.setHint("" + GlobalSharedPrefs.hapityPref.getString("USER_NAME", "Username Not Already Define"));
        mEmailView.setHint("" + GlobalSharedPrefs.hapityPref.getString("USER_EMAIL", "Email  Not Already Define"));

        if (GlobalSharedPrefs.hapityPref.getString("LOGED_IN_TYPE", "Type empty").equalsIgnoreCase("simple"))
            mPassConView.setVisibility(View.GONE);
        else {
            mPassView.setVisibility(View.GONE);
            mPassConView.setVisibility(View.GONE);
        }

        String urlImage = GlobalSharedPrefs.hapityPref.getString("USER_PROFILE_PICTURE",
                "http://api.hapity.com/assets/images/null.jpg");
        ImageUtils.loadWebImageIntoCircular(profilePic, urlImage, UserSettingsActivity.this);
    }

    @Override
    public void onBackPressed() {
        if (updateInfoStatus)
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit? any unsaved changes will be discarded.")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            UserSettingsActivity.this.finish();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        else
            super.onBackPressed();
    }

    private void convertAndUpload(String path) {

        final int DESIRED_WIDTH = 640;
        final BitmapFactory.Options sizeOptions = new BitmapFactory.Options();
        sizeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, sizeOptions);
        final float widthSampling = sizeOptions.outWidth / DESIRED_WIDTH;
        sizeOptions.inJustDecodeBounds = false;
        sizeOptions.inSampleSize = (int) widthSampling;
        final Bitmap bm = BitmapFactory.decodeFile(path, sizeOptions);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] byteArrayImage = baos.toByteArray();
        String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        try {
            baos.close();
            bm.recycle();
            byteArrayImage = null;

            System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UploadProfilePictureTask task = new UploadProfilePictureTask();
        task.execute(appUserId, encodedImage);
    }

    private void attemptUpdateInfo() {
        // Reset errors.
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPassView.setError(null);
        mPassConView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();
        String email = mEmailView.getText().toString().trim();
        String password = mPassView.getText().toString().trim();
        String passCon = mPassConView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password, passCon) && !TextUtils.isEmpty(password)) {
            mPassView.setError("Passwords does not match.");
            focusView = mPassView;
            cancel = true;
        }

        // Check for a valid email address.
        if (!isEmailValid(email) && !TextUtils.isEmpty(email)) {
            mEmailView.setError("Email you entered is not valid.");
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            validateUpdateUserInfoTask task = new validateUpdateUserInfoTask();
            task.execute(appToken, username, email, password, appUserId);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String pass1, String pass2) {
        if (pass1.equals(pass2) && pass1.length() >= 4) {
            return true;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == mPassView && hasFocus) {
            mPassConView.setVisibility(View.VISIBLE);
        } else if (v == mPassView && !hasFocus && mPassView.getText().toString().isEmpty()) {
            mPassConView.setVisibility(View.GONE);
        } else if (hasFocus) {
            updateInfoStatus = true;
            v.requestFocus();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Log.e(TAG, "Result Received by Crop : Begin Cropping");
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP && data != null) {
            Log.e(TAG, "Result Received by Crop : Handle Cropping");
            handleCrop(resultCode, data);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(UserSettingsActivity.this.getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(UserSettingsActivity.this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            convertAndUpload(Crop.getOutput(result).getPath());
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(UserSettingsActivity.this, "" + Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // async task for making web services call

    private class validateUpdateUserInfoTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new SweetAlertDialog(UserSettingsActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progress.setTitleText("Please Wait, Profile Updating ...");
            progress.setCancelable(false);
            progress.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(final String... params) {

            HashMap<String, String> paramsHash = new HashMap<>();
            paramsHash.put("user_id", params[4]);

            if (params[1].isEmpty()) {
                params[1] = GlobalSharedPrefs.hapityPref.getString("USER_NAME", "");
                paramsHash.put("username", params[1]);
            } else
                paramsHash.put("username", params[1]);

            if (params[2].isEmpty()) {
                params[2] = GlobalSharedPrefs.hapityPref.getString("USER_EMAIL", "");
                paramsHash.put("email", params[2]);
            } else
                paramsHash.put("email", params[2]);

            if (params[3].isEmpty())
                paramsHash.put("password", params[3]);

            paramsHash.put("token", params[0]);

            String url = Constants.EDIT_PROFILE_URL;
            RestClientManager.getInstance().makeJsonRequest(Request.Method.POST,
                    url,
                    new RequestHandler<>(new RequestCallbacks<JsonResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(JsonResponse response) {
                            progress.dismiss();
                            if (!response.error.isEmpty() && response.error.equalsIgnoreCase("parameter missing")) {
                                PrettyToast.showSuccess(UserSettingsActivity.this, "parameter missing");
                            } else {
                                PrettyToast.showSuccess(UserSettingsActivity.this, "Settings Successfully Saved.");
                                GlobalSharedPrefs.hapityPref.edit().putString("USER_EMAIL", response.user_info[0].email).apply();
                                GlobalSharedPrefs.hapityPref.edit().putString("USER_NAME", response.user_info[0].username).apply();
                                GlobalSharedPrefs.hapityPref.edit().putString("PASSWORD", response.user_info[0].password).apply();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mUsernameView.setHint("" + GlobalSharedPrefs.hapityPref.getString("USER_NAME", "Username"));
                                        mEmailView.setHint("" + GlobalSharedPrefs.hapityPref.getString("USER_EMAIL", "Empty"));
                                        mPassView.setHint("" + GlobalSharedPrefs.hapityPref.getString("PASSWORD", "Empty"));
                                    }
                                });
                                eraseFocus();
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            progress.dismiss();
                        }
                    }, paramsHash));

            return "";
        }//close doInBackground
    }// close validateUserTask

    private class UploadProfilePictureTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new SweetAlertDialog(UserSettingsActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progress.setTitleText("Uploading Picture Please Wait ...");
            progress.setCancelable(false);
            progress.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(final String... params) {

            HashMap<String, String> paramsHash = new HashMap<>();
            paramsHash.put("user_id", params[0]);
            paramsHash.put("profile_picture", params[1]);
            paramsHash.put("token", appToken);

            String urlParse = Constants.INSERT_PROFILE_PICTURE_URL;
            RestClientManager.getInstance().makeJsonRequest(Request.Method.POST,
                    urlParse,
                    new RequestHandler<>(new RequestCallbacks<JsonResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(JsonResponse response) {
                            progress.dismiss();
                            String status = response.status;
                            if (status.equalsIgnoreCase("success")) {
                                GlobalSharedPrefs.hapityPref.edit().
                                        putString("USER_PROFILE_PICTURE", response.user_info[0].profile_picture).commit();
                                ImageUtils.loadWebImageIntoCircular(profilePic, response.user_info[0].profile_picture, UserSettingsActivity.this);
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            progress.dismiss();
                        }
                    }, paramsHash));

            return "";
        }//close doInBackground
    }// close validateUserTask

}