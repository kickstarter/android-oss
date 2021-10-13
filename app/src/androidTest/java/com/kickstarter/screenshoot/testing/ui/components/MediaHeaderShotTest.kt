package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
import com.kickstarter.R
import com.kickstarter.screenshoot.testing.InstrumentedApp
import com.kickstarter.ui.views.MediaHeader
import org.junit.Before
import org.junit.Test

class MediaHeaderShotTest : ScreenshotTest {
    val url = "https://ksr-ugc.imgix.net/assets/012/032/069/46817a8c099133d5bf8b64aad282a696_original.png?crop=faces&w=1552&h=873&fit=crop&v=1463725702&auto=format&q=92&s=72501d155e4a5e399276632687c77959"

    lateinit var mediaHeader: MediaHeader
    lateinit var component: ApplicationComponent

    @Before
    fun setup() {
        // - Test Application
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as InstrumentedApp
        // - Test Dagger component for injecting on environment Mock Objects
        component = app.component()

        mediaHeader = (LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
            R.layout.media_header, null) as ConstraintLayout)
            .findViewById(R.id.media_header)

        mediaHeader.inputs.setProjectPhoto(url)
    }

    @Test
    fun playButton_whenVisibilityFalse_isGone(){
        mediaHeader.inputs.setPlayButtonVisibility(false)

        compareScreenshot(mediaHeader)
    }

    @Test
    fun playButton_whenVisibilityTrue_isVisible(){
        mediaHeader.inputs.setPlayButtonVisibility(true)

        compareScreenshot(mediaHeader)
    }
}