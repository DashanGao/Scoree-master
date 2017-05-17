package com.sustech.se.scoree;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;


public class Audio2Key_refactor extends AppCompatActivity{

    //private static final String PERMISSION_AUDIO="android.permission.RECORD_AUDIO";
    Data data = Data.getInstance();
    Detector detector;

    int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    Audio audio;
    TextView key_view;
    Button button;
    public boolean started = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);
        detector = new Detector();
        key_view=(TextView) findViewById(R.id.key);
        button = (Button) findViewById(R.id.audioButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (started){//cancel
                    started = false;
                    button.setText("Start");
                    audio.cancel(true);
                }
                else {
                    started = true;
                    audio = new Audio();
                    button.setText("Stop");
                    audio.execute();
                }
            }
        });
    }


    public class Audio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                int bufferSize = AudioRecord.getMinBufferSize(data.getFrequency(),
                        channelConfig, audioFormat);
                Log.v("bufSize", String.valueOf(bufferSize));
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, data.getFrequency(),
                        channelConfig, audioFormat, bufferSize);
                data.setAudioRecord(audioRecord);
                audioRecord.startRecording();
                while (started) {
                    if(detector.detect()){
                        publishProgress(data.getFreq_vct());
                    }
                }
                audioRecord.stop();
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording failed");
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(double[]... values) {//最大值用蓝色标出来

            double[] value = values[0];
            int key = Decoder.decode(value);
            //int key = data.getKeyValue();
            key_view.setText("Key:"+String.valueOf(key));
        }


    }
}
