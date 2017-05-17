package com.sustech.se.scoree;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by David GAO on 2017/4/20.
 */
import com.sustech.se.scoree.fftpack.RealDoubleFFT;

public class Decoder {


    public static int decode(double[] value){
        Data data = Data.getInstance();
        ArrayList<Integer> candidate = new ArrayList<Integer>();
        double max_frequency ;
        double max_value = 0;
        double key ;
        int kk ;
        //画图
        thre(value);
        //printVector(value);//打印出频谱
        int upper_bound = 1000*2*data.getBlockSize()/data.getFrequency(); //候选频率的上界
        //int lower_bound =  100*2*data.getBlockSize()/data.getFrequency(); //下界
        for (int i = 0; i < value.length/2; i++) {//只画频谱左半部分（左右基本对称）
            if(value[i]>max_value && i< upper_bound && i>3){//最大值
                max_value = value[i];
                candidate.add(i);
            }
        }
        max_frequency = find_max(candidate, data); //计算按键频率
        max_frequency = max_frequency*data.getFrequency()/data.getBlockSize()/2;
        key = log2(max_frequency);
        kk = (int)(key+0.5); //kk 是key取整后的按键
        //kk = delete_black_key(key);  //不考虑黑键
        //data.setKeyValue(kk);
        return kk;
    }

    private static double[] thre(double[] value){ //门限函数
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

    public static int delete_black_key(double key){//用于检测是否为黑键，并且不显示黑键
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

    private static double find_max(ArrayList<Integer> candidate, Data data){//计算按键频率
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
                    System.out.println("Output:"+String.valueOf(i*data.getFrequency()/data.getBlockSize()/2)+"Hz");
                    return i;
                }
            }
            if((max>500 && max <535) ||(max>480 && max<493)) {
                int trip = max / 3;
                for (int i = (int) (trip * 1.08); i > (int) (trip * 0.92); i--) {
                    if (candidate.contains(i)) {
                        System.out.println("Output:"+String.valueOf(i*data.getFrequency()/data.getBlockSize()/2)+"Hz");
                        return i;
                    }
                }
            }
        }
        int result = candidate.isEmpty()?0:candidate.get(candidate.size()-1);
        System.out.println("After:"+String.valueOf(result*data.getFrequency()/data.getBlockSize()/2)+"Hz");
        return result;
    }

    public static double log2(double f){//以110Hz为基准
        return 12*Math.log(f/110)/Math.log(2);//返回110Hz A2键向后的位数
    }


}
