package com.sustech.se.scoree;

import android.util.Log;
import com.sustech.se.scoree.fftpack.RealDoubleFFT;


/**
 * Created by David GAO on 2017/4/20.
 */

public class Detector{
    private Data data = Data.getInstance();
    RealDoubleFFT fftTrans;

    Detector(){
        fftTrans = new RealDoubleFFT(data.getBlockSize());
    }

    public boolean detect(){
    int result = data.audioRecord.read(data.getAudioBuffer(), 0, data.getBlockSize());
    Log.d("result",String.valueOf(result));
    data.setAve(average(data.getAudioBuffer()));
    data.det[data.loop_cun % 3] = data.getAve();
    data.loop_cun++;
    if(data.getAve() > 3 *data.det[(data.loop_cun + 1)%3]){// 至少比上上个信号强5倍
        if(data.getCounter() >3){ //与上一个按键至少间隔3个采样周期
            for (int i = 0; i < data.getBlockSize() && i < result; i++) {
                data.freq_vct[i] = (double) data.getAudioBuffer()[i] / Short.MAX_VALUE;
            }
            fftTrans.ft(data.freq_vct);
            return true;
        }
        data.setCounter(0);
    }else data.counter ++;
    return false;

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