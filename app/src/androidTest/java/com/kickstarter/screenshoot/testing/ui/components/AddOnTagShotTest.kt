package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
import com.kickstarter.R
import com.kickstarter.screenshoot.testing.InstrumentedApp
import com.kickstarter.ui.views.AddOnTagComponent
import org.junit.Before
import org.junit.Test

class AddOnTagShotTest : ScreenshotTest {

    private lateinit var addOnTagComponent: AddOnTagComponent
    lateinit var component: ApplicationComponent

    @Before
    fun setup() {
        // - Test Application
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as InstrumentedApp
        // - Test Dagger component for injecting on environment Mock Objects
        component = app.component()

        addOnTagComponent =
            (
                LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext)
                    .inflate(
                        R.layout.item_add_on_pledge, null
                    ) as CardView
                )
                .findViewById(R.id.addon_quantity_remaining)
    }

    @Test
    fun addOnTagComponentViewScreenshotTest_TextValue() {
        addOnTagComponent.setAddOnTagText("30 left")
        compareScreenshot(addOnTagComponent)
    }
}
