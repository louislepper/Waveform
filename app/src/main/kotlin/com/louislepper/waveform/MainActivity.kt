package com.louislepper.waveform

import android.Manifest
import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.NumberPicker
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.app.ActivityCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.levien.synthesizer.android.widgets.keyboard.KeyboardView
import com.louislepper.waveform.ImageSoundManipulationUtils.soundArrayToImage
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class MainActivity : Activity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private var mOpenCvCameraView: CameraBridgeViewBase? = null

    private var smoothing = true
    private var lineFeedback = true
    private var paused = false

    // SCreens:
    private val SCREEN = "screen"
    private val SETTINGS = "settings"
    private val NORMAL = "normal"
    private val KEYBOARD = "keyboard"

    private var currentScreen = NORMAL
    private var numberPicker: NumberPicker? = null

    private var keyboardView: KeyboardView? = null

    private var app_preferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var settingsView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        // TODO: Add this to camera activity
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Permissions for Android 6+
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )

        setContentView(R.layout.activity_fullscreen)

        actionBar?.hide()

        mOpenCvCameraView = findViewById<CameraBridgeViewBase>(R.id.camera_content)

        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE

        mOpenCvCameraView!!.setCvCameraViewListener(this)

        val audio = getSystemService(AUDIO_SERVICE) as AudioManager
        val streamMaxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        // Always start quiet, so as not to frighten people.
        // Always start quiet, so as not to frighten people.
        val desiredInitialVolume = streamMaxVolume / 10
        audio.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            desiredInitialVolume,
            AudioManager.FLAG_SHOW_UI
        )

        app_preferences = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE)
        editor = app_preferences?.edit()
        settingsView = findViewById<View>(R.id.settings_content)
        keyboardView = findViewById<View>(R.id.keyboard_view) as KeyboardView
        numberPicker = findViewById<View>(R.id.numberPicker) as NumberPicker
        numberPicker?.maxValue = 8
        numberPicker?.minValue = 0

        keepControlsClearOfTheSystemBars()
    }

    /**
     * From targetSdk 36 an app is always drawn edge to edge and cannot opt out, so the window
     * extends underneath the status and navigation bars. The camera preview is meant to fill the
     * screen, but anything the user has to touch is not: without this the settings toggles sit
     * half under the status bar, where the system swallows taps aimed at their centre.
     */
    private fun keepControlsClearOfTheSystemBars() {
        val settings = settingsView ?: return
        ViewCompat.setOnApplyWindowInsetsListener(settings) { view, windowInsets ->
            val obstructions = windowInsets.obstructions()
            view.setPadding(
                obstructions.left,
                obstructions.top,
                obstructions.right,
                obstructions.bottom
            )
            windowInsets
        }

        val controls = findViewById<View>(R.id.fullscreen_content_controls)
        ViewCompat.setOnApplyWindowInsetsListener(controls) { view, windowInsets ->
            val obstructions = windowInsets.obstructions()
            // The bar is already pinned to the bottom of the window, so only the edges it
            // actually touches need insetting; padding it away from the status bar would just
            // make it needlessly tall.
            view.setPadding(
                obstructions.left,
                view.paddingTop,
                obstructions.right,
                obstructions.bottom
            )
            windowInsets
        }
    }

    private fun WindowInsetsCompat.obstructions(): Insets =
        getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mOpenCvCameraView!!.setCameraPermissionGranted()
                } else {
                    val message = "Camera permission was not granted"
                    Log.e(TAG, message)
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }

            else -> {
                Log.e(TAG, "Unexpected permission request")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    override fun onResume() {
        super.onResume()
        startOpenCvAndCamera()

        smoothing = app_preferences!!.getBoolean(SMOOTHING, true)
        updateSmoothingButton()
        lineFeedback = app_preferences!!.getBoolean(LINE_FEEDBACK, true)
        updateLineFeedbackButton()
        updateOctaveSelector()
        when (app_preferences!!.getString(SCREEN, NORMAL)) {
            NORMAL -> displayMainView(null)
            SETTINGS -> displaySettings(null)
            KEYBOARD -> displayKeyboard(null)
            else -> displayMainView(null)
        }
    }

    /**
     * OpenCV 4.x is bundled inside the application package, so it is initialised straight from
     * there. The asynchronous OpenCV Manager path (BaseLoaderCallback/initAsync) was removed in
     * OpenCV 4.14. The ordering still matters: the app's own native library links against OpenCV
     * and must only be loaded once OpenCV itself is up.
     */
    private fun startOpenCvAndCamera() {
        if (!OpenCVLoader.initLocal()) {
            val message = "OpenCV failed to initialise"
            Log.e(TAG, message)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            return
        }
        Log.i(TAG, "OpenCV loaded successfully")

        // Load native library after(!) OpenCV initialization
        System.loadLibrary("native-lib")

        mOpenCvCameraView?.enableView()
    }

    private fun updateOctaveSelector() {
        numberPicker!!.value =
            app_preferences!!.getInt(OCTAVE, DEFAULT_OCTAVE)
    }

    private fun updateSmoothingButton() {
        val smoothingButton = findViewById<View>(R.id.toggleSmoothingButton) as ToggleButton
        smoothingButton.isChecked = smoothing
    }

    private fun updateLineFeedbackButton() {
        val smoothingButton = findViewById<View>(R.id.toggleLineButton) as ToggleButton
        smoothingButton.isChecked = lineFeedback
    }

    fun toggleSmoothing(view: View?) {
        smoothing = !smoothing
        editor!!.putBoolean(SMOOTHING, smoothing)
    }

    fun toggleLineFeedback(view: View?) {
        lineFeedback = !lineFeedback
        editor!!.putBoolean(LINE_FEEDBACK, lineFeedback)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    private var currentPreviewImage: Mat? = null

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame): Mat {
        val curPrevImage = currentPreviewImage

        if (paused && curPrevImage != null) {
            return curPrevImage
        }
        // get current camera inputFrame as OpenCV Mat object
        val currentMat = inputFrame.gray()

        val processedFrame = WaveformProcessor.processFrame(currentMat, soundData, smoothing)
        soundData = processedFrame.soundData
        val startAndEnd = processedFrame.startAndEnd

        if (audioThread == null || (audioThread?.isAlive != true)) {
            audioThread = AudioThread()
            keyboardView?.setMidiListener(audioThread)
            //This should maybe get its value from preferences. Not sure if number picker will have been set in time.
            numberPicker?.value?.let { audioThread?.setOctave(it) }
            if (currentScreen == KEYBOARD) {
                audioThread?.keyboardOn()
            } else {
                audioThread?.keyboardOff()
            }
            audioThread?.start()
        }

        //Send array here.
        audioThread?.setWaveform(soundData, startAndEnd)

        if (lineFeedback) {
            Imgproc.cvtColor(currentMat, currentMat, Imgproc.COLOR_GRAY2RGBA)
            soundArrayToImage(soundData, currentMat)
        }

        currentPreviewImage = currentMat

        // return processed frame for live preview
        return currentMat
    }

    var soundData: ShortArray = shortArrayOf()

    fun displaySettings(view: View?) {
        allViewsOff()
        settingsView!!.visibility = View.VISIBLE
        currentScreen = SETTINGS
        editor!!.putString(SCREEN, SETTINGS)
    }

    fun displayMainView(view: View?) {
        allViewsOff()
        currentScreen = NORMAL
        editor!!.putString(SCREEN, NORMAL)
    }

    fun displayKeyboard(view: View?) {
        allViewsOff()
        keyboardView!!.visibility = View.VISIBLE
        if (audioThread != null) {
            audioThread?.setOctave(numberPicker!!.value)
            audioThread?.keyboardOn()
        }
        currentScreen = KEYBOARD
        editor!!.putString(SCREEN, KEYBOARD)
    }

    private fun allViewsOff() {
        settingsView!!.visibility = View.GONE
        keyboardView!!.visibility = View.GONE
        if (audioThread != null) {
            audioThread?.keyboardOff()
        }
    }

    fun quit(view: View?) {
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView!!.disableView()
        }
        stopAudio()
        finishAffinity()
    }

    fun pause(view: View?) {
        paused = !paused
    }

    private fun stopAudio() {
        if (audioThread != null) {
            audioThread?.stopAudio()
            try {
                audioThread?.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                Log.d(TAG, "Audio track potentially wasn't freed!")
            }
        }
    }

    companion object {
        private var audioThread: AudioThread? = null
        private const val TAG = "FullscreenActivity"
        private const val CAMERA_PERMISSION_REQUEST = 1

        private const val LINE_FEEDBACK = "lineFeedback"
        private const val SMOOTHING = "smoothing"

        private const val OCTAVE = "octave"
        private const val DEFAULT_OCTAVE = 2
    }
}
