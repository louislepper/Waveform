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

public class SampleCrossfader {
    private SampleCrossfader(){}

    /**
     * When someone draws a soundwave, the start and the end of the line aren't going to match up.
     * This results in a coarser sound, and potentially isn't what the user intended, so we smooth out
     * this bump here.
     *
     * This method changes the array in-place, for performance reasons.
     *
     * @param shortSoundArray The soundwave array
     * @param start Where the first sample occurs
     * @param length The size of the array
     * @return Smoothed array
     */
    public static short[] crossfade(short[] shortSoundArray, int start, int length) {
        if(shortSoundArray.length == 0) return shortSoundArray;
        if(length < 2) return  shortSoundArray;

        double firstSample = (double) shortSoundArray[start];
        double lastSample  = (double) shortSoundArray[start + length - 1];

        if(equals(firstSample, lastSample)) return shortSoundArray;
        //                   --                         --
        //                 --  --                         --
        //End of wave:   --      --     Start of wave:       --    Midpoint: --
        //                         --                          --
        //                           --
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

        //We set each sample to be the midpoint between the sample value, and the baseline, until
        //the new line intersects with an original sample
        short baseLine = (short) smaller;

        //Here's a basic example:
        //This represents the start and end of a sample array, shown joined together
        //eg. i = 99, 100, 101, 0, 1, 2, 3
        //             __________
        //           /        . /
        //          /       .  /
        //         /       .  /
        //        /      .   /
        //       /     *    /
        //      /    .     /
        //     /   .      /
        //	  / .        /
        //   /__________/
        // The diagonal lines(/) represent the original sample values
        // The star(*) represents the midpoint
        // The underscores(_) represent each baseline.
        // The new soundwave array is made by replacing each value with the midpoint of it and the baseline


       //If the next sample is past the baseline, then we've finished smoothing for this direction.
        while(shortSoundArray[Constants.moduloLowerAndUpperBound(index + direction, start + length, start)] > baseLine) {
            index = Constants.moduloLowerAndUpperBound(index + direction, start + length, start);
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

    public static class Line{
        private double gradient, yIntercept;

        public Line(double xStart, double yStart, double xEnd, double yEnd) {
            //y = mx + b
            this.gradient = (yEnd - yStart)/(xEnd - xStart);
            this.yIntercept = yStart - gradient * xStart;
        }

        public double getyPoint(double xValue){
            return gradient * xValue + yIntercept;
        }
    }

}
