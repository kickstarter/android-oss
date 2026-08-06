package com.kickstarter.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockStatsigClient
import com.kickstarter.libs.featureflag.StatsigGateKey
import com.kickstarter.libs.utils.extensions.reduceProjectPayload
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.FlaggingOption
import com.kickstarter.models.Project
import com.kickstarter.models.UserPrivacy
import com.kickstarter.type.FlaggingContent
import com.kickstarter.type.FlaggingKind
import com.kickstarter.ui.IntentKey
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.COMMUNITY_GUIDELINES_TAG
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.PROHIBITED_ITEMS
import com.kickstarter.viewmodels.ReportProjectViewModel.Companion.PROHIBITED_ITEMS_TAG
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportProjectViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ReportProjectViewModel.ReportProjectViewModel

    private val projectUrl = TestSubscriber.create<String>()
    private val email = TestSubscriber.create<String>()
    private val finish = TestSubscriber.create<ReportProjectViewModel.ReportProjectViewModel.NavigationResult>()
    private val progressBarVisible = TestSubscriber.create<Boolean>()
    private val openExternal = TestSubscriber.create<String>()
    private val flaggingOptions = TestSubscriber.create<List<FlaggingOption>>()
    private val placeholder = TestSubscriber.create<String>()
    private val disposables = CompositeDisposable()

    private fun getEnvironment() = environment().toBuilder().apolloClientV2(object : MockApolloClientV2() {
        override fun userPrivacy(): Observable<UserPrivacy> {
            return Observable.just(
                UserPrivacy("Some Name", "some@email.com", true, true, true, true, "USD")
            )
        }

        override fun createFlagging(
            project: Project?,
            details: String,
            flaggingKind: String
        ): Observable<String> {
            return Observable.just(FlaggingKind.SPAM.rawValue)
        }
    }).build()

    private fun sampleFlaggingOptions() = listOf(
        FlaggingOption("project/our_rules", "project", null, true, "This project breaks one of Our Rules", "subtitle", null),
        FlaggingOption("project/our_rules/resale", "project/our_rules", null, true, "Copying, reselling or plagiarism", null, null),
        FlaggingOption("project/our_rules/resale/reselling", "project/our_rules/resale", "RESELLING", false, "Reselling", null, "Please provide a URL.")
    )

    private fun getEnvironmentWithReportFlow(options: List<FlaggingOption>) =
        environment().toBuilder()
            .statsigClient(
                MockStatsigClient(
                    context = application(),
                    gateMap = mapOf(StatsigGateKey.ANDROID_REPORT_PROJECT.key to true)
                )
            )
            .apolloClientV2(object : MockApolloClientV2() {
                override fun userPrivacy(): Observable<UserPrivacy> {
                    return Observable.just(
                        UserPrivacy("Some Name", "some@email.com", true, true, true, true, "USD")
                    )
                }

                override fun flaggingOptions(contentType: FlaggingContent): Observable<List<FlaggingOption>> {
                    return Observable.just(options)
                }
            })
            .build()

    private fun getBundle(project: Project): Bundle {
        val bundle = Bundle()
        bundle.putParcelable(
            IntentKey.PROJECT,
            project.reduceProjectPayload()
        )

        return bundle
    }

    private fun setUpEnvironment(environment: Environment, bundle: Bundle?) {

        this.vm = ReportProjectViewModel.ReportProjectViewModel(environment, bundle)

        disposables.add(this.vm.outputs.projectUrl().subscribe { this.projectUrl.onNext(it) })
        disposables.add(this.vm.outputs.email().subscribe { this.email.onNext(it) })
        disposables.add(this.vm.outputs.finish().subscribe { this.finish.onNext(it) })
        disposables.add(this.vm.outputs.openExternalBrowserWithUrl().subscribe { this.openExternal.onNext(it) })
        disposables.add(this.vm.outputs.progressBarIsVisible().subscribe { this.progressBarVisible.onNext(it) })
        disposables.add(this.vm.outputs.flaggingOptions().subscribe { this.flaggingOptions.onNext(it) })
        disposables.add(this.vm.outputs.placeholder().subscribe { this.placeholder.onNext(it) })
    }

    @Test
    fun testEmailUSer() {
        val project = ProjectFactory.project()

        setUpEnvironment(getEnvironment(), getBundle(project))
        email.assertValue("some@email.com")
    }

    @Test
    fun testProjectUrl() {
        val project = ProjectFactory.project()
        val url = project.webProjectUrl()

        setUpEnvironment(getEnvironment(), getBundle(project))
        projectUrl.assertValue(url)
    }

    @Test
    fun testFinishNavigationSuccess_When_ProjectReported() {
        val project = ProjectFactory.project()
        setUpEnvironment(getEnvironment(), getBundle(project))

        vm.inputs.inputDetails(FlaggingKind.SPAM.rawValue)
        vm.inputs.kind(FlaggingKind.SPAM.rawValue)
        vm.inputs.createFlagging()
        finish.assertValue(ReportProjectViewModel.ReportProjectViewModel.NavigationResult(true, FlaggingKind.SPAM.rawValue))
    }

    @Test
    fun testCommunityGuidelinesClicked() {
        val project = ProjectFactory.project()

        setUpEnvironment(getEnvironment(), getBundle(project))

        vm.inputs.openExternalBrowser(COMMUNITY_GUIDELINES_TAG)
        openExternal.assertValueCount(1)
        openExternal.assertValue("${environment().webEndpoint()}$COMMUNITY_GUIDELINES")
    }

    @Test
    fun testProhibitedItemsClicked() {
        val project = ProjectFactory.project()

        setUpEnvironment(getEnvironment(), getBundle(project))

        vm.inputs.openExternalBrowser(PROHIBITED_ITEMS_TAG)
        openExternal.assertValueCount(1)
        openExternal.assertValue("${environment().webEndpoint()}$PROHIBITED_ITEMS")
    }

    @Test
    fun testInvalidTag() {
        val project = ProjectFactory.project()

        setUpEnvironment(getEnvironment(), getBundle(project))

        vm.inputs.openExternalBrowser("")
        openExternal.assertValueCount(0)
        openExternal.assertNoValues()
    }

    @Test
    fun testNullBundle_doesNotCrash() {
        setUpEnvironment(getEnvironment(), null)

        projectUrl.assertNoValues()
        email.assertValue("some@email.com")
        finish.assertNoValues()
        openExternal.assertNoValues()
    }

    @Test
    fun testReportFlowDisabled_byDefault_flaggingOptionsEmpty() {
        val project = ProjectFactory.project()
        setUpEnvironment(getEnvironment(), getBundle(project))

        assertFalse(vm.isReportFlowEnabled)
        flaggingOptions.assertValue(emptyList())
    }

    @Test
    fun testReportFlowEnabled_flaggingOptionsFetched() {
        val project = ProjectFactory.project()
        val options = sampleFlaggingOptions()
        setUpEnvironment(getEnvironmentWithReportFlow(options), getBundle(project))

        assertTrue(vm.isReportFlowEnabled)
        flaggingOptions.assertValue(options)
    }

    @Test
    fun testPlaceholder_whenOptionSelected() {
        val project = ProjectFactory.project()
        setUpEnvironment(getEnvironment(), getBundle(project))

        vm.inputs.inputPlaceholder("Please provide more info")
        placeholder.assertValues("", "Please provide more info")
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
}
