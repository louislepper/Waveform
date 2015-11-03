package com.louislepper.waveform;

import junit.framework.TestCase;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class ConstantsTest extends TestCase{

    @Test
    public void testIndexToFrequency(){
        double toneA = Constants.getFrequencyByIndex(0);
        double toneB = Constants.getFrequencyByIndex(1);
        assertTrue("Tones should be equal.", equals(toneB, Constants.getOffsetNote(toneA, 1)));
    }

    private boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
}