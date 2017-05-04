package com.sustech.se.scoree;

import java.util.concurrent.LinkedBlockingQueue;

import android.app.Application;

import java.util.concurrent.BlockingQueue;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Created by liaoweiduo on 08/04/2017.
 */

public class Data extends Application {
    private Queue<short[]> dataQueue;

    //private int
    private Queue<short[]> getDataQueue() {
        return dataQueue;
    }

    public void offer(short[] data) {
        getDataQueue().offer(data);
    }

    public short[] poll() {
        return dataQueue.poll();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dataQueue = new LinkedList<short[]>();
    }
}
