package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.R
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUser
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.mock.factories.*
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.data.PledgeStatusData
import com.kickstarter.ui.data.ProjectData
import com.stripe.android.model.Card
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.math.RoundingMode

class BackingFragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: BackingFragmentViewModel.ViewModel

    private val backerAvatar = TestSubscriber.create<String>()
    private val backerName = TestSubscriber.create<String>()
    private val backerNumber = TestSubscriber.create<String>()
    private val cardExpiration = TestSubscriber.create<String>()
    private val cardIssuer = TestSubscriber.create<Either<String, Int>>()
    private val cardLastFour = TestSubscriber.create<String>()
    private val cardLogo = TestSubscriber.create<Int>()
    private val fixPaymentMethodButtonIsGone = TestSubscriber.create<Boolean>()
    private val fixPaymentMethodMessageIsGone = TestSubscriber.create<Boolean>()
    private val notifyDelegateToRefreshProject = TestSubscriber.create<Void>()
    private val notifyDelegateToShowFixPledge = TestSubscriber.create<Void>()
    private val paymentMethodIsGone = TestSubscriber.create<Boolean>()
    private val pledgeAmount = TestSubscriber.create<CharSequence>()
    private val pledgeDate = TestSubscriber.create<String>()
    private val pledgeStatusData = TestSubscriber.create<PledgeStatusData>()
    private val pledgeSummaryIsGone = TestSubscriber.create<Boolean>()
    private val projectDataAndReward = TestSubscriber.create<Pair<ProjectData, Reward>>()
    private val receivedCheckboxChecked = TestSubscriber.create<Boolean>()
    private val receivedSectionIsGone = TestSubscriber.create<Boolean>()
    private val receivedSectionCreatorIsGone = TestSubscriber.create<Boolean>()
    private val shippingAmount = TestSubscriber.create<CharSequence>()
    private val shippingLocation = TestSubscriber.create<String>()
    private val shippingSummaryIsGone = TestSubscriber.create<Boolean>()
    private val showUpdatePledgeSuccess = TestSubscriber.create<Void>()
    private val swipeRefresherProgressIsVisible = TestSubscriber.create<Boolean>()
    private val totalAmount = TestSubscriber.create<CharSequence>()
    private val listAddOns = TestSubscriber.create<Pair<ProjectData, List<Reward>>>()
    private val bonusAmount = TestSubscriber.create<CharSequence>()
    private val disclaimerSectionIsGone = TestSubscriber.create<Boolean>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = BackingFragmentViewModel.ViewModel(environment)
        this.vm.outputs.backerAvatar().subscribe(this.backerAvatar)
        this.vm.outputs.backerName().subscribe(this.backerName)
        this.vm.outputs.backerNumber().subscribe(this.backerNumber)
        this.vm.outputs.cardExpiration().subscribe(this.cardExpiration)
        this.vm.outputs.cardIssuer().subscribe(this.cardIssuer)
        this.vm.outputs.cardLastFour().subscribe(this.cardLastFour)
        this.vm.outputs.cardLogo().subscribe(this.cardLogo)
        this.vm.outputs.fixPaymentMethodButtonIsGone().subscribe(this.fixPaymentMethodButtonIsGone)
        this.vm.outputs.fixPaymentMethodMessageIsGone().subscribe(this.fixPaymentMethodMessageIsGone)
        this.vm.outputs.notifyDelegateToRefreshProject().subscribe(this.notifyDelegateToRefreshProject)
        this.vm.outputs.notifyDelegateToShowFixPledge().subscribe(this.notifyDelegateToShowFixPledge)
        this.vm.outputs.paymentMethodIsGone().subscribe(this.paymentMethodIsGone)
        this.vm.outputs.pledgeAmount().map { it.toString() }.subscribe(this.pledgeAmount)
        this.vm.outputs.pledgeDate().subscribe(this.pledgeDate)
        this.vm.outputs.pledgeStatusData().subscribe(this.pledgeStatusData)
        this.vm.outputs.pledgeSummaryIsGone().subscribe(this.pledgeSummaryIsGone)
        this.vm.outputs.projectDataAndReward().subscribe(this.projectDataAndReward)
        this.vm.outputs.receivedCheckboxChecked().subscribe(this.receivedCheckboxChecked)
        this.vm.outputs.receivedSectionIsGone().subscribe(this.receivedSectionIsGone)
        this.vm.outputs.receivedSectionCreatorIsGone().subscribe(this.receivedSectionCreatorIsGone)
        this.vm.outputs.shippingAmount().map { it.toString() }.subscribe(this.shippingAmount)
        this.vm.outputs.shippingLocation().subscribe(this.shippingLocation)
        this.vm.outputs.shippingSummaryIsGone().subscribe(this.shippingSummaryIsGone)
        this.vm.outputs.showUpdatePledgeSuccess().subscribe(this.showUpdatePledgeSuccess)
        this.vm.outputs.swipeRefresherProgressIsVisible().subscribe(this.swipeRefresherProgressIsVisible)
        this.vm.outputs.totalAmount().map { it.toString() }.subscribe(this.totalAmount)
        this.vm.outputs.projectDataAndAddOns().subscribe(this.listAddOns)
        this.vm.outputs.bonusSupport().map { it.toString() }.subscribe(this.bonusAmount)
        this.vm.outputs.deliveryDisclaimerSectionIsGone().subscribe(this.disclaimerSectionIsGone)
    }

    @Test
    fun testBackerAvatar() {
        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(BackingFactory.backing()))
            .build()

        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.backerAvatar.assertValue("www.avatars.com/medium.jpg")
    }

    @Test
    fun testBackerName() {
        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(BackingFactory.backing()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.backerName.assertValue("Pikachu")
    }

    @Test
    fun testBackingObjectNullFields() {
        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(BackingFactory.backingNull()))
            .build()
        setUpEnvironment(environment)
        val project = ProjectFactory.backedProject().toBuilder().backing(BackingFactory.backingNull()).build()
        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        this.backerName.assertNoValues()
        this.backerAvatar.assertNoValues()
        this.cardExpiration.assertNoValues()
        this.cardLogo.assertNoValues()
        this.cardLastFour.assertNoValues()
        this.cardIssuer.assertNoValues()
        this.pledgeAmount.assertValue("$0")
        this.bonusAmount.assertValue("$0")
        this.shippingAmount.assertValue("$0")
        this.shippingLocation.assertNoValues()
    }

    @Test
    fun testBackerNumber() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .sequence(15L)
            .build()

        val environment = environment().toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

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

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.cardExpiration.assertValue("08/2019")
    }

    @Test
    fun testPaymentMethodIsGone_whenNull() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(null)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.paymentMethodIsGone.assertValue(true)
    }

    @Test
    fun testPaymentMethodIsGone_whenApplePay() {
        val paymentSource = PaymentSourceFactory.applePay()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.paymentMethodIsGone.assertValue(false)
    }

    @Test
    fun testPaymentMethodIsGone_whenGooglePay() {
        val paymentSource = PaymentSourceFactory.googlePay()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.paymentMethodIsGone.assertValue(false)
    }

    @Test
    fun testPaymentMethodIsGone_whenCreditCard() {
        val paymentSource = PaymentSourceFactory.visa()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.paymentMethodIsGone.assertValue(false)
    }

    @Test
    fun testPaymentMethodIsGone_whenNotCardType() {
        val paymentSource = PaymentSourceFactory.bankAccount()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.paymentMethodIsGone.assertValue(true)
    }

    @Test
    fun testCardLogo_whenApplePay() {
        val paymentSource = PaymentSourceFactory.applePay()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.cardLogo.assertValue(R.drawable.apple_pay_mark)
    }

    @Test
    fun testCardLogo_whenGooglePay() {
        val paymentSource = PaymentSourceFactory.googlePay()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.cardLogo.assertValue(R.drawable.google_pay_mark)
    }

    @Test
    fun testCardLogo_whenCreditCard() {
        val paymentSource = PaymentSourceFactory.visa()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.cardLogo.assertValue(R.drawable.visa_md)
    }

    @Test
    fun testCardIssuer_whenApplePay() {
        val paymentSource = PaymentSourceFactory.applePay()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.cardIssuer.assertValue(Either.Right(R.string.apple_pay_content_description))
    }

    @Test
    fun testCardIssuer_whenGooglePay() {
        val paymentSource = PaymentSourceFactory.googlePay()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.cardIssuer.assertValue(Either.Right(R.string.googlepay_button_content_description))
    }

    @Test
    fun testCardIssuer_whenCreditCard() {
        val paymentSource = PaymentSourceFactory.visa()
        val backing = BackingFactory.backing()
            .toBuilder()
            .paymentSource(paymentSource)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.cardIssuer.assertValue(Either.Left(Card.CardBrand.VISA))
    }

    @Test
    fun testFixPaymentMethodButtonIsGone_whenBackingIsErrored() {
        val backing = backingWithStatus(Backing.STATUS_ERRORED)
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(DateTime.parse("2019-11-11T17:10:04+00:00"))
            .state(Project.STATE_SUCCESSFUL)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.fixPaymentMethodButtonIsGone.assertValue(false)
    }

    @Test
    fun testFixPaymentMethodButtonIsGone_whenBackingIsNotErrored() {
        val backing = backingWithStatus(Backing.STATUS_COLLECTED)
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(DateTime.parse("2019-11-11T17:10:04+00:00"))
            .state(Project.STATE_SUCCESSFUL)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.fixPaymentMethodButtonIsGone.assertValue(true)
    }

    @Test
    fun testFixPaymentMethodMessageIsGone_whenBackingIsErrored() {
        val backing = backingWithStatus(Backing.STATUS_ERRORED)
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(DateTime.parse("2019-11-11T17:10:04+00:00"))
            .state(Project.STATE_SUCCESSFUL)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.fixPaymentMethodMessageIsGone.assertValue(false)
    }

    @Test
    fun testFixPaymentMethodMessageIsGone_whenBackingIsNotErrored() {
        val backing = backingWithStatus(Backing.STATUS_COLLECTED)
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(DateTime.parse("2019-11-11T17:10:04+00:00"))
            .state(Project.STATE_SUCCESSFUL)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.fixPaymentMethodMessageIsGone.assertValue(true)
    }

    @Test
    fun testNotifyDelegateToRefreshProject() {
        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(BackingFactory.backing()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.vm.inputs.refreshProject()
        this.notifyDelegateToRefreshProject.assertValueCount(1)
    }

    @Test
    fun testNotifyDelegateToShowFixPledge() {
        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(BackingFactory.backing()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.vm.inputs.fixPaymentMethodButtonClicked()
        this.notifyDelegateToShowFixPledge.assertValueCount(1)
    }

    @Test
    fun testPledgeAmount() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .amount(50.0)
            .shippingAmount(10f)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        val backedProject = ProjectFactory.backedProject()
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.pledgeAmount.assertValue(expectedCurrency(environment, backedProject, 40.0))
    }

    @Test
    fun testPledgeDate() {
        val pledgeDate = DateTime.parse("2019-08-28T18:34:27+00:00")
        val backing = BackingFactory.backing()
            .toBuilder()
            .pledgedAt(pledgeDate)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.pledgeDate.assertValue(DateTimeUtils.longDate(pledgeDate))
    }

    @Test
    fun testPledgeDateNull() {
        val pledgeDate = null
        val backing = BackingFactory.backing()
            .toBuilder()
            .pledgedAt(pledgeDate)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.pledgeDate.assertNoValues()
    }

    @Test
    fun testPledgeStatusData_whenProjectIsCanceled() {
        val backing = backingWithStatus(Backing.STATUS_PLEDGED)
        val deadline = DateTime.parse("2019-11-11T17:10:04+00:00")
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_CANCELED)
            .deadline(deadline)
            .build()

        val environment = environment()
            .toBuilder()
            .currentUser(MockCurrentUser(UserFactory.user()))
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.pledgeStatusData.assertValue(
            PledgeStatusData(
                R.string.The_creator_canceled_this_project_so_your_payment_method_was_never_charged,
                expectedCurrency(environment, backedProject, 20.0),
                DateTimeUtils.longDate(deadline)
            )
        )
    }

    @Test
    fun testPledgeStatusData_whenProjectIsUnsuccessful() {
        val backing = backingWithStatus(Backing.STATUS_PLEDGED)
        val deadline = DateTime.parse("2019-11-11T17:10:04+00:00")
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .state(Project.STATE_FAILED)
            .deadline(deadline)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .currentUser(MockCurrentUser(UserFactory.user()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.pledgeStatusData.assertValue(
            PledgeStatusData(
                R.string.This_project_didnt_reach_its_funding_goal_so_your_payment_method_was_never_charged,
                expectedCurrency(environment, backedProject, 20.0),
                DateTimeUtils.longDate(deadline)
            )
        )
    }

    @Test
    fun testPledgeStatusData_whenBackingIsCanceled() {
        val backing = backingWithStatus(Backing.STATUS_CANCELED)
        val deadline = DateTime.parse("2019-11-11T17:10:04+00:00")
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(deadline)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .currentUser(MockCurrentUser(UserFactory.user()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.pledgeStatusData.assertValue(
            PledgeStatusData(
                R.string.You_canceled_your_pledge_for_this_project,
                expectedCurrency(environment, backedProject, 20.0),
                DateTimeUtils.longDate(deadline)
            )
        )
    }

    @Test
    fun testPledgeStatusData_whenBackingIsCollected() {
        val backing = backingWithStatus(Backing.STATUS_COLLECTED)
        val deadline = DateTime.parse("2019-11-11T17:10:04+00:00")
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(deadline)
            .state(Project.STATE_SUCCESSFUL)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .currentUser(MockCurrentUser(UserFactory.user()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.pledgeStatusData.assertValue(
            PledgeStatusData(
                R.string.We_collected_your_pledge_for_this_project,
                expectedCurrency(environment, backedProject, 20.0),
                DateTimeUtils.longDate(deadline)
            )
        )
    }

    @Test
    fun testPledgeStatusData_whenBackingIsDropped() {
        val backing = backingWithStatus(Backing.STATUS_DROPPED)
        val deadline = DateTime.parse("2019-11-11T17:10:04+00:00")
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(deadline)
            .state(Project.STATE_SUCCESSFUL)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .currentUser(MockCurrentUser(UserFactory.user()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.pledgeStatusData.assertValue(
            PledgeStatusData(
                R.string.Your_pledge_was_dropped_because_of_payment_errors,
                expectedCurrency(environment, backedProject, 20.0),
                DateTimeUtils.longDate(deadline)
            )
        )
    }

    @Test
    fun testPledgeStatusData_whenBackingIsErrored() {
        val backing = backingWithStatus(Backing.STATUS_ERRORED)
        val deadline = DateTime.parse("2019-11-11T17:10:04+00:00")
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(deadline)
            .state(Project.STATE_SUCCESSFUL)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .currentUser(MockCurrentUser(UserFactory.user()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.pledgeStatusData.assertValue(
            PledgeStatusData(
                R.string.We_cant_process_your_pledge_Please_update_your_payment_method,
                expectedCurrency(environment, backedProject, 20.0),
                DateTimeUtils.longDate(deadline)
            )
        )
    }

    @Test
    fun testPledgeStatusData_whenBackingIsPledged() {
        val backing = backingWithStatus(Backing.STATUS_PLEDGED)
        val deadline = DateTime.parse("2019-11-11T17:10:04+00:00")
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(deadline)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .currentUser(MockCurrentUser(UserFactory.user()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.pledgeStatusData.assertValue(
            PledgeStatusData(
                R.string.If_the_project_reaches_its_funding_goal_you_will_be_charged_total_on_project_deadline,
                expectedCurrency(environment, backedProject, 20.0),
                DateTimeUtils.longDate(deadline)
            )
        )
    }

    @Test
    fun testPledgeStatusData_whenBackingIsPreAuth() {
        val backing = backingWithStatus(Backing.STATUS_PREAUTH)
        val deadline = DateTime.parse("2019-11-11T17:10:04+00:00")
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .deadline(deadline)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .currentUser(MockCurrentUser(UserFactory.user()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.pledgeStatusData.assertValue(
            PledgeStatusData(
                R.string.We_re_processing_your_pledge_pull_to_refresh,
                expectedCurrency(environment, backedProject, 20.0),
                DateTimeUtils.longDate(deadline)
            )
        )
    }

    @Test
    fun testPledgeSummaryIsGone_whenReward_isNull() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .reward(null)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.pledgeSummaryIsGone.assertValue(true)
    }

    @Test
    fun testPledgeSummaryIsGone_whenReward_isNotNull() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .reward(RewardFactory.rewardWithShipping())
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.pledgeSummaryIsGone.assertValue(false)
    }

    @Test
    fun testProjectDataAndReward() {
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

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)

        val projectData = ProjectDataFactory.project(backedProject)
        this.vm.inputs.configureWith(projectData)
        this.projectDataAndReward.assertValue(Pair(projectData, reward))
    }

    @Test
    fun testProjectAndRewardAsCreator() {
        val reward = RewardFactory.reward()
        val backer = UserFactory.user()

        val backing = BackingFactory.backing(backer)
            .toBuilder()
            .reward(reward)
            .rewardId(reward.id())
            .build()

        val creator = UserFactory
            .creator()

        val project = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .backing(backing)
            .rewards(listOf(RewardFactory.noReward(), reward))
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)

        val projectData = ProjectData.builder()
            .project(project)
            .backing(backing)
            .user(creator)
            .build()

        this.vm.inputs.configureWith(projectData)
        this.vm.outputs.projectDataAndReward()
            .subscribe {
                TestCase.assertEquals(it.first, projectData)
                TestCase.assertEquals(it.second, reward)
            }
    }

    @Test
    fun testProjectAndRewardNoCreatorNoBacker() {
        val reward = RewardFactory.reward()

        val project = ProjectFactory.project()
            .toBuilder()
            .rewards(listOf(RewardFactory.noReward(), reward))
            .build()

        val environment = environment()
            .toBuilder()
            .build()
        setUpEnvironment(environment)

        val projectData = ProjectDataFactory.project(project)

        this.vm.inputs.configureWith(projectData)
        this.projectDataAndReward.assertNoValues()
    }

    @Test
    fun testReceivedCheckboxChecked_whenChecked() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .completedByBacker(true)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.receivedCheckboxChecked.assertValue(true)
    }

    @Test
    fun testReceivedCheckboxChecked_whenUnchecked() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .completedByBacker(false)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.receivedCheckboxChecked.assertValue(false)
    }

    @Test
    fun testReceivedCheckboxChecked_whenNotReceived() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .backerCompletedAt(null)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.receivedCheckboxChecked.assertValue(false)
    }

    @Test
    fun testReceivedSectionIsGone_whenBackingIsNotCollectedNotCreator() {
        val user = UserFactory.user()

        val backing = BackingFactory.backing()
            .toBuilder()
            .backer(user)
            .status(Backing.STATUS_PLEDGED)
            .build()

        val currentUser = MockCurrentUser(UserFactory.creator())

        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .apolloClient(mockApolloClientForBacking(backing))
            .build()

        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.receivedSectionIsGone.assertValue(true)
        this.receivedSectionCreatorIsGone.assertValue(true)
    }

    @Test
    fun testReceivedSectionIsGone_whenBackingIsCollectedNoCreator_actualReward() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .rewardId(3L)
            .status(Backing.STATUS_COLLECTED)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.receivedSectionIsGone.assertValue(false)
        this.receivedSectionCreatorIsGone.assertValue(true)
    }

    @Test
    fun testReceivedSectionIsGone_whenBackingIsCollectedAndUserIsCreator_actualReward() {
        val user = UserFactory.creator()

        val project = ProjectFactory.backedProject()
            .toBuilder()
            .creator(user)
            .build()

        val backing = BackingFactory.backing()
            .toBuilder()
            .project(project)
            .rewardId(3L)
            .status(Backing.STATUS_COLLECTED)
            .build()

        val currentUser = MockCurrentUser(user)
        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .apolloClient(mockApolloClientForBacking(backing))
            .build()

        setUpEnvironment(environment)

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        this.receivedSectionIsGone.assertValue(true)
        this.receivedSectionCreatorIsGone.assertValue(false)
    }

    @Test
    fun testReceivedSectionIsGone_whenBackingIsCollectedNoCreator_noReward() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .backer(UserFactory.user())
            .rewardId(null)
            .status(Backing.STATUS_COLLECTED)
            .build()

        val creator = UserFactory.creator()
        val currentUser = MockCurrentUser(creator)

        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)

        val project = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .backing(backing)
            .build()

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        this.receivedSectionCreatorIsGone.assertValue(true)
        this.receivedSectionIsGone.assertValue(true)
    }

    @Test
    fun testDisclaimerSectionIsGone_whenUserIsCreator_isGoneTrue() {
        val creator = UserFactory.creator()
        val currentUser = MockCurrentUser(creator)

        val project = ProjectFactory.project()
            .toBuilder()
            .creator(creator)
            .build()

        val backing = BackingFactory.backing()
            .toBuilder()
            .backer(UserFactory.user())
            .build()

        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        this.disclaimerSectionIsGone.assertValue(true)
    }

    @Test
    fun testDisclaimerSectionIsGone_whenUserIsCreator_isGoneFalse() {
        val user = UserFactory.user()

        val project = ProjectFactory.project()
            .toBuilder()
            .creator(UserFactory.creator())
            .build()

        val backing = BackingFactory.backing()
            .toBuilder()
            .project(project)
            .backer(user)
            .build()

        val currentUser = MockCurrentUser(user)

        val environment = environment()
            .toBuilder()
            .currentUser(currentUser)
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        this.disclaimerSectionIsGone.assertValue(false)
    }

    @Test
    fun testShippingAmount() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .shippingAmount(3f)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.shippingAmount.assertValue("$3")
    }

    @Test
    fun testShippingLocation() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .locationName(LocationFactory.nigeria().displayableName())
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.shippingLocation.assertValue("Nigeria")
    }

    @Test
    fun testShippingSummaryIsGone_whenLocationId_isNull() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .locationId(null)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.shippingSummaryIsGone.assertValue(true)
    }

    @Test
    fun testShippingSummaryIsGone_whenLocationId_isNotNull() {
        val backing = BackingFactory.backing()
            .toBuilder()
            .locationId(4L)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.shippingSummaryIsGone.assertValue(false)
    }

    @Test
    fun testShowUpdatePledgeSuccess() {
        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(BackingFactory.backing()))
            .build()
        setUpEnvironment(environment)
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.vm.inputs.pledgeSuccessfullyUpdated()
        this.showUpdatePledgeSuccess.assertValueCount(1)
    }

    @Test
    fun testSwipeRefresherProgressIsVisible() {
        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(BackingFactory.backing()))
            .build()
        setUpEnvironment(environment)

        // initial project is loaded
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))

        this.vm.inputs.refreshProject()
        this.swipeRefresherProgressIsVisible.assertValue(true)

        // Project is refreshed
        this.vm.inputs.configureWith(ProjectDataFactory.project(ProjectFactory.backedProject()))
        this.swipeRefresherProgressIsVisible.assertValues(true, false)
    }

    @Test
    fun testTotalAmount() {
        val amount = 10.5
        val backing = BackingFactory.backing()
            .toBuilder()
            .amount(amount)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)
        val backedProject = ProjectFactory.backedProject()
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.totalAmount.assertValue(expectedCurrency(environment, backedProject, amount))
    }

    @Test
    fun testRewardWithAddOn() {
        val addOns = RewardFactory.backers().toBuilder().isAddOn(true).quantity(7).build()
        val reward = RewardFactory.reward()

        val backing = BackingFactory.backing()
            .toBuilder()
            .reward(reward)
            .addOns(listOf(addOns))
            .build()

        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(backing)
            .rewards(listOf(RewardFactory.noReward(), reward))
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(object : MockApolloClient() {
                override fun getProjectBacking(slug: String): Observable<Backing> {
                    return Observable.just(backing)
                }
            })
            .build()
        setUpEnvironment(environment)

        val projectData = ProjectDataFactory.project(backedProject)
        this.vm.inputs.configureWith(projectData)

        this.listAddOns.assertValue(Pair(projectData, listOf(addOns)))
    }

    fun testWithBonusSupport() {
        val bonusAmount = 5.0
        val backing = BackingFactory.backing()
            .toBuilder()
            .bonusAmount(bonusAmount)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)

        val backedProject = ProjectFactory.backedProject()
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.bonusAmount.assertValue(expectedCurrency(environment, backedProject, bonusAmount))
    }

    @Test
    fun testWithBonusSupportFormat() {
        val bonusAmount = 50.0
        val backing = BackingFactory.backing()
            .toBuilder()
            .bonusAmount(bonusAmount)
            .build()

        val environment = environment()
            .toBuilder()
            .apolloClient(mockApolloClientForBacking(backing))
            .build()
        setUpEnvironment(environment)

        val backedProject = ProjectFactory.backedProject()
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.bonusAmount.assertValue("$50")
    }

    private fun backingWithStatus(@Backing.Status backingStatus: String): Backing {
        return BackingFactory.backing()
            .toBuilder()
            .amount(20.0)
            .status(backingStatus)
            .build()
    }

    private fun mockApolloClientForBacking(backing: Backing): MockApolloClient {
        return object : MockApolloClient() {
            override fun getProjectBacking(slug: String): Observable<Backing> {
                return Observable.just(backing)
            }
        }
    }

    private fun expectedCurrency(environment: Environment, project: Project, amount: Double): String =
        environment.ksCurrency().format(amount, project, RoundingMode.HALF_UP)
}
