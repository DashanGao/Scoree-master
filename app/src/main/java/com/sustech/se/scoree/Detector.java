package com.sustech.se.scoree;

import android.widget.Toast;

/**
 * Created by David GAO on 2017/4/20.
 */

public class Detector implements Runnable {
    public volatile boolean exit = false;
    private static int counter = 0;//This counter is used to count the number of short[] got from listener.
    private static int[] det = new int[3];//This is an int array to record recent average value for comparision.
    private static int ave = 0;//average data
    private static int cun = 0;//counter of det
    private static int loop_cun = 0;
    private static int baseline = 0;
    private static int baseline_cun = 0;
    long startTime = System.nanoTime();
    private Data gData = null;
    private Decoder decoder = null;

    public Detector(Data data) {
        gData = data;
        decoder = new Decoder(data);
    }

    @Override
    public void run() {
        detect();
    }

    public void detect() {
        short[] data;
        System.out.print(gData.poll());
        while (!exit) {
            do {
                data = gData.poll();
            } while (data == null);
            ave = average(data);
            det[loop_cun % 3] = ave;
            loop_cun++;
            String a = null;
            //                 if (ave > 1.21 * det[(loop_cun+1 ) % 3]) {


            if (ave > 5 * det[(loop_cun + 1) % 3]) {
                if (counter > 3) {
                    long time = System.nanoTime() - startTime;
                    startTime = System.nanoTime();
                    // System.out.println("Key press detected, #" + cun + " time interval: " + (double) time / 1000000000 + " second\t");

                    //Here invoke decoder.
                   // double frequency = decoder.decode(data, gData);
                    System.out.println(cun + "  key press detected, frequency is: " +  "  intervel: " + (double) time / 1000000000 + " s\t");
                    cun++;
                }
                counter = 0;
            } else {
                counter++;
            }
        }
        //ap.play(getAudioData(),0,getAudioData().length);
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
