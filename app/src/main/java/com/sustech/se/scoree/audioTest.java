package com.sustech.se.scoree;
import com.sustech.se.scoree.fftpack.RealDoubleFFT;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.ImageView;



public class audioTest extends AppCompatActivity{

    //private static final String PERMISSION_AUDIO="android.permission.RECORD_AUDIO";

    int frequency = 44100;
    int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    RealDoubleFFT fftTrans;
    int blockSize = 1024;
    TextView tv;
    Button button;
    public boolean started = false;
    Detect detect ;
    ImageView imgView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    long startTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);

        tv=(TextView) findViewById(R.id.data);
        button = (Button) findViewById(R.id.audioButton);
        fftTrans = new RealDoubleFFT(blockSize);
        imgView = (ImageView) findViewById(R.id.imgView);
        bitmap = Bitmap.createBitmap(256, 100, Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imgView.setImageBitmap(bitmap);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (started){
                    started = false;
                    button.setText("Start");
                    detect.cancel(true);
                    startTime = 0;

            }
                else {
                    started = true;

                    detect = new Detect();
                    button.setText("Stop");
                    detect.execute();
                    startTime = System.nanoTime();
                }
            }
        });

    }



    private class RecordAudioTask extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        channelConfig, audioFormat);
                Log.v("bufSize", String.valueOf(bufferSize));
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfig, audioFormat, bufferSize);

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

            for (int i = 0; i < values[0].length; i++) {
            }
            //System.out.println("freq");
        }
    }


    public class Detect extends AsyncTask<Void, double[], Void> {
        private  int counter = 0;//This counter is used to count the number of short[] got from listener.
        private  int[] det = new int[3];//This is an int array to record recent average value for comparision.
        private  int ave = 0;//average data
        private  int cun = 0;//counter of det
        private  int loop_cun = 0;
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
                    ave = average(audioBuffer);
                    det[loop_cun % 3] = ave;
                    loop_cun++;
                    if(ave > 3 *det[(loop_cun + 1)%3]){// 至少比上上个信号强5倍
                        if(counter >3){ //与上一个按键至少间隔3个采样周期

                            long time = System.nanoTime() - startTime;
                            for (int i = 0; i < blockSize && i < result; i++) {
                                toTrans[i] = (double) audioBuffer[i] / Short.MAX_VALUE;
                            }
                            fftTrans.ft(toTrans);

                            publishProgress(toTrans);
                        }
                        counter = 0;
                    }else counter ++;
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
                    int downy = (int) (150 - (values[0][i] * 10));
                    int upy = 150;

                    canvas.drawLine(x, downy, x, upy, paint);
                }
            }
            imgView.invalidate();
            max_frequency = max_frequency/blockSize/4*frequency;
            //System.out.println(max_frequency);
            Log.d("Frequency",String.valueOf(max_frequency));
            tv.setText(String.valueOf(max_frequency));
        }
        public int average(short[] d) {
            long tmp = 0;
            for (int i = 0; i < d.length; i++) {
                if (d[i] > 0) {
                    tmp += d[i];
                } else {
                    tmp -= d[i];
                }
            }
            tmp /= d.length;
            return (int) tmp;
        }

    }

    private class displayData{
        long time = 0;
        double[] toTrans;
        displayData(long t, double[] a) {
            time = t;
            toTrans = a;
        }
    }
}