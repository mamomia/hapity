package com.app.hopity.connections;

import android.util.Log;

import com.wmspanel.libstream.AudioEncoder;
import com.wmspanel.libstream.Streamer;

/**
 * Created by Mushi on 8/16/2016.
 */
public class ConnectionProfile {
    private final static String TAG = "ConnectionProfile";
    private Streamer.Size mResolution;
    private String mType;
    private int mBitrate;
    private int mFramerate;
    private int mAudioChannel;
    private int mKeyInterval;
    private int mSampleRate;

    public ConnectionProfile(String type) {
        this.mType = type;
        if (type.equalsIgnoreCase("low")) {
            mResolution = new Streamer.Size(360, 640);
            mBitrate = 200000;
            mFramerate = 30;
            mSampleRate = 44100;
            mAudioChannel = 1;
            mKeyInterval = 2;
        }
        if (type.equalsIgnoreCase("med")) {
            mResolution = new Streamer.Size(540, 960);
            mBitrate = 800000;
            mFramerate = 30;
            mSampleRate = 44100;
            mAudioChannel = 1;
            mKeyInterval = 2;
        }
        if (type.equalsIgnoreCase("high")) {
            mResolution = new Streamer.Size(1080, 1920);
            mBitrate = 1280000;
            mFramerate = 30;
            mSampleRate = 44100;
            mAudioChannel = 1;
            mKeyInterval = 2;
        }
    }

    public static ConnectionProfile instanceOf(String type) {
        return new ConnectionProfile(type);
    }

    public Streamer.Size getResolution(Streamer.CameraInfo cameraInfo) {
        if (mType.equalsIgnoreCase("low"))
            mResolution = cameraInfo.recordSizes[cameraInfo.recordSizes.length - 1];
        if (mType.equalsIgnoreCase("med"))
            mResolution = cameraInfo.recordSizes[cameraInfo.recordSizes.length / 2];
        if (mType.equalsIgnoreCase("high"))
            mResolution = cameraInfo.recordSizes[0];
        Log.e(TAG, "Resolution : " + mResolution.height + "x" + mResolution.width);
        return mResolution;
    }

    public int getAudioSampleRate() {
        AudioEncoder audioEncoder = AudioEncoder.createAudioEncoder();
        int[] mSampleRateArray = audioEncoder.getSupportedSampleRates();
        if (mType.equalsIgnoreCase("low"))
            mSampleRate = mSampleRateArray[0];
        if (mType.equalsIgnoreCase("med"))
            mSampleRate = mSampleRateArray[mSampleRateArray.length / 2];
        if (mType.equalsIgnoreCase("high"))
            mSampleRate = mSampleRateArray[mSampleRateArray.length - 1];
        audioEncoder.release();
        return mSampleRate;
    }

    public String getType() {
        return mType;
    }

    public int getBitrate() {
        return mBitrate;
    }

    public int getFramerate() {
        return mFramerate;
    }

    public int getAudioChannel() {
        AudioEncoder audioEncoder = AudioEncoder.createAudioEncoder();
        if (audioEncoder != null) {
            int mMaxInputChannelCount = audioEncoder.getMaxInputChannelCount();
            audioEncoder.release();
            return mMaxInputChannelCount;
        }
        return 1;
    }

    public int getKeyInterval() {
        return mKeyInterval;
    }
}

