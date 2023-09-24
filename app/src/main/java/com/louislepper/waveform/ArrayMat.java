package com.louislepper.waveform;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ArrayMat {
    public final int rows, cols;
    public final byte[] array;

    public ArrayMat(int rows, int cols, byte[] array) {
        this.rows = rows;
        this.cols = cols;
        this.array = array;
    }

    public ArrayMat(Mat mat) {
        //TODO: Not sure if this will work for colour images.
        int size = (int) (mat.total() * mat.channels());
        array = new byte[size];
        this.rows = mat.rows();
        this.cols = mat.cols();
        mat.get(0, 0, array);
    }

    public int cols() {
        return cols;
    }

    public int rows() {
        return rows;
    }

    public Mat toMat() {
        Mat mat = new Mat(rows, cols, Imgproc.COLOR_RGBA2GRAY);
        mat.put(0, 0, array);
        return mat;
    }

    public byte get(int row, int col) {
        return array[cols * row + col];
    }
}
