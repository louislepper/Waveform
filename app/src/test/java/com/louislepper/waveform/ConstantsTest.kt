package com.louislepper.waveform

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Locks in the current behavior of [Constants.getFrequencyByIndex] and
 * [Constants.moduloLowerAndUpperBound].
 */
class ConstantsTest {

    private companion object {
        const val FREQUENCY_DELTA = 0.0001
    }

    @Test
    fun `index 33 is A1 at 55Hz, the base note used for the offset`() {
        assertEquals(55.0, Constants.getFrequencyByIndex(33), FREQUENCY_DELTA)
    }

    @Test
    fun `index 45 is one octave above A1 at 110Hz`() {
        assertEquals(110.0, Constants.getFrequencyByIndex(45), FREQUENCY_DELTA)
    }

    @Test
    fun `index 57 is two octaves above A1 at 220Hz`() {
        assertEquals(220.0, Constants.getFrequencyByIndex(57), FREQUENCY_DELTA)
    }

    @Test
    fun `C1 midi note index matches the documented C1_FREQUENCY constant`() {
        val frequency = Constants.getFrequencyByIndex(Constants.C1_MIDI_NOTE)

        assertEquals(Constants.C1_FREQUENCY, frequency, FREQUENCY_DELTA)
        assertEquals(32.7032, frequency, FREQUENCY_DELTA)
    }

    @Test
    fun `modulo in range returns the value unchanged`() {
        assertEquals(5, Constants.moduloLowerAndUpperBound(5, 10, 0))
    }

    @Test
    fun `modulo above the upper bound wraps once`() {
        assertEquals(2, Constants.moduloLowerAndUpperBound(12, 10, 0))
    }

    @Test
    fun `modulo multiple ranges above the upper bound wraps correctly`() {
        assertEquals(5, Constants.moduloLowerAndUpperBound(25, 10, 0))
    }

    @Test
    fun `modulo below the lower bound wraps once`() {
        assertEquals(9, Constants.moduloLowerAndUpperBound(-1, 10, 0))
    }

    @Test
    fun `modulo multiple ranges below the lower bound wraps correctly`() {
        assertEquals(5, Constants.moduloLowerAndUpperBound(-15, 10, 0))
    }

    @Test
    fun `modulo with a non-zero lower bound in range returns the value unchanged`() {
        assertEquals(7, Constants.moduloLowerAndUpperBound(7, 8, 3))
    }

    @Test
    fun `modulo with a non-zero lower bound wraps below correctly`() {
        assertEquals(6, Constants.moduloLowerAndUpperBound(1, 8, 3))
    }

    @Test
    fun `modulo with a non-zero lower bound wraps above correctly`() {
        assertEquals(5, Constants.moduloLowerAndUpperBound(10, 8, 3))
    }
}
