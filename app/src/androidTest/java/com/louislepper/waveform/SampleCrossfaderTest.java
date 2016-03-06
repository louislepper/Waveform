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

import com.google.common.primitives.Shorts;

import junit.framework.TestCase;

import java.util.Arrays;

import static com.louislepper.waveform.SampleCrossfader.crossfade;
import static org.junit.Assert.assertArrayEquals;

public class SampleCrossfaderTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testCrossfadeA() throws Exception {
        short[] beforeFade = {5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,6,7,8,9,10,11,12,13,14,15};
        //short[] intendedFade = {9.5,9,8.5,8,7.5,7,6.5,6,5.5,5,5,5,5,5,5,5,5.5,6,6.5,7,7.5,8,8.5,9,9.5,10};
        short[] intendedFade = {9,9,8,8,7,7,6,6,5,5,5,5,5,5,5,5,5,6,6,7,7,8,8,9,9,10};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }


    public void testCrossfadeB() throws Exception {
        short[] beforeFade = {15,14,13,12,11,10,9,8,7,6,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5};
//        short[] intendedFade = {10,9.5,9,8.5,8,7.5,7,6.5,6,5.5,5,5,5,5,5,5,5,5.5,6,6.5,7,7.5,8,8.5,9,9.5};
        short[] intendedFade = {10,9,9,8,8,7,7,6,6,5,5,5,5,5,5,5,5,5,6,6,7,7,8,8,9,9};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testCrossfadeC() throws Exception {
        short[] beforeFade = {15,14,13,12,11,10,9,8,7,6,5,5,5,6,5,5,5,5,5,5,5,5,5,5,5,5};
//        short[] intendedFade = {10,9.5,9,8.5,8,7.5,7,6.5,6,5.5,5,5,5,6,5,5,5,5.5,6,6.5,7,7.5,8,8.5,9,9.5};
        short[] intendedFade = {10,9,9,8,8,7,7,6,6,5,5,5,5,6,5,5,5,5,6,6,7,7,8,8,9,9};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeD() throws Exception {
        short[] beforeFade = {15,14,13,13,13,10,9,8,7,6,5,5,5,6,5,5,5,5,3,5,7,5,5,5,5,5};
//        short[] intendedFade = {10,9.5,9,9,9,7.5,7,6.5,6,5.5,5,5,5,6,5,5,5,5.5,5,6.5,8,7.5,8,8.5,9,9.5};
        short[] intendedFade = {10,9,9,9,9,7,7,6,6,5,5,5,5,6,5,5,5,5,5,6,8,7,8,8,9,9};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeE() throws Exception {
        short[] beforeFade = {10,8,6,4,2,0,0,2,4,6,8,10,8,6,4,2,0};
//      short[] intendedFade = {5,4,3,2,1,0,0,2,4,6,8,10,8,6,4,4,4};
        short[] intendedFade = {5,4,3,2,1,0,0,2,4,6,8,10,8,6,4,4,4};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeF() throws Exception {
        short[] beforeFade = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        short[] intendedFade = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeG() throws Exception {
        short[] beforeFade = {1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1};
        short[] intendedFade = {1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeH() throws Exception {
        short[] beforeFade = {99, 99, 99, 1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1};
        short[] intendedFade = {99, 99, 99, 1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 3, beforeFade.length - 3);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }
    public void testCrossfadeI() throws Exception {
        short[] beforeFade = {99, 99, 99, 1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1, 99, 99, 99};
        short[] intendedFade = {99, 99, 99, 1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1, 99, 99, 99};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 3, beforeFade.length - 6);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testCrossfadeJ() throws Exception {
        short[] beforeFade = {123, 431, 123, 5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,6,7,8,9,10,11,12,13,14,15, 333, 222};
        //short[] intendedFade = {123, 431, 123, 9.5,9,8.5,8,7.5,7,6.5,6,5.5,5,5,5,5,5,5,5,5.5,6,6.5,7,7.5,8,8.5,9,9.5,10, 333, 222};
        short[] intendedFade = {123, 431, 123, 9,9,8,8,7,7,6,6,5,5,5,5,5,5,5,5,5,6,6,7,7,8,8,9,9,10, 333, 222};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 3, beforeFade.length - 5);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testCrossfadeJustTwo() throws Exception {
        short[] beforeFade = {5,15};
        short[] intendedFade = {5, 10};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testCrossfadeOne() throws Exception {
        short[] beforeFade = {5};
        short[] intendedFade = {5};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testCrossfadeZero() throws Exception {
        short[] beforeFade = {};
        short[] intendedFade = {};

        short[] afterFade = crossfade(Arrays.copyOf(beforeFade, beforeFade.length), 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testLine() throws Exception {
        SampleCrossfader.Line line = new SampleCrossfader.Line(0,0,10,10);
        assertTrue(ConstantsTest.equals(line.getyPoint(5.0), 5.0));
        assertTrue(ConstantsTest.equals(line.getyPoint(1.0), 1.0));
        assertTrue(ConstantsTest.equals(line.getyPoint(0.5), 0.5));
    }

    public void testUpperLowerModulo() throws Exception {
        assertTrue(Constants.moduloLowerAndUpperBound(-5, 4, 0) == 3);
        assertTrue(Constants.moduloLowerAndUpperBound(-6, 4, 0) == 2);
        assertTrue(Constants.moduloLowerAndUpperBound(0, 4, 0) == 0);
        assertTrue(Constants.moduloLowerAndUpperBound(2, 4, 0) == 2);
        assertTrue(Constants.moduloLowerAndUpperBound(4, 4, 0) == 0);
        assertTrue(Constants.moduloLowerAndUpperBound(5, 4, 0) == 1);
        assertTrue(Constants.moduloLowerAndUpperBound(40, 4, 0) == 0);
        assertTrue(Constants.moduloLowerAndUpperBound(5, 5, 0) == 0);

        assertTrue(Constants.moduloLowerAndUpperBound(5, 5, 1) == 1);
        assertTrue(Constants.moduloLowerAndUpperBound(5, 5, 2) == 2);
        assertTrue(Constants.moduloLowerAndUpperBound(6, 5, 2) == 3);
        assertTrue(Constants.moduloLowerAndUpperBound(2, 5, 2) == 2);
        assertTrue(Constants.moduloLowerAndUpperBound(1, 5, 2) == 4);
    }


    private static String errorMessage(short[] original, short[] result, short[] intended) {
        return "Original: " + "\n" + "{" + Shorts.join(",", original) + "}" + "\n" + "{" + Shorts.join(",", result) + "}" + " should equal " + "\n" + "{" + Shorts.join(",", intended) + "}";
    }

}