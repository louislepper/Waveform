package com.louislepper.waveform;

import junit.framework.TestCase;
import static org.junit.Assert.assertArrayEquals;


public class SampleInterpolatorTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testInterpolateInvalidSamples() throws Exception {
        short[] samples = new short[]{-1, -1, -1, 1, 2, -1, -1, -1, -1, -1, 8, 9, 10, -1, -1, -1};
        short[] expectedArray = new short[]{-1, -1, -1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -1, -1, -1};
        SampleInterpolator.StartAndEnd expectedStartAndEnd = new SampleInterpolator.StartAndEnd(3, 12);
        SampleInterpolator.StartAndEnd startAndEnd = SampleInterpolator.interpolateInvalidSamples(samples);

        assertTrue(expectedStartAndEnd.equals(startAndEnd));
        assertArrayEquals(expectedArray, samples);
    }

    public void testInterpolateInvalidSamples1() throws Exception {

    }
}