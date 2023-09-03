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

public class SampleInterpolator {
    private SampleInterpolator(){}

    private static final int INVALID_SAMPLE = -1;

    private enum State {
        FINDING_FIRST_SAMPLE,
        VALID_SAMPLES,
        INVALID_STRETCH
    }

    public static class StartAndEnd{
        private final int start;
        private final int end;

        public StartAndEnd(int start, int end){
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public int getLength(){
            return 1 + end - start;
        }

        public boolean equals(StartAndEnd a) {
            return a.start == this.start && a.end == this.end;
        }

    }

    public static StartAndEnd interpolateInvalidSamples(short[] samples) {
        // Ignore all missing samples at the start and end of the array.
        // Interpolate missing sample blocks in the middle

        State state = State.FINDING_FIRST_SAMPLE;
        int firstValidSampleIndex = 0;
        int lastValidSampleIndex = 0;

        for (int i = 0; i < samples.length; i++) {
            short sample = samples[i];
            switch (state) {
                case FINDING_FIRST_SAMPLE:
                    if (sample != INVALID_SAMPLE) {
                        firstValidSampleIndex = i;
                        lastValidSampleIndex = i;
                        state = State.VALID_SAMPLES;
                    }
                    break;
                case VALID_SAMPLES:
                    if (sample == INVALID_SAMPLE) {
                        state = State.INVALID_STRETCH;
                    } else {
                        lastValidSampleIndex = i;
                    }
                    break;
                case INVALID_STRETCH:
                    if (sample != INVALID_SAMPLE) {
                        // interpolate between the last valid sample and this one
                        short lastValidSample = samples[lastValidSampleIndex];
                        double increment = (sample - lastValidSample) / (double) (i - lastValidSampleIndex);
                        double accumulator = lastValidSample;

                        for (int j = lastValidSampleIndex + 1; j < i; j++) {
                            samples[j] = (short) (accumulator += increment);
                        }
                        state = State.VALID_SAMPLES;
                    }
                    break;
            }
        }
        return new StartAndEnd(firstValidSampleIndex, lastValidSampleIndex);
    }
}
