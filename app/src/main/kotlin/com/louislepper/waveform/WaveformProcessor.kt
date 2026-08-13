package com.louislepper.waveform

import com.louislepper.waveform.ImageSoundManipulationUtils.imageArrayToSoundArray
import org.opencv.core.Mat

/**
 * The pure "camera frame -> sound array" pipeline.
 *
 * This lives outside of [MainActivity] so that it can be exercised without an Activity (and so the
 * JNI entry point below has a stable home). [MainActivity.onCameraFrame] delegates to
 * [processFrame], so tests calling [processFrame] run exactly the production pipeline.
 */
object WaveformProcessor {

    /**
     * The result of running one frame through the pipeline.
     *
     * [soundData] may be the same array instance that was passed in, since both
     * [imageArrayToSoundArray] and [SampleCrossfader.crossfade] work in place.
     */
    class ProcessedFrame(
        val soundData: ShortArray,
        val startAndEnd: SampleInterpolator.StartAndEnd
    )

    /**
     * Thresholds/edge-detects [mat] in place (native OpenCV), then converts the resulting edge
     * image into a waveform.
     *
     * @param mat a single channel (grayscale) image. It is modified in place.
     * @param soundData a scratch array to write into. A new array is allocated if it is the wrong
     *  size for [mat].
     * @param smoothing whether to crossfade the start and end of the waveform together.
     */
    fun processFrame(mat: Mat, soundData: ShortArray, smoothing: Boolean): ProcessedFrame {
        // Native call to process the current camera frame.
        adaptiveThresholdFromJNI(mat.nativeObjAddr)

        val samples = if (soundData.size != mat.cols()) ShortArray(mat.cols()) else soundData

        imageArrayToSoundArray(ArrayMat(mat), samples)

        val startAndEnd = SampleInterpolator.interpolateInvalidSamples(samples)

        val smoothed = if (smoothing) {
            SampleCrossfader.crossfade(samples, startAndEnd.start, startAndEnd.length)
        } else {
            samples
        }

        return ProcessedFrame(smoothed, startAndEnd)
    }

    external fun adaptiveThresholdFromJNI(matAddr: Long)
}
