package com.louislepper.waveform

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Locks in the current behavior of [SampleCrossfader.crossfade].
 *
 * This algorithm is intricate (it walks outward from the midpoint of the first/last sample in
 * both directions along a circular buffer defined by [start, start + length)), so the expected
 * arrays here were derived by hand-tracing the algorithm step by step AND cross-checked by
 * actually running the method, since manual tracing of this much index/modulo arithmetic is
 * error-prone. Every case is a genuine snapshot of current behavior.
 */
class SampleCrossfaderTest {

    @Test
    fun `empty array is returned unchanged`() {
        val input = shortArrayOf()

        val result = SampleCrossfader.crossfade(input, 0, 0)

        assertSame(input, result)
        assertArrayEquals(shortArrayOf(), result)
    }

    @Test
    fun `length less than two is a no-op`() {
        val input = shortArrayOf(1, 2)

        val result = SampleCrossfader.crossfade(input, 0, 1)

        assertSame(input, result)
        assertArrayEquals(shortArrayOf(1, 2), result)
    }

    @Test
    fun `first sample equal to last sample is a no-op`() {
        val input = shortArrayOf(5, 1, 2, 3, 5)

        val result = SampleCrossfader.crossfade(input, 0, 5)

        assertSame(input, result)
        assertArrayEquals(shortArrayOf(5, 1, 2, 3, 5), result)
    }

    @Test
    fun `crossfade mutates the array in place and returns the same reference`() {
        val input = shortArrayOf(10, 3, 4, 5, 2)

        val result = SampleCrossfader.crossfade(input, 0, 5)

        assertSame(input, result)
    }

    @Test
    fun `first sample greater than last sample smooths the whole range`() {
        val input = shortArrayOf(10, 3, 4, 5, 2)

        val result = SampleCrossfader.crossfade(input, 0, 5)

        assertArrayEquals(shortArrayOf(6, 2, 3, 4, 5), result)
    }

    @Test
    fun `first sample less than last sample smooths the whole range`() {
        val input = shortArrayOf(2, 3, 4, 5, 10)

        val result = SampleCrossfader.crossfade(input, 0, 5)

        assertArrayEquals(shortArrayOf(5, 4, 3, 3, 6), result)
    }

    @Test
    fun `smoothing can stop after only touching the midpoint sample`() {
        // Here the samples adjacent to the endpoints already sit on/under the baseline and
        // on/under the extrapolated top line, so both walk loops exit immediately and only the
        // single midpoint sample (index 4, the last sample) is changed.
        val input = shortArrayOf(8, 1, 6, 2, 9)

        val result = SampleCrossfader.crossfade(input, 0, 5)

        assertArrayEquals(shortArrayOf(8, 1, 6, 2, 8), result)
    }

    @Test
    fun `samples outside start plus length are untouched, including with a non-zero start`() {
        // The crossfade window is indices [1, 6) = {10, 3, 4, 5, 90}. Index 0 and indices 6,7
        // sit outside the window and must be left exactly as they were, even though the
        // circular modulo addressing inside the algorithm wraps within [start, start+length).
        val input = shortArrayOf(100, 10, 3, 4, 5, 90, 0, 0)

        val result = SampleCrossfader.crossfade(input, 1, 5)

        assertArrayEquals(shortArrayOf(100, 10, 3, 4, 5, 50, 0, 0), result)
    }
}
