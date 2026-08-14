package com.louislepper.waveform

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Locks in the current behavior of [WaveformWrapper].
 */
class WaveformWrapperTest {

    private companion object {
        val WAVEFORM = shortArrayOf(0, 10, 20, 30)
    }

    @Test
    fun `size returns the underlying array length for the simple constructor`() {
        val wrapper = WaveformWrapper(WAVEFORM)

        assertEquals(4, wrapper.size())
    }

    @Test
    fun `get interpolates linearly between two samples`() {
        val wrapper = WaveformWrapper(WAVEFORM)

        assertEquals(5, wrapper.get(0.5).toInt())
        assertEquals(15, wrapper.get(1.5).toInt())
    }

    @Test
    fun `get wraps past the end back to the start`() {
        val wrapper = WaveformWrapper(WAVEFORM)

        // Halfway between the last sample (30) and the first sample (0), wrapping around.
        assertEquals(15, wrapper.get(3.5).toInt())
    }

    @Test
    fun `get wraps below the start back to the end`() {
        val wrapper = WaveformWrapper(WAVEFORM)

        // Halfway between the first sample (0) and the last sample (30), wrapping backward.
        assertEquals(15, wrapper.get(-0.5).toInt())
    }

    @Test
    fun `get on an empty waveform returns zero`() {
        val wrapper = WaveformWrapper(shortArrayOf())

        assertEquals(0, wrapper.get(0.0).toInt())
        assertEquals(0, wrapper.get(2.0).toInt())
    }

    @Test
    fun `StartAndEnd constructor derives size as end minus start, not inclusive`() {
        // Note: this differs from SampleInterpolator.StartAndEnd#getLength(), which is
        // inclusive (1 + end - start = 3 for this range). Here size() uses end - start
        // directly (= 2), so the wrapper's window is effectively one sample narrower than
        // StartAndEnd's own notion of length for the same start/end pair.
        val waveform = shortArrayOf(0, 10, 20, 30, 40)
        val startAndEnd = SampleInterpolator.StartAndEnd(1, 3)

        val wrapper = WaveformWrapper(waveform, startAndEnd)

        assertEquals(2, wrapper.size())
        assertEquals(3, startAndEnd.length)
    }
}
