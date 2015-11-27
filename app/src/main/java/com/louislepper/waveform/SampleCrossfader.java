package com.louislepper.waveform;

public class SampleCrossfader {
    private SampleCrossfader(){}
    public static short[] crossfade(short[] shortSoundArray, int start, int length) {
        if(shortSoundArray.length == 0) return shortSoundArray;
        if(length < 2) return  shortSoundArray;

        //double[] soundArray = Doubles.toArray(Shorts.asList(Arrays.copyOfRange(shortSoundArray, start, length)));
        double firstSample = (double) shortSoundArray[start];
        double lastSample  = (double) shortSoundArray[start + length - 1];
        if(equals(firstSample, lastSample)) return shortSoundArray;

        double midPoint = halfwayBetween(firstSample, lastSample);

        double bigger = Math.max(firstSample, lastSample);
        double smaller = Math.min(firstSample, lastSample);

        int index;
        int direction = 0;
        if(firstSample == bigger) {
            index = start;
            direction = RIGHT;
        } else {
            index = start + length - 1;
            direction = LEFT;
        }

        shortSoundArray[index] = (short) midPoint;
        int midPointIndex = index;

        short baseLine = (short) smaller;

        //Want to catch if it goes past too.
        while(shortSoundArray[Constants.moduloLowerAndUpperBound(index + direction, start + length, start)] > baseLine) {
            index = Constants.moduloLowerAndUpperBound(index + direction, start + length, start);
            //TODO: Should we change the original array? Or make a new one?
            shortSoundArray[index] = (short) halfwayBetween(baseLine, shortSoundArray[index]);
        }
        index = index + direction;
        Line topLine = new Line(midPointIndex, bigger, index, shortSoundArray[index]);

        direction = direction * -1;
        index = midPointIndex;

        int topLineIncrementer = direction * -1;
        int topLineIndex = index;
        while(shortSoundArray[Constants.moduloLowerAndUpperBound((index + direction), start + length, start)] < topLine.getyPoint(topLineIndex + topLineIncrementer)) {
            shortSoundArray[Constants.moduloLowerAndUpperBound((index + direction), start + length, start)] = (short) halfwayBetween(topLine.getyPoint(topLineIndex + topLineIncrementer), shortSoundArray[Constants.moduloLowerAndUpperBound((index + direction), start + length, start)]);
            topLineIndex += topLineIncrementer;
            index = index + direction;
        }

        return shortSoundArray;
    }

    private static double halfwayBetween(double a, double b){
        return (a + b)/2;
    }
    private static int halfwayBetween(int a, int b){
        return (a + b)/2;
    }

    private static boolean equals(double a, double b) {
        return Math.abs(a - b) < 0.0001;
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
