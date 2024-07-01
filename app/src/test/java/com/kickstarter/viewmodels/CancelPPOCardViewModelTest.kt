package com.kickstarter.viewmodels

import android.os.Bundle
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.viewmodels.CancelPledgeViewModel.CancelPledgeViewModel
import com.kickstarter.viewmodels.CancelPledgeViewModel.Factory
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test
import java.math.RoundingMode

class CancelPPOCardViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: CancelPledgeViewModel

    private val cancelButtonIsVisible = TestSubscriber<Boolean>()
    private val dismiss = TestSubscriber<Unit>()
    private val pledgeAmount = TestSubscriber<String>()
    private val progressBarIsVisible = TestSubscriber<Boolean>()
    private val projectName = TestSubscriber<String>()
    private val showCancelError = TestSubscriber<String>()
    private val showServerError = TestSubscriber<Unit>()
    private val success = TestSubscriber<Unit>()

    private val disposables = CompositeDisposable()

    @After
    fun cleanUp() {
        disposables.clear()
    }
    private fun setUpEnvironment(environment: Environment, project: Project? = ProjectFactory.backedProject()) {
        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.CANCEL_PLEDGE_PROJECT, project)
        this.vm = Factory(environment, bundle).create(CancelPledgeViewModel::class.java)

        this.vm.outputs.cancelButtonIsVisible().subscribe { this.cancelButtonIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.dismiss().subscribe { this.dismiss.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.pledgeAmountAndProjectName().map { it.first }.subscribe { this.pledgeAmount.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.pledgeAmountAndProjectName().map { it.second }.subscribe { this.projectName.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.progressBarIsVisible().subscribe { this.progressBarIsVisible.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showCancelError().subscribe { this.showCancelError.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showServerError().subscribe { this.showServerError.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.success().subscribe { this.success.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testDismiss() {
        setUpEnvironment(environment())

        this.vm.inputs.goBackButtonClicked()
        this.dismiss.assertValueCount(1)
    }

    @Test
    fun testPledgeAmountAndProjectName() {
        var backedProject = ProjectFactory.backedProject()
        val amount = 30.0
        val backing = (backedProject.backing() ?: BackingFactory.backing())
            .toBuilder()
            .amount(amount)
            .build()
        backedProject = backedProject.toBuilder()
            .backing(backing)
            .build()
        val environment = environment()
        setUpEnvironment(environment, backedProject)

        val expectedCurrency = requireNotNull(environment.ksCurrency()).format(amount, backedProject, RoundingMode.HALF_UP)
        this.pledgeAmount.assertValue(expectedCurrency)
        this.projectName.assertValue("Some Name")
    }

    @Test
    fun testCancelingPledge_whenErrorMessage() {
        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(object : MockApolloClientV2() {
                    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                        return Observable.just("Error")
                    }
                }).build()
        )

        this.vm.inputs.confirmCancellationClicked("")
        this.progressBarIsVisible.assertValues(true, false)
        this.cancelButtonIsVisible.assertValues(false, true)
        this.showCancelError.assertValues("Error")
        this.showServerError.assertNoValues()
        this.success.assertNoValues()
    }

    @Test
    fun testCancelingPledge_whenBackingNotCancelled() {
        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(object : MockApolloClientV2() {
                    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                        return Observable.just(false)
                    }
                }).build()
        )

        this.vm.inputs.confirmCancellationClicked("")
        this.progressBarIsVisible.assertValues(true, false)
        this.cancelButtonIsVisible.assertValues(false, true)
        this.showCancelError.assertNoValues()
        this.showServerError.assertValueCount(1)
        this.success.assertNoValues()
    }

    @Test
    fun testCancelingPledge_whenServerError() {
        setUpEnvironment(
            environment().toBuilder()
                .apolloClientV2(object : MockApolloClientV2() {
                    override fun cancelBacking(backing: Backing, note: String): Observable<Any> {
                        return Observable.error(Throwable("error"))
                    }
                }).build()
        )

        this.vm.inputs.confirmCancellationClicked("")
        this.progressBarIsVisible.assertValues(true, false)
        this.cancelButtonIsVisible.assertValues(false, true)
        this.showCancelError.assertNoValues()
        this.showServerError.assertValueCount(1)
        this.success.assertNoValues()
    }

    @Test
    fun testCancelingPledge_whenSuccessful() {
        setUpEnvironment(environment())

        this.vm.inputs.confirmCancellationClicked("")
        this.progressBarIsVisible.assertValues(true, false)
        this.cancelButtonIsVisible.assertValues(false, true)
        this.showCancelError.assertNoValues()
        this.showServerError.assertNoValues()
        this.success.assertValueCount(1)
    }
}
