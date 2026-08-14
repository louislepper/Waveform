package com.louislepper.waveform

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/**
 * End to end tests for the image -> waveform pipeline, running the real native OpenCV code via
 * [WaveformProcessor.processFrame] (exactly what [MainActivity.onCameraFrame] runs).
 */
@RunWith(AndroidJUnit4::class)
class WaveformPipelineInstrumentedTest {

    companion object {
        /**
         * Canny reports edges at both borders of the drawn stroke, and the preceding blurs and the
         * 5x5 dilation widen the stroke further. The column scan always finds the upper border
         * first, so the detected row sits a few rows *above* the centre of the drawn curve
         * (measured: 4 to 16 rows for a 3px stroke, more on the steep parts of the sine). This
         * tolerance covers stroke thickness + blur radius + dilation with a little headroom.
         */
        private const val ROW_TOLERANCE = 20

        @BeforeClass
        @JvmStatic
        fun loadLibraries() {
            OpenCvTestSupport.loadNativeLibraries()
        }
    }

    @Test
    fun sineImageProducesAGapFreeWaveformThatTracksTheDrawnCurve() {
        val mat = OpenCvTestSupport.sineFrame()
        val result = WaveformProcessor.processFrame(mat, ShortArray(0), smoothing = false)

        val samples = result.soundData
        val startAndEnd = result.startAndEnd
        assertEquals(OpenCvTestSupport.COLS, samples.size)

        logDeviations("sine", samples, startAndEnd)

        // The curve spans the full width, so the valid span should cover almost all of it.
        assertTrue(
            "start should be near the left edge but was ${startAndEnd.start}",
            startAndEnd.start <= 2
        )
        assertTrue(
            "end should be near the right edge but was ${startAndEnd.end}",
            startAndEnd.end >= OpenCvTestSupport.COLS - 3
        )

        // Interpolation should have filled every hole inside the valid span.
        for (x in startAndEnd.start..startAndEnd.end) {
            assertTrue("sample $x is still invalid", samples[x].toInt() != -1)
        }

        // And the waveform should follow the drawn sine.
        for (x in startAndEnd.start..startAndEnd.end) {
            val deviation = abs(samples[x] - OpenCvTestSupport.sineRow(x))
            assertTrue(
                "column $x: detected row ${samples[x]}, drawn row ${OpenCvTestSupport.sineRow(x)}",
                deviation <= ROW_TOLERANCE
            )
        }
    }

    @Test
    fun sineImageProducesTheSameShapeWithSmoothingEnabled() {
        val mat = OpenCvTestSupport.sineFrame()
        val result = WaveformProcessor.processFrame(mat, ShortArray(0), smoothing = true)

        val samples = result.soundData
        val startAndEnd = result.startAndEnd

        for (x in startAndEnd.start..startAndEnd.end) {
            assertTrue("sample $x is still invalid", samples[x].toInt() != -1)
        }

        // Crossfading only reshapes the ends of the wave, so the middle must still track the sine.
        val quarter = OpenCvTestSupport.COLS / 4
        for (x in quarter until OpenCvTestSupport.COLS - quarter) {
            val deviation = abs(samples[x] - OpenCvTestSupport.sineRow(x))
            assertTrue(
                "column $x: detected row ${samples[x]}, drawn row ${OpenCvTestSupport.sineRow(x)}",
                deviation <= ROW_TOLERANCE
            )
        }

        // The crossfade joins the two ends together, so they should be much closer than the raw
        // wave's ends, which are a full sine period apart in gradient.
        assertTrue(
            "crossfaded ends should meet: ${samples[startAndEnd.start]} vs ${samples[startAndEnd.end]}",
            abs(samples[startAndEnd.start] - samples[startAndEnd.end]) <= ROW_TOLERANCE
        )
    }

    @Test
    fun horizontalLineProducesANearConstantWaveform() {
        val drawnRow = 200
        val mat = OpenCvTestSupport.horizontalLineFrame(drawnRow)
        val result = WaveformProcessor.processFrame(mat, ShortArray(0), smoothing = false)

        val samples = result.soundData
        val startAndEnd = result.startAndEnd
        val span = samples.copyOfRange(startAndEnd.start, startAndEnd.end + 1)

        val min = span.minOrNull()!!.toInt()
        val max = span.maxOrNull()!!.toInt()
        Log.i(OpenCvTestSupport.TAG, "horizontal: drawn=$drawnRow min=$min max=$max span=${span.size}")

        assertTrue("valid span should cover the width but was ${span.size}", span.size >= OpenCvTestSupport.COLS - 4)
        assertTrue("waveform should be flat but ranged $min..$max", max - min <= 2)
        assertTrue("waveform sat at $min..$max, expected near $drawnRow", abs(min - drawnRow) <= ROW_TOLERANCE)
        assertTrue("waveform sat at $min..$max, expected near $drawnRow", abs(max - drawnRow) <= ROW_TOLERANCE)
    }

    @Test
    fun diagonalLineProducesAMonotonicWaveform() {
        val startRow = 400
        val endRow = 80
        val mat = OpenCvTestSupport.diagonalLineFrame(startRow, endRow)
        val result = WaveformProcessor.processFrame(mat, ShortArray(0), smoothing = false)

        val samples = result.soundData
        val startAndEnd = result.startAndEnd

        var maxRise = 0
        for (x in startAndEnd.start until startAndEnd.end) {
            maxRise = maxOf(maxRise, samples[x + 1] - samples[x])
        }
        val first = samples[startAndEnd.start].toInt()
        val last = samples[startAndEnd.end].toInt()
        Log.i(OpenCvTestSupport.TAG, "diagonal: first=$first last=$last maxRise=$maxRise")

        assertTrue("waveform should never step back upwards, but rose by $maxRise", maxRise <= 0)
        assertTrue("waveform should descend from $startRow towards $endRow, got $first -> $last", first > last)
        assertTrue("waveform start $first should be near $startRow", abs(first - startRow) <= ROW_TOLERANCE)
        assertTrue("waveform end $last should be near $endRow", abs(last - endRow) <= ROW_TOLERANCE)
    }

    @Test
    fun blankImageProducesNoEdgesAtAll() {
        val mat = OpenCvTestSupport.blankFrame()
        val result = WaveformProcessor.processFrame(mat, ShortArray(0), smoothing = true)

        val samples = result.soundData
        val startAndEnd = result.startAndEnd

        for (x in samples.indices) {
            assertEquals("column $x should have no edge", -1, samples[x].toInt())
        }

        // Current behaviour of SampleInterpolator when it never finds a valid sample: it leaves
        // both indices at 0, so the "valid span" is reported as a single sample at index 0.
        // SampleCrossfader then returns early because the length is less than 2.
        assertEquals(0, startAndEnd.start)
        assertEquals(0, startAndEnd.end)
        assertEquals(1, startAndEnd.length)
    }

    @Test
    fun waveformIsConsumableByTheAudioSide() {
        val mat = OpenCvTestSupport.sineFrame()
        val result = WaveformProcessor.processFrame(mat, ShortArray(0), smoothing = true)
        val startAndEnd = result.startAndEnd

        // This mirrors AudioThread.setWaveform, without touching AudioTrack.
        val wave = result.soundData.copyOfRange(startAndEnd.start, startAndEnd.end + 1)
        val normalised = WaveStretcher.normalize(wave)
        val wrapper = WaveformWrapper(normalised)

        assertEquals(startAndEnd.end + 1 - startAndEnd.start, wrapper.size())

        val sampled = ShortArray(1000) { wrapper.get(it * 0.937) }
        val min = sampled.minOrNull()!!.toInt()
        val max = sampled.maxOrNull()!!.toInt()
        Log.i(OpenCvTestSupport.TAG, "audio side: min=$min max=$max")

        // normalize scales the wave out to (nearly) the full short range, so playback should be
        // loud and clearly non-degenerate.
        assertTrue("normalised wave should swing negative, min was $min", min < -20000)
        assertTrue("normalised wave should swing positive, max was $max", max > 20000)

        // AudioThread reads the wrapper with an ever increasing index, so it must wrap cleanly.
        assertEquals(wrapper.get(5.0), wrapper.get(5.0 + wrapper.size()))
    }

    private fun logDeviations(
        name: String,
        samples: ShortArray,
        startAndEnd: SampleInterpolator.StartAndEnd
    ) {
        var maxDeviation = 0
        var minSigned = Int.MAX_VALUE
        var maxSigned = Int.MIN_VALUE
        for (x in startAndEnd.start..startAndEnd.end) {
            val signed = samples[x] - OpenCvTestSupport.sineRow(x)
            maxDeviation = maxOf(maxDeviation, abs(signed))
            minSigned = minOf(minSigned, signed)
            maxSigned = maxOf(maxSigned, signed)
        }
        Log.i(
            OpenCvTestSupport.TAG,
            "$name: start=${startAndEnd.start} end=${startAndEnd.end} " +
                "maxDeviation=$maxDeviation signedRange=$minSigned..$maxSigned"
        )
    }
}
