package com.kickstarter.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.models.ProjectFaq
import com.kickstarter.ui.ArgumentsKey
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
        val bundle = Bundle()
        bundle.putParcelableArrayList(
            ArgumentsKey.PROJECT_QUESTIONS_ANSWERS,
            faqList
        )

        this.vm.arguments(bundle)
        this.projectFaqList.assertValue(faqList)
        this.bindEmptyState.assertNoValues()
    }

    @Test
    fun testBindEmptyList() {
        setUpEnvironment(environment())
        val faqList = arrayListOf<ProjectFaq>()
        val bundle = Bundle()
        bundle.putParcelableArrayList(
            ArgumentsKey.PROJECT_QUESTIONS_ANSWERS,
            faqList
        )

        this.vm.arguments(bundle)
        this.projectFaqList.assertNoValues()
        this.bindEmptyState.assertValueCount(1)
    }
}
