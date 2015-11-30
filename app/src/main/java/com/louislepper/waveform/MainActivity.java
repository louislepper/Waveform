package com.louislepper.waveform;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;



public class MainActivity extends CameraActivity{

    private static AudioThread audioThread;
    private View settingsView;
    private boolean smoothing = true;
    private boolean lineFeedback = true;
    private View keyboardView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences app_preferences = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE);
        settingsView = findViewById(R.id.settings_content);
        keyboardView = findViewById(R.id.keyboard_view);

//        final SharedPreferences.Editor editor = app_preferences.edit();
//        editor.putBoolean("SMOOTH_EDGES", false);
//        editor.apply();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        audioThread.stopAudio();
        try {
            audioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG, "Audio track potentially wasn't freed!");
        }
    }

    private final int CANNY_LOW = 10;
    private final int CANNY_HIGH = 30;
    private final int LIGHT_THRESH = 160;

    //Consider moving the initialisation outside of the loop.
    short[] soundData;

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat currentMat = inputFrame.rgba();

        Imgproc.cvtColor(currentMat, currentMat, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(currentMat, currentMat, new Size(5, 5), 2, 2);
        Imgproc.threshold(currentMat, currentMat, LIGHT_THRESH, LIGHT_THRESH, Imgproc.THRESH_TRUNC);
        Imgproc.GaussianBlur(currentMat, currentMat, new Size(5, 5), 2, 2);
        Imgproc.GaussianBlur(currentMat, currentMat, new Size(9, 9), 0, 0);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.dilate(currentMat, currentMat, kernel);

        Imgproc.Canny(currentMat, currentMat, CANNY_LOW, CANNY_HIGH);

        if(soundData == null || soundData.length != currentMat.cols()) {
            soundData = new short[currentMat.cols()];
        }

        imageArrayToSoundArray(new ArrayMat(currentMat), soundData);


        SampleInterpolator.StartAndEnd startAndEnd = SampleInterpolator.interpolateInvalidSamples(soundData);

        if(smoothing) {
            soundData = SampleCrossfader.crossfade(soundData, startAndEnd.getStart(), startAndEnd.getLength());
        }


        if(audioThread == null || !audioThread.isAlive()) {
            audioThread = new AudioThread();
            audioThread.start();
        }

        //Send array here.
        final double step = audioThread.setWaveform(soundData, startAndEnd);

        if (lineFeedback) {
            Imgproc.cvtColor(currentMat, currentMat, Imgproc.COLOR_GRAY2RGBA);

            soundArrayToImage(soundData, currentMat, (int) Math.round(step));
        }
        return currentMat;
    }

    private Mat soundArrayToImage(short[] array, Mat image, int intStep) {
        final double[] red = new double[] {255.0,0.0,0.0,0.0};
        final double[] green = new double[] {0.0,255.0,0.0,0.0};
        for(int x = 0; x < image.cols(); x++) {
            if(!(array[x] == -1)){
                image.put(array[x],x,red);
                if(intStep != 0 && x % intStep == 0) {
                    image.put(moreThanZero(array[x] - 1), x, green);
                    image.put(array[x], x, green);
                    image.put(lessThanValue(array[x] + 1, image.cols()), x, green);
                }
            }
        }
        return image;
    }

    private int moreThanZero(int value) {
        if(value < 0) return 0;
        return value;
    }

    private int lessThanValue(int value, int max) {
        if(value >= max) return max = 1;
        return value;
    }

    private void imageArrayToSoundArray(ArrayMat mat, short[] soundData) {

        //Find a white pixel in each column of the image.
        //Once a white pixel is found, start searching the next column near to where the previous pixel was found.
        int previousWhitePoint = 0;
        for(int x = 0; x < mat.cols(); x++) {
            short newPoint = smartFindWhitePointInColumn(mat, x, previousWhitePoint);
            //TODO: Add a check here to ensure that a int is large enough.
            soundData[x] = newPoint;
            if(newPoint != -1) {
                previousWhitePoint = newPoint;
            }
        }

        //This should never happen, but we found that occasionally the image matrix would change dimensions. Perhaps on rotate.
        if (soundData.length > mat.cols()) {
            for (int i = mat.cols(); i < soundData.length; i++) {
                soundData[i] = -1;
            }
        }
    }

    private short smartFindWhitePointInColumn(ArrayMat image, int column, int startingPoint) {

        int topBound = startingPoint;
        int lowerBound = startingPoint;

        while(topBound < image.rows && lowerBound >= 0)  {
            if(image.get(topBound, column) < 0) {
                return (short) topBound;
            }
            topBound++;
            if(image.get(lowerBound, column) < 0) {
                return (short) lowerBound;
            }
            lowerBound--;
        }

        while(topBound < image.rows) {
            if(image.get(topBound, column) < 0) {
                return (short) topBound;
            }
            topBound++;
        }

        while(lowerBound >= 0)  {
            if(image.get(lowerBound, column) < 0) {
                return (short) lowerBound;
            }
            lowerBound--;
        }
        return -1;
    }

    public void displaySettings(View view) {
        allViewsOff();
        settingsView.setVisibility(View.VISIBLE);
    }
    public void displayMainView(View view) {
        allViewsOff();
        mOpenCvCameraView.setVisibility(View.VISIBLE);
    }

    public void displayKeyboard(View view) {
        allViewsOff();
        mOpenCvCameraView.setVisibility(View.VISIBLE);
        keyboardView.setVisibility(View.VISIBLE);
    }

    private void allViewsOff(){
        mOpenCvCameraView.setVisibility(View.GONE);
        settingsView.setVisibility(View.GONE);
        keyboardView.setVisibility(View.GONE);
    }

    public void quit(View view) {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        audioThread.stopAudio();
        try {
            audioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG,"Audio track potentially wasn't freed!");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.finishAffinity();
        } else {
            this.finish();
        }
    }



    public void toggleSmoothing(View view) {
        smoothing = !smoothing;
    }

    public void toggleLineFeedback(View view) {
        lineFeedback = !lineFeedback;
    }

    private class ArrayMat {
        private final int rows, cols;
        private final byte[] array;

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
            //Is this the right type?
            Mat mat = new Mat(rows, cols, Imgproc.COLOR_RGBA2GRAY);
            mat.put(0,0,array);
            return mat;
        }

        public byte get(int row, int col) {
            return array[cols * row + col];
        }
    }
}
