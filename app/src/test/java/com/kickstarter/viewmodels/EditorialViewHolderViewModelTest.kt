package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.ui.data.Editorial
import org.junit.Test
import rx.observers.TestSubscriber

class EditorialViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: EditorialViewHolderViewModel.ViewModel

    private val backgroundColor = TestSubscriber<Int>()
    private val ctaDescription = TestSubscriber<Int>()
    private val ctaTitle = TestSubscriber<Int>()
    private val editorial = TestSubscriber<Editorial>()
    private val graphic = TestSubscriber<Int>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = EditorialViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.backgroundColor().subscribe(this.backgroundColor)
        this.vm.outputs.ctaDescription().subscribe(this.ctaDescription)
        this.vm.outputs.ctaTitle().subscribe(this.ctaTitle)
        this.vm.outputs.editorial().subscribe(this.editorial)
        this.vm.outputs.graphic().subscribe(this.graphic)
    }

    @Test
    fun testBackgroundColor() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)

        this.backgroundColor.assertValue(R.color.trust_700)
    }

    @Test
    fun testCtaDescription() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)

        this.ctaDescription.assertValue(R.string.Find_projects_that_speak_to_you)
    }

    @Test
    fun testCtaTitle() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)

        this.ctaTitle.assertValue(R.string.Back_it_because_you_believe_in_it)
    }

    @Test
    fun testEditorial() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)

        this.vm.inputs.editorialClicked()

        this.editorial.assertValue(Editorial.GO_REWARDLESS)
    }

    @Test
    fun testGraphic() {
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(Editorial.GO_REWARDLESS)

        this.graphic.assertValue(R.drawable.go_rewardless_tout)
    }
}
