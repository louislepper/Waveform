package com.louislepper.waveform

import org.opencv.core.Mat

object ImageSoundManipulationUtils {
    fun soundArrayToImage(array: ShortArray, image: Mat): Mat {
        val red = doubleArrayOf(255.0, 0.0, 0.0, 0.0)
        val imageCols = image.cols()
        for (x in 0 until imageCols) {
            if (array[x].toInt() != -1) {
                val row = array[x].toInt()

                image.put((row - 2).coerceAtLeast(0), x, *red)
                image.put((row - 1).coerceAtLeast(0), x, *red)
                image.put(row, x, *red)
                image.put((row + 1).coerceAtMost(imageCols), x, *red)
                image.put((row + 2).coerceAtMost(imageCols), x, *red)
            }
        }
        return image
    }

    fun imageArrayToSoundArray(mat: ArrayMat, soundData: ShortArray) {

        //Find a white pixel in the first column of the image.
        //Once a white pixel is found, start searching the next column near to where the previous pixel was found.
        var previousWhitePoint = 0
        for (x in 0 until mat.cols) {
            val newPoint = smartFindWhitePointInColumn(mat, x, previousWhitePoint)
            soundData[x] = newPoint
            if (newPoint.toInt() != -1) {
                previousWhitePoint = newPoint.toInt()
            }
        }

        //This should never happen, but we found that occasionally the image matrix would change dimensions. Perhaps on rotate.
        if (soundData.size > mat.cols) {
            for (i in mat.cols until soundData.size) {
                soundData[i] = -1
            }
        }
    }

    fun smartFindWhitePointInColumn(
        image: ArrayMat,
        column: Int,
        startingPoint: Int
    ): Short {
        var highIndex = startingPoint
        var lowIndex = startingPoint
        while (highIndex < image.rows && lowIndex >= 0) {
            if (image.get(highIndex, column) < 0) {
                return highIndex.toShort()
            }
            highIndex++
            if (image.get(lowIndex, column) < 0) {
                return lowIndex.toShort()
            }
            lowIndex--
        }
        while (highIndex < image.rows) {
            if (image.get(highIndex, column) < 0) {
                return highIndex.toShort()
            }
            highIndex++
        }
        while (lowIndex >= 0) {
            if (image.get(lowIndex, column) < 0) {
                return lowIndex.toShort()
            }
            lowIndex--
        }
        return -1
    }
}