package com.app.hopity.utils;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Mushi on 8/30/2016.
 */
public final class StorageUtils {
    public static final String file = ".hapity";
    private static final String TAG = "StorageUtils";
    private static final String folder = "Hapity";

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private static File getDefaultDirectory() {
        File dir = null;
        if (isExternalStorageWritable()) {
            dir = new File(Environment.getExternalStorageDirectory(), folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        return dir;
    }

    private static String createVideoFilename() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return "VID_" + timeStamp + ".mp4";
    }

    @Nullable
    public static File newMp4File() {
        File f = getDefaultDirectory();
        if (f != null) {
            Log.e(TAG, "output File generated at " + f.getAbsolutePath());
            return new File(f, createVideoFilename());
        }
        return null;
    }
}
