package com.louislepper.waveform;

public class Constants {
    private Constants(){}
    public static double getFrequencyByIndex(int index) { //Index corresponds to note of scale
        return 440.0 * Math.pow(2.0, ((double) index)/12.0);
    }
    //TODO: Add tests to these methods to make sure you got the equations right.
    public static double getOffsetNote(double startingFrequency, int noteOffset) {
        if(startingFrequency <= 0.0) throw new IllegalArgumentException("Starting frequency cannot be lower than zero.");
        return ((12.0 * Math.log(startingFrequency/55.0))/Math.log(2.0)) - 36;
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

}
