package com.louislepper.waveform;

import junit.framework.TestCase;

public class WaveformWrapperTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();

    }

    public void tearDown() throws Exception {

    }

    public void testGet() throws Exception {
        short[] test = {1,2,3, 0, 10, 4, 5,6,7};
        WaveformWrapper wrapper = new WaveformWrapper(test);
        //Double arithmetic probably means this is fucked.
        assertTrue(ConstantsTest.equals(wrapper.get(3.0), 0));
        assertTrue(ConstantsTest.equals(wrapper.get(4.0), 10));
        assertTrue(ConstantsTest.equals(wrapper.get(3.5), 5));
        assertTrue(ConstantsTest.equals(wrapper.get(4.5), 7));
        assertTrue(ConstantsTest.equals(wrapper.get(0.5), 1));
        assertTrue(ConstantsTest.equals(wrapper.get(0.0), 1));
        assertTrue(ConstantsTest.equals(wrapper.get(-1.0), 7));
        assertTrue(ConstantsTest.equals(wrapper.get(-2.0), 6));
        assertTrue(ConstantsTest.equals(wrapper.get(-4.5), 7));
        assertTrue(ConstantsTest.equals(wrapper.get(8.0), 7));
        assertTrue(ConstantsTest.equals(wrapper.get(8.5), 4));
        assertTrue(ConstantsTest.equals(wrapper.get(9.0), 1));

    }
    //TODO: Add test for constructor that takes startandend object.
}