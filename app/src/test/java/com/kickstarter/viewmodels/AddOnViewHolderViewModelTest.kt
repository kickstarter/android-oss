package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.models.RewardsItem
import org.junit.Test
import rx.observers.TestSubscriber

class AddOnViewHolderViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: AddOnViewHolderViewModel.ViewModel

    private val quantityIsGone = TestSubscriber.create<Boolean>()
    private val conversion = TestSubscriber.create<String>()
    private val conversionIsGone = TestSubscriber.create<Boolean>()
    private val descriptionForNoReward = TestSubscriber.create<Int>()
    private val descriptionForReward = TestSubscriber.create<String?>()
    private val rewardItems = TestSubscriber.create<List<RewardsItem>>()
    private val rewardItemsAreGone = TestSubscriber.create<Boolean>()
    private val titleForReward = TestSubscriber.create<String>()
    private val titleForNoReward = TestSubscriber.create<Int>()
    private val titleForAddOn = TestSubscriber.create<Pair<String, Int>>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = AddOnViewHolderViewModel.ViewModel(environment)
        this.vm.outputs.isAddonTitleGone().subscribe(this.quantityIsGone)
        this.vm.outputs.conversion().subscribe(this.conversion)
        this.vm.outputs.conversionIsGone().subscribe(this.conversionIsGone)
        this.vm.outputs.descriptionForNoReward().subscribe(this.descriptionForNoReward)
        this.vm.outputs.descriptionForReward().subscribe(this.descriptionForReward)
        this.vm.outputs.rewardItems().subscribe(this.rewardItems)
        this.vm.outputs.rewardItemsAreGone().subscribe(this.rewardItemsAreGone)
        this.vm.outputs.titleForNoReward().subscribe(this.titleForNoReward)
        this.vm.outputs.titleForReward().subscribe(this.titleForReward)
        this.vm.outputs.titleForAddOn().subscribe(this.titleForAddOn)
    }

    @Test
    fun testRewardWithoutAddon() {

    }

    @Test
    fun testRewardWithOneAddOn() {

    }

    @Test
    fun testRewardWithMultipleAddons() {

    }

    @Test
    fun testRewardAndAddOnWithItems() {

    }

    @Test
    fun testRewardNoRewardNoAddOns() {

    }

    @Test
    fun testRewardWithAddOnsAndQuantity() {

    }

    @Test
    fun testCurrencyIsGone() {

    }
}
