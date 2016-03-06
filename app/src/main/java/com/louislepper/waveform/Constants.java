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

public class Constants {
    private Constants(){}

    public static final double C1_FREQUENCY = 32.7032; //C1
    public static final int C1_MIDI_NOTE = 24;

    public static double getFrequencyByIndex(int index) { //Index corresponds to note of scale
        //Our chosen base note in this method is A1, but we want everything to correspond with the
        // midi numbers of each note, so this offset is necessary.
        final int OFFSET = 33;
        return 55.0 * Math.pow(2.0, ((double) (index - OFFSET)) / 12.0);
    }

    public static int moduloLowerAndUpperBound(int value, int upperMod, int lowerMod){
        upperMod -= lowerMod;
        value -= lowerMod;

        if (value >= upperMod) {
            int thing = value/upperMod;
            return lowerMod + (value - (thing * upperMod));
        } else if (value < 0) {
            int thing = Math.abs(value/upperMod);
            return lowerMod + (value + (thing + 1) * upperMod);
        }
        return lowerMod + value;
    }
}
