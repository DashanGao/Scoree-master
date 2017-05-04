/*
 *  COPYRIGHT NOTICE
 *  Copyright (C) 2016, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/Android
 *
 *  @license under the Apache License, Version 2.0
 *
 *  @file    AudioCapturer.java
 *
 *  @version 1.0
 *  @author  Jhuster
 *  @date    2016/03/10
 */
package com.sustech.se.scoree.audioCapturer;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;

public class AudioCapturer implements AudioCapturerInterface {

    private static final String TAG = "AudioCapturer";

    private static final int DEFAULT_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int DEFAULT_SAMPLE_RATE = 44100;   //44100
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord;
    private int mMinBufferSize = 0;

    private Thread mCaptureThread;
    private boolean mIsCaptureStarted = false;
    private volatile boolean mIsLoopExit = false;

    private OnAudioFrameCapturedListener mAudioFrameCapturedListener;

    @Override
    public boolean isCaptureStarted() {
        return mIsCaptureStarted;
    }

    @Override
    public void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener listener) {
        mAudioFrameCapturedListener = listener;
    }

    @Override
    public boolean startCapture() {
        /*从Data里读取这几个参数*/
        return startCapture(DEFAULT_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT);
    }

    @Override
    public void stopCapture() {

        if (!mIsCaptureStarted) {
            return;
        }

        mIsLoopExit = true;
        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }

        mAudioRecord.release();

        mIsCaptureStarted = false;
        mAudioFrameCapturedListener = null;

        Log.d(TAG, "Stop audio capture success !");
    }

    private boolean startCapture(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {

        if (mIsCaptureStarted) {
            Log.e(TAG, "Capture already started !");
            return false;
        }

        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        //mMinBufferSize = 300;
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return false;
        }
        Log.d(TAG, "getMinBufferSize = " + mMinBufferSize + " bytes !");

        mAudioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, mMinBufferSize);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return false;
        }

        mAudioRecord.startRecording();

        mIsLoopExit = false;
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();

        mIsCaptureStarted = true;

        Log.d(TAG, "Start audio capture success !");

        return true;
    }

    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {

            while (!mIsLoopExit) {

                short[] buffer = new short[mMinBufferSize];

                int ret = mAudioRecord.read(buffer, 0, mMinBufferSize);
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "Error ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "Error ERROR_BAD_VALUE");
                } else {
                    if (mAudioFrameCapturedListener != null) {
                        mAudioFrameCapturedListener.onAudioFrameCaptured(buffer);
                    }
                    Log.d(TAG, "OK, Captured " + ret + " bytes !");
                }

                //SystemClock.sleep(10);
            }
        }
    }


/*
    public void createAudioRecord() {
        int recBufSize = 0;
        AudioRecord audioRecord;
        for (int sampleRate : new int[]{44100, 8000, 11025, 16000, 22050, 32000,
                47250, 48000}) {
            for (short audioFormat : new short[]{
                    AudioFormat.ENCODING_PCM_16BIT,
                    AudioFormat.ENCODING_PCM_8BIT}) {
                for (short channelConfig : new short[]{
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.CHANNEL_IN_STEREO}) {

                    // Try to initialize
                    try {
                        recBufSize = AudioRecord.getMinBufferSize(sampleRate,
                                channelConfig, audioFormat);

                        if (recBufSize < 0) {
                            continue;
                        }

                        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                sampleRate, channelConfig, audioFormat,
                                recBufSize * 2);
                        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {

                            System.out.println("get success: "+sampleRate+" "+audioFormat+" "+channelConfig+" "+recBufSize);

                            return;
                        }
                        audioRecord.release();
                    } catch (Exception e) {
                        Log.e(TAG, "createAudioRecord: error "+sampleRate+" "+audioFormat+" "+channelConfig+" "+recBufSize);
                    }
                }
            }
        }

        throw new IllegalStateException(
                "getInstance() failed : no suitable audio configurations on this device.");
    }
*/
}

