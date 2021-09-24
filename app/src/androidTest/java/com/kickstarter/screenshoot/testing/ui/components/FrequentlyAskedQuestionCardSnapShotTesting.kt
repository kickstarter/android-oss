package com.kickstarter.screenshoot.testing.ui.components

import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.platform.app.InstrumentationRegistry
import com.karumi.shot.ScreenshotTest
import com.kickstarter.ApplicationComponent
import com.kickstarter.R
import com.kickstarter.screenshoot.testing.InstrumentedApp
import com.kickstarter.ui.views.FrequentlyAskedQuestionCard
import org.junit.Before
import org.junit.Test
class FrequentlyAskedQuestionCardSnapShotTesting : ScreenshotTest {

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
        val card = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_frequently_asked_question_card, null
            ) as ConstraintLayout
            ).findViewById(R.id.question_answer_layout) as FrequentlyAskedQuestionCard

        compareScreenshot(card)
    }

    @Test
    fun layoutExpandTest() {
        val frequentlyAskedQuestionCard = (
            LayoutInflater.from(InstrumentationRegistry.getInstrumentation().targetContext).inflate(
                R.layout.item_frequently_asked_question_card, null
            ) as ConstraintLayout
            ).findViewById(R.id.question_answer_layout) as FrequentlyAskedQuestionCard
        this.disableFlakyComponentsAndWaitForIdle(frequentlyAskedQuestionCard)
        this.runOnUi {
            frequentlyAskedQuestionCard.toggleAnswerLayout()
        }
        compareScreenshot(frequentlyAskedQuestionCard)
    }
}
