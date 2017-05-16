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

import java.util.ArrayList;


public class Audio2key extends AppCompatActivity{

    //private static final String PERMISSION_AUDIO="android.permission.RECORD_AUDIO";

    public int frequency = 8000;
    int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    RealDoubleFFT fftTrans;
    public int blockSize = 2048;
    TextView tv;
    TextView key_view;
    Button button;
    public boolean started = false;
    Detect detect ;
    ImageView imgView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    Paint paintB;
    long startTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);

        tv=(TextView) findViewById(R.id.data);
        key_view=(TextView) findViewById(R.id.key);
        button = (Button) findViewById(R.id.audioButton);
        fftTrans = new RealDoubleFFT(blockSize);
        imgView = (ImageView) findViewById(R.id.imgView);
        bitmap = Bitmap.createBitmap(256, 150, Bitmap.Config.ARGB_8888);
        //draw graph
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paintB = new Paint();
        paintB.setColor(Color.BLUE);
        imgView.setImageBitmap(bitmap);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (started){//cancel
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
                short[] audioBuffer = new short[blockSize];
                double[] toTrans = new double[blockSize];
                audioRecord.startRecording();
                while (started) {
                    int result = audioRecord.read(audioBuffer, 0, blockSize);
                    Log.d("result",String.valueOf(result));
                    ave = average(audioBuffer);
                    det[loop_cun % 3] = ave;
                    loop_cun++;
                    if(ave > 3 *det[(loop_cun + 1)%3]){// 至少比上上个信号强5倍
                        if(counter >3){ //与上一个按键至少间隔3个采样周期
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

//        @Override
//        protected void onProgressUpdate(double[]... values) {
//            double max_frequency = 0;
//            double max_value = 0;
//            long time = System.nanoTime() - startTime;
//            //画图
//            canvas.drawColor(Color.BLACK);
//            for (int i = 0; i < values[0].length; i++) {
//                int x  = 0, downy = 0, upy=0;
//
//                if(values[0][i]>max_value ){
//                    max_frequency = i;
//                    max_value = values[0][i];
//                    x = i;
//                    downy = (int) (150 - (values[0][i] * 10));
//                    upy = 150;
//                    canvas.drawLine(x, downy, x, upy, paint);
//
//                }
//
//            }
//            //显示画图
//            imgView.invalidate();
//            max_frequency = max_frequency/blockSize/4*frequency;
//            //System.out.println(max_frequency);
//            Log.d("Frequency",String.valueOf(max_frequency));
//            tv.setText(String.valueOf(max_frequency));
//        }

//@Override
//protected void onProgressUpdate(double[]... values) {//最大值用蓝色标出来
//    double max_frequency = 0;
//    double max_value = 0;
//    int x  = 0, downy = 0, upy=0;
//    long time = System.nanoTime() - startTime;
//    //画图
//    canvas.drawColor(Color.BLACK);
//    for (int i = 0; i < values[0].length; i++) {
//        x = i*256/blockSize;
//        downy = (int) (150 - (values[0][i] * 10));
//        upy = 150;
//        if(values[0][i]>max_value ){//最大值
//            max_frequency = i;
//            max_value = values[0][i];
//            canvas.drawLine(x, downy, x, upy, paintB);
//        }else {
//            canvas.drawLine(x, downy, x, upy, paint);
//        }
//    }
//    //显示画图
//    imgView.invalidate();
//    max_frequency = max_frequency/blockSize/4*frequency;
//    //System.out.println(max_frequency);
//    Log.d("Frequency",String.valueOf(max_frequency));
//    tv.setText(String.valueOf(max_frequency));
//}


        @Override
        protected void onProgressUpdate(double[]... values) {//最大值用蓝色标出来
            ArrayList<Integer> candidate = new ArrayList<Integer>();
            double max_frequency = 0;
            double max_value = 0;
            double key = 0;
            int kk = 0;
            int x  = 0, downy = 0, upy=0;
            double[] value = values[0];
            //画图
            canvas.drawColor(Color.BLACK);
            thre(value);
            //printVector(value);//打印出频谱
            int upper_bound = 1000*2*blockSize/frequency; //候选频率的上界
            int lower_bound = 100*2*blockSize/frequency; //下界
            for (int i = 0; i < value.length/2; i++) {//只画频谱左半部分（左右基本对称）
                x = i*256/(blockSize/2);
                downy = (int) (150 - (value[i] * 8));
                upy = 150;
                if(value[i]>max_value && i< upper_bound && i>3){//最大值
                    max_value = value[i];
                    candidate.add(i);
                    canvas.drawLine(x, downy, x, upy, paintB);
                }else {
                    canvas.drawLine(x, downy, x, upy, paint);
                }
            }
            max_frequency = find_max(candidate); //计算按键频率
            max_frequency = max_frequency*frequency/blockSize/2;
            key = log2(max_frequency);
            kk = (int)(key+0.5); //kk 是key取整后的按键
            //kk = delete_black_key(key);  //不考虑黑键

            //显示频谱
            imgView.invalidate();
            Log.d("Frequency",String.valueOf(max_frequency));
            tv.setText(String.valueOf(max_frequency)+"Hz");
            key_view.setText(String.valueOf(key)+"  Key:"+String.valueOf(kk));
        }

        private double[] thre(double[] value){ //门限函数
            for (int i = 0; i<value.length/2 ; i++){
                if (value[i]<2){
                    value[i] = 0;
                }
            }
            for (int i = value.length/2;i<value.length;i++){
                value[i]=0;
            }
            return value;
        }

        public int delete_black_key(double key){//用于检测是否为黑键，并且不显示黑键
            int kk = (int)(key+0.5); //kk 是key取整后的按键
            int black = kk%12;
            if (black == 1 || black == 4 || black == 6 || black ==9 || black == 11 ){
                if(key>kk){
                    kk++;
                }else {
                    kk--;
                }
            }
            return kk;
        }

        private double find_max(ArrayList<Integer> candidate){//计算按键频率
            for(int i = 0;i<candidate.size()-1;i++){
                if(candidate.get(i)>(candidate.get(i+1)-25)){
                    candidate.remove(i);
                    i--;
                }
            }
            //candidate_show(candidate);
            if(candidate.size()>1){
                int max = (int)candidate.get(candidate.size()-1);
                int half = max/2;
                for(int i = (int)(half*1.08); i>(int)(half*0.92);i--){
                    if(candidate.contains(i)){
                        System.out.println("Output:"+String.valueOf(i*frequency/blockSize/2)+"Hz");
                        return i;
                    }
                }
                if((max>500 && max <535) ||(max>480 && max<493)) {
                    int trip = max / 3;
                    for (int i = (int) (trip * 1.08); i > (int) (trip * 0.92); i--) {
                        if (candidate.contains(i)) {
                            System.out.println("Output:"+String.valueOf(i*frequency/blockSize/2)+"Hz");
                            return i;
                        }
                    }
                }
            }
            int result = candidate.isEmpty()?0:candidate.get(candidate.size()-1);
            System.out.println("After:"+String.valueOf(result*frequency/blockSize/2)+"Hz");
            return result;
        }

        public double log2(double f){//以110Hz为基准
            return 12*Math.log(f/110)/Math.log(2);//返回110Hz A2键向后的位数
        }

        public void candidate_show(ArrayList<Integer> candidate){
            for(int ite = 0;ite <candidate.size();ite++){
                System.out.print(String.valueOf(candidate.get(ite))+"  ");
            }
            System.out.println();
            for(int ite = 0;ite <candidate.size();ite++){
                System.out.print(String.valueOf(candidate.get(ite)*frequency/blockSize/2)+"  ");
            }
            System.out.println();
        }

        private void printVector(double[] value){//只打印左半部分
            for (int i = 0; i<value.length/2;i++){
                System.out.print(String.valueOf(i)+"\t");
            }
            System.out.println();
            for (int i = 0; i<value.length/2;i++){
                System.out.print(String.valueOf(value[i])+"\t");
            }
            System.out.println();
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
}
