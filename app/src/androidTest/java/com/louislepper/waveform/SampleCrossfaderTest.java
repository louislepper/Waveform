package com.louislepper.waveform;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;

import junit.framework.TestCase;

import static com.louislepper.waveform.SampleCrossfader.*;
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

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }


    public void testCrossfadeB() throws Exception {
        short[] beforeFade = {15,14,13,12,11,10,9,8,7,6,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5};
//        short[] intendedFade = {10,9.5,9,8.5,8,7.5,7,6.5,6,5.5,5,5,5,5,5,5,5,5.5,6,6.5,7,7.5,8,8.5,9,9.5};
        short[] intendedFade = {10,9,9,8,8,7,7,6,6,5,5,5,5,5,5,5,5,5,6,6,7,7,8,8,9,9};

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testCrossfadeC() throws Exception {
        short[] beforeFade = {15,14,13,12,11,10,9,8,7,6,5,5,5,6,5,5,5,5,5,5,5,5,5,5,5,5};
//        short[] intendedFade = {10,9.5,9,8.5,8,7.5,7,6.5,6,5.5,5,5,5,6,5,5,5,5.5,6,6.5,7,7.5,8,8.5,9,9.5};
        short[] intendedFade = {10,9,9,8,8,7,7,6,6,5,5,5,5,6,5,5,5,5,6,6,7,7,8,8,9,9};

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeD() throws Exception {
        short[] beforeFade = {15,14,13,13,13,10,9,8,7,6,5,5,5,6,5,5,5,5,3,5,7,5,5,5,5,5};
//        short[] intendedFade = {10,9.5,9,9,9,7.5,7,6.5,6,5.5,5,5,5,6,5,5,5,5.5,5,6.5,8,7.5,8,8.5,9,9.5};
        short[] intendedFade = {10,9,9,9,9,7,7,6,6,5,5,5,5,6,5,5,5,5,5,6,8,7,8,8,9,9};

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeE() throws Exception {
        short[] beforeFade = {10,8,6,4,2,0,0,2,4,6,8,10,8,6,4,2,0};
//      short[] intendedFade = {5,4,3,2,1,0,0,2,4,6,8,10,8,6,4,4,4};
        short[] intendedFade = {5,4,3,2,1,0,0,2,4,6,8,10,8,6,4,4,4};

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeF() throws Exception {
        short[] beforeFade = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
        short[] intendedFade = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeG() throws Exception {
        short[] beforeFade = {1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1};
        short[] intendedFade = {1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1};

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);

    }

    public void testCrossfadeH() throws Exception {
        short[] beforeFade = {99, 99, 99, 1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1};
        short[] intendedFade = {99, 99, 99, 1,50,-40,10,1,1,1,50,50,-10,1,1,1,1,1,-90,1};

        short[] afterFade = crossfade(beforeFade, 3, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testCrossfadeJustTwo() throws Exception {
        short[] beforeFade = {5,15};
        short[] intendedFade = {5, 10};

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testCrossfadeOne() throws Exception {
        short[] beforeFade = {5};
        short[] intendedFade = {5};

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testCrossfadeZero() throws Exception {
        short[] beforeFade = {};
        short[] intendedFade = {};

        short[] afterFade = crossfade(beforeFade, 0, beforeFade.length);
        assertArrayEquals(errorMessage(beforeFade, afterFade, intendedFade), afterFade, intendedFade);
    }

    public void testLine() throws Exception {
        SampleCrossfader.Line line = new SampleCrossfader.Line(0,0,10,10);
        assertTrue(ConstantsTest.equals(line.getyPoint(5.0), 5.0));
        assertTrue(ConstantsTest.equals(line.getyPoint(1.0), 1.0));
        assertTrue(ConstantsTest.equals(line.getyPoint(0.5), 0.5));
    }

    public void testModuloIncludingZero() throws Exception {
        assertTrue(SampleCrossfader.moduloIncludingZero(-5, 4) == 3);
        assertTrue(SampleCrossfader.moduloIncludingZero(-6, 4) == 2);
        assertTrue(SampleCrossfader.moduloIncludingZero(0, 4) == 0);
        assertTrue(SampleCrossfader.moduloIncludingZero(2, 4) == 2);
        assertTrue(SampleCrossfader.moduloIncludingZero(4, 4) == 0);
        assertTrue(SampleCrossfader.moduloIncludingZero(5, 4) == 1);
        assertTrue(SampleCrossfader.moduloIncludingZero(40, 4) == 0);
        assertTrue(SampleCrossfader.moduloIncludingZero(5, 5) == 0);
    }

    private static String errorMessage(short[] original, short[] result, short[] intended) {
        return "Original: " + "\n" + "{" + Shorts.join(",", original) + "}" + "\n" + "{" + Shorts.join(",", result) + "}" + " should equal " + "\n" + "{" + Shorts.join(",", intended) + "}";
    }

}