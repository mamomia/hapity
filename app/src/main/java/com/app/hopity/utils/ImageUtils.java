package com.app.hopity.utils;

import android.content.Context;
import android.widget.ImageView;

import com.app.hopity.R;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

/**
 * Created by Mushi on 8/25/2016.
 */
public class ImageUtils {
    public static void loadWebImageIntoCircular(final CircularImageView imageView, String imageUri, Context mContext) {
        if (!imageUri.isEmpty())
            Picasso.with(mContext).
                    load(imageUri).
                    placeholder(R.drawable.com_facebook_profile_picture_blank_square).
                    resize(800, 800).
                    into(imageView);
    }

    public static void loadWebImageIntoSimple(final ImageView imageView, String imageUri, Context mContext) {
        if (!imageUri.isEmpty())
            Picasso.with(mContext).
                    load(imageUri).
                    placeholder(R.drawable.loading_hour_glass).
                    resize(800, 800).
                    into(imageView);
    }

    public static void loadWebImageIntoCircular(final CircularImageView imageView, String imageUri, Context mContext, int width, int height) {
        if (!imageUri.isEmpty())
            Picasso.with(mContext).
                    load(imageUri).
                    placeholder(R.drawable.com_facebook_profile_picture_blank_square).
                    resize(width, height).
                    into(imageView);
    }

    public static void loadWebImageIntoSimple(final ImageView imageView, String imageUri, Context mContext, int width, int height) {
        if (!imageUri.isEmpty())
            Picasso.with(mContext).
                    load(imageUri).
                    placeholder(R.drawable.loading_hour_glass).
                    resize(width, height).
                    into(imageView);
    }
}
