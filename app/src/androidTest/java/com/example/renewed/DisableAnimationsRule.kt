package com.example.renewed

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A JUnit Test Rule that disables animations on the device before a test runs
 * and re-enables them after the test completes.
 *
 * This is crucial for creating stable and reliable Espresso tests. To use it,
 * you first need to add the uiautomator dependency to your build.gradle file:
 *
 * androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
 */
class DisableAnimationsRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                // Disable animations before the test
                setAnimationState(enabled = false)
                try {
                    // Run the actual test
                    base.evaluate()
                } finally {
                    // Re-enable animations after the test, even if it fails
                    setAnimationState(enabled = true)
                }
            }
        }
    }

    private fun setAnimationState(enabled: Boolean) {
        val scale = if (enabled) "1.0" else "0.0"
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)

        // Grant permission to change settings for API 23+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            instrumentation.uiAutomation.executeShellCommand(
                "appops set ${instrumentation.targetContext.packageName} " +
                        "android:write_settings allow"
            )
        }

        // Disable all three animation scales
        device.executeShellCommand("settings put global window_animation_scale $scale")
        device.executeShellCommand("settings put global transition_animation_scale $scale")
        device.executeShellCommand("settings put global animator_duration_scale $scale")
    }
}