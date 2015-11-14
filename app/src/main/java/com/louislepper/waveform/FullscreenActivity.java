package com.louislepper.waveform;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private static final String TAG = "FullscreenActivity";

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;
    private CameraBridgeViewBase mOpenCvCameraView;

    private static AudioThread audioThread;

    static{ System.loadLibrary("opencv_java3"); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        mOpenCvCameraView = (CameraBridgeViewBase) mContentView;

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //This must be disabled before stopping audio. Otherwise it'll be started back up again on the next camera frame.
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        audioThread.stopAudio();
        try {
            audioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(TAG,"Audio track potentially wasn't freed!");
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

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

        Imgproc.cvtColor(currentMat, currentMat, Imgproc.COLOR_GRAY2RGBA);

        SampleInterpolator.StartAndEnd startAndEnd = SampleInterpolator.interpolateInvalidSamples(soundData);

        soundData = SampleCrossfader.crossfade(soundData, startAndEnd.getStart(), startAndEnd.getLength());

       // soundArrayToImage(soundData, empty);
//        int[] shouldBeIdenticalSoundArray = imageArrayToSoundArray(new ArrayMat(empty));

        if(audioThread == null || !audioThread.isAlive()) {
            audioThread = new AudioThread();
            audioThread.start();
        }

        //Send array here.
        final double step = audioThread.setWaveform(soundData, startAndEnd);

        soundArrayToImage(soundData, currentMat, (int) Math.round(step));

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
//            short newPoint = findWhitePointInColumn(mat, x, previousWhitePoint - 60, mat.rows());
//            if(newPoint == -1) {
//                newPoint = findWhitePointInColumn(mat, x, 0, previousWhitePoint - 60);
//            }

            short newPoint = smartFindWhitePointInColumn(mat, x, previousWhitePoint);
//            if(newPoint == -1) {
//                newPoint = smartFindWhitePointInColumn(mat, x, previousWhitePoint);
//            }

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


    private short findWhitePointInColumn(ArrayMat image, int column, int yMin, int yMax) {
        yMin = Math.max(yMin, 0);
        yMax = Math.min(image.rows(), yMax);

        for(int y = yMin; y < yMax; y++) {
            if(image.get(y, column) < 0){
                //TODO: Do some tests to make sure this is safe here.
                return (short) y;
            }
        }
        return -1;
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
