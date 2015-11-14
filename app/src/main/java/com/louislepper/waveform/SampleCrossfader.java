package com.louislepper.waveform;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;

import java.io.LineNumberReader;
import java.util.Arrays;

public class SampleCrossfader {
    private SampleCrossfader(){}
    public static short[] crossfade(short[] shortSoundArray, int start, int length) {
        if(shortSoundArray.length == 0) return shortSoundArray;
        if(length < 2) return  shortSoundArray;

        double[] soundArray = Doubles.toArray(Shorts.asList(Arrays.copyOfRange(shortSoundArray, start, length)));
        double firstSample = soundArray[0];
        double lastSample = soundArray[soundArray.length - 1];
        if(equals(firstSample, lastSample)) return shortSoundArray;

        double midPoint = halfwayBetween(firstSample, lastSample);

        double bigger = Math.max(firstSample, lastSample);
        double smaller = Math.min(firstSample, lastSample);

        int index;
        int direction = 0;
        if(firstSample == bigger) {
            index = 0;
            direction = RIGHT;
        } else {
            index = soundArray.length - 1;
            direction = LEFT;
        }

        soundArray[index] = midPoint;
        int midPointIndex = index;

        double baseLine = smaller;

        //Want to catch if it goes past too.
        while(soundArray[moduloIncludingZero(index + direction, soundArray.length)] > baseLine) {
            index = moduloIncludingZero(index + direction, soundArray.length);
            //TODO: Should we change the original array? Or make a new one?
            soundArray[index] = halfwayBetween(baseLine, soundArray[index]);
        }
        index = index + direction;
        Line topLine = new Line(midPointIndex, bigger, index, soundArray[index]);

        direction = direction * -1;
        index = midPointIndex;

        int topLineIncrementer = direction * -1;
        int topLineIndex = index;
        while(soundArray[moduloIncludingZero((index + direction), soundArray.length)] < topLine.getyPoint(topLineIndex + topLineIncrementer)) {
            soundArray[moduloIncludingZero((index + direction), soundArray.length)] = halfwayBetween(topLine.getyPoint(topLineIndex + topLineIncrementer), soundArray[moduloIncludingZero((index + direction), soundArray.length)]);
            topLineIndex += topLineIncrementer;
            index = index + direction;
        }

        short[] arrayToReturn = Arrays.copyOf(shortSoundArray, shortSoundArray.length);
        short[] finalShorts = Shorts.toArray(Doubles.asList(soundArray));
        for(int i = start, o = 0; i < length; i++, o++) {
            arrayToReturn[i] = finalShorts[o];
        }

        return arrayToReturn;
    }

    private static double halfwayBetween(double a, double b){
        return (a + b)/2;
    }

    private static boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.0001;
    }

    //Visible for testing
    public static int moduloIncludingZero(int value, int modBy){
        if (value >= modBy) {
            int thing = value/modBy;
            return value - (thing * modBy);
        } else if (value < 0) {
            int thing = Math.abs(value/modBy);
            return value + (thing + 1) * modBy;
        }
        return value;
    }

    final private static int LEFT = -1;
    final private static int RIGHT = 1;

    //Visible for testing
    public static class Line{
        private double xStart, yStart, xEnd, yEnd, m, b;

        public Line(double xStart, double yStart, double xEnd, double yEnd) {
            //y = mx + b
            this.xStart = xStart;
            this.yStart = yStart;
            this.xEnd = xEnd;
            this.yEnd = yEnd;
            this.m = (yEnd - yStart)/(xEnd - xStart);
            //y - b = mx
            //-b = mx - y
            //b = y -mx
            this.b = yStart - m * xStart;
        }

        public double getyPoint(double xValue){
            return m * xValue + b;
        }
    }

}
