package com.louislepper.waveform;

import junit.framework.TestCase;
import static org.junit.Assert.assertArrayEquals;


import java.util.Arrays;

public class WaveStretcherTest extends TestCase {

    public void testNormalize() throws Exception {
        short[] testArray = {0, 56};
        short[] resultArray = Arrays.copyOf(testArray, testArray.length);
        WaveStretcher.normalize(resultArray);
        assertArrayEquals(new short[]{Short.MIN_VALUE + 1, Short.MAX_VALUE}, resultArray);
    }

    public void testNormalizeZero() throws Exception {
        short[] testArray = {};
        short[] resultArray = Arrays.copyOf(testArray, testArray.length);
        WaveStretcher.normalize(resultArray);
        assertArrayEquals(new short[]{}, resultArray);
    }

    public void testNormalizeOne() throws Exception {
        short[] testArray = {1};
        short[] resultArray = Arrays.copyOf(testArray, testArray.length);
        WaveStretcher.normalize(resultArray);
        assertArrayEquals(new short[]{0}, resultArray);
    }

    public void testNormalizeThree() throws Exception {
        short[] testArray = {0, 25, 50};
        short[] resultArray = Arrays.copyOf(testArray, testArray.length);
        WaveStretcher.normalize(resultArray);
        assertArrayEquals(new short[]{Short.MIN_VALUE + 1, 0,  Short.MAX_VALUE}, resultArray);
    }
}