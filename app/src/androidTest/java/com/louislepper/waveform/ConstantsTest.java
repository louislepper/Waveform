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

import junit.framework.TestCase;

public class ConstantsTest extends TestCase {

    public static final int D7_MIDI_NOTE = 98;
    public static final double D7_FREQUENCY = 2349.31814;

    public void testMidiC1IndexToFrequency() {
        final double generatedFrequency = Constants.getFrequencyByIndex(Constants.C1_MIDI_NOTE);
        final double knownFrequency = Constants.C1_FREQUENCY;
        assertTrue("Frequencies should be equal. Known frequency = " + knownFrequency +". Generated = " + generatedFrequency, equals(generatedFrequency, knownFrequency));
    }

    public void testMidiD7IndexToFrequency() {
        final double generatedFrequency = Constants.getFrequencyByIndex(D7_MIDI_NOTE);
        final double knownFrequency = D7_FREQUENCY;
        assertTrue("Frequencies should be equal. Known frequency = " + knownFrequency +". Generated = " + generatedFrequency, equals(generatedFrequency, knownFrequency));
    }

    static boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
}