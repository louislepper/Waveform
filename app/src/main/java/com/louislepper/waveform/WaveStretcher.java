/*
 * Copyright 2015 Louis Lepper.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
