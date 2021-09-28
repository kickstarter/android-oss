package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
import com.kickstarter.R
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
        val layout = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.fragment_checkout_risk_message, null
            ) as ConstraintLayout
            ).findViewById(R.id.risk_message_cl) as ConstraintLayout

        compareScreenshot(layout)
    }
}
