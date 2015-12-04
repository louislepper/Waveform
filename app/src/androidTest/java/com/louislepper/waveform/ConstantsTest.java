package com.louislepper.waveform;

import junit.framework.TestCase;

public class ConstantsTest extends TestCase{

    public static final int D7_MIDI_NOTE = 98;
    public static final double D7_FREQUENCY = 2349.31814;

    public static final int A0_MIDI_NOTE = 21;
    public static final double A0_FREQUENCY = 27.5;

    //    public void testIndexToFrequency(){
//        double toneA = Constants.getFrequencyByIndex(0);
//        double toneB = Constants.getFrequencyByIndex(1);
//        double offsetNote = Constants.getOffsetNote(toneA, 1);
//        assertTrue("Tones should be equal. Tone A = " + toneA +". Tone B = " + toneB + ". Tone B generated from offset = " + offsetNote, equals(toneB, offsetNote));
//    }
    public void testMidiC1IndexToFrequency(){
        final double generatedFrequency = Constants.getFrequencyByIndex(Constants.C1_MIDI_NOTE);
        final double knownFrequency = Constants.C1_FREQUENCY;
        assertTrue("Frequencies should be equal. Known frequency = " + knownFrequency +". Generated = " + generatedFrequency, equals(generatedFrequency, knownFrequency));
    }

    public void testMidiD7IndexToFrequency(){
        final double generatedFrequency = Constants.getFrequencyByIndex(D7_MIDI_NOTE);
        final double knownFrequency = D7_FREQUENCY;
        assertTrue("Frequencies should be equal. Known frequency = " + knownFrequency +". Generated = " + generatedFrequency, equals(generatedFrequency, knownFrequency));
    }

    public void testC1D7Offset(){
        final double generatedFrequency = Constants.getOffsetNote(Constants.C1_FREQUENCY, D7_MIDI_NOTE - Constants.C1_MIDI_NOTE);
        final double knownFrequency = D7_FREQUENCY;

        assertTrue("Frequencies should be equal. Known frequency = " + knownFrequency +". Generated = " + generatedFrequency, equals(generatedFrequency, knownFrequency));
    }

    public void testC1A0Offset(){
        final double generatedFrequency = Constants.getOffsetNote(Constants.C1_FREQUENCY, A0_MIDI_NOTE - Constants.C1_MIDI_NOTE);
        final double knownFrequency = A0_FREQUENCY;

        assertTrue("Frequencies should be equal. Known frequency = " + knownFrequency +". Generated = " + generatedFrequency, equals(generatedFrequency, knownFrequency));
    }


    static boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.00001;
    }
}