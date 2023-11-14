package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.data.Editorial
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class EditorialViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: EditorialViewHolderViewModel.ViewModel

    private val backgroundColor = TestSubscriber<Int>()
    private val ctaDescription = TestSubscriber<Int>()
    private val ctaTitle = TestSubscriber<Int>()
    private val editorial = TestSubscriber<Editorial>()
    private val graphic = TestSubscriber<Int>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        this.vm = EditorialViewHolderViewModel.ViewModel()

        this.vm.outputs.backgroundColor().subscribe { this.backgroundColor.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.ctaDescription().subscribe { this.ctaDescription.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.ctaTitle().subscribe { this.ctaTitle.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.editorial().subscribe { this.editorial.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.graphic().subscribe { this.graphic.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testBackgroundColor() {
        setUpEnvironment()

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)
        this.backgroundColor.assertValue(R.color.kds_trust_700)
    }

    @Test
    fun testCtaDescription() {
        setUpEnvironment()

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)
        this.ctaDescription.assertValue(R.string.Find_projects_that_speak_to_you)
    }

    @Test
    fun testCtaTitle() {
        setUpEnvironment()

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)
        this.ctaTitle.assertValue(R.string.Back_it_because_you_believe_in_it)
    }

    @Test
    fun testEditorial() {
        setUpEnvironment()

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)
        this.vm.inputs.editorialClicked()
        this.editorial.assertValue(Editorial.GO_REWARDLESS)
    }

    @Test
    fun testGraphic() {
        setUpEnvironment()

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)
        this.graphic.assertValue(R.drawable.go_rewardless_header)
    }

    @Test
    fun testLightsOnTitle() {
        setUpEnvironment()

        this.vm.inputs.configureWith(Editorial.LIGHTS_ON)
        this.ctaTitle.assertValue(R.string.Introducing_Lights_On)
    }

    @Test
    fun testLightsOnDescription() {
        setUpEnvironment()

        this.vm.inputs.configureWith(Editorial.LIGHTS_ON)
        this.ctaDescription.assertValue(R.string.Support_creative_spaces_and_businesses_affected_by)
    }

    @Test
    fun testLightsOnGrafic() {
        setUpEnvironment()

        this.vm.inputs.configureWith(Editorial.LIGHTS_ON)
        this.graphic.assertValue(R.drawable.lights_on)
    }

    @Test
    fun testLightsOnEditorialClicked() {
        setUpEnvironment()

        this.vm.inputs.configureWith(Editorial.LIGHTS_ON)
        this.vm.inputs.editorialClicked()
        this.editorial.assertValue(Editorial.LIGHTS_ON)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
