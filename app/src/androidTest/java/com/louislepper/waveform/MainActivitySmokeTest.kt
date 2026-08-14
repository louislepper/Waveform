package com.louislepper.waveform

import android.Manifest
import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.CoreMatchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Launch and navigation smoke tests for [MainActivity].
 *
 * These deliberately make no assertions about camera frames actually flowing: whether a device's
 * camera connects is environment dependent. They assert the activity survives and stays usable.
 * (On the API 36 emulator the virtual camera does connect and real frames do reach the pipeline.)
 */
@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val cameraPermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA)

    @Test
    fun activityLaunchesAndKeepsRunningWithTheCameraViewOnScreen() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            awaitSettled(scenario)
            assertEquals(Lifecycle.State.RESUMED, scenario.state)

            onView(withId(R.id.camera_content)).check(matches(isDisplayed()))

            // Let the camera pipeline run for a while. Frames may or may not arrive depending on
            // the device's camera, so we only require that nothing blows up.
            Thread.sleep(CAMERA_SETTLE_MILLIS)

            assertNotEquals(Lifecycle.State.DESTROYED, scenario.state)
            onView(withId(R.id.camera_content)).check(matches(isDisplayed()))

            // Diagnostic only: reports whether live frames actually reached the pipeline.
            scenario.onActivity { activity ->
                val samples = activity.soundData
                val valid = samples.count { it.toInt() != -1 }
                Log.i(TAG, "live pipeline: soundData size=${samples.size} validSamples=$valid")
            }
        }
    }

    @Test
    fun activityForcesLandscapeAndLaysOutTheControlBar() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            awaitSettled(scenario)

            onView(withId(R.id.fullscreen_content_controls)).check(matches(isDisplayed()))
            onView(withId(R.id.settings_button)).check(matches(isDisplayed()))
            onView(withId(R.id.keyboard_button)).check(matches(isDisplayed()))
            onView(withId(R.id.main_view_button)).check(matches(isDisplayed()))
            onView(withId(R.id.pause_button)).check(matches(isDisplayed()))

            scenario.onActivity { activity ->
                val configuration = activity.resources.configuration
                Log.i(TAG, "orientation=${configuration.orientation} w=${configuration.screenWidthDp}")
                assertTrue(
                    "activity should be landscape but was ${configuration.screenWidthDp}" +
                        "x${configuration.screenHeightDp}",
                    configuration.screenWidthDp >= configuration.screenHeightDp
                )
            }
        }
    }

    @Test
    fun theSettingsKeyboardAndMainViewButtonsSwapScreens() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            awaitSettled(scenario)

            // Starts on the main view: neither overlay is showing.
            onView(withId(R.id.settings_content)).check(matches(not(isDisplayed())))
            onView(withId(R.id.keyboard_view)).check(matches(not(isDisplayed())))

            onView(withId(R.id.settings_button)).perform(click())
            onView(withId(R.id.settings_content)).check(matches(isDisplayed()))
            onView(withId(R.id.toggleSmoothingButton)).check(matches(isDisplayed()))
            onView(withId(R.id.toggleLineButton)).check(matches(isDisplayed()))
            onView(withId(R.id.numberPicker)).check(matches(isDisplayed()))
            onView(withId(R.id.keyboard_view)).check(matches(not(isDisplayed())))

            onView(withId(R.id.keyboard_button)).perform(click())
            onView(withId(R.id.keyboard_view)).check(matches(isDisplayed()))
            onView(withId(R.id.settings_content)).check(matches(not(isDisplayed())))

            onView(withId(R.id.main_view_button)).perform(click())
            onView(withId(R.id.settings_content)).check(matches(not(isDisplayed())))
            onView(withId(R.id.keyboard_view)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun theSmoothingAndLineFeedbackTogglesFlipInTheUi() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            awaitSettled(scenario)

            onView(withId(R.id.settings_button)).perform(click())

            // Both default to on.
            onView(withId(R.id.toggleSmoothingButton)).check(matches(isChecked()))
            onView(withId(R.id.toggleLineButton)).check(matches(isChecked()))

            onView(withId(R.id.toggleSmoothingButton)).perform(click())
            onView(withId(R.id.toggleSmoothingButton)).check(matches(isNotChecked()))

            onView(withId(R.id.toggleLineButton)).perform(click())
            onView(withId(R.id.toggleLineButton)).check(matches(isNotChecked()))

            onView(withId(R.id.toggleSmoothingButton)).perform(click())
            onView(withId(R.id.toggleSmoothingButton)).check(matches(isChecked()))
        }
    }

    /**
     * Locks in a known bug rather than fixing it: `MainActivity.toggleSmoothing` and
     * `toggleLineFeedback` call `editor.putBoolean(...)` but never `apply()` or `commit()`, so
     * nothing is ever written to SharedPreferences and the toggles reset to their defaults on
     * recreate. The same applies to the current screen, written by `displaySettings` etc.
     */
    @Test
    fun toggleStateIsNotPersistedBecauseThePreferenceEditorIsNeverCommitted() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            awaitSettled(scenario)

            onView(withId(R.id.settings_button)).perform(click())
            onView(withId(R.id.toggleSmoothingButton)).perform(click())
            onView(withId(R.id.toggleSmoothingButton)).check(matches(isNotChecked()))

            val preferences = InstrumentationRegistry.getInstrumentation().targetContext
                .getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
            assertTrue(
                "the editor is never committed, so nothing should be stored",
                !preferences.contains(SMOOTHING_PREFERENCE) && !preferences.contains(SCREEN_PREFERENCE)
            )

            scenario.recreate()
            awaitSettled(scenario)

            // Back to the main view (the screen preference was not persisted either) with the
            // toggle reset to its default.
            onView(withId(R.id.settings_content)).check(matches(not(isDisplayed())))
            onView(withId(R.id.settings_button)).perform(click())
            onView(withId(R.id.toggleSmoothingButton)).check(matches(isChecked()))
        }
    }

    /**
     * [MainActivity] is not stable straight after launch: `onCreate` forces
     * `SCREEN_ORIENTATION_LANDSCAPE`, which recreates the activity when it launches from a portrait
     * device, and the camera permission request then bounces it through onPause/onResume once more.
     * Every `onResume` re-runs `updateSmoothingButton`/`updateLineFeedbackButton`, so interacting
     * before that has finished silently resets the toggles. Wait for it to settle first.
     */
    private fun awaitSettled(scenario: ActivityScenario<MainActivity>) {
        val deadline = SystemClock.uptimeMillis() + SETTLE_TIMEOUT_MILLIS
        var lastActivity: MainActivity? = null
        var stableSince = SystemClock.uptimeMillis()
        while (SystemClock.uptimeMillis() < deadline) {
            val current = arrayOfNulls<MainActivity>(1)
            scenario.onActivity { current[0] = it }
            if (scenario.state != Lifecycle.State.RESUMED || current[0] !== lastActivity) {
                lastActivity = current[0]
                stableSince = SystemClock.uptimeMillis()
            } else if (SystemClock.uptimeMillis() - stableSince >= REQUIRED_STABLE_MILLIS) {
                return
            }
            Thread.sleep(POLL_MILLIS)
        }
        throw AssertionError("MainActivity never settled into a stable RESUMED state")
    }

    companion object {
        private const val TAG = "MainActivitySmokeTest"
        private const val CAMERA_SETTLE_MILLIS = 4000L
        private const val SETTLE_TIMEOUT_MILLIS = 20000L
        private const val REQUIRED_STABLE_MILLIS = 1500L
        private const val POLL_MILLIS = 100L
        private const val APP_PREFERENCES = "APP_PREFERENCES"
        private const val SMOOTHING_PREFERENCE = "smoothing"
        private const val SCREEN_PREFERENCE = "screen"
    }
}
