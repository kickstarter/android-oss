package com.kickstarter.viewmodels

import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.R
import com.kickstarter.libs.Either
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSString
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.rx.transformers.Transformers.takePairWhenV2
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProjectViewUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.backedReward
import com.kickstarter.libs.utils.extensions.isErrored
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.libs.utils.extensions.userIsCreator
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.PaymentSource
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.models.User
import com.kickstarter.models.extensions.getCardTypeDrawable
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.type.CreditCardTypes
import com.kickstarter.ui.data.PledgeStatusData
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.BackingFragment
import com.stripe.android.model.Card
import com.stripe.android.model.CardBrand
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

interface BackingFragmentViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)

        /** Call when the fix payment method button is clicked. */
        fun fixPaymentMethodButtonClicked()

        /** Call when the pledge has been successfully updated. */
        fun pledgeSuccessfullyUpdated()

        /** Call when the mark as received checkbox is checked. */
        fun receivedCheckboxToggled(checked: Boolean)

        /** Call when the swipe refresh layout is triggered. */
        fun refreshProject()

        fun isExpanded(state: Boolean?)
    }

    interface Outputs {
        /** Emits the backer's avatar URL. */
        fun backerAvatar(): Observable<String>

        /** Emits the backer's name. */
        fun backerName(): Observable<String>

        /** Emits the backer's sequence. */
        fun backerNumber(): Observable<String>

        /** Emits the expiration of the backing's card. */
        fun cardExpiration(): Observable<String>

        /** Emits the name of the card issuer from [Card.CardBrand] or Google Pay or Apple Pay string resources. */
        fun cardIssuer(): Observable<Either<String, Int>>

        /** Emits the last four digits of the backing's card. */
        fun cardLastFour(): Observable<String>

        /** Emits the card brand drawable to display. */
        fun cardLogo(): Observable<Int>

        /** Emits a boolean determining if the fix payment method button should be visible. */
        fun fixPaymentMethodButtonIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the fix payment method message should be visible. */
        fun fixPaymentMethodMessageIsGone(): Observable<Boolean>

        /** Emits when we should notify the [BackingFragment.BackingDelegate] to refresh the project. */
        fun notifyDelegateToRefreshProject(): Observable<Unit>

        /** Call when the [BackingFragment.BackingDelegate] should be notified to show the fix pledge flow. */
        fun notifyDelegateToShowFixPledge(): Observable<Unit>

        /** Emits a boolean determining if the payment method section should be visible. */
        fun paymentMethodIsGone(): Observable<Boolean>

        /** Emits the amount pledged minus the shipping. */
        fun pledgeAmount(): Observable<CharSequence>

        /** Emits the date the backing was pledged on. */
        fun pledgeDate(): Observable<String>

        /** Emits the string resource ID that best represents the pledge status and associated data. */
        fun pledgeStatusData(): Observable<PledgeStatusData>

        /** Emits a boolean determining if the pledge summary should be visible. */
        fun pledgeSummaryIsGone(): Observable<Boolean>

        /** Emits the [ProjectData] and currently backed [Reward]. */
        fun projectDataAndReward(): Observable<Pair<ProjectData, Reward>>

        /** Emits the [ProjectData] and currently selected AddOns: [List<Reward>]. */
        fun projectDataAndAddOns(): Observable<Pair<ProjectData, List<Reward>>>

        /** Emits a boolean that determines if received checkbox should be checked. */
        fun receivedCheckboxChecked(): Observable<Boolean>

        /** Emits a boolean determining if the delivered section should be visible for the backer perspective. */
        fun receivedSectionIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the delivered section should be visible for the creator perspective. */
        fun receivedSectionCreatorIsGone(): Observable<Boolean>

        /** Emits the shipping amount of the backing. */
        fun shippingAmount(): Observable<CharSequence>

        /** Emits the shipping location of the backing. */
        fun shippingLocation(): Observable<String>

        /** Emits a boolean determining if the shipping summary should be visible. */
        fun shippingSummaryIsGone(): Observable<Boolean>

        /** Emits when the backing has successfully been updated. */
        fun showUpdatePledgeSuccess(): Observable<Unit>

        /** Emits a boolean determining if the swipe refresher is visible. */
        fun swipeRefresherProgressIsVisible(): Observable<Boolean>

        /** Emits the total amount pledged. */
        fun totalAmount(): Observable<CharSequence>

        /** Emits the bonus support added to the pledge, if any **/
        fun bonusSupport(): Observable<CharSequence>

        /** Emits the estimated delivery date of this reward **/
        fun estimatedDelivery(): Observable<String>

        /** Emits a boolean determining if the delivery disclaimer section is visible **/
        fun deliveryDisclaimerSectionIsGone(): Observable<Boolean>
    }

    class BackingFragmentViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {

        private val fixPaymentMethodButtonClicked = PublishSubject.create<Unit>()
        private val pledgeSuccessfullyCancelled = PublishSubject.create<Unit>()
        private val projectDataInput = PublishSubject.create<ProjectData>()
        private val receivedCheckboxToggled = PublishSubject.create<Boolean>()
        private val refreshProject = PublishSubject.create<Unit>()
        private val isExpanded = PublishSubject.create<Boolean>()

        private val backerAvatar = BehaviorSubject.create<String>()
        private val backerName = BehaviorSubject.create<String>()
        private val backerNumber = BehaviorSubject.create<String>()
        private val cardExpiration = BehaviorSubject.create<String>()
        private val cardIssuer = BehaviorSubject.create<Either<String, Int>>()
        private val cardLastFour = BehaviorSubject.create<String>()
        private val cardLogo = BehaviorSubject.create<Int>()
        private val fixPaymentMethodButtonIsGone = BehaviorSubject.create<Boolean>()
        private val fixPaymentMethodMessageIsGone = BehaviorSubject.create<Boolean>()
        private val notifyDelegateToRefreshProject = PublishSubject.create<Unit>()
        private val notifyDelegateToShowFixPledge = PublishSubject.create<Unit>()
        private val paymentMethodIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeAmount = BehaviorSubject.create<CharSequence>()
        private val pledgeDate = BehaviorSubject.create<String>()
        private val pledgeStatusData = BehaviorSubject.create<PledgeStatusData>()
        private val pledgeSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val projectDataAndReward = BehaviorSubject.create<Pair<ProjectData, Reward>>()
        private val receivedCheckboxChecked = BehaviorSubject.create<Boolean>()
        private val receivedSectionIsGone = BehaviorSubject.create<Boolean>()
        private val receivedSectionCreatorIsGone = BehaviorSubject.create<Boolean>()
        private val shippingAmount = BehaviorSubject.create<CharSequence>()
        private val shippingLocation = BehaviorSubject.create<String>()
        private val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val showUpdatePledgeSuccess = PublishSubject.create<Unit>()
        private val swipeRefresherProgressIsVisible = BehaviorSubject.create<Boolean>()
        private val totalAmount = BehaviorSubject.create<CharSequence>()
        private val addOnsList = BehaviorSubject.create<Pair<ProjectData, List<Reward>>>()
        private val bonusSupport = BehaviorSubject.create<CharSequence>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val deliveryDisclaimerSectionIsGone = BehaviorSubject.create<Boolean>()

        private val apiClient = requireNotNull(this.environment.apiClientV2())
        private val apolloClient = requireNotNull(this.environment.apolloClientV2())
        private val ksCurrency = requireNotNull(this.environment.ksCurrency())
        private val analyticEvents = requireNotNull(this.environment.analytics())
        val ksString: KSString? = this.environment.ksString()
        private val currentUser = requireNotNull(this.environment.currentUserV2())
        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.pledgeSuccessfullyCancelled
                .subscribe { this.showUpdatePledgeSuccess.onNext(it) }
                .addToDisposable(disposables)

            this.projectDataInput
                .filter { it.project().isBacking() || it.project().userIsCreator(it.user()) }
                .map { projectData -> joinProjectDataAndReward(projectData) }
                .subscribe { this.projectDataAndReward.onNext(it) }
                .addToDisposable(disposables)

            val backedProject = this.projectDataInput
                .map { it.project() }

            val backing = this.projectDataInput
                .switchMap { getBackingInfo(it) }
                .compose(neverErrorV2())
                .filter { it.isNotNull() }
                .share()

            val rewardA = backing
                .filter { it.reward().isNotNull() }
                .map { requireNotNull(it.reward()) }

            val rewardB = projectDataAndReward
                .filter { it.second.isNotNull() }
                .map { requireNotNull(it.second) }

            val reward = Observable.merge(rewardA, rewardB)
                .distinctUntilChanged()

            val isCreator = Observable.combineLatest(
                this.currentUser.observable(),
                backedProject
            ) { user, project ->
                Pair(user, project)
            }
                .map { it.second.userIsCreator(it.first.getValue()) }

            backing
                .filter { it.backerName().isNotNull() }
                .map { requireNotNull(it.backerName()) }
                .subscribe { this.backerName.onNext(it) }
                .addToDisposable(disposables)

            backing
                .filter { it.backerUrl().isNotNull() }
                .map { requireNotNull(it.backerUrl()) }
                .subscribe { this.backerAvatar.onNext(it) }
                .addToDisposable(disposables)

            backing
                .map { NumberUtils.format(it.sequence().toFloat()) }
                .distinctUntilChanged()
                .subscribe { this.backerNumber.onNext(it) }
                .addToDisposable(disposables)

            backing
                .filter { it.pledgedAt().isNotNull() }
                .map { DateTimeUtils.longDate(requireNotNull(it.pledgedAt())) }
                .distinctUntilChanged()
                .subscribe { this.pledgeDate.onNext(it) }
                .addToDisposable(disposables)

            backing
                .map { it.amount() - it.shippingAmount() - it.bonusAmount() }
                .filter { it.isNotNull() }
                .compose<Pair<Double, Project>>(combineLatestPair(backedProject))
                .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                .distinctUntilChanged()
                .subscribe { this.pledgeAmount.onNext(it) }
                .addToDisposable(disposables)

            backing
                .map {
                    shouldHideShipping(it)
                }
                .distinctUntilChanged()
                .subscribe {
                    this.shippingSummaryIsGone.onNext(it)
                }.addToDisposable(disposables)

            backing
                .map { it.reward().isNull() }
                .distinctUntilChanged()
                .subscribe {
                    this.pledgeSummaryIsGone.onNext(it)
                }.addToDisposable(disposables)

            Observable.combineLatest(
                backedProject,
                backing,
                this.currentUser.loggedInUser()
            ) { p, b, user -> Triple(p, b, user) }
                .map { pledgeStatusData(it.first, it.second, it.third) }
                .distinctUntilChanged()
                .subscribe { this.pledgeStatusData.onNext(it) }
                .addToDisposable(disposables)

            backing
                .filter { it.shippingAmount().isNotNull() }
                .map { requireNotNull(it.shippingAmount()) }
                .compose<Pair<Float, Project>>(combineLatestPair(backedProject))
                .map {
                    ProjectViewUtils.styleCurrency(
                        it.first.toDouble(),
                        it.second,
                        this.ksCurrency
                    )
                }
                .distinctUntilChanged()
                .subscribe { this.shippingAmount.onNext(it) }
                .addToDisposable(disposables)

            backing
                .filter { it.locationName().isNotNull() }
                .map { requireNotNull(it.locationName()) }
                .distinctUntilChanged()
                .subscribe { this.shippingLocation.onNext(it) }
                .addToDisposable(disposables)

            backing
                .filter { it.amount().isNotNull() }
                .map { it.amount() }
                .compose<Pair<Double, Project>>(combineLatestPair(backedProject))
                .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                .distinctUntilChanged()
                .subscribe { this.totalAmount.onNext(it) }
                .addToDisposable(disposables)

            backing
                .map { CreditCardPaymentType.safeValueOf(it.paymentSource()?.paymentType() ?: "") }
                .map { it == CreditCardPaymentType.ANDROID_PAY || it == CreditCardPaymentType.APPLE_PAY || it == CreditCardPaymentType.CREDIT_CARD }
                .map { it.negate() }
                .distinctUntilChanged()
                .subscribe { this.paymentMethodIsGone.onNext(it) }
                .addToDisposable(disposables)

            val paymentSource = backing
                .filter { it.paymentSource().isNotNull() }
                .map { requireNotNull(it.paymentSource()) }
                .ofType(PaymentSource::class.java)

            val simpleDateFormat = SimpleDateFormat(StoredCard.DATE_FORMAT, Locale.getDefault())

            paymentSource
                .map { source ->
                    source.expirationDate()?.let { simpleDateFormat.format(it) } ?: ""
                }
                .distinctUntilChanged()
                .subscribe { this.cardExpiration.onNext(it) }
                .addToDisposable(disposables)

            paymentSource
                .map { cardIssuer(it) }
                .distinctUntilChanged()
                .subscribe { this.cardIssuer.onNext(it) }
                .addToDisposable(disposables)

            paymentSource
                .map { it.lastFour() ?: "" }
                .distinctUntilChanged()
                .subscribe { this.cardLastFour.onNext(it) }
                .addToDisposable(disposables)

            paymentSource
                .map { cardLogo(it) }
                .distinctUntilChanged()
                .subscribe { this.cardLogo.onNext(it) }
                .addToDisposable(disposables)

            val backingIsNotErrored = backing
                .map { it.isErrored() }
                .distinctUntilChanged()
                .map { it.negate() }

            backingIsNotErrored
                .subscribe { this.fixPaymentMethodButtonIsGone.onNext(it) }
                .addToDisposable(disposables)

            backingIsNotErrored
                .subscribe { this.fixPaymentMethodMessageIsGone.onNext(it) }
                .addToDisposable(disposables)

            this.fixPaymentMethodButtonClicked
                .subscribe { this.notifyDelegateToShowFixPledge.onNext(Unit) }
                .addToDisposable(disposables)

            backing
                .map { it.completedByBacker() }
                .distinctUntilChanged()
                .subscribe { this.receivedCheckboxChecked.onNext(it) }
                .addToDisposable(disposables)

            backing
                .compose<Pair<Backing, Project>>(combineLatestPair(backedProject))
                .compose(takePairWhenV2(this.receivedCheckboxToggled))
                .switchMap {
                    this.apiClient.postBacking(it.first.second, it.first.first, it.second)
                        .compose(neverErrorV2())
                }
                .share()
                .subscribe()

            this.isExpanded
                .filter { it }
                .compose(combineLatestPair(backing))
                .map { it.second }
                .compose<Pair<Backing, ProjectData>>(combineLatestPair(projectDataInput))
                .subscribe {
                    this.analyticEvents.trackManagePledgePageViewed(it.first, it.second)
                }.addToDisposable(disposables)

            val rewardIsReceivable = reward
                .map {
                    RewardUtils.isReward(it) && it.estimatedDeliveryOn().isNotNull()
                }

            val backingIsCollected = backing
                .map { it.status() }
                .map { it == Backing.STATUS_COLLECTED }
                .distinctUntilChanged()

            val sectionShouldBeGone = rewardIsReceivable
                .compose(combineLatestPair<Boolean, Boolean>(backingIsCollected))
                .map { it.first && it.second }
                .map { it.negate() }
                .distinctUntilChanged()

            sectionShouldBeGone
                .compose<Pair<Boolean, Boolean>>(combineLatestPair(isCreator))
                .subscribe {
                    val isUserCreator = it.second
                    val shouldBeGone = it.first

                    if (isUserCreator) {
                        this.receivedSectionIsGone.onNext(true)
                        this.receivedSectionCreatorIsGone.onNext(shouldBeGone)
                    } else {
                        this.receivedSectionIsGone.onNext(shouldBeGone)
                        this.receivedSectionCreatorIsGone.onNext(true)
                    }
                }.addToDisposable(disposables)

            this.refreshProject
                .subscribe {
                    this.notifyDelegateToRefreshProject.onNext(Unit)
                    this.swipeRefresherProgressIsVisible.onNext(true)
                }.addToDisposable(disposables)

            val refreshTimeout = this.notifyDelegateToRefreshProject
                .delay(10, TimeUnit.SECONDS)

            Observable.merge(refreshTimeout, backedProject.skip(1))
                .map { false }
                .subscribe { this.swipeRefresherProgressIsVisible.onNext(it) }
                .addToDisposable(disposables)

            val addOns = backing
                .map { it.addOns()?.toList() ?: emptyList() }

            projectDataInput
                .compose<Pair<ProjectData, List<Reward>>>(combineLatestPair(addOns))
                .subscribe { this.addOnsList.onNext(it) }
                .addToDisposable(disposables)

            backing
                .filter { it.bonusAmount().isNotNull() }
                .map { requireNotNull(it.bonusAmount()) }
                .compose<Pair<Double, Project>>(combineLatestPair(backedProject))
                .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                .distinctUntilChanged()
                .subscribe { this.bonusSupport.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isReward(it) && it.estimatedDeliveryOn().isNotNull() }
                .map<DateTime> { it.estimatedDeliveryOn() }
                .map { DateTimeUtils.estimatedDeliveryOn(it) }
                .subscribe { this.estimatedDelivery.onNext(it) }
                .addToDisposable(disposables)

            isCreator
                .subscribe { this.deliveryDisclaimerSectionIsGone.onNext(it) }
                .addToDisposable(disposables)
        }

        private fun shouldHideShipping(it: Backing) =
            it.locationId().isNull() || it.reward()?.let { rw ->
                RewardUtils.isLocalPickup(rw)
            } ?: true

        private fun getBackingInfo(it: ProjectData): Observable<Backing> {
            return if (it.backing() == null) {
                this.apolloClient.getProjectBacking(it.project().slug() ?: "")
            } else {
                Observable.just(it.backing())
            }
        }

        private fun joinProjectDataAndReward(projectData: ProjectData): Pair<ProjectData, Reward> {
            val reward = projectData.backing()?.reward()
                ?: projectData.project().backing()?.backedReward(projectData.project())
                ?: RewardFactory.noReward().toBuilder()
                    .minimum(projectData.backing()?.amount() ?: 1.0)
                    .build()

            return Pair(projectData, reward)
        }

        private fun cardIssuer(paymentSource: PaymentSource): Either<String, Int> {
            return when (CreditCardPaymentType.safeValueOf(paymentSource.paymentType())) {
                CreditCardPaymentType.ANDROID_PAY -> Either.Right(R.string.googlepay_button_content_description)
                CreditCardPaymentType.APPLE_PAY -> Either.Right(R.string.apple_pay_content_description)
                CreditCardPaymentType.CREDIT_CARD -> Either.Left(StoredCard.issuer(CreditCardTypes.safeValueOf(paymentSource.type() ?: "")))
                else -> Either.Left(CardBrand.Unknown.code)
            }
        }

        private fun cardLogo(paymentSource: PaymentSource): Int {
            return when (CreditCardPaymentType.safeValueOf(paymentSource.paymentType())) {
                CreditCardPaymentType.ANDROID_PAY -> R.drawable.google_pay_mark
                CreditCardPaymentType.APPLE_PAY -> R.drawable.apple_pay_mark
                CreditCardPaymentType.CREDIT_CARD -> paymentSource.getCardTypeDrawable()
                else -> R.drawable.generic_bank_md
            }
        }

        private fun pledgeStatusData(
            project: Project,
            backing: Backing,
            user: User
        ): PledgeStatusData {

            var statusStringRes: Int?

            if (!project.userIsCreator(user)) {
                statusStringRes = when (project.state()) {
                    Project.STATE_CANCELED -> R.string.The_creator_canceled_this_project_so_your_payment_method_was_never_charged
                    Project.STATE_FAILED -> R.string.This_project_didnt_reach_its_funding_goal_so_your_payment_method_was_never_charged
                    else -> when (backing.status()) {
                        Backing.STATUS_CANCELED -> R.string.You_canceled_your_pledge_for_this_project
                        Backing.STATUS_COLLECTED -> R.string.We_collected_your_pledge_for_this_project
                        Backing.STATUS_DROPPED -> R.string.Your_pledge_was_dropped_because_of_payment_errors
                        Backing.STATUS_ERRORED -> R.string.We_cant_process_your_pledge_Please_update_your_payment_method
                        Backing.STATUS_PLEDGED -> {
                            if (project.isPledgeOverTimeAllowed() == true &&
                                environment.featureFlagClient()
                                    ?.getBoolean(FlagKey.ANDROID_PLEDGE_OVER_TIME) == true
                            ) {
                                R.string.fpo_you_have_selected_pledge_over_time_if_the_project_reaches_its_funding_goal_the_first_charge_of
                            } else {
                                R.string.If_your_project_reaches_its_funding_goal_the_backer_will_be_charged_total_on_project_deadline
                            }
                        }

                        Backing.STATUS_PREAUTH -> R.string.We_re_processing_your_pledge_pull_to_refresh
                        else -> null
                    }
                }
            } else {
                statusStringRes = when (project.state()) {
                    Project.STATE_CANCELED -> R.string.You_canceled_this_project_so_the_backers_payment_method_was_never_charged
                    Project.STATE_FAILED -> R.string.Your_project_didnt_reach_its_funding_goal_so_the_backers_payment_method_was_never_charged
                    else -> when (backing.status()) {
                        Backing.STATUS_CANCELED -> R.string.The_backer_canceled_their_pledge_for_this_project
                        Backing.STATUS_COLLECTED -> R.string.We_collected_the_backers_pledge_for_this_project
                        Backing.STATUS_DROPPED -> R.string.This_pledge_was_dropped_because_of_payment_errors
                        Backing.STATUS_ERRORED -> R.string.We_cant_process_this_pledge_because_of_a_problem_with_the_backers_payment_method
                        Backing.STATUS_PLEDGED -> R.string.If_your_project_reaches_its_funding_goal_the_backer_will_be_charged_total_on_project_deadline
                        Backing.STATUS_PREAUTH -> R.string.We_re_processing_this_pledge_pull_to_refresh
                        else -> null
                    }
                }
            }

            val projectDeadline = project.deadline()?.let { DateTimeUtils.longDate(it) }
            val pledgeTotal = backing.amount()
            val pledgeTotalString = this.ksCurrency.format(pledgeTotal, project)

            return PledgeStatusData(statusStringRes, pledgeTotalString, projectDeadline)
        }

        override fun configureWith(projectData: ProjectData) {
            this.projectDataInput.onNext(projectData)
        }

        override fun fixPaymentMethodButtonClicked() {
            this.fixPaymentMethodButtonClicked.onNext(Unit)
        }

        override fun pledgeSuccessfullyUpdated() {
            this.showUpdatePledgeSuccess.onNext(Unit)
        }

        override fun receivedCheckboxToggled(checked: Boolean) {
            this.receivedCheckboxToggled.onNext(checked)
        }

        override fun refreshProject() {
            this.refreshProject.onNext(Unit)
        }

        override fun isExpanded(state: Boolean?) {
            state?.let {
                this.isExpanded.onNext(it)
            }
        }

        override fun backerAvatar(): Observable<String> = this.backerAvatar

        override fun backerName(): Observable<String> = this.backerName

        override fun backerNumber(): Observable<String> = this.backerNumber

        override fun cardExpiration(): Observable<String> = this.cardExpiration

        override fun cardIssuer(): Observable<Either<String, Int>> = this.cardIssuer

        override fun cardLastFour(): Observable<String> = this.cardLastFour

        override fun cardLogo(): Observable<Int> = this.cardLogo

        override fun fixPaymentMethodButtonIsGone(): Observable<Boolean> =
            this.fixPaymentMethodButtonIsGone

        override fun fixPaymentMethodMessageIsGone(): Observable<Boolean> =
            this.fixPaymentMethodMessageIsGone

        override fun notifyDelegateToRefreshProject(): Observable<Unit> =
            this.notifyDelegateToRefreshProject

        override fun notifyDelegateToShowFixPledge(): Observable<Unit> =
            this.notifyDelegateToShowFixPledge

        override fun paymentMethodIsGone(): Observable<Boolean> = this.paymentMethodIsGone

        override fun pledgeAmount(): Observable<CharSequence> = this.pledgeAmount

        override fun pledgeDate(): Observable<String> = this.pledgeDate

        override fun pledgeStatusData(): Observable<PledgeStatusData> = this.pledgeStatusData

        override fun pledgeSummaryIsGone(): Observable<Boolean> = this.pledgeSummaryIsGone

        override fun projectDataAndReward(): Observable<Pair<ProjectData, Reward>> =
            this.projectDataAndReward

        override fun projectDataAndAddOns(): Observable<Pair<ProjectData, List<Reward>>> =
            this.addOnsList

        override fun receivedCheckboxChecked(): Observable<Boolean> = this.receivedCheckboxChecked

        override fun receivedSectionIsGone(): Observable<Boolean> = this.receivedSectionIsGone

        override fun receivedSectionCreatorIsGone(): Observable<Boolean> =
            this.receivedSectionCreatorIsGone

        override fun shippingAmount(): Observable<CharSequence> = this.shippingAmount

        override fun shippingLocation(): Observable<String> = this.shippingLocation

        override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone

        override fun showUpdatePledgeSuccess(): Observable<Unit> = this.showUpdatePledgeSuccess

        override fun swipeRefresherProgressIsVisible(): Observable<Boolean> =
            this.swipeRefresherProgressIsVisible

        override fun totalAmount(): Observable<CharSequence> = this.totalAmount

        override fun bonusSupport(): Observable<CharSequence> = this.bonusSupport

        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        override fun deliveryDisclaimerSectionIsGone(): Observable<Boolean> =
            this.deliveryDisclaimerSectionIsGone

        override fun onCleared() {
            apolloClient.cleanDisposables()
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackingFragmentViewModel(environment) as T
        }
    }
}
