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