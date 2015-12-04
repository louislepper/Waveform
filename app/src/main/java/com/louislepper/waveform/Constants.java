package com.louislepper.waveform;

public class Constants {
    private Constants(){}

    public static final double C1_FREQUENCY = 32.7032; //C1
    public static final int C1_MIDI_NOTE = 24;

    public static double getFrequencyByIndex(int index) { //Index corresponds to note of scale
        //Our chosen base note in this method is A1, but we want everything to correspond with the
        // midi numbers of each note, so this offset is necessary.
        final int OFFSET = 33;
        return 55.0 * Math.pow(2.0, ((double) (index - OFFSET))/12.0);
    }
    //TODO: Add tests to these methods to make sure you got the equations right.
    public static double getOffsetNote(double startingFrequency, int noteOffset) {
//        if(startingFrequency <= 0.0) throw new IllegalArgumentException("Starting frequency cannot be lower than zero.");
//        return ((12.0 * Math.log(startingFrequency/55.0))/Math.log(2.0)) - 36;
        return (12.0 * Math.log((1.0/55.0) * Math.pow(2.0, (11.0/4.0 - ((double) noteOffset)/12.0)) * startingFrequency))/Math.log(2.0);
    }

    //Visible for testing
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

    //Visible for testing
    public static long moduloLowerAndUpperBound(long value, long upperMod, long lowerMod){
        upperMod -= lowerMod;
        value -= lowerMod;

        if (value >= upperMod) {
            long thing = value/upperMod;
            return lowerMod + (value - (thing * upperMod));
        } else if (value < 0) {
            long thing = Math.abs(value/upperMod);
            return lowerMod + (value + (thing + 1) * upperMod);
        }
        return lowerMod + value;
    }

    //TODO: I don't know if this works. Test this.
    public static double moduloLowerAndUpperBound(double value, double upperMod, double lowerMod){
        upperMod -= lowerMod;
        value -= lowerMod;

        if (value >= upperMod) {
            long thing = (long) value / (long) upperMod;
            return lowerMod + (value - (thing * upperMod));
        } else if (value < 0) {
            double thing = Math.abs(value/upperMod);
            return lowerMod + (value + (thing + 1) * upperMod);
        }
        return lowerMod + value;
    }
}
