package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectFaqFactory
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Before
import org.junit.Test

class FrequentlyAskedQuestionsViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: FrequentlyAskedQuestionsViewHolderViewModel.FrequentlyAskedQuestionsViewHolderViewModel

    private val question = TestSubscriber.create<String>()
    private val answer = TestSubscriber.create<String>()
    private val updatedDate = TestSubscriber.create<String>()
    private val disposables = CompositeDisposable()

    @Before
    fun setupEnvironment() {
        this.vm = FrequentlyAskedQuestionsViewHolderViewModel.FrequentlyAskedQuestionsViewHolderViewModel()
        this.vm.outputs.question().subscribe { this.question.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.answer().subscribe { this.answer.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.updatedDate().subscribe { this.updatedDate.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testBindProjectFAQ() {
        val faq = ProjectFaqFactory.getFaqs()[0]

        this.vm.configureWith(faq)

        this.question.assertValue(faq.question)
        this.answer.assertValue(faq.answer)
        this.updatedDate.assertValue(DateTimeUtils.longDate(faq.createdAt))
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
}
