package com.louislepper.waveform

import org.opencv.core.Mat

data class ArrayMat(val rows: Int, val cols: Int, val array: ByteArray) {
    //Not sure if this will work for colour images.
    constructor(mat: Mat): this(
        mat.rows(),
        mat.cols(),
        ByteArray((mat.total() * mat.channels()).toInt())
    ) {
        mat.get(0, 0, array)
    }

    fun get(row: Int, col: Int): Byte {
        return array[cols * row + col]
    }
}