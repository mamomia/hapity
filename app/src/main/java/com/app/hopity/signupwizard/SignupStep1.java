package com.app.hopity.signupwizard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.hopity.R;
import com.pkmmte.view.CircularImageView;
import com.soundcloud.android.crop.Crop;

import org.codepond.wizardroid.WizardStep;
import org.codepond.wizardroid.persistence.ContextVariable;

import java.io.ByteArrayOutputStream;

/**
 * Created by Mushi on 3/7/2016.
 */
public class SignupStep1 extends WizardStep {

    private static String TAG = "Step 1";
    private static CircularImageView ivProfilePic;
    private static String encodedImageString = "";
    private static boolean uploadFlag = false;
    @ContextVariable
    private String profile_picture;
    @ContextVariable
    private boolean uploadProfilePictureFlag = false;

    public SignupStep1() {
    }

    public static void convertToBase64(String path) {
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
        encodedImageString = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
        uploadFlag = true;
        SignupStep1.ivProfilePic.setImageBitmap(bm);
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_signup_step1, container, false);

        SignupStep1.ivProfilePic = (CircularImageView) v.findViewById(R.id.ivProfilePictureSignup);
        SignupStep1.ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crop.pickImage(getActivity());
            }
        });

        if (!profile_picture.isEmpty())
            SignupStep1.ivProfilePic.setImageBitmap(Base64ToBitmap(profile_picture));
        return v;
    }

    @Override
    public void onExit(int exitCode) {
        switch (exitCode) {
            case WizardStep.EXIT_NEXT:
                bindDataFields();
                break;
            case WizardStep.EXIT_PREVIOUS:
                //Do nothing...
                getActivity().finish();
                break;
        }
    }

    private void bindDataFields() {
        //bind data
        profile_picture = encodedImageString;
        uploadProfilePictureFlag = uploadFlag;
    }

    private Bitmap Base64ToBitmap(String myImageData) {
        byte[] imageAsBytes = Base64.decode(myImageData.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }
}