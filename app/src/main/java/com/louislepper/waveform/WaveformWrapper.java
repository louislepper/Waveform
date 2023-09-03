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

public class WaveformWrapper {
    private final short[] waveform;

    public int size() {
        return length;
    }

    private int length;
    private int start;

    public WaveformWrapper(short[] waveform){
        this.waveform = waveform;
        this.length = waveform.length;
        this.start = 0;
    }

    public WaveformWrapper(short[] waveform, SampleInterpolator.StartAndEnd startAndEnd){
        this.waveform = waveform;
        this.length = startAndEnd.getEnd() - startAndEnd.getStart();
        this.start = startAndEnd.getStart();
    }

    private short getWrapped(int index) {
        index = Constants.moduloLowerAndUpperBound(index, this.start + this.length, this.start);
        return waveform[index];
    }

    public short get(double index){
        if(length == 0) return 0;
        index = index - (double) start;
        int lowerIndex = (int) Math.floor(index);

        int upperIndex = ((int) Math.ceil(index));

        double indexDifference = index - lowerIndex; //Should always be between 0 and 1

        short lower = getWrapped(lowerIndex);
        short upper = getWrapped(upperIndex);

        double difference = upper - lower;
        double unroundedResult = ((double) lower) + (difference * indexDifference);
        return (short) unroundedResult;
    }
}