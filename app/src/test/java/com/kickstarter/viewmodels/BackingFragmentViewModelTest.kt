package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.mock.factories.*
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeStatusData
import com.stripe.android.model.Card
import org.joda.time.DateTime
import org.junit.Test
import rx.observers.TestSubscriber

class BackingFragmentViewModelTest :  KSRobolectricTestCase() {
    private lateinit var vm: BackingFragmentViewModel.ViewModel

    private val backerAvatar = TestSubscriber.create<String>()
    private val backerName = TestSubscriber.create<String>()
    private val backerNumber = TestSubscriber.create<String>()
    private val cardExpiration = TestSubscriber.create<String>()
    private val cardIssuer = TestSubscriber.create<Either<String, Int>>()
    private val cardLastFour = TestSubscriber.create<String>()
    private val cardLogo = TestSubscriber.create<Int>()
    private val paymentMethodIsGone = TestSubscriber.create<Boolean>()
    private val pledgeAmount = TestSubscriber.create<CharSequence>()
    private val pledgeDate = TestSubscriber.create<String>()
    private val pledgeStatusData = TestSubscriber.create<PledgeStatusData>()
    private val pledgeSummaryIsGone = TestSubscriber.create<Boolean>()
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
        this.vm.outputs.backerAvatar().subscribe(this.backerAvatar)
        this.vm.outputs.backerName().subscribe(this.backerName)
        this.vm.outputs.backerNumber().subscribe(this.backerNumber)
        this.vm.outputs.cardExpiration().subscribe(this.cardExpiration)
        this.vm.outputs.cardIssuer().subscribe(this.cardIssuer)
        this.vm.outputs.cardLastFour().subscribe(this.cardLastFour)
        this.vm.outputs.cardLogo().subscribe(this.cardLogo)
        this.vm.outputs.paymentMethodIsGone().subscribe(this.paymentMethodIsGone)
        this.vm.outputs.pledgeAmount().map { it.toString() }.subscribe(this.pledgeAmount)
        this.vm.outputs.pledgeDate().subscribe(this.pledgeDate)
        this.vm.outputs.pledgeStatusData().subscribe(this.pledgeStatusData)
        this.vm.outputs.pledgeSummaryIsGone().subscribe(this.pledgeSummaryIsGone)
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
    fun testBackerAvatar() {
        val user = UserFactory.user()
                .toBuilder()
                .avatar(AvatarFactory.avatar("www.avatars.com/"))
                .build()
        val environment = environment()
                .toBuilder()
                .currentUser(MockCurrentUser(user))
                .build()
        setUpEnvironment(environment)

        this.vm.inputs.project(ProjectFactory.backedProject())
        this.backerAvatar.assertValue("www.avatars.com/medium.jpg")
    }

    @Test
    fun testBackerName() {
        val user = UserFactory.user()
                .toBuilder()
                .name("Nathan Squid")
                .build()
        val environment = environment()
                .toBuilder()
                .currentUser(MockCurrentUser(user))
                .build()
        setUpEnvironment(environment)

        this.vm.inputs.project(ProjectFactory.backedProject())
        this.backerName.assertValue("Nathan Squid")
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
    fun testPaymentMethodIsGone_whenApplePay() {
        val paymentSource = PaymentSourceFactory.applePay()
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
        this.paymentMethodIsGone.assertValue(false)
    }

    @Test
    fun testPaymentMethodIsGone_whenGooglePay() {
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
        this.paymentMethodIsGone.assertValue(false)
    }

    @Test
    fun testPaymentMethodIsGone_whenCreditCard() {
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
        this.paymentMethodIsGone.assertValue(false)
    }

    @Test
    fun testPaymentMethodIsGone_whenNotCardType() {
        val paymentSource = PaymentSourceFactory.bankAccount()
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
        this.paymentMethodIsGone.assertValue(true)
    }

    @Test
    fun testCardLogo_whenApplePay() {
        val paymentSource = PaymentSourceFactory.applePay()
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
        this.cardLogo.assertValue(R.drawable.apple_pay_mark)
    }

    @Test
    fun testCardLogo_whenGooglePay() {
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
        this.cardLogo.assertValue(R.drawable.google_pay_mark)
    }

    @Test
    fun testCardLogo_whenCreditCard() {
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
    fun testCardIssuer_whenApplePay() {
        val paymentSource = PaymentSourceFactory.applePay()
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
        this.cardIssuer.assertValue(Either.Right(R.string.apple_pay_content_description))
    }

    @Test
    fun testCardIssuer_whenGooglePay() {
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
        this.cardIssuer.assertValue(Either.Right(R.string.googlepay_button_content_description))
    }

    @Test
    fun testCardIssuer_whenCreditCard() {
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
        this.cardIssuer.assertValue(Either.Left(Card.CardBrand.VISA))
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
    fun testPledgeStatusData_whenProjectIsCanceled() {
        val backedProject = backedProjectWithBackingStatus(Backing.STATUS_PLEDGED)
                .toBuilder()
                .state(Project.STATE_CANCELED)
                .deadline(DateTime.parse("2019-11-11T17:10:04+00:00"))
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.pledgeStatusData.assertValue(PledgeStatusData(R.string.The_creator_canceled_this_project_so_your_payment_method_was_never_charged,
                "$20", "November 11, 2019"))
    }

    @Test
    fun testPledgeStatusData_whenProjectIsUnsuccessful() {
        val backedProject = backedProjectWithBackingStatus(Backing.STATUS_PLEDGED)
                .toBuilder()
                .state(Project.STATE_FAILED)
                .deadline(DateTime.parse("2019-11-11T17:10:04+00:00"))
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.pledgeStatusData.assertValue(PledgeStatusData(R.string.This_project_didnt_reach_its_funding_goal_so_your_payment_method_was_never_charged,
                "$20", "November 11, 2019"))
    }

    @Test
    fun testPledgeStatusData_whenBackingIsCanceled() {
        val backedProject = backedProjectWithBackingStatus(Backing.STATUS_CANCELED)

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.pledgeStatusData.assertValue(PledgeStatusData(R.string.We_collected_your_pledge_for_this_project,
                "$20", "November 11, 2019"))
    }

    @Test
    fun testPledgeStatusData_whenBackingIsCollected() {
        val backedProject = backedProjectWithBackingStatus(Backing.STATUS_COLLECTED)
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.pledgeStatusData.assertValue(PledgeStatusData(R.string.You_canceled_your_pledge_for_this_project,
                "$20", "November 11, 2019"))
    }

    @Test
    fun testPledgeStatusData_whenBackingIsDropped() {
        val backedProject = backedProjectWithBackingStatus(Backing.STATUS_DROPPED)
                .toBuilder()
                .state(Project.STATE_SUCCESSFUL)
                .build()

        this.vm.inputs.project(backedProject)
        this.pledgeStatusData.assertValue(PledgeStatusData(R.string.Your_pledge_was_dropped_because_of_payment_errors,
                "$20", "November 11, 2019"))
    }

    @Test
    fun testPledgeStatusData_whenBackingIsPledged() {
        val backedProject = backedProjectWithBackingStatus(Backing.STATUS_PLEDGED)

        setUpEnvironment(environment())

        this.vm.inputs.project(backedProject)
        this.pledgeStatusData.assertValue(PledgeStatusData(R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline,
                "$20", "November 11, 2019"))
    }

    @Test
    fun testPledgeSummaryIsGone_whenLocationId_isNull() {
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
        this.pledgeSummaryIsGone.assertValue(true)
    }

    @Test
    fun testPledgeSummaryIsGone_whenLocationId_isNotNull() {
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
        this.pledgeSummaryIsGone.assertValue(false)
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
    fun testShippingSummaryIsGone_whenLocationId_isNull() {
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
    fun testShippingSummaryIsGone_whenLocationId_isNotNull() {
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

    private fun backedProjectWithBackingStatus(@Backing.Status backingStatus: String): Project {
        val backing = BackingFactory.backing()
                .toBuilder()
                .amount(20.0)
                .status(backingStatus)
                .build()
        return ProjectFactory.backedProject()
                .toBuilder()
                .backing(backing)
                .build()
    }

}
