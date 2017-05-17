package com.sustech.se.scoree;

import android.app.Application;
import android.media.AudioFormat;
import android.media.AudioRecord;

/**
 * Created by liaoweiduo on 08/04/2017.
 */

public class Data extends Application {

    private int keyValue;
    private int frequency = 8000;
    public AudioRecord audioRecord;
    private int blockSize = 2048;
    public short[] audioBuffer = new short[blockSize];
    public double[] freq_vct = new double[blockSize];
    public  int counter = 0;//This counter is used to count the number of short[] got from listener.
    public  int[] det = new int[3];//This is an int array to record recent average value for comparision.
    private  int ave = 0;//average data
    public  int loop_cun = 0;



    public int getAve() {
        return ave;
    }

    public void setAve(int ave) {
        this.ave = ave;
    }


    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }



    public int getFrequency() {
        return frequency;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public double[] getFreq_vct() {
        return freq_vct;
    }

    public short[] getAudioBuffer() {
        return audioBuffer;
    }
    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public void setAudioRecord(AudioRecord audioRecord) {
        this.audioRecord = audioRecord;
    }


    public int getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(int keyValue) {
        this.keyValue = keyValue;
    }
    private static class SingletonHolder {//单例模式
        private static final Data INSTANCE = new Data();
    }

    public static final Data getInstance() {
        return SingletonHolder.INSTANCE;
    }


}
