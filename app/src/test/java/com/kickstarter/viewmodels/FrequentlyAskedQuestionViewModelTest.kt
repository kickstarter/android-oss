package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.models.ProjectFaq
import org.junit.Test
import rx.observers.TestSubscriber

class FrequentlyAskedQuestionViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: FrequentlyAskedQuestionViewModel.ViewModel

    private val projectFaqList = TestSubscriber.create<List<ProjectFaq>>()
    private val bindEmptyState = TestSubscriber.create<Void>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = FrequentlyAskedQuestionViewModel.ViewModel(environment)

        this.vm.outputs.projectFaqList().subscribe(this.projectFaqList)
        this.vm.outputs.bindEmptyState().subscribe(this.bindEmptyState)
    }

    @Test
    fun testBindProjectFqaList() {
        setUpEnvironment(environment())
        val faqList = arrayListOf(ProjectFaq.builder().build())

        this.vm.configureWith(faqList)

        this.projectFaqList.assertValue(faqList)
        this.bindEmptyState.assertNoValues()
    }

    @Test
    fun testBindEmptyList() {
        setUpEnvironment(environment())
        val faqList = arrayListOf<ProjectFaq>()

        this.vm.configureWith(faqList)

        this.projectFaqList.assertNoValues()
        this.bindEmptyState.assertValueCount(1)
    }
}
