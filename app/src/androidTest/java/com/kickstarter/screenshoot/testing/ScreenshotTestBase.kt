package com.kickstarter.screenshoot.testing

import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.facebook.testing.screenshot.Screenshot
import com.facebook.testing.screenshot.ViewHelpers
import com.kickstarter.R
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName
import java.util.*
import kotlin.collections.ArrayList

open class ScreenshotTestBase {

    // https://stackoverflow.com/a/32827600
    @JvmField
    @Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @JvmField
    @Rule
    val testName = TestName()

    // Resolution DP 320 x 480, AspectRatio 3:2
    private val DEVICE_1 = TestDevice(320, 480)
    // Resolution DP 360 x 640, AspectRatio 16:9
    private val DEVICE_2 = TestDevice(360, 640)
    // Resolution DP 480 x 800, AspectRatio 5:3
    private val DEVICE_3 = TestDevice(480, 800)
    // Resolution DP 600 x 960, AspectRatio 8:5
    private val DEVICE_4 = TestDevice(600, 960)

    protected lateinit var context: Context

    @Before
    open fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.setTheme(R.style.AppTheme)
    }

    protected fun updateResources(locale: Locale, isDarkTheme: Boolean) {
        val configuration = context.resources.configuration

        configuration.setLocale(locale)

        // https://stackoverflow.com/a/32476027
        configuration.uiMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()
        if (isDarkTheme) {
            configuration.uiMode = configuration.uiMode or Configuration.UI_MODE_NIGHT_YES
        } else {
            configuration.uiMode = configuration.uiMode or Configuration.UI_MODE_NIGHT_NO
        }

        context = context.createConfigurationContext(configuration)
    }

    protected fun record(view: View, device: TestDevice) {
        if (device.height == WRAP_CONTENT) {
            ViewHelpers.setupView(view).setExactWidthDp(device.width).layout()
        } else {
            ViewHelpers.setupView(view).setExactWidthDp(device.width).setExactHeightDp(device.height).layout()
        }

        Screenshot.snap(view).setName(testName.methodName + "_" + device.info).record()
    }

    protected fun getViewConfigCombos(height: Int): List<TestDevice> {
        val devices = ArrayList<TestDevice>()
        // here we can add more languages but in order to reduce the number of screenshots for the PR demo we removed 'el'
        val languages = arrayOf("en")

        devices.addAll(generateCombos(languages, TestDevice(DEVICE_1.width, height), false))
        devices.addAll(generateCombos(languages, TestDevice(DEVICE_4.width, height), true))

        return devices
    }

    private fun generateCombos(locales: Array<String>, device: TestDevice, isDarkTheme: Boolean): List<TestDevice> {
        val devices = ArrayList<TestDevice>()

        for (localeStr in locales) {
            val newDevice =
                TestDevice(device.width, device.height, isDarkTheme, Locale(localeStr))
            devices.add(newDevice)
        }

        return devices
    }
}
