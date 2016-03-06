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

import junit.framework.TestCase;

public class WaveformWrapperTest extends TestCase {
    public void testGet() throws Exception {
        short[] test = {1,2,3, 0, 10, 4, 5,6,7};
        WaveformWrapper wrapper = new WaveformWrapper(test);

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
}