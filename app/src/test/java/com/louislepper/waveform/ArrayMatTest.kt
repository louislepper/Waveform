package com.louislepper.waveform

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Locks in the current behavior of [ArrayMat]. Only the primary constructor is exercised here
 * (rows, cols, ByteArray) - the secondary constructor requires a native OpenCV [org.opencv.core.Mat]
 * and is out of scope for a JVM unit test.
 */
class ArrayMatTest {

    private companion object {
        const val ROWS = 3
        const val COLS = 4
    }

    private fun rowMajorBytes(rows: Int, cols: Int): ByteArray =
        ByteArray(rows * cols) { index -> index.toByte() }

    @Test
    fun `get reads row-major indexed bytes`() {
        val mat = ArrayMat(ROWS, COLS, rowMajorBytes(ROWS, COLS))

        // index = cols * row + col
        assertEquals(0.toByte(), mat.get(0, 0))
        assertEquals(5.toByte(), mat.get(1, 1))
        assertEquals(11.toByte(), mat.get(2, 3)) // last element: 4*2 + 3 = 11
    }

    @Test
    fun `get reads the first and last elements of a single row`() {
        val mat = ArrayMat(1, COLS, rowMajorBytes(1, COLS))

        assertEquals(0.toByte(), mat.get(0, 0))
        assertEquals(3.toByte(), mat.get(0, 3))
    }

    @Test
    fun `equals is content based for rows and cols but reference based for the array`() {
        // Kotlin data classes generate equals()/hashCode() using each property's own equals().
        // ByteArray's equals() is reference identity, not content equality, so two ArrayMat
        // instances with identical row/col/content are NOT equal unless they share the same
        // array instance. This is a common Kotlin data-class-with-array pitfall worth flagging.
        val first = ArrayMat(ROWS, COLS, rowMajorBytes(ROWS, COLS))
        val second = ArrayMat(ROWS, COLS, rowMajorBytes(ROWS, COLS))

        assertFalse(first == second)
    }

    @Test
    fun `equals is true when the same array instance is shared`() {
        val sharedArray = rowMajorBytes(ROWS, COLS)
        val first = ArrayMat(ROWS, COLS, sharedArray)
        val second = ArrayMat(ROWS, COLS, sharedArray)

        assertEquals(first, second)
    }
}
