package com.louislepper.waveform;

public class WaveStretcher {

    public static short[] normalize(short[] samples) {
        if(samples.length == 0) return samples;

        short max = samples[0];
        short min = samples[0];

        for (short sample : samples) {
            if (sample > max) {
                max = sample;
            }
            if (sample < min) {
                min = sample;
            }
        }

        double top_bound = (max - min)/2.0d;
        short desired_max = Short.MAX_VALUE;

        double shift_factor, scaling_factor;

        shift_factor = top_bound - max;
        scaling_factor = desired_max / top_bound;

        for (int i = 0; i < samples.length; i++) {
            samples[i] = (short)(scaling_factor * (samples[i] + shift_factor));
        }

        return samples;
    }
}
