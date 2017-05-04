package com.sustech.se.scoree.audioCapturer;

/**
 * Created by liaoweiduo on 14/04/2017.
 */

public interface AudioCapturerInterface {
    boolean isCaptureStarted();
    void setOnAudioFrameCapturedListener(OnAudioFrameCapturedListener listener);
    boolean startCapture();
    void stopCapture();

    interface OnAudioFrameCapturedListener {
        void onAudioFrameCaptured(short[] audioData);
    }
}
