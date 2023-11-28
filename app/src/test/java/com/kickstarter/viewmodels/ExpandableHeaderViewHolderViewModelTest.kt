package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.Test

class ExpandableHeaderViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ExpandableHeaderViewHolderViewModel.ViewModel

    private val amountForSummary = TestSubscriber.create<String>()
    private val titleForSummary = TestSubscriber.create<String>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment() {
        this.vm = ExpandableHeaderViewHolderViewModel.ViewModel(environment())

        this.vm.outputs.amountForSummary().subscribe { this.amountForSummary.onNext(it) }
                .addToDisposable(disposables)
        this.vm.outputs.titleForSummary().subscribe { this.titleForSummary.onNext(it) }.addToDisposable(disposables)
    }
        @Test
        fun testTitleForSummary_whenIsAddonButQuantityNull_noEmission(){
            setUpEnvironment()

            val project = ProjectFactory.project()
            val reward = RewardFactory.addOn().toBuilder().quantity(null).build()
            vm.inputs.configureWith(Pair(project, reward))

            this.titleForSummary.assertNoValues()
        }

        @Test
        fun testTitleForSummary_whenAddonAndQuantity0_noEmission(){
            setUpEnvironment()

            val project = ProjectFactory.project()
            val reward = RewardFactory.addOn().toBuilder().quantity(0).build()
            vm.inputs.configureWith(Pair(project, reward))

            this.titleForSummary.assertNoValues()
        }

        @Test
        fun testTitleForSummary_whenAddonAndQuantityMoreThan0_emitString() {
            setUpEnvironment()

            val project = ProjectFactory.project()
            val reward = RewardFactory.addOn().toBuilder().title("pins").quantity(5).build()
            vm.inputs.configureWith(Pair(project, reward))

            this.titleForSummary.assertValue("5 X pins")
        }

    @Test
    fun testTitleForSummary_whenNotAddonAndTitleEmpty_emitEmptyString(){
        setUpEnvironment()

        val project = ProjectFactory.project()
        val reward = RewardFactory.reward().toBuilder().title(null).build()
        vm.inputs.configureWith(Pair(project, reward))

        this.titleForSummary.assertValue("")
    }

    @Test
    fun testTitleForSummary_whenNotAddonAndTitlNotNull_emitString() {
        setUpEnvironment()

        val project = ProjectFactory.project()
        val reward = RewardFactory.reward().toBuilder().title("pins").quantity(5).build()
        vm.inputs.configureWith(Pair(project, reward))

        this.titleForSummary.assertValue("pins")
    }

    @Test
    fun testAmountForSummary_whenNotAddonAndTitlNotNull_emitString() {
        setUpEnvironment()

        val project = ProjectFactory.project()
        val reward = RewardFactory.reward().toBuilder().minimum(23.0).build()
        vm.inputs.configureWith(Pair(project, reward))

        this.amountForSummary.assertValue("$23")
    }
}