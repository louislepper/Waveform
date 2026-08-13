package com.louislepper.waveform

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Locks in the current (pre-toolchain-upgrade) behavior of [WaveStretcher.normalize].
 *
 * normalize() centers a sample array around zero and scales it so the wider of the two
 * extremes touches [Short.MAX_VALUE]. All expected values below were derived by hand from
 * the algorithm:
 *   midpoint = (max + min) / 2
 *   scale    = Short.MAX_VALUE / ((max - min) / 2.0)
 *   output[i] = (short) (scale * (input[i] - midpoint))
 *
 * IMPORTANT / potential bug: when every sample has the same value, max == min, so the half
 * range ("top_bound") is 0.0 and scale becomes Double.POSITIVE_INFINITY. Every sample then
 * becomes input[i] - midpoint == 0.0, and Infinity * 0.0 is NaN under IEEE-754. Casting a NaN
 * double to short in Java/Kotlin yields 0. So a flat/silent waveform is silently normalized to
 * all-zero rather than left unchanged or throwing. This was confirmed by actually running the
 * method (see flatArray* tests below) rather than assumed, exactly because it's surprising.
 */
class WaveStretcherTest {

    private companion object {
        const val FLAT_VALUE: Short = 5
    }

    @Test
    fun `empty array returns same empty array`() {
        val input = shortArrayOf()

        val result = WaveStretcher.normalize(input)

        assertSame(input, result)
        assertArrayEquals(shortArrayOf(), result)
    }

    @Test
    fun `flat non-zero array normalizes to all zeros via NaN cast`() {
        val input = shortArrayOf(FLAT_VALUE, FLAT_VALUE, FLAT_VALUE, FLAT_VALUE)

        val result = WaveStretcher.normalize(input)

        // max == min => division by zero => NaN => (short) NaN == 0. See class doc.
        assertArrayEquals(shortArrayOf(0, 0, 0, 0), result)
    }

    @Test
    fun `flat zero array normalizes to all zeros`() {
        val input = shortArrayOf(0, 0, 0)

        val result = WaveStretcher.normalize(input)

        assertArrayEquals(shortArrayOf(0, 0, 0), result)
    }

    @Test
    fun `single element array normalizes to zero`() {
        val input = shortArrayOf(5000)

        val result = WaveStretcher.normalize(input)

        // Same max == min degenerate case as the flat-array tests, just with length 1.
        assertArrayEquals(shortArrayOf(0), result)
    }

    @Test
    fun `symmetric wave scales the extremes to Short MAX_VALUE`() {
        // min = -16384, max = 16384 (both powers of two, so the arithmetic below is exact
        // in double precision and there is no floating point ambiguity in the expectation).
        val input = shortArrayOf(-16384, 0, 8192, 16384)

        val result = WaveStretcher.normalize(input)

        // scale = 32767 / 16384 = 1.99993896484375 exactly.
        // -16384 -> -32767, 16384 -> 32767 (both exact).
        // 8192 -> 16383.5, truncated toward zero by the (short) cast -> 16383.
        assertArrayEquals(shortArrayOf(-32767, 0, 16383, 32767), result)
    }

    @Test
    fun `asymmetric wave is centered around its midpoint before scaling`() {
        // min = -8192, max = 24576 => midpoint = 8192, half-range = 16384 (power of two).
        val input = shortArrayOf(-8192, 0, 8192, 24576)

        val result = WaveStretcher.normalize(input)

        // (-8192 - 8192) * scale = -16384 * 1.99993896484375 = -32767 (exact)
        // (0 - 8192) * scale     =  -8192 * 1.99993896484375 = -16383.5 -> -16383 (truncated)
        // (8192 - 8192) * scale  = 0
        // (24576 - 8192) * scale =  16384 * 1.99993896484375 = 32767 (exact)
        assertArrayEquals(shortArrayOf(-32767, -16383, 0, 32767), result)
    }

    @Test
    fun `already normalized symmetric input is left unchanged`() {
        val input = shortArrayOf(-32767, 0, 32767)

        val result = WaveStretcher.normalize(input)

        // max = 32767, min = -32767 => top_bound = max => scale = 1.0, shift = 0.
        assertArrayEquals(shortArrayOf(-32767, 0, 32767), result)
    }
}
