package com.kickstarter.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test
import type.FlaggingKind

class ReportProjectViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ReportProjectViewModel.ReportProjectViewModel

    private val projectUrl = TestSubscriber.create<String>()
    private val email = TestSubscriber.create<String>()
    private val finish = TestSubscriber.create<ReportProjectViewModel.ReportProjectViewModel.NavigationResult>()
    private val progressBarVisible = TestSubscriber.create<Boolean>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {

        val environment = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
            override fun userPrivacy(): Observable<UserPrivacyQuery.Data> {
                return Observable.just(
                    UserPrivacyQuery.Data(
                        UserPrivacyQuery.Me(
                            "", "Some Name",
                            "some@email.com", true, true, true, true, "USD"
                        )
                    )
                )
            }

            override fun createFlagging(
                project: Project?,
                details: String,
                flaggingKind: String
            ): Observable<String> {
                return Observable.just(FlaggingKind.SPAM.rawValue())
            }
        }).build()

        val bundle = Bundle()
        bundle.putParcelable(
            IntentKey.PROJECT,
            ProjectFactory.project()
        )

        this.vm = ReportProjectViewModel.ReportProjectViewModel(environment, bundle)

        disposables.add(this.vm.outputs.projectUrl().subscribe { this.projectUrl.onNext(it) })
        disposables.add(this.vm.outputs.email().subscribe { this.email.onNext(it) })
        disposables.add(this.vm.outputs.finish().subscribe { this.finish.onNext(it) })
        disposables.add(this.vm.outputs.progressBarIsVisible().subscribe { this.progressBarVisible.onNext(it) })
    }

    @Test
    fun testEmailUSer() {
        setUpEnvironment()
        email.assertValue("some@email.com")
    }

    @Test
    fun testProjectUrl() {
        setUpEnvironment()
        projectUrl.assertValue(ProjectFactory.project().webProjectUrl())
    }

    @Test
    fun testFinishNavigationSuccess_When_ProjectReported() {
        setUpEnvironment()

        vm.inputs.createFlagging()
        vm.inputs.inputDetails(FlaggingKind.SPAM.rawValue())
        finish.assertValue(ReportProjectViewModel.ReportProjectViewModel.NavigationResult(true, FlaggingKind.SPAM.rawValue()))
    }

    @Test
    fun testFinishNavigationSuccess_When_UserNavigatedBack() {
        setUpEnvironment()
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
}
