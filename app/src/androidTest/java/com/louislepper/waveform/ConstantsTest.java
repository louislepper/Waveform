package com.louislepper.waveform;

import junit.framework.TestCase;

public class ConstantsTest extends TestCase{

    public void testIndexToFrequency(){
        double toneA = Constants.getFrequencyByIndex(0);
        double toneB = Constants.getFrequencyByIndex(1);
        double offsetNote = Constants.getOffsetNote(toneA, 1);
        assertTrue("Tones should be equal. Tone A = " + toneA +". Tone B = " + toneB + ". Tone B generated from offset = " + offsetNote, equals(toneB, offsetNote));
    }

    private boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
}