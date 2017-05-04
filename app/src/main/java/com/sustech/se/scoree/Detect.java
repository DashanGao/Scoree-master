package com.sustech.se.scoree;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sustech.se.scoree.fftpack.RealDoubleFFT;

public class Detect extends AsyncTask<Void, double[], Void> {
    int frequency = 44100;
    int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    RealDoubleFFT fftTrans;
    int blockSize = 1024;
    TextView tv;
    Button button;
    public boolean started = false;
    ImageView imgView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;

    public Detect(TextView tv) {
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    channelConfig, audioFormat);
            Log.v("bufSize", String.valueOf(bufferSize));
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, frequency,
                    channelConfig, audioFormat, bufferSize);
            System.out.println(bufferSize);

            short[] audioBuffer = new short[blockSize];
            double[] toTrans = new double[blockSize];

            audioRecord.startRecording();

            while (started) {
                int result = audioRecord.read(audioBuffer, 0, blockSize);

                for (int i = 0; i < blockSize && i < result; i++) {
                    toTrans[i] = (double) audioBuffer[i] / Short.MAX_VALUE;
                }
                fftTrans.ft(toTrans);
                publishProgress(toTrans);
            }
            audioRecord.stop();
        } catch (Throwable t) {
            Log.e("AudioRecord", "Recording failed");
        }

        return null;
    }
    @Override
    protected void onProgressUpdate(double[]... values) {
        double max_frequency = 0;
        double max_value = 0;
        canvas.drawColor(Color.BLACK);
        for (int i = 0; i < values[0].length; i++) {
            if(values[0][i]>max_value ){
                max_frequency = i;
                max_value = values[0][i];
                int x = i;
                int downy = (int) (100 - (values[0][i] * 10));
                int upy = 100;

                canvas.drawLine(x, downy, x, upy, paint);
            }
        }
        imgView.invalidate();
        max_frequency = max_frequency/blockSize*frequency;
        //System.out.println(max_frequency);
        Log.d("Frequency",String.valueOf(max_frequency));
        tv.setText(String.valueOf(max_frequency));
    }

}