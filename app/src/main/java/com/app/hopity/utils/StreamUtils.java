package com.app.hopity.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import com.wmspanel.libstream.AudioEncoder;
import com.wmspanel.libstream.Streamer;

import java.util.List;

/**
 * Created by Mushi on 7/12/2016.
 */
public class StreamUtils {
    private static String TAG = "StreamUtils";

    public static int channelCount() {
        AudioEncoder audioEncoder = AudioEncoder.createAudioEncoder();
        if (audioEncoder != null) {
            int mMaxInputChannelCount = audioEncoder.getMaxInputChannelCount();
            audioEncoder.release();
            return mMaxInputChannelCount;
        }
        return 1;
    }

    public static int sampleRate() {
        AudioEncoder audioEncoder = AudioEncoder.createAudioEncoder();
        int[] mSupportedSampleRates = {44100};
        if (audioEncoder != null) {
            mSupportedSampleRates = audioEncoder.getSupportedSampleRates();
            audioEncoder.release();
        }
        int sampleRate = 44100;
        if (mSupportedSampleRates == null || mSupportedSampleRates.length == 0) {
            sampleRate = 44100;
        } else {
            sampleRate = mSupportedSampleRates[mSupportedSampleRates.length - 1];
        }
        Log.e(TAG, "Sample Selected : " + sampleRate + " from : " + (mSupportedSampleRates.length - 1));
        return sampleRate;
    }

    public static float frameRate() {
        return 30;
    }

    public static int keyFrameInterval() {
        return 2;
    }

    public static int videoBitRate() {
        return 800000;
    }

    public static int audioSource(int src) {
        if (src == 0)
            return MediaRecorder.AudioSource.CAMCORDER;
        else if (src == 1)
            return MediaRecorder.AudioSource.MIC;
        else if (src == 2)
            return MediaRecorder.AudioSource.DEFAULT;
        else if (src == 3)
            return MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        return MediaRecorder.AudioSource.CAMCORDER;
    }

    public static Streamer.Size getVideoSize(Streamer.CameraInfo cameraInfo) {
        Streamer.Size videoSize = null;

        if (cameraInfo != null) {
            if (cameraInfo.recordSizes != null && cameraInfo.recordSizes.length != 0) {
//                int sizeIndex = Integer.parseInt(video_size_list);
                float halfSize = cameraInfo.recordSizes.length / 2;
                int sizeIndex = (int) (halfSize / 2);
                Log.e(TAG, "total length is : " + cameraInfo.recordSizes.length);
                Log.e(TAG, "chosen index is : " + sizeIndex);
                if (sizeIndex < 0 || sizeIndex >= cameraInfo.recordSizes.length) {
                    videoSize = cameraInfo.recordSizes[0];
                } else {
                    videoSize = cameraInfo.recordSizes[sizeIndex];
                }
            }
        }
        return videoSize;
    }

    public static Streamer.CameraInfo getActiveCameraInfo(List<Streamer.CameraInfo> cameraList, String cameraId) {
        Streamer.CameraInfo cameraInfo = null;

        if (cameraList == null || cameraList.size() == 0) {
            Log.e("", "no camera found");
        } else {
            for (Streamer.CameraInfo cursor : cameraList) {
                if (cursor.cameraId.equals(cameraId)) {
                    cameraInfo = cursor;
                }
            }
            if (cameraInfo == null) {
                cameraInfo = cameraList.get(0);
            }
        }
        return cameraInfo;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean allowCamera2Support(Context context) {

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        Log.d(TAG, manufacturer + " " + model);

        // Some known camera api dependencies and issues:

        // New Samsung devices (Galaxy S7, S7 Edge, Note 5) have issues with Camera2 API
        // So far all Samsungs should stay on stable Camera API
        if (manufacturer.equalsIgnoreCase("samsung")) {
            return false;
        }

        // Nexus 5X should use Camera2
        // http://www.theverge.com/2015/11/9/9696774/google-nexus-5x-upside-down-camera

        /*
         LEGACY Camera2 implementation has problem with aspect ratio.
         Rather than allowing Camera2 API on all Android 5+ devices, we restrict it to
         cases where all cameras have at least LIMITED support.
         (E.g., Nexus 6 has FULL support on back camera, LIMITED support on front camera.)
         For now, devices with only LEGACY support should still use Camera API.
        */
        boolean result = true;
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int support = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

                switch (support) {
                    case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                        Log.d(TAG, "Camera " + cameraId + " has LEGACY Camera2 support");
                        break;
                    case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                        Log.d(TAG, "Camera " + cameraId + " has LIMITED Camera2 support");
                        break;
                    case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                        Log.d(TAG, "Camera " + cameraId + " has FULL Camera2 support");
                        break;
                    default:
                        Log.d(TAG, "Camera " + cameraId + " has unknown Camera2 support?!");
                }

                if (support == CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    // Can't use Camera2, bul let other cameras info to log
                    result = false;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            result = false;
        }
        return result;
    }
}
