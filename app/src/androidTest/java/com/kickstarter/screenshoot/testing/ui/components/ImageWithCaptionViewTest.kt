package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
import com.kickstarter.R
import com.kickstarter.screenshoot.testing.InstrumentedApp
import com.kickstarter.ui.views.ImageWithCaptionView
import org.junit.Before
import org.junit.Test

class ImageWithCaptionViewTest : ScreenshotTest {

    lateinit var component: ApplicationComponent

    @Before
    fun setup() {
        // - Test Application
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as InstrumentedApp
        // - Test Dagger component for injecting on environment Mock Objects
        component = app.component()
    }

    @Test
    fun imageWithCaptionInitializationTest() {
        val imageWithCaptionView = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext)
                .inflate(
                    R.layout.view_element_image_from_html, null
                ) as ConstraintLayout
            ).findViewById(R.id.image_view) as ImageWithCaptionView

        imageWithCaptionView.setImage("http://record.pt/")

        compareScreenshot(imageWithCaptionView)
    }

    @Test
    fun imageWithCaptionInitializationByDefaultTest() {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val imageWithCaptionView = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext)
                .inflate(
                    R.layout.view_element_image_from_html, null
                ) as ConstraintLayout
            ).findViewById(R.id.image_view) as ImageWithCaptionView

        imageWithCaptionView.setImage("http://record.pt/")
        imageWithCaptionView.setCaption(targetContext.getString(R.string.A_little_extra_to_help))

        compareScreenshot(imageWithCaptionView)
    }

    @Test
    fun imageWithCaptionAndLinkInitializationByDefaultTest() {
        val imageWithCaptionView = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext)
                .inflate(
                    R.layout.view_element_image_from_html, null
                ) as ConstraintLayout
            ).findViewById(R.id.image_view) as ImageWithCaptionView

        imageWithCaptionView.setImage("http://record.pt/")
        imageWithCaptionView.setCaption("This is an Android with a caption and a link", "http://record.pt/")

        compareScreenshot(imageWithCaptionView)
    }
}
