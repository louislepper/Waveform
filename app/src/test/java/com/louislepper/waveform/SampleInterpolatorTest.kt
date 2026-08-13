package com.louislepper.waveform

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Locks in the current behavior of [SampleInterpolator.interpolateInvalidSamples].
 *
 * -1 marks an invalid/missing sample. Leading and trailing runs of -1 are left untouched.
 * Gaps in the middle are linearly interpolated between the last valid sample before the gap
 * and the first valid sample after it.
 *
 * POTENTIAL BUG, confirmed by running the actual code: [SampleInterpolator.StartAndEnd.end]
 * (the "last valid sample index") is only advanced while consecutive valid samples are being
 * read one after another (the `VALID_SAMPLES` -> `VALID_SAMPLES` transition). It is NOT
 * updated when an `INVALID_STRETCH` gap is closed by a new valid sample - the code only flips
 * the state back to `VALID_SAMPLES` without recording `i` as the new last-valid index. Two
 * consequences, both reproduced below:
 *   1. `getEnd()` after any array containing a gap is far lower than the true last valid index
 *      (see `singleGap...` and `leadingAndTrailing...` tests).
 *   2. Worse: because `lastValidSampleIndex` never advances past the gap, a SECOND gap later in
 *      the array re-interpolates all the way from the ORIGINAL first valid run, silently
 *      overwriting the values written for the first gap and even overwriting a genuine,
 *      previously-valid sample that happened to close the first gap. See
 *      `secondGapOverwritesFirstGapAndAGenuineValidSample`.
 */
class SampleInterpolatorTest {

    private companion object {
        const val INVALID: Short = -1
    }

    @Test
    fun `all valid samples are left unchanged and span the whole array`() {
        val input = shortArrayOf(1, 2, 3, 4, 5)

        val result = SampleInterpolator.interpolateInvalidSamples(input)

        assertArrayEquals(shortArrayOf(1, 2, 3, 4, 5), input)
        assertEquals(0, result.start)
        assertEquals(4, result.end)
        assertEquals(5, result.length)
    }

    @Test
    fun `all invalid samples are left unchanged with degenerate start-and-end`() {
        val input = shortArrayOf(INVALID, INVALID, INVALID)

        val result = SampleInterpolator.interpolateInvalidSamples(input)

        assertArrayEquals(shortArrayOf(INVALID, INVALID, INVALID), input)
        // No valid sample was ever found, but start/end default to 0/0 (misleadingly implying
        // index 0 is valid). Callers must not assume a non-empty StartAndEnd means real data.
        assertEquals(0, result.start)
        assertEquals(0, result.end)
        assertEquals(1, result.length)
    }

    @Test
    fun `empty array returns degenerate start-and-end`() {
        val input = shortArrayOf()

        val result = SampleInterpolator.interpolateInvalidSamples(input)

        assertArrayEquals(shortArrayOf(), input)
        assertEquals(0, result.start)
        assertEquals(0, result.end)
        assertEquals(1, result.length)
    }

    @Test
    fun `leading and trailing invalid runs are ignored and reflected in start-and-end`() {
        // Leading run: indices 0,1. Trailing run: indices 4,5 (never closed by a valid sample,
        // so it is left as-is, exactly like the leading run).
        val input = shortArrayOf(INVALID, INVALID, 5, 10, INVALID, INVALID)

        val result = SampleInterpolator.interpolateInvalidSamples(input)

        assertArrayEquals(shortArrayOf(INVALID, INVALID, 5, 10, INVALID, INVALID), input)
        assertEquals(2, result.start)
        assertEquals(3, result.end)
        assertEquals(2, result.length)
    }

    @Test
    fun `single sample gap is linearly interpolated`() {
        val input = shortArrayOf(0, INVALID, 10)

        val result = SampleInterpolator.interpolateInvalidSamples(input)

        assertArrayEquals(shortArrayOf(0, 5, 10), input)
        assertEquals(0, result.start)
        assertEquals(0, result.end) // see class doc: end is not advanced when a gap closes.
    }

    @Test
    fun `multi-sample gap interpolates exactly and truncates toward zero`() {
        // increment = (20 - 10) / 4 = 2.5, so the intermediate values land on a half-sample
        // boundary and exercise the (short) truncation-toward-zero cast.
        val input = shortArrayOf(10, INVALID, INVALID, INVALID, 20)

        val result = SampleInterpolator.interpolateInvalidSamples(input)

        assertArrayEquals(shortArrayOf(10, 12, 15, 17, 20), input)
        assertEquals(0, result.start)
        assertEquals(0, result.end) // bug: should logically be 4.
    }

    @Test
    fun `multi-sample gap with negative values truncates toward zero`() {
        // increment = (10 - -10) / 3 = 6.6666..., exercising truncation toward zero on the
        // negative side of the interpolation (-3.333 -> -3, not -4).
        val input = shortArrayOf(-10, INVALID, INVALID, 10)

        SampleInterpolator.interpolateInvalidSamples(input)

        assertArrayEquals(shortArrayOf(-10, -3, 3, 10), input)
    }

    @Test
    fun `second gap overwrites first gap and a genuine valid sample`() {
        // Two gaps: [5 .. 20] then [20 .. 35]. A naive reader would expect the first gap
        // interpolated relative to (5, 20) and the second relative to (20, 35). Because
        // lastValidSampleIndex is never advanced past index 0 (see class doc), the second gap
        // is actually interpolated as one straight line from index 0 (value 5) to index 5
        // (value 35), overwriting both the first gap's results AND the genuinely valid sample
        // at index 3 (originally 20).
        val input = shortArrayOf(5, INVALID, INVALID, 20, INVALID, 35)

        val result = SampleInterpolator.interpolateInvalidSamples(input)

        // increment = (35 - 5) / 5 = 6.0, applied from index 0 all the way to index 5.
        assertArrayEquals(shortArrayOf(5, 11, 17, 23, 29, 35), input)
        assertEquals(0, result.start)
        assertEquals(0, result.end)
    }

    @Test
    fun `StartAndEnd getLength is inclusive of both endpoints`() {
        assertEquals(2, SampleInterpolator.StartAndEnd(2, 3).length)
        assertEquals(1, SampleInterpolator.StartAndEnd(0, 0).length)
        assertEquals(5, SampleInterpolator.StartAndEnd(0, 4).length)
    }
}
