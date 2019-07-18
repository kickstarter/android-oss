package com.kickstarter.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class CancelPledgeViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CancelPledgeViewModel.ViewModel

    private val cancelButtonIsVisible = TestSubscriber<Boolean>()
    private val dismiss = TestSubscriber<Void>()
    private val pledgeAmount = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val projectName = TestSubscriber<String>()
    private val showCancelError = TestSubscriber<String>()
    private val showServerError = TestSubscriber<Void>()
    private val success = TestSubscriber<Void>()

    private fun setUpEnvironment(environment: Environment, project: Project? = ProjectFactory.backedProject()) {
        this.vm = CancelPledgeViewModel.ViewModel(environment)

        this.vm.outputs.cancelButtonIsVisible().subscribe(this.cancelButtonIsVisible)
        this.vm.outputs.dismiss().subscribe(this.dismiss)
        this.vm.outputs.pledgeAmountAndProjectName().map { it.first }.subscribe(this.pledgeAmount)
        this.vm.outputs.pledgeAmountAndProjectName().map { it.second }.subscribe(this.projectName)
        this.vm.outputs.progressBarIsVisible().subscribe(this.progressBarIsVisible)
        this.vm.outputs.showCancelError().subscribe(this.showCancelError)
        this.vm.outputs.showServerError().subscribe(this.showServerError)
        this.vm.outputs.success().subscribe(this.success)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT, project)
        this.vm.arguments(bundle)
    }

    @Test
    fun testDismiss() {
        setUpEnvironment(environment())

        this.vm.inputs.closeButtonClicked()
        this.dismiss.assertValueCount(1)

        this.vm.inputs.goBackButtonClicked()
        this.dismiss.assertValueCount(2)
    }

    @Test
    fun testPledgeAmountAndProjectName() {
        var backedProject = ProjectFactory.backedProject()
        val backing = (backedProject.backing()?: BackingFactory.backing())
                .toBuilder()
                .amount(30.0)
                .build()
        backedProject = backedProject.toBuilder()
                .backing(backing)
                .build()
        setUpEnvironment(environment(), backedProject)

        this.pledgeAmount.assertValue("$30")
        this.projectName.assertValue("Some Name")
    }

    @Test
    fun testServerResponses() {
        setUpEnvironment(environment().toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                        return Observable.just("Error")
                    }
                }).build())

        this.vm.inputs.confirmCancellationClicked("")
        this.progressBarIsVisible.assertValuesAndClear(true, false)
        this.cancelButtonIsVisible.assertValuesAndClear(false, true)
        this.showCancelError.assertValuesAndClear("Error")
        this.showServerError.assertNoValues()
        this.success.assertNoValues()

        setUpEnvironment(environment().toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                        return Observable.just(false)
                    }
                }).build())

        this.vm.inputs.confirmCancellationClicked("")
        this.progressBarIsVisible.assertValuesAndClear(true, false)
        this.cancelButtonIsVisible.assertValuesAndClear(false, true)
        this.showCancelError.assertNoValues()
        this.showServerError.assertValueCount(1)
        this.success.assertNoValues()

        setUpEnvironment(environment().toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                        return Observable.error(Throwable("error"))
                    }
                }).build())

        this.vm.inputs.confirmCancellationClicked("")
        this.progressBarIsVisible.assertValuesAndClear(true, false)
        this.cancelButtonIsVisible.assertValuesAndClear(false, true)
        this.showCancelError.assertNoValues()
        this.showServerError.assertValueCount(2)
        this.success.assertNoValues()

        setUpEnvironment(environment())

        this.vm.inputs.confirmCancellationClicked("")
        this.progressBarIsVisible.assertValuesAndClear(true, false)
        this.cancelButtonIsVisible.assertValuesAndClear(false, true)
        this.showCancelError.assertNoValues()
        this.showServerError.assertValueCount(2)
        this.success.assertValueCount(1)
    }

}
