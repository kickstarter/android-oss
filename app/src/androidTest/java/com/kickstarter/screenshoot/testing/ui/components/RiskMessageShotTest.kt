package com.kickstarter.screenshoot.testing.ui.components

import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
import com.kickstarter.screenshoot.testing.InstrumentedApp
import org.junit.Before
import org.junit.Test

class RiskMessageShotTest : ScreenshotTest {

    lateinit var component: ApplicationComponent

    @Before
    fun setup() {
        // - Test Application
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as InstrumentedApp
        // - Test Dagger component for injecting on environment Mock Objects
        component = app.component()
    }

    @Test
    fun layoutInitializationByDefaultTest() {
        // TODO: View migrated to compose, on next steps we will add Screenshot tests for compose
    }
}
