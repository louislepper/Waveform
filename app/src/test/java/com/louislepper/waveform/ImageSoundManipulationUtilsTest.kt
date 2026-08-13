package com.louislepper.waveform

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Locks in the current behavior of [ImageSoundManipulationUtils.imageArrayToSoundArray] and
 * [ImageSoundManipulationUtils.smartFindWhitePointInColumn], using the [ArrayMat] primary
 * constructor so no native OpenCV [org.opencv.core.Mat] is required.
 *
 * A "white point" is a pixel byte value < 0, i.e. an unsigned pixel value > 127 stored in a
 * signed [Byte] (255 -> -1 as a byte), matching the OpenCV Canny edge detector's output where
 * edges are 255 and background is 0. [WHITE] below models that explicitly.
 */
class ImageSoundManipulationUtilsTest {

    private companion object {
        const val WHITE: Byte = 255.toByte() // Canny edge pixel, stored as -1 in a signed byte.
        const val BLACK: Byte = 0
        const val NO_WHITE_POINT: Short = -1
    }

    /** Builds a single-channel ArrayMat where every pixel is [BLACK] except the given points. */
    private fun matWithWhitePoints(rows: Int, cols: Int, whitePoints: List<Pair<Int, Int>>): ArrayMat {
        val bytes = ByteArray(rows * cols) { BLACK }
        for ((row, col) in whitePoints) {
            bytes[cols * row + col] = WHITE
        }
        return ArrayMat(rows, cols, bytes)
    }

    // ---- smartFindWhitePointInColumn -----------------------------------------------------

    @Test
    fun `white point exactly at the starting point is returned immediately`() {
        val mat = matWithWhitePoints(rows = 10, cols = 1, whitePoints = listOf(5 to 0))

        val result = ImageSoundManipulationUtils.smartFindWhitePointInColumn(mat, 0, 5)

        assertEquals(5, result.toInt())
    }

    @Test
    fun `search finds a white point above the starting point`() {
        val mat = matWithWhitePoints(rows = 10, cols = 1, whitePoints = listOf(7 to 0))

        val result = ImageSoundManipulationUtils.smartFindWhitePointInColumn(mat, 0, 5)

        assertEquals(7, result.toInt())
    }

    @Test
    fun `search finds a white point below the starting point`() {
        val mat = matWithWhitePoints(rows = 10, cols = 1, whitePoints = listOf(3 to 0))

        val result = ImageSoundManipulationUtils.smartFindWhitePointInColumn(mat, 0, 5)

        assertEquals(3, result.toInt())
    }

    @Test
    fun `equidistant white points prefer the higher row because high is checked first`() {
        // Rows 3 and 7 are both 2 away from starting point 5. The alternating scan checks the
        // "high" (increasing row) direction before the "low" direction on every iteration, so
        // ties resolve to the higher row index.
        val mat = matWithWhitePoints(rows = 10, cols = 1, whitePoints = listOf(3 to 0, 7 to 0))

        val result = ImageSoundManipulationUtils.smartFindWhitePointInColumn(mat, 0, 5)

        assertEquals(7, result.toInt())
    }

    @Test
    fun `column with no white point returns negative one`() {
        val mat = matWithWhitePoints(rows = 10, cols = 1, whitePoints = emptyList())

        val result = ImageSoundManipulationUtils.smartFindWhitePointInColumn(mat, 0, 5)

        assertEquals(NO_WHITE_POINT, result)
    }

    @Test
    fun `first column search starts from row zero`() {
        val mat = matWithWhitePoints(rows = 10, cols = 1, whitePoints = listOf(0 to 0))

        // imageArrayToSoundArray always seeds previousWhitePoint at 0 for column 0.
        val result = ImageSoundManipulationUtils.smartFindWhitePointInColumn(mat, 0, 0)

        assertEquals(0, result.toInt())
    }

    @Test
    fun `edge at the top row is found once the high side runs off the top of the search`() {
        // startingPoint = 3 in a 4-row image: the "high" direction is immediately out of rows,
        // so the match at row 0 is only found by the low-side cleanup loop.
        val mat = matWithWhitePoints(rows = 4, cols = 1, whitePoints = listOf(0 to 0))

        val result = ImageSoundManipulationUtils.smartFindWhitePointInColumn(mat, 0, 3)

        assertEquals(0, result.toInt())
    }

    @Test
    fun `edge at the bottom row is found once the low side runs off the bottom of the search`() {
        // startingPoint = 0 in a 4-row image: the "low" direction is immediately out of rows,
        // so the match at row 3 is only found by the high-side cleanup loop.
        val mat = matWithWhitePoints(rows = 4, cols = 1, whitePoints = listOf(3 to 0))

        val result = ImageSoundManipulationUtils.smartFindWhitePointInColumn(mat, 0, 0)

        assertEquals(3, result.toInt())
    }

    @Test
    fun `no white point anywhere in the column exhausts every search loop`() {
        val mat = matWithWhitePoints(rows = 5, cols = 1, whitePoints = emptyList())

        val result = ImageSoundManipulationUtils.smartFindWhitePointInColumn(mat, 0, 2)

        assertEquals(NO_WHITE_POINT, result)
    }

    // ---- imageArrayToSoundArray -----------------------------------------------------------

    @Test
    fun `a column with no white point yields negative one but does not disturb the running search point`() {
        // Column 0 finds row 4, column 1 has no white point at all (-> -1), column 2 must still
        // find row 5 by resuming the search from column 0's row (4), proving previousWhitePoint
        // is left untouched by column 1's failed search rather than reset.
        val mat = matWithWhitePoints(rows = 10, cols = 3, whitePoints = listOf(4 to 0, 5 to 2))
        val soundData = ShortArray(3)

        ImageSoundManipulationUtils.imageArrayToSoundArray(mat, soundData)

        assertArrayEquals(shortArrayOf(4, -1, 5), soundData)
    }

    @Test
    fun `soundData tracks a drawn line across columns`() {
        // A line that moves down by exactly one row per column, close enough that each column's
        // search finds it on the first alternating iteration.
        val whitePoints = listOf(2 to 0, 3 to 1, 4 to 2, 5 to 3, 6 to 4)
        val mat = matWithWhitePoints(rows = 10, cols = 5, whitePoints = whitePoints)
        val soundData = ShortArray(5)

        ImageSoundManipulationUtils.imageArrayToSoundArray(mat, soundData)

        assertArrayEquals(shortArrayOf(2, 3, 4, 5, 6), soundData)
    }

    @Test
    fun `soundData longer than mat cols has its tail filled with negative one`() {
        val mat = matWithWhitePoints(rows = 10, cols = 3, whitePoints = listOf(4 to 0, 5 to 1, 6 to 2))
        // Pre-fill with a sentinel to prove the tail is actively overwritten, not just left as
        // whatever ShortArray's default (0) would have been.
        val soundData = ShortArray(6) { 99 }

        ImageSoundManipulationUtils.imageArrayToSoundArray(mat, soundData)

        assertArrayEquals(shortArrayOf(4, 5, 6, -1, -1, -1), soundData)
    }
}
