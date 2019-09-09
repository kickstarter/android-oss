package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.*
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class BackingFragmentViewModelTest :  KSRobolectricTestCase() {
    private lateinit var vm: BackingFragmentViewModel.ViewModel

    private val backerNumber = TestSubscriber.create<String>()
    private val cardExpiration = TestSubscriber.create<String>()
    private val cardIsGone = TestSubscriber.create<Boolean>()
    private val cardLastFour = TestSubscriber.create<String>()
    private val cardLogo = TestSubscriber.create<Int>()
    private val pledgeAmount = TestSubscriber.create<CharSequence>()
    private val pledgeDate = TestSubscriber.create<String>()
    private val projectAndReward = TestSubscriber.create<Pair<Project, Reward>>()
    private val receivedCheckboxChecked = TestSubscriber.create<Boolean>()
    private val receivedSectionIsGone = TestSubscriber.create<Boolean>()
    private val shippingAmount = TestSubscriber.create<CharSequence>()
    private val shippingLocation = TestSubscriber.create<String>()
    private val shippingSummaryIsGone = TestSubscriber.create<Boolean>()
    private val showUpdatePledgeSuccess = TestSubscriber.create<Void>()
    private val totalAmount = TestSubscriber.create<CharSequence>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = BackingFragmentViewModel.ViewModel(environment)
        this.vm.outputs.backerNumber().subscribe(this.backerNumber)
        this.vm.outputs.cardExpiration().subscribe(this.cardExpiration)
        this.vm.outputs.cardIsGone().subscribe(this.cardIsGone)
        this.vm.outputs.cardLastFour().subscribe(this.cardLastFour)
        this.vm.outputs.cardLogo().subscribe(this.cardLogo)
        this.vm.outputs.pledgeAmount().map { it.toString() }.subscribe(this.pledgeAmount)
        this.vm.outputs.pledgeDate().subscribe(this.pledgeDate)
        this.vm.outputs.projectAndReward().subscribe(this.projectAndReward)
        this.vm.outputs.receivedCheckboxChecked().subscribe(this.receivedCheckboxChecked)
        this.vm.outputs.receivedSectionIsGone().subscribe(this.receivedSectionIsGone)
        this.vm.outputs.shippingAmount().map { it.toString() }.subscribe(this.shippingAmount)
        this.vm.outputs.shippingLocation().subscribe(this.shippingLocation)
        this.vm.outputs.shippingSummaryIsGone().subscribe(this.shippingSummaryIsGone)
        this.vm.outputs.showUpdatePledgeSuccess().subscribe(this.showUpdatePledgeSuccess)
        this.vm.outputs.totalAmount().map { it.toString() }.subscribe(this.totalAmount)
    }

    @Test
    fun testBackerNumber() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .sequence(15L)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.backerNumber.assertValue("15")
    }

    @Test
    fun testCardExpiration() {
        val expiration = DateTime.parse("2019-08-28T18:34:27+00:00").toDate()
        val paymentSource = PaymentSourceFactory.visa()
                .toBuilder()
                .expirationDate(expiration)
                .build()
        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(paymentSource)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.cardExpiration.assertValue("08/2019")
    }

    @Test
    fun testCardIsGone_whenCreditCard() {
        val paymentSource = PaymentSourceFactory.visa()
        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(paymentSource)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.cardIsGone.assertValue(false)
    }

    @Test
    fun testCardIsGone_whenNotCreditCard() {
        val paymentSource = PaymentSourceFactory.googlePay()
        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(paymentSource)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.cardIsGone.assertValue(true)
    }

    @Test
    fun testCardLogo() {
        val paymentSource = PaymentSourceFactory.visa()
        val backing = BackingFactory.backing()
                .toBuilder()
                .paymentSource(paymentSource)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.cardLogo.assertValue(R.drawable.visa_md)
    }

    @Test
    fun testPledgeAmount() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(50.0)
                .shippingAmount(10f)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.pledgeAmount.assertValue("$40")
    }

    @Test
    fun testPledgeDate() {
        val pledgeDate = DateTime.parse("2019-08-28T18:34:27+00:00")
        val backing = BackingFactory.backing()
                .toBuilder()
                .pledgedAt(pledgeDate)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.pledgeDate.assertValue("August 28, 2019")
    }

    @Test
    fun testProjectAndReward() {
        val reward = RewardFactory.reward()
        val backing = BackingFactory.backing()
                .toBuilder()
                .rewardId(reward.id())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .rewards(listOf(RewardFactory.noReward(), reward))
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.projectAndReward.assertValue(Pair(backedProject, reward))
    }

    @Test
    fun testReceivedCheckboxChecked_whenReceived() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .backerCompletedAt(DateTime.now())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.receivedCheckboxChecked.assertValue(true)
    }

    @Test
    fun testReceivedCheckboxChecked_whenNotReceived() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .backerCompletedAt(null)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.receivedCheckboxChecked.assertValue(false)
    }

    @Test
    fun testReceivedSectionIsGone_whenBackingIsNotCollected() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .status(Backing.STATUS_PLEDGED)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.receivedSectionIsGone.assertValue(true)
    }

    @Test
    fun testReceivedSectionIsGone_whenBackingIsCollected_actualReward() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .rewardId(3L)
                .status(Backing.STATUS_COLLECTED)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.receivedSectionIsGone.assertValue(false)
    }

    @Test
    fun testReceivedSectionIsGone_whenBackingIsCollected_noReward() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .rewardId(null)
                .status(Backing.STATUS_COLLECTED)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.receivedSectionIsGone.assertValue(true)
    }

    @Test
    fun testShippingAmount() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .shippingAmount(3f)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.shippingAmount.assertValue("$3")
    }

    @Test
    fun testShippingLocation() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .locationName(LocationFactory.nigeria().displayableName())
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.shippingLocation.assertValue("Nigeria")
    }

    @Test
    fun testShippingSummaryIsGone_whenDigitalReward() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .locationId(null)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.shippingSummaryIsGone.assertValue(true)
    }

    @Test
    fun testShippingSummaryIsGone_whenShippableReward() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .locationId(4L)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.shippingSummaryIsGone.assertValue(false)
    }

    @Test
    fun testShowUpdatePledgeSuccess() {
        setUpEnvironment(environment())

        this.vm.inputs.pledgeSuccessfullyUpdated()
        this.showUpdatePledgeSuccess.assertValueCount(1)
    }

    @Test
    fun testTotalAmount() {
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(10.50)
                .build()
        val backedProject = ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.totalAmount.assertValue("$10.50")
    }

}
