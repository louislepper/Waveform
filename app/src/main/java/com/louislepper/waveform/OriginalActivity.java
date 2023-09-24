/*
 * Copyright 2015 Louis Lepper.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.louislepper.waveform;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ToggleButton;

import com.levien.synthesizer.android.widgets.keyboard.KeyboardView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class OriginalActivity extends CameraActivity {

    private static final String LINE_FEEDBACK = "lineFeedback";
    private static final String SMOOTHING = "smoothing";

    private static final String OCTAVE = "octave";
    private static final int DEFAULT_OCTAVE = 2;

    private static AudioThread audioThread;
    private View settingsView;
    private boolean smoothing = true;
    private boolean lineFeedback = true;
    private KeyboardView keyboardView;

    private SharedPreferences app_preferences;
    private SharedPreferences.Editor editor;
    private String currentScreen;

    private NumberPicker numberPicker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int streamMaxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // Always start quiet, so as not to frighten people.
        int desiredInitialVolume = streamMaxVolume / 10;
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, desiredInitialVolume, AudioManager.FLAG_SHOW_UI);

        app_preferences = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE);
        editor = app_preferences.edit();
        settingsView = findViewById(R.id.settings_content);
        keyboardView = (KeyboardView) findViewById(R.id.keyboard_view);
        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(8);
        numberPicker.setMinValue(0);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        editor.putInt(OCTAVE, numberPicker.getValue());
        editor.apply();
        stopAudio();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        smoothing = app_preferences.getBoolean(SMOOTHING, true);
        updateSmoothingButton();
        lineFeedback = app_preferences.getBoolean(LINE_FEEDBACK, true);
        updateLineFeedbackButton();
        updateOctaveSelector();
        switch (app_preferences.getString(SCREEN, NORMAL)) {
            case NORMAL:
                displayMainView(null);
                break;
            case SETTINGS:
                displaySettings(null);
                break;
            case KEYBOARD:
                displayKeyboard(null);
                break;
            default:
                displayMainView(null);
        }
    }

    private void updateOctaveSelector() {
        numberPicker.setValue(app_preferences.getInt(OCTAVE, DEFAULT_OCTAVE));
    }

    private void updateSmoothingButton() {
        final ToggleButton smoothingButton = (ToggleButton) findViewById(R.id.toggleSmoothingButton);
        if (smoothing) {
            smoothingButton.setChecked(true);
        } else {
            smoothingButton.setChecked(false);
        }
    }

    private void updateLineFeedbackButton() {
        final ToggleButton smoothingButton = (ToggleButton) findViewById(R.id.toggleLineButton);
        if (lineFeedback) {
            smoothingButton.setChecked(true);
        } else {
            smoothingButton.setChecked(false);
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

//        Imgproc.cvtColor(currentMat, currentMat, Imgproc.COLOR_RGBA2GRAY);
//        Imgproc.GaussianBlur(currentMat, currentMat, new Size(5, 5), 2, 2);
//        Imgproc.threshold(currentMat, currentMat, LIGHT_THRESH, LIGHT_THRESH, Imgproc.THRESH_TRUNC);
//        Imgproc.GaussianBlur(currentMat, currentMat, new Size(5, 5), 2, 2);
//        Imgproc.GaussianBlur(currentMat, currentMat, new Size(9, 9), 0, 0);
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
//        Imgproc.dilate(currentMat, currentMat, kernel);
//
//        //Canny edge detection
//        Imgproc.Canny(currentMat, currentMat, CANNY_LOW, CANNY_HIGH);
//
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
//            keyboardView.setMidiListener(audioThread);
            //This should maybe get its value from preferences. Not sure if number picker will have been set in time.
//            audioThread.setOctave(numberPicker.getValue());
            if (currentScreen.equals(KEYBOARD)) {
                audioThread.keyboardOn();
            } else {
                audioThread.keyboardOff();
            }
            audioThread.start();
        }

        //Send array here.
        audioThread.setWaveform(soundData, startAndEnd);

        if (lineFeedback) {
            Imgproc.cvtColor(currentMat, currentMat, Imgproc.COLOR_GRAY2RGBA);

            soundArrayToImage(soundData, currentMat);
        }

        return currentMat;
    }

    private Mat soundArrayToImage(short[] array, Mat image) {
        final double[] red = new double[] {255.0,0.0,0.0,0.0};
        for(int x = 0; x < image.cols(); x++) {
            if(!(array[x] == -1)){
                image.put(array[x],x,red);
            }
        }
        return image;
    }

    private void imageArrayToSoundArray(ArrayMat mat, short[] soundData) {

        //Find a white pixel in the first column of the image.
        //Once a white pixel is found, start searching the next column near to where the previous pixel was found.
        int previousWhitePoint = 0;
        for(int x = 0; x < mat.cols(); x++) {
            short newPoint = smartFindWhitePointInColumn(mat, x, previousWhitePoint);
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

        int highIndex = startingPoint;
        int lowIndex = startingPoint;

        while(highIndex < image.rows && lowIndex >= 0)  {
            if(image.get(highIndex, column) < 0) {
                return (short) highIndex;
            }
            highIndex++;
            if(image.get(lowIndex, column) < 0) {
                return (short) lowIndex;
            }
            lowIndex--;
        }

        while(highIndex < image.rows) {
            if(image.get(highIndex, column) < 0) {
                return (short) highIndex;
            }
            highIndex++;
        }

        while(lowIndex >= 0)  {
            if(image.get(lowIndex, column) < 0) {
                return (short) lowIndex;
            }
            lowIndex--;
        }
        return -1;
    }

    private final String SCREEN = "screen";
    private final String SETTINGS = "settings";
    private final String NORMAL = "normal";
    private final String KEYBOARD = "keyboard";

    public void displaySettings(View view) {
        allViewsOff();
        settingsView.setVisibility(View.VISIBLE);
        currentScreen = SETTINGS;
        editor.putString(SCREEN, SETTINGS);
    }

    public void displayMainView(View view) {
        allViewsOff();
        currentScreen = NORMAL;
        editor.putString(SCREEN, NORMAL);
    }

    public void displayKeyboard(View view) {
        allViewsOff();
        keyboardView.setVisibility(View.VISIBLE);
        if(audioThread != null) {
            audioThread.setOctave(numberPicker.getValue());
            audioThread.keyboardOn();
        }
        currentScreen = KEYBOARD;
        editor.putString(SCREEN, KEYBOARD);
    }

    private void allViewsOff(){
        settingsView.setVisibility(View.GONE);
        keyboardView.setVisibility(View.GONE);
        if(audioThread != null) {
            audioThread.keyboardOff();
        }
    }

    public void quit(View view) {
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        stopAudio();
        this.finishAffinity();
    }

    private void stopAudio() {
        if (audioThread != null) {
            audioThread.stopAudio();
            try {
                audioThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(TAG,"Audio track potentially wasn't freed!");
            }
        }
    }

    public void toggleSmoothing(View view) {
        smoothing = !smoothing;
        editor.putBoolean(SMOOTHING, smoothing);
    }

    public void toggleLineFeedback(View view) {
        lineFeedback = !lineFeedback;
        editor.putBoolean(LINE_FEEDBACK, lineFeedback);
    }

}
