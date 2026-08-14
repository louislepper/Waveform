package com.louislepper.waveform

import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Shared helpers for the instrumented pipeline tests: native library loading and synthetic
 * "camera frame" generation.
 */
object OpenCvTestSupport {

    const val TAG = "WaveformPipelineTest"

    const val ROWS = 480
    const val COLS = 640

    /** Background grey of the synthetic frames. Well under the 160 truncation threshold. */
    const val DARK = 20.0

    /** The drawn curve's brightness. */
    const val BRIGHT = 255.0

    const val LINE_THICKNESS = 3

    private val WHITE = Scalar(BRIGHT)

    @Volatile
    private var loaded = false

    /**
     * Initialises OpenCV and then the app's own native library, in the same order as
     * [MainActivity.onResume] does.
     */
    @Synchronized
    fun loadNativeLibraries() {
        if (loaded) return
        val openCvInitialised = OpenCVLoader.initLocal()
        Log.i(TAG, "OpenCVLoader.initLocal() returned $openCvInitialised")
        check(openCvInitialised) { "OpenCV failed to initialise on this device" }
        System.loadLibrary("native-lib")
        loaded = true
    }

    /** A uniformly dark, single channel frame. */
    fun blankFrame(rows: Int = ROWS, cols: Int = COLS): Mat =
        Mat(rows, cols, CvType.CV_8UC1, Scalar(DARK))

    /** The row a sine curve occupies at column [x]. */
    fun sineRow(x: Int, cols: Int = COLS, centre: Int = ROWS / 2, amplitude: Int = 120, cycles: Int = 2): Int =
        (centre + amplitude * sin(2.0 * PI * cycles * x.toDouble() / cols.toDouble())).roundToInt()

    /** A dark frame with a bright, full width, gap free sine curve drawn on it. */
    fun sineFrame(rows: Int = ROWS, cols: Int = COLS, amplitude: Int = 120, cycles: Int = 2): Mat {
        val mat = blankFrame(rows, cols)
        val centre = rows / 2
        for (x in 1 until cols) {
            drawSegment(
                mat,
                x - 1,
                sineRow(x - 1, cols, centre, amplitude, cycles),
                x,
                sineRow(x, cols, centre, amplitude, cycles)
            )
        }
        return mat
    }

    /** A dark frame with a bright horizontal line spanning the full width at [row]. */
    fun horizontalLineFrame(row: Int, rows: Int = ROWS, cols: Int = COLS): Mat {
        val mat = blankFrame(rows, cols)
        drawSegment(mat, 0, row, cols - 1, row)
        return mat
    }

    /** A dark frame with a bright straight line running from [startRow] to [endRow]. */
    fun diagonalLineFrame(startRow: Int, endRow: Int, rows: Int = ROWS, cols: Int = COLS): Mat {
        val mat = blankFrame(rows, cols)
        drawSegment(mat, 0, startRow, cols - 1, endRow)
        return mat
    }

    private fun drawSegment(mat: Mat, xStart: Int, rowStart: Int, xEnd: Int, rowEnd: Int) {
        Imgproc.line(
            mat,
            Point(xStart.toDouble(), rowStart.toDouble()),
            Point(xEnd.toDouble(), rowEnd.toDouble()),
            WHITE,
            LINE_THICKNESS
        )
    }
}
