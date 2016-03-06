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
import java.lang.Math;

public class WaveResizer {
    public short[] resize(short[] samples, int outputLength) {
        // Given a bunch of samples, scale them to the length given in the constructor

        int oldLength = samples.length;
        short[] output = new short[outputLength];
        double factor = oldLength / (double) outputLength;
        double doubleIndex;
        int intIndex;

        for (int i = 0; i < outputLength; i++) {
            doubleIndex = factor * i;
            intIndex = (int) Math.floor(doubleIndex);
            if (i == 0) {
                output[i] = samples[0];
            } else {
                short baseSample = samples[intIndex];
                short nextSample = samples[(intIndex + 1) % oldLength];
                int difference = nextSample - baseSample;
                double positionBetweenSamples = doubleIndex - intIndex;
                double newSample = baseSample + positionBetweenSamples * difference;
                output[i] = (short) (newSample);
            }
        }

        return output;
    }

}
