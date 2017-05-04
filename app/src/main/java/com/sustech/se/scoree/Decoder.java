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
    private Data gData = null;
    private ArrayList<Short> sod;
    private short[] tmp = null;
    private List tmp_to_list;

    public Decoder(Data data) {
        gData = data;
    }

    public double decode(short[] sound, Data data) {
        //Get sound array and data object which contains Queue.
        // Create a short[]  dft array  dft which contains more than 2000 samples.
        //Do DFT to the dft[]. get frequency.

        List sound_to_list = (ArrayList) Arrays.asList(sound);
        sod = new ArrayList<Short>();
        sod.addAll(sound_to_list);
        while (sod.size() < 2000) {
            if ((tmp = data.poll()) != null) {
                sod.addAll((ArrayList) Arrays.asList(tmp));
            }
        }
        Short[] dft_By = (Short[]) sod.toArray();
        //short[] dft = new short[sod.size()];
        double[] dft_double = new double[sod.size()];
        for (int i = 0; i < dft_By.length; i++) {
            dft_double[i] = (double) dft_By[i].shortValue() / Short.MAX_VALUE;
        }
        //dft_By = null;
        DFT(dft_double);
        int max_index = findMax(dft_double);
        double frequency = (double) max_index / dft_double.length * 44100;
        return frequency;
    }

    public void DFT(double[] dft) {
        RealDoubleFFT fftTrans = new RealDoubleFFT(dft.length);
        fftTrans.ft(dft);
    }

    public int findMax(double[] dft) {
        int index = 0, tmp = 0;
        for (int i = 0; i < dft.length; i++) {
            if (dft[i] > tmp)
                index = i;
        }
        return index;
    }

}
