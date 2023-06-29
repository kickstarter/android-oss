package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.models.Project
import com.kickstarter.models.ProjectFaq
import com.kickstarter.viewmodels.projectpage.FrequentlyAskedQuestionViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class FrequentlyAskedQuestionViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: FrequentlyAskedQuestionViewModel.FrequentlyAskedQuestionViewModel

    private val projectFaqList = TestSubscriber.create<List<ProjectFaq>>()
    private val bindEmptyState = TestSubscriber.create<Unit>()

    private val askQuestionButtonIsGone = TestSubscriber<Boolean>()
    private val startComposeMessageActivity = TestSubscriber<Project>()
    private val startMessagesActivity = TestSubscriber<Project>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = FrequentlyAskedQuestionViewModel.FrequentlyAskedQuestionViewModel(environment)

        this.vm.outputs.projectFaqList().subscribe { this.projectFaqList.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.bindEmptyState().subscribe { this.bindEmptyState.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.askQuestionButtonIsGone().subscribe { this.askQuestionButtonIsGone.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startComposeMessageActivity().subscribe { this.startComposeMessageActivity.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.startMessagesActivity().subscribe { this.startMessagesActivity.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testBindProjectFqaList() {
        setUpEnvironment(environment())
        val faqList = arrayListOf(ProjectFaq.builder().build())

        this.vm.configureWith(
            ProjectDataFactory.project(
                ProjectFactory.project().toBuilder()
                    .projectFaqs(faqList).build()
            )
        )

        this.projectFaqList.assertValue(faqList)
        this.bindEmptyState.assertNoValues()
    }

    @Test
    fun testBindEmptyList() {
        setUpEnvironment(environment())
        val faqList = arrayListOf<ProjectFaq>()

        this.vm.configureWith(
            ProjectDataFactory.project(
                ProjectFactory.project().toBuilder()
                    .projectFaqs(faqList).build()
            )
        )

        this.projectFaqList.assertNoValues()
        this.bindEmptyState.assertValueCount(1)
    }

    @Test
    fun testMessageIconIsGone_WhenUserIsLoggedOut() {
        setUpEnvironment(environment())

        this.vm.configureWith(ProjectDataFactory.project(ProjectFactory.project()))
        this.askQuestionButtonIsGone.assertValue(true)
    }

    @Test
    fun testGoToComposeMessageActivity_WhenLoggedInUserIsNotBacker() {
        setUpEnvironment(environment().toBuilder().currentUserV2(MockCurrentUserV2(UserFactory.user())).build())

        this.vm.configureWith(ProjectDataFactory.project(ProjectFactory.project()))

        this.askQuestionButtonIsGone.assertValue(false)
        this.vm.inputs.askQuestionButtonClicked()
        this.startComposeMessageActivity.assertValueCount(1)
        this.startMessagesActivity.assertNoValues()
    }

    @Test
    fun testGoToMessagesActivity_WhenLoggedInUserIsABacker() {
        val project = ProjectFactory.project().toBuilder().isBacking(true).build()
        setUpEnvironment(environment().toBuilder().currentUserV2(MockCurrentUserV2(UserFactory.user())).build())

        this.vm.configureWith(ProjectDataFactory.project(project))

        this.askQuestionButtonIsGone.assertValue(false)
        this.vm.inputs.askQuestionButtonClicked()
        this.startMessagesActivity.assertValueCount(1)
        this.startComposeMessageActivity.assertNoValues()
    }

    @Test
    fun testMessageIconIsGone_WhenLoggedInUserIsCreatorOfProject() {
        setUpEnvironment(environment())

        val creator = UserFactory.creator()
        val project = ProjectFactory.project().toBuilder().creator(creator).build()
        this.vm.configureWith(ProjectDataFactory.project(project))

        this.askQuestionButtonIsGone.assertValue(true)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
