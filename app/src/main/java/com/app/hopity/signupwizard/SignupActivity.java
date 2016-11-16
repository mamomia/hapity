package com.app.hopity.signupwizard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.app.hopity.R;
import com.soundcloud.android.crop.Crop;

import java.io.File;

import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class SignupActivity extends FragmentActivity {
    private String TAG = "Signup Activity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Crop.REQUEST_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Log.e(TAG, "Result Received by Crop : Begin Cropping");
            beginCrop(data.getData());
        } else if (requestCode == Crop.REQUEST_CROP && data != null) {
            Log.e(TAG, "Result Received by Crop : Handle Cropping");
            handleCrop(resultCode, data);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(this.getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == Activity.RESULT_OK) {
            SignupStep1.convertToBase64(Crop.getOutput(result).getPath());
        } else if (resultCode == Crop.RESULT_ERROR && getApplicationContext() != null) {
            PrettyToast.showError(getApplicationContext(), Crop.getError(result).getMessage());
        }
    }
}