package com.kickstarter.viewmodels

import android.os.Bundle
import android.text.SpannableString
import android.util.Pair
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.R
import com.kickstarter.libs.Config
import com.kickstarter.libs.Environment
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValuesV2
import com.kickstarter.libs.rx.transformers.Transformers.neverErrorV2
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.rx.transformers.Transformers.zipPairV2
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ProjectViewUtils
import com.kickstarter.libs.utils.RefTagUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.ThirdPartyEventValues
import com.kickstarter.libs.utils.extensions.acceptedCardType
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isFalse
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.libs.utils.extensions.parseToDouble
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Checkout
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.models.extensions.getBackingData
import com.kickstarter.models.extensions.isFromPaymentSheet
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.type.CreditCardPaymentType
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.CheckoutData
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.State
import com.kickstarter.viewmodels.usecases.SendThirdPartyEventUseCaseV2
import com.stripe.android.StripeIntentResult
import com.stripe.android.paymentsheet.PaymentSheetResult
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.math.RoundingMode
import java.text.NumberFormat
import kotlin.math.max

interface PledgeFragmentViewModel {
    interface Inputs {
        /** Call when a card has been inserted into the stored cards list. */
        fun addedCardPosition(position: Int)

        /** Call when a card has successfully saved. */
        fun cardSaved(storedCard: StoredCard)

        /** Call when user selects a card they want to pledge with. */
        fun cardSelected(storedCard: StoredCard, position: Int)

        /** Call when logged out user clicks the continue button. */
        fun continueButtonClicked()

        /** Call when user clicks the decrease pledge button. */
        fun decreasePledgeButtonClicked()

        /** Call when user clicks the increase pledge button. */
        fun increasePledgeButtonClicked()

        /** Call when user clicks the decrease bonus button. */
        fun decreaseBonusButtonClicked()

        /** Call when user clicks the increase bonus button. */
        fun increaseBonusButtonClicked()

        /** Call when user clicks a url. */
        fun linkClicked(url: String)

        /** Call when user clicks the mini reward. */
        fun miniRewardClicked()

        /** Call when the new card button is clicked. */
        fun newCardButtonClicked()

        /** Call when the user updates the pledge amount. */
        fun pledgeInput(amount: String)

        /** Call when user clicks the pledge button. */
        fun pledgeButtonClicked()

        /** Call when user selects a shipping location. */
        fun shippingRuleSelected(shippingRule: ShippingRule)

        /** Call when Stripe SCA is successful. */
        fun stripeSetupResultSuccessful(outcome: Int)

        /** Call when Stripe SCA is unsuccessful. */
        fun stripeSetupResultUnsuccessful(exception: Exception)

        fun onRiskMessageDismissed()

        fun paymentSheetResult(paymentSheetResult: PaymentSheetResult)

        fun paymentSheetPresented(isSuccesfullyPresented: Boolean)
    }

    interface Outputs {
        /** Emits a newly added stored card and the project. */
        fun addedCard(): Observable<Pair<StoredCard, Project>>

        /** Emits the additional pledge amount string. */
        fun additionalPledgeAmount(): Observable<String>

        /** Emits when the additional pledge amount should be hidden. */
        fun additionalPledgeAmountIsGone(): Observable<Boolean>

        /** Emits the base URL to build terms URLs. */
        fun baseUrlForTerms(): Observable<String>

        /** Emits a list of stored cards for a user. */
        fun cardsAndProject(): Observable<Pair<List<StoredCard>, Project>>

        /** Emits a boolean determining if the continue button should be enabled. */
        fun continueButtonIsEnabled(): Observable<Boolean>

        /** Emits a boolean determining if the continue button should be hidden. */
        fun continueButtonIsGone(): Observable<Boolean>

        /** Emits a string representing the total pledge amount in the user's preferred currency.  */
        fun conversionText(): Observable<String>

        /** Returns `true` if the conversion should be hidden, `false` otherwise.  */
        fun conversionTextViewIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the decrease pledge button should be enabled. */
        fun decreasePledgeButtonIsEnabled(): Observable<Boolean>

        /** Emits the estimated delivery date string of the reward. */
        fun estimatedDelivery(): Observable<String>

        /**  Emits a boolean determining if the estimated delivery info should be hidden. */
        fun estimatedDeliveryInfoIsGone(): Observable<Boolean>

        /**  Emits a boolean determining if the increase pledge button should be enabled.*/
        fun increasePledgeButtonIsEnabled(): Observable<Boolean>

        /** Emits a boolean determining if the payment container should be hidden. */
        fun paymentContainerIsGone(): Observable<Boolean>

        /** Emits the pledge amount string of the reward or backing. */
        fun pledgeAmount(): Observable<String>

        /** Emits the string resource ID of the pledge button. */
        fun pledgeButtonCTA(): Observable<Int>

        /** Emits a boolean determining if the pledge button should be enabled. */
        fun pledgeButtonIsEnabled(): Observable<Boolean>

        /** Emits a boolean determining if the pledge button should be hidden. */
        fun pledgeButtonIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the pledge progress bar should be hidden. */
        fun pledgeProgressIsGone(): Observable<Boolean>

        /** Emits the hint text for the pledge amount. */
        fun pledgeHint(): Observable<String>

        /** Emits the maximum pledge amount in the project's currency. */
        fun pledgeMaximum(): Observable<String>

        /** Emits a boolean determining if the pledge maximum should be hidden. */
        fun pledgeMaximumIsGone(): Observable<Boolean>

        /** Emits the minimum pledge amount in the project's currency. */
        fun pledgeMinimum(): Observable<String>

        /** Emits a boolean determining if the pledge section should be hidden. */
        fun pledgeSectionIsGone(): Observable<Boolean>

        /** Emits the pledge amount string of the backing. */
        fun pledgeSummaryAmount(): Observable<CharSequence>

        /** Emits a boolean determining if the pledge summary section should be hidden. */
        fun pledgeSummaryIsGone(): Observable<Boolean>

        /** Emits the color resource ID of the pledge amount. */
        fun pledgeTextColor(): Observable<Int>

        /** Emits the currency symbol string of the project. */
        fun projectCurrencySymbol(): Observable<Pair<SpannableString, Boolean>>

        /** Emits a boolean determining if the delivery section should be hidden. */
        fun rewardSummaryIsGone(): Observable<Boolean>

        /** Emits the title of the current reward. */
        fun rewardTitle(): Observable<String>

        /** Emits the currently selected shipping rule. */
        fun selectedShippingRule(): Observable<ShippingRule>

        /** Emits the shipping amount of the selected shipping rule. */
        fun shippingAmount(): Observable<CharSequence>

        /** Emits the shipping rule. */
        fun shippingRule(): Observable<ShippingRule>

        /** Emits a pair of list of shipping rules to be selected and the project. */
        fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>>

        /** Emits when the shipping rules section should be hidden. */
        fun shippingRulesSectionIsGone(): Observable<Boolean>

        /** Emits the shipping amount of the selected shipping rule. */
        fun shippingSummaryAmount(): Observable<CharSequence>

        /** Emits a boolean determining if the shipping summary should be hidden. */
        fun shippingSummaryIsGone(): Observable<Boolean>

        /** Emits the displayable name of the backing's location. */
        fun shippingSummaryLocation(): Observable<String>

        /**  Emits when the pledge call was unsuccessful. */
        fun showPledgeError(): Observable<Unit>

        /** Emits when the creating backing mutation was successful. */
        fun showPledgeSuccess(): Observable<Pair<CheckoutData, PledgeData>>

        /** Emits when the cards adapter should update the selected position. */
        fun showSelectedCard(): Observable<Pair<Int, CardState>>

        /** Emits when we should show the SCA flow with the client secret. */
        fun showSCAFlow(): Observable<String>

        /**  Emits when the update payment source mutation was unsuccessful. */
        fun showUpdatePaymentError(): Observable<Unit>

        /**  Emits when the update payment source mutation was successful. */
        fun showUpdatePaymentSuccess(): Observable<Unit>

        /** Emits when the update pledge call was unsuccessful. */
        fun showUpdatePledgeError(): Observable<Unit>

        /** Emits when the update pledge call was successful. */
        fun showUpdatePledgeSuccess(): Observable<Unit>

        /** Emits when we should start a Chrome tab. */
        fun startChromeTab(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.LoginToutActivity]. */
        fun startLoginToutActivity(): Observable<Unit>

        /** Emits the total amount string of the pledge. */
        fun totalAmount(): Observable<CharSequence>

        /** Emits the total pledge amount in the project's currency and the project's deadline. */
        fun totalAndDeadline(): Observable<Pair<String, String>>

        /** Emits when the total and deadline warning should be shown. */
        fun totalAndDeadlineIsVisible(): Observable<Unit>

        /** Emits a boolean determining if the divider above the total should be hidden. */
        fun totalDividerIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the header whould be hidden */
        fun headerSectionIsGone(): Observable<Boolean>

        /** Emits a Pair containing reward/add-on title and the amount */
        fun headerSelectedItems(): Observable<List<Pair<Project, Reward>>>

        /** Emits a boolean determining if the minimum pledge amount subtitle should be shown */
        fun isPledgeMinimumSubtitleGone(): Observable<Boolean>

        /** Emits a boolean determining if the bonus support section is visible */
        fun isBonusSupportSectionGone(): Observable<Boolean>

        /** Emits a boolean determining if the decrease bonus button should be enabled. */
        fun decreaseBonusButtonIsEnabled(): Observable<Boolean>

        /**  Emits a boolean determining if the increase bonus button should be enabled.*/
        fun increaseBonusButtonIsEnabled(): Observable<Boolean>

        /** Emits the bonus amount string of the reward or backing. */
        fun bonusAmount(): Observable<String>

        /** Emits the hint text for the bonus amount. */
        fun bonusHint(): Observable<String>

        /** Emits if a reward is a No Reward. */
        fun isNoReward(): Observable<Boolean>

        /** Emits the title of the project for `No Reward` use case */
        fun projectTitle(): Observable<String>

        /** Emits the selected reward + addOns list*/
        fun rewardAndAddOns(): Observable<List<Reward>>

        /** Emits if the static shipping selection area should be gone */
        fun shippingRuleStaticIsGone(): Observable<Boolean>

        /** Emits if the bonus summary section area should be gone */
        fun bonusSummaryIsGone(): Observable<Boolean>

        /** Emits the bonus summary amount */
        fun bonusSummaryAmount(): Observable<CharSequence>

        /** Emits the total pledgeAmount for Rewards + AddOns **/
        fun pledgeAmountHeader(): Observable<CharSequence>

        /** Emits a boolean that determines if the local PickUp section should be hidden **/
        fun localPickUpIsGone(): Observable<Boolean>

        /** Emits the String with the Local Pickup Displayable name **/
        fun localPickUpName(): Observable<String>

        /** Emits the String with the SetupIntent ClientID to present the PaymentSheet **/
        fun presentPaymentSheet(): Observable<Pair<String, String>>

        fun showError(): Observable<String>

        /** Emits the state LOADONG | DEFAULT when createSetupIntent mutation is called **/
        fun setState(): Observable<State>

        /** Emits with the third party analytic event mutation response **/
        fun eventSent(): Observable<Boolean>
    }

    class PledgeFragmentViewModel(
        private val environment: Environment,
        private val bundle: Bundle? = null
    ) : ViewModel(), Inputs, Outputs {

        private val addedCardPosition = BehaviorSubject.create<Int>()
        private val cardSaved = BehaviorSubject.create<StoredCard>()
        private val cardSelected = BehaviorSubject.create<Pair<StoredCard, Int>>()
        private val continueButtonClicked = BehaviorSubject.create<Unit>()
        private val decreasePledgeButtonClicked = BehaviorSubject.create<Unit>()
        private val increasePledgeButtonClicked = BehaviorSubject.create<Unit>()
        private val linkClicked = BehaviorSubject.create<String>()
        private val miniRewardClicked = BehaviorSubject.create<Unit>()
        private val newCardButtonClicked = BehaviorSubject.create<Unit>()
        private val pledgeButtonClicked = BehaviorSubject.create<Unit>()
        private val pledgeInput = BehaviorSubject.create<String>()
        private val shippingRule = BehaviorSubject.create<ShippingRule>()
        private val stripeSetupResultSuccessful = BehaviorSubject.create<Int>()
        private val stripeSetupResultUnsuccessful = BehaviorSubject.create<Exception>()
        private val decreaseBonusButtonClicked = BehaviorSubject.create<Unit>()
        private val increaseBonusButtonClicked = BehaviorSubject.create<Unit>()
        private val onRiskMessageDismissed = BehaviorSubject.create<Unit>()

        private val addedCard = BehaviorSubject.create<Pair<StoredCard, Project>>()
        private val additionalPledgeAmount = BehaviorSubject.create<String>()
        private val additionalPledgeAmountIsGone = BehaviorSubject.create<Boolean>()
        private val baseUrlForTerms = BehaviorSubject.create<String>()
        private val cardsAndProject = BehaviorSubject.create<Pair<List<StoredCard>, Project>>()
        private val continueButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val continueButtonIsGone = BehaviorSubject.create<Boolean>()
        private val conversionText = BehaviorSubject.create<String>()
        private val conversionTextViewIsGone = BehaviorSubject.create<Boolean>()
        private val decreasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val rewardSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val estimatedDeliveryInfoIsGone = BehaviorSubject.create<Boolean>()
        private val increasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val paymentContainerIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeAmount = BehaviorSubject.create<String>()
        private val pledgeButtonCTA = BehaviorSubject.create<Int>()
        private val pledgeButtonIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val pledgeHint = BehaviorSubject.create<String>()
        private val pledgeMaximum = BehaviorSubject.create<String>()
        private val pledgeMaximumIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeMinimum = BehaviorSubject.create<String>()
        private val pledgeProgressIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeSectionIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeSummaryAmount = BehaviorSubject.create<CharSequence>()
        private val pledgeSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeTextColor = BehaviorSubject.create<Int>()
        private val projectCurrencySymbol = BehaviorSubject.create<Pair<SpannableString, Boolean>>()
        private val rewardTitle = BehaviorSubject.create<String>()
        private val shippingAmount = BehaviorSubject.create<CharSequence>()
        private val shippingRulesAndProject = BehaviorSubject.create<Pair<List<ShippingRule>, Project>>()
        private val shippingRulesSectionIsGone = BehaviorSubject.create<Boolean>()

        // - when having add-ons the shipping location is an static field, no changes allowed in there
        private val shippingRuleStaticIsGone = BehaviorSubject.create<Boolean>()
        private val shippingSummaryAmount = BehaviorSubject.create<CharSequence>()
        private val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val shippingSummaryLocation = BehaviorSubject.create<String>()
        private val showPledgeError = PublishSubject.create<Unit>()
        private val showPledgeSuccess = PublishSubject.create<Pair<CheckoutData, PledgeData>>()
        private val showSelectedCard = BehaviorSubject.create<Pair<Int, CardState>>()
        private val showSCAFlow = PublishSubject.create<String>()
        private val showUpdatePaymentError = PublishSubject.create<Unit>()
        private val showUpdatePaymentSuccess = PublishSubject.create<Unit>()
        private val showUpdatePledgeError = PublishSubject.create<Unit>()
        private val showUpdatePledgeSuccess = PublishSubject.create<Unit>()
        private val startChromeTab = PublishSubject.create<String>()
        private val startLoginToutActivity = PublishSubject.create<Unit>()
        private val totalAmount = BehaviorSubject.create<CharSequence>()
        private val totalAndDeadline = BehaviorSubject.create<Pair<String, String>>()
        private val totalAndDeadlineIsVisible = BehaviorSubject.create<Unit>()
        private val totalDividerIsGone = BehaviorSubject.create<Boolean>()

        private val headerSectionIsGone = BehaviorSubject.create<Boolean>()
        private val headerSelectedItems = BehaviorSubject.create<List<Pair<Project, Reward>>>()
        private val isPledgeMinimumSubtitleGone = BehaviorSubject.create<Boolean>()
        private val isBonusSupportSectionGone = BehaviorSubject.create<Boolean>()
        private val bonusAmount = BehaviorSubject.create<String>()
        private val decreaseBonusButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val increaseBonusButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val bonusHint = BehaviorSubject.create<String>()

        // - Keep track if the bonus stepper increase/decrease has being pressed at some point
        private val bonusAmountHasChanged = BehaviorSubject.createDefault<Boolean>(false)
        private val isNoReward = BehaviorSubject.create<Boolean>()
        private val projectTitle = BehaviorSubject.create<String>()

        private val apolloClient = requireNotNull(environment.apolloClientV2())
        private val ffClient = requireNotNull(environment.featureFlagClient())
        private val cookieManager = requireNotNull(environment.cookieManager())
        private val currentConfig = requireNotNull(environment.currentConfigV2())
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val ksCurrency = requireNotNull(environment.ksCurrency())
        private val sharedPreferences = requireNotNull(environment.sharedPreferences())
        private val analyticEvents = requireNotNull(environment.analytics())
        private val minPledgeByCountry = BehaviorSubject.create<Double>()
        private val shippingRuleUpdated = BehaviorSubject.createDefault<Boolean>(false)
        private val selectedReward = BehaviorSubject.create<Reward>()
        private val rewardAndAddOns = BehaviorSubject.create<List<Reward>>()
        private val shippingAmountSelectedRw = BehaviorSubject.createDefault<Double>(0.0)

        private val bonusSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val bonusSummaryAmount = BehaviorSubject.create<CharSequence>()

        private val thirdpartyEventIsSuccessful = BehaviorSubject.create<Boolean>()

        // - Flag to know if the shipping location should be the default one,
        // - meaning we don't have shipping location selected yet
        // - Use case: (Reward shippable without addOns in new pledge or updating pledge with restricted location)
        private val shouldLoadDefaultLocation = BehaviorSubject.create<Boolean>()
        private val pledgeAmountHeader = BehaviorSubject.create<CharSequence>()
        private val stepperAmount = 1

        private val localPickUpIsGone = BehaviorSubject.create<Boolean>()
        private val localPickUpName = BehaviorSubject.create<String>()

        private val presentPaymentSheet = PublishSubject.create<Pair<String, String>>()
        private val paymentSheetResult = PublishSubject.create<PaymentSheetResult>()
        private val paySheetPresented = PublishSubject.create<Boolean>()
        private val showError = PublishSubject.create<String>()

        private val loadingState = BehaviorSubject.create<State>()

        private val disposables = CompositeDisposable()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private fun arguments() = bundle?.let { Observable.just(it) } ?: Observable.empty()
        init {
            val userIsLoggedIn = this.currentUser.isLoggedIn
                .distinctUntilChanged()

            val pledgeData = arguments()
                .map { it.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData? }
                .ofType(PledgeData::class.java)

            pledgeData
                .map { it.reward() }
                .subscribe { this.selectedReward.onNext(it) }
                .addToDisposable(disposables)

            val projectData = pledgeData
                .map { it.projectData() }

            val project = projectData
                .map { it.project() }

            pledgeData
                .map {
                    it.bonusAmount()
                }
                .subscribe {
                    this.bonusAmount.onNext(it.toString())
                }.addToDisposable(disposables)

            pledgeData
                .filter { it.shippingRule().isNotNull() && it.shippingRule()?.location().isNotNull() }
                .map {
                    requireNotNull(it.shippingRule())
                }
                .subscribe {
                    this.shippingRule.onNext(it)
                }.addToDisposable(disposables)

            // Shipping rules section
            val shippingRules = BehaviorSubject.create<List<ShippingRule>>()
            this.selectedReward
                .distinctUntilChanged()
                .filter { RewardUtils.isShippable(it) }
                .switchMap {
                    this.apolloClient.getShippingRules(it).compose(neverErrorV2())
                }
                .map { it.shippingRules() }
                .subscribe {
                    shippingRules.onNext(it)
                }
                .addToDisposable(disposables)

            val pledgeReason = arguments()
                .map { it.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason }
                .distinctUntilChanged()

            val updatingPayment = pledgeReason
                .map { it == PledgeReason.UPDATE_PAYMENT || it == PledgeReason.FIX_PLEDGE }
                .distinctUntilChanged()

            val updatingPaymentOrUpdatingPledge = pledgeReason
                .map { it == PledgeReason.UPDATE_PAYMENT || it == PledgeReason.UPDATE_PLEDGE || it == PledgeReason.FIX_PLEDGE }
                .distinctUntilChanged()

            val addOns = pledgeData
                .map { if (it.addOns().isNullOrEmpty()) emptyList() else it.addOns() as List<Reward> }

            val backing = projectData
                .compose<Pair<ProjectData, PledgeReason>>(combineLatestPair(pledgeReason))
                .filter { it.second != PledgeReason.PLEDGE }
                .map { it.first.backing() ?: it.first.project().backing() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }

            backing
                .map { it.locationId() == null }
                .subscribe {
                    this.shouldLoadDefaultLocation.onNext(it)
                }
                .addToDisposable(disposables)

            val backingShippingRule = backing
                .compose<Pair<Backing, PledgeData>>(combineLatestPair(pledgeData))
                .filter {
                    shouldLoadShippingRuleFromBacking(it)
                }
                .filter { it.first.locationId().isNotNull() }
                .map { requireNotNull(it.first.locationId()) }
                .compose<Pair<Long, List<ShippingRule>>>(combineLatestPair(shippingRules))
                .map { shippingInfo ->
                    selectedShippingRule(shippingInfo)
                }

            val backingWhenPledgeReasonUpdate = backing
                .compose<Pair<Backing, PledgeReason>>(combineLatestPair(pledgeReason))
                .filter { PledgeReason.UPDATE_PAYMENT == it.second || PledgeReason.UPDATE_PLEDGE == it.second }
                .map { it.first }

            val backingShippingRuleUpdate = backingWhenPledgeReasonUpdate
                .filter { it.reward()?.let { reward -> !RewardUtils.isNoReward(reward) } ?: false }
                .compose<Pair<Backing, PledgeData>>(combineLatestPair(pledgeData))
                .filter { it.first.locationId().isNotNull() }
                .map { requireNotNull(it.first.locationId()) }
                .compose<Pair<Long, List<ShippingRule>>>(combineLatestPair(shippingRules))
                .map { shippingInfo ->
                    selectedShippingRule(shippingInfo)
                }

            val initShippingRule = pledgeData
                .distinctUntilChanged()
                .filter { it.shippingRule().isNotNull() }
                .map { requireNotNull(it.shippingRule()) }

            pledgeData
                .map { it.shippingRule() == null && RewardUtils.isShippable(it.reward()) }
                .subscribe { this.shouldLoadDefaultLocation.onNext(it) }
                .addToDisposable(disposables)

            val preSelectedShippingRule = Observable.merge(initShippingRule, backingShippingRule, backingShippingRuleUpdate)
                .distinctUntilChanged()

            preSelectedShippingRule
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .subscribe {
                    this.shippingRule.onNext(it)
                }
                .addToDisposable(disposables)

            backing
                .map { if (it.addOns().isNullOrEmpty()) emptyList() else requireNotNull(it.addOns()) }
                .compose<Pair<List<Reward>, Reward>>(combineLatestPair(this.selectedReward))
                .subscribe {
                    val updatedList = it.first.toMutableList()
                    updatedList.add(0, it.second)
                    this.rewardAndAddOns.onNext(updatedList.toList())
                }
                .addToDisposable(disposables)

            this.selectedReward
                .compose<Pair<Reward, PledgeReason>>(combineLatestPair(pledgeReason))
                .filter { it.second == PledgeReason.PLEDGE || it.second == PledgeReason.UPDATE_REWARD }
                .map { it.first }
                .compose<Pair<Reward, List<Reward>>>(combineLatestPair(addOns))
                .map {
                    joinRewardAndAddOns(it.first, it.second)
                }
                .subscribe { this.rewardAndAddOns.onNext(it) }
                .addToDisposable(disposables)

            val pledgeAmountHeader = this.rewardAndAddOns
                .filter { !RewardUtils.isNoReward(it.first()) }
                .map { getPledgeAmount(it, backing.blockingLast(Backing.builder().build()).isPostCampaign()) }

            pledgeAmountHeader
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                .subscribe { this.pledgeAmountHeader.onNext(it) }
                .addToDisposable(disposables)

            val projectAndReward = project
                .compose<Pair<Project, Reward>>(combineLatestPair(this.selectedReward))

            val country = project
                .map { Country.findByCurrencyCode(it.currency()) }
                .filter { it != null }
                .distinctUntilChanged()
                .ofType(Country::class.java)

            country
                .map { it.minPledge.toDouble() }
                .compose<Pair<Double, Reward>>(combineLatestPair(this.selectedReward))
                .filter { RewardUtils.isNoReward(it.second) }
                .map { it.first }
                .distinctUntilChanged()
                .subscribe {
                    minPledgeByCountry.onNext(it)
                }
                .addToDisposable(disposables)

            projectAndReward
                .map { rewardTitle(it.first, it.second) }
                .distinctUntilChanged()
                .subscribe { this.rewardTitle.onNext(it) }
                .addToDisposable(disposables)

            this.selectedReward
                .filter { !RewardUtils.isShippable(it) }
                .map {
                    RewardUtils.isLocalPickup(it)
                }
                .subscribe {
                    this.localPickUpIsGone.onNext(!it)
                }
                .addToDisposable(disposables)

            this.selectedReward
                .filter { !RewardUtils.isShippable(it) }
                .filter { RewardUtils.isLocalPickup(it) }
                .map { it.localReceiptLocation()?.displayableName() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .subscribe { this.localPickUpName.onNext(it) }
                .addToDisposable(disposables)

            this.selectedReward
                .filter { it.estimatedDeliveryOn().isNotNull() }
                .map { requireNotNull(it.estimatedDeliveryOn()) }
                .map { dateTime -> dateTime.let { DateTimeUtils.estimatedDeliveryOn(it) } }
                .distinctUntilChanged()
                .subscribe { this.estimatedDelivery.onNext(it) }
                .addToDisposable(disposables)

            this.selectedReward
                .map { it.estimatedDeliveryOn().isNull() || RewardUtils.isNoReward(it) }
                .subscribe { this.estimatedDeliveryInfoIsGone.onNext(it) }
                .addToDisposable(disposables)

            val minRw = this.selectedReward
                .filter { !RewardUtils.isNoReward(it) }
                .map { it.minimum() }
                .distinctUntilChanged()

            val rewardMinimum = Observable.merge(minRw, minPledgeByCountry)

            rewardMinimum
                .map { NumberUtils.format(it.toInt()) }
                .subscribe { this.pledgeHint.onNext(it) }
                .addToDisposable(disposables)

            Observable.combineLatest(rewardMinimum, project) { amount, project ->
                return@combineLatest this.ksCurrency.format(amount, project)
            }
                .distinctUntilChanged()
                .subscribe { this.pledgeMinimum.onNext(it) }
                .addToDisposable(disposables)

            this.rewardAndAddOns
                .compose<Pair<List<Reward>, Project>>(combineLatestPair(project))
                .map { joinProject(it) }
                .subscribe { this.headerSelectedItems.onNext(it) }
                .addToDisposable(disposables)

            project
                .map { ProjectViewUtils.currencySymbolAndPosition(it, this.ksCurrency) }
                .subscribe { this.projectCurrencySymbol.onNext(it) }
                .addToDisposable(disposables)

            project
                .map { it.name() }
                .subscribe { this.projectTitle.onNext(it) }
                .addToDisposable(disposables)

            // Pledge stepper section
            val additionalPledgeAmount = BehaviorSubject.createDefault(0.0)

            val additionalAmountOrZero = additionalPledgeAmount
                .map { max(0.0, it) }

            val stepAmount = country
                .map { it.minPledge }

            additionalAmountOrZero
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .map { this.ksCurrency.format(it.first, it.second, RoundingMode.HALF_UP) }
                .distinctUntilChanged()
                .subscribe { this.additionalPledgeAmount.onNext(it) }
                .addToDisposable(disposables)

            additionalAmountOrZero
                .map { it <= 0.0 }
                .distinctUntilChanged()
                .subscribe { this.additionalPledgeAmountIsGone.onNext(it) }
                .addToDisposable(disposables)

            val initialAmount = rewardMinimum
                .compose<Pair<Double, Boolean>>(combineLatestPair(updatingPaymentOrUpdatingPledge))
                .filter { it.second.isFalse() }
                .map { it.first }

            // - For no Reward the amount of the RW and the bonus amount are the same value
            val backingAmountNR = backing
                .filter { it.reward() == null }
                .map { it.bonusAmount() }
                .distinctUntilChanged()

            val backingAmountRW = backing
                .filter { it.reward()?.let { rw -> !RewardUtils.isNoReward(rw) } ?: false }
                .map { it.amount() - it.shippingAmount() - it.bonusAmount() }
                .distinctUntilChanged()

            val backingAmount = Observable.merge(backingAmountNR, backingAmountRW)

            val pledgeInput = Observable.merge(initialAmount, this.pledgeInput.map { it.parseToDouble() }, backingAmount)
                .map { it }
                .distinctUntilChanged()

            pledgeInput
                .compose<Double>(takeWhenV2(this.increasePledgeButtonClicked))
                .map { it + this.stepperAmount }
                .map { it.toString() }
                .subscribe { this.pledgeInput.onNext(it) }
                .addToDisposable(disposables)

            pledgeInput
                .compose<Double>(takeWhenV2(this.decreasePledgeButtonClicked))
                .map { it - this.stepperAmount }
                .map { it.toString() }
                .subscribe { this.pledgeInput.onNext(it) }
                .addToDisposable(disposables)

            pledgeInput
                .compose<Pair<Double, Double>>(combineLatestPair(minPledgeByCountry))
                .map { it.first - it.second }
                .subscribe { additionalPledgeAmount.onNext(it) }
                .addToDisposable(disposables)

            pledgeInput
                .compose<Pair<Double, Double>>(combineLatestPair(minPledgeByCountry))
                .map { max(it.first, it.second) > it.second }
                .distinctUntilChanged()
                .subscribe { this.decreasePledgeButtonIsEnabled.onNext(it) }
                .addToDisposable(disposables)

            pledgeInput
                .map {
                    val formatter = NumberFormat.getNumberInstance()
                    formatter.maximumFractionDigits = 2
                    formatter.format(it)
                }
                .subscribe { this.pledgeAmount.onNext(it) }
                .addToDisposable(disposables)

            Observable.merge(this.decreaseBonusButtonClicked, this.decreasePledgeButtonClicked, this.increaseBonusButtonClicked, this.increasePledgeButtonClicked)
                .distinctUntilChanged()
                .subscribe {
                    this.bonusAmountHasChanged.onNext(true)
                }
                .addToDisposable(disposables)

            val rulesAndProject = shippingRules
                .compose<Pair<List<ShippingRule>, Project>>(combineLatestPair(project))

            rulesAndProject
                .subscribe { this.shippingRulesAndProject.onNext(it) }
                .addToDisposable(disposables)

            Observable.combineLatest(
                shippingRules, this.currentConfig.observable(), shouldLoadDefaultLocation
            ) { rules, config, isDefault ->
                if (isDefault && rules.isNotEmpty()) defaultConfigShippingRule(
                    rules.toMutableList(),
                    config
                ) else ShippingRuleFactory.emptyShippingRule()
            }
                .filter { it.location()?.id()?.let { it > 0 } ?: false }
                .compose<Pair<ShippingRule, PledgeReason>>(combineLatestPair(pledgeReason))
                .filter { it.second == PledgeReason.PLEDGE || it.second == PledgeReason.UPDATE_REWARD || it.second == PledgeReason.FIX_PLEDGE }
                .map { it.first }
                .compose<Pair<ShippingRule, Project>>(combineLatestPair(project))
                .subscribe {
                    this.shippingRule.onNext(it.first)
                }
                .addToDisposable(disposables)

            val backingShippingAmount = backing
                .map { it.shippingAmount() }

            val shippingAmountWhenBacking = Observable.combineLatest(this.shippingRule, pledgeReason, backingShippingAmount, rewardAndAddOns) { rule, reason, bShippingAmount, listRw ->
                return@combineLatest getShippingAmount(rule, reason, bShippingAmount, listRw)
            }
                .distinctUntilChanged()

            val newPledge = pledgeReason
                .filter { it == PledgeReason.PLEDGE || it == PledgeReason.UPDATE_REWARD }
                .map { it }

            // - Shipping amount when no backing
            val shippingAmountNewPledge = Observable.combineLatest(this.shippingRule, newPledge, rewardAndAddOns) { rule, reason, listRw ->
                return@combineLatest getShippingAmount(rule, reason, listRw = listRw)
            }
                .distinctUntilChanged()

            val shippingAmount = Observable.merge(shippingAmountNewPledge, shippingAmountWhenBacking)

            shippingAmount
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .distinctUntilChanged()
                .subscribe {
                    shippingAmountSelectedRw.onNext(it.first)
                    this.shippingAmount.onNext(ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency))
                }
                .addToDisposable(disposables)

            // - When updating payment, shipping location area should always be gone
            updatingPayment
                .compose<Pair<Boolean, Reward>>(combineLatestPair(this.selectedReward))
                .filter { it.first == true && RewardUtils.isShippable(it.second) }
                .subscribe {
                    this.shippingRulesSectionIsGone.onNext(true)
                }
                .addToDisposable(disposables)

            val isRewardWithShipping = this.selectedReward
                .filter { RewardUtils.isShippable(it) }
                .distinctUntilChanged()

            val isDigitalRw = this.selectedReward
                .filter { RewardUtils.isDigital(it) || RewardUtils.isLocalPickup(it) }
                .distinctUntilChanged()

            // - Calculate total for Reward || Rewards + AddOns with Shipping location
            val totalWShipping = Observable.combineLatest(isRewardWithShipping, pledgeAmountHeader, shippingAmount, this.bonusAmount, pledgeReason) {
                    _, pAmount, shippingAmount, bAmount, pReason ->
                return@combineLatest getAmount(pAmount, shippingAmount, bAmount, pReason)
            }
                .distinctUntilChanged()

            // - Calculate total for NoReward
            val totalNR = this.selectedReward
                .filter { RewardUtils.isNoReward(it) }
                .compose<Pair<Reward, String>>(combineLatestPair(this.pledgeInput.startWith("")))
                .map { if (it.second.isNotEmpty()) it.second.parseToDouble() else it.first.minimum() }

            // - Calculate total for DigitalRewards || DigitalReward + DigitalAddOns || LocalPickup
            val totalNoShipping = Observable.combineLatest(isDigitalRw, pledgeAmountHeader, this.bonusAmount, pledgeReason) { _, pledgeAmount, bonusAmount, pReason ->
                return@combineLatest getAmountDigital(pledgeAmount, bonusAmount.parseToDouble(), pReason)
            }
                .distinctUntilChanged()

            val total = Observable.merge(totalWShipping, totalNR, totalNoShipping)
                .distinctUntilChanged()

            total
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                .subscribe {
                    this.totalAmount.onNext(it)
                }
                .addToDisposable(disposables)

            total
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .map { Pair(this.ksCurrency.format(it.first, it.second, RoundingMode.HALF_UP), it.second) }
                .filter { it.second.deadline().isNotNull() }
                .map { totalAndProject -> totalAndProject.second.deadline()?.let { Pair(totalAndProject.first, DateTimeUtils.longDate(it)) } }
                .map { requireNotNull(it) }
                .distinctUntilChanged()
                .subscribe { this.totalAndDeadline.onNext(it) }
                .addToDisposable(disposables)

            this.totalAndDeadline
                .compose(ignoreValuesV2())
                .subscribe { this.totalAndDeadlineIsVisible.onNext(it) }
                .addToDisposable(disposables)

            total
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .filter { it.second.currency() != it.second.currentCurrency() }
                .map { this.ksCurrency.formatWithUserPreference(it.first, it.second, RoundingMode.UP, 2) }
                .subscribe { this.conversionText.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { it.first.currency() != it.first.currentCurrency() }
                .map { it.negate() }
                .distinctUntilChanged()
                .subscribe { this.conversionTextViewIsGone.onNext(it) }
                .addToDisposable(disposables)

            val currencyMaximum = country
                .map { it.maxPledge.toDouble() }
                .distinctUntilChanged()

            val threshold = pledgeAmountHeader
                .compose<Pair<Double, Double>>(combineLatestPair(shippingAmount))
                .map { it.first + it.second }
                .distinctUntilChanged()

            val selectedPledgeAmount = Observable.merge(pledgeAmountHeader, threshold, minPledgeByCountry)

            val bonusSupportMaximum = currencyMaximum
                .compose<Pair<Double, Double>>(combineLatestPair(selectedPledgeAmount))
                .compose<Pair<Pair<Double, Double>, Reward>>(combineLatestPair(this.selectedReward))
                .map { if (RewardUtils.isNoReward(it.second)) it.first.first else it.first.first - it.first.second }

            val pledgeMaximumIsGone = currencyMaximum
                .compose<Pair<Double, Double>>(combineLatestPair(total))
                .map { it.first >= it.second }
                .distinctUntilChanged()

            pledgeMaximumIsGone
                .distinctUntilChanged()
                .subscribe {
                    this.pledgeMaximumIsGone.onNext(it)
                }
                .addToDisposable(disposables)

            bonusSupportMaximum
                .distinctUntilChanged()
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .map { this.ksCurrency.format(it.first, it.second, RoundingMode.HALF_UP) }
                .subscribe {
                    this.pledgeMaximum.onNext(it)
                }
                .addToDisposable(disposables)

            val minAndMaxPledge = rewardMinimum
                .compose<Pair<Double, Double>>(combineLatestPair(currencyMaximum))

            pledgeInput
                .compose<Pair<Double, Pair<Double, Double>>>(combineLatestPair(minAndMaxPledge))
                .map { it.first in it.second.first..it.second.second }
                .map { if (it) R.color.kds_create_700 else R.color.kds_alert }
                .distinctUntilChanged()
                .subscribe { this.pledgeTextColor.onNext(it) }
                .addToDisposable(disposables)

            val stepAndMaxPledge = stepAmount
                .map { it.toDouble() }
                .compose<Pair<Double, Double>>(combineLatestPair(currencyMaximum))

            pledgeInput
                .compose<Pair<Double, Pair<Double, Double>>>(combineLatestPair(stepAndMaxPledge))
                .map { it.second.second - it.first >= it.second.first }
                .distinctUntilChanged()
                .subscribe { this.increasePledgeButtonIsEnabled.onNext(it) }
                .addToDisposable(disposables)

            // Manage pledge section
            backingAmount
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                .distinctUntilChanged()
                .subscribe { this.pledgeSummaryAmount.onNext(it) }
                .addToDisposable(disposables)

            updatingPayment
                .subscribe { this.totalDividerIsGone.onNext(it) }
                .addToDisposable(disposables)

            backing
                .map { it.shippingAmount().toDouble() }
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                .subscribe { this.shippingSummaryAmount.onNext(it) }
                .addToDisposable(disposables)

            backing
                .map { it.bonusAmount() }
                .compose<Pair<Double, PledgeReason>>(combineLatestPair(pledgeReason))
                .filter { it.second == PledgeReason.UPDATE_PAYMENT }
                .map { it.first }
                .compose<Pair<Double, Project>>(combineLatestPair(project))
                .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                .subscribe {
                    this.bonusSummaryAmount.onNext(it)
                }
                .addToDisposable(disposables)

            val summary: Observable<String> = this.shippingRule
                .map { it.location()?.displayableName() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .distinctUntilChanged()

            summary
                .subscribe { this.shippingSummaryLocation.onNext(it) }
                .addToDisposable(disposables)

            val updatingPledge = pledgeReason
                .map { it == PledgeReason.UPDATE_PLEDGE }

            val rewardAmountUpdated = total
                .compose<Pair<Double, Reward>>(combineLatestPair(this.selectedReward))
                .filter { !RewardUtils.isNoReward(it.second) }
                .map { it.first }
                .compose<Pair<Double, Boolean>>(combineLatestPair(updatingPledge))
                .filter { it.second }
                .map { it.first }
                .compose<Pair<Double, Backing>>(combineLatestPair(backing))
                .map { it.first != it.second.amount() }
                .startWith(false)

            val noRewardAmountUpdated = pledgeInput
                .compose<Pair<Double, Reward>>(combineLatestPair(this.selectedReward))
                .filter { RewardUtils.isNoReward(it.second) }
                .map { it.first }
                .compose<Pair<Double, Boolean>>(combineLatestPair(updatingPledge))
                .filter { it.second }
                .map { it.first }
                .compose<Pair<Double, Double>>(combineLatestPair(backingAmount))
                .map { it.first != it.second }
                .startWith(false)

            val amountUpdated = Observable.combineLatest(this.selectedReward, rewardAmountUpdated, noRewardAmountUpdated) { rw, rAmount, noRAmount ->
                if (RewardUtils.isNoReward(rw)) return@combineLatest noRAmount
                else return@combineLatest rAmount
            }
                .distinctUntilChanged()

            Observable.combineLatest(this.shippingRule, backingShippingRule) { rule, dfRule ->
                return@combineLatest rule.id() != dfRule.id()
            }
                .distinctUntilChanged()
                .subscribe { this.shippingRuleUpdated.onNext(it) }
                .addToDisposable(disposables)

            val shippingOrAmountChanged = Observable.combineLatest(shippingRuleUpdated, this.bonusAmountHasChanged, amountUpdated, pledgeReason) { shippingUpdated, bHasChanged, aUpdated, pReason ->
                return@combineLatest hasBeenUpdated(shippingUpdated, pReason, bHasChanged, aUpdated)
            }
                .distinctUntilChanged()

            val totalIsValid = total
                .compose<Pair<Double, Pair<Double, Double>>>(combineLatestPair(minAndMaxPledge))
                .map { it.first in it.second.first..it.second.second }
                .distinctUntilChanged()

            val validChange = shippingOrAmountChanged
                .compose<Pair<Boolean, Boolean>>(combineLatestPair(totalIsValid))
                .map { it.first && it.second }

            val changeDuringUpdatingPledge = validChange
                .compose<Pair<Boolean, Boolean>>(combineLatestPair(updatingPledge))
                .filter { it.second.isTrue() }
                .map { it.first }

            // Payment section
            pledgeReason
                .map { it == PledgeReason.UPDATE_PLEDGE || it == PledgeReason.UPDATE_REWARD }
                .compose<Pair<Boolean, Boolean>>(combineLatestPair(userIsLoggedIn))
                .map { it.first || !it.second }
                .distinctUntilChanged()
                .subscribe { this.paymentContainerIsGone.onNext(it) }
                .addToDisposable(disposables)

            userIsLoggedIn
                .subscribe { this.continueButtonIsGone.onNext(it) }
                .addToDisposable(disposables)

            userIsLoggedIn
                .map { it.negate() }
                .subscribe { this.pledgeButtonIsGone.onNext(it) }
                .addToDisposable(disposables)

            val storedCards = BehaviorSubject.create<List<StoredCard>>()

            userIsLoggedIn
                .filter { it.isTrue() }
                .compose<Pair<Boolean, PledgeReason>>(combineLatestPair(pledgeReason))
                .take(1)
                .switchMap { storedCards() }
                .subscribe {
                    storedCards.onNext(it)
                }
                .addToDisposable(disposables)

            val cardsAndProject = storedCards
                .compose<Pair<List<StoredCard>, Project>>(combineLatestPair(project))

            cardsAndProject
                .subscribe { this.cardsAndProject.onNext(it) }
                .addToDisposable(disposables)

            val initialCardSelection = cardsAndProject
                .take(1)
                .map { initialCardSelection(it.first, it.second) }

            // - When setupIntent finishes with error reload the payment methods
            this.paymentSheetResult
                .filter {
                    it != PaymentSheetResult.Completed
                }
                .withLatestFrom(cardsAndProject) { _, cardsAndProject ->
                    return@withLatestFrom cardsAndProject
                }
                .subscribe { this.cardsAndProject.onNext(it) }
                .addToDisposable(disposables)

            this.cardSaved
                .compose<Pair<StoredCard, Project>>(combineLatestPair(project))
                .subscribe { this.addedCard.onNext(it) }
                .addToDisposable(disposables)

            val selectedCardAndPosition = Observable.merge(
                initialCardSelection,
                this.cardSelected,
                this.cardSaved.compose<Pair<StoredCard, Int>>(zipPairV2(this.addedCardPosition))
            )

            selectedCardAndPosition
                .map { it.second }
                .filter { it >= 0 }
                .subscribe { this.showSelectedCard.onNext(Pair(it, CardState.SELECTED)) }
                .addToDisposable(disposables)

            val userHasCards = selectedCardAndPosition
                .map { it.second >= 0 }

            Observable.combineLatest(changeDuringUpdatingPledge.startWith(false), userHasCards, pledgeReason) {
                    changedValues, hasCards, pReason ->
                return@combineLatest shouldBeEnabled(changedValues, hasCards, pReason)
            }
                .distinctUntilChanged()
                .subscribe {
                    this.pledgeButtonIsEnabled.onNext(it)
                }
                .addToDisposable(disposables)

            val changeCard = Observable.merge(
                this.cardSelected,
                this.cardSaved.compose<Pair<StoredCard, Int>>(zipPairV2(this.addedCardPosition))
            ).map {
                it.second >= 0
            }

            tPAddPaymentMethodEvent(project, changeCard, pledgeData, shippingAmount, total)

            // - Present PaymentSheet if user logged in, and add card button pressed
            val shouldPresentPaymentSheet = PublishSubject.create<Notification<String>>()
            this.newCardButtonClicked
                .withLatestFrom(project) { _, latestProject -> latestProject }
                .switchMap {
                    this.apolloClient.createSetupIntent(it)
                        .doOnSubscribe {
                            this.loadingState.onNext(State.LOADING)
                            this.pledgeButtonIsEnabled.onNext(false)
                        }
                        .doOnError {
                            this.loadingState.onNext(State.DEFAULT)
                            this.pledgeButtonIsEnabled.onNext(true)
                        }
                        .materialize()
                }
                .subscribe {
                    shouldPresentPaymentSheet.onNext(it)
                }
                .addToDisposable(disposables)

            shouldPresentPaymentSheet
                .compose(valuesV2())
                .compose(combineLatestPair(userEmail()))
                .subscribe {
                    this.presentPaymentSheet.onNext(it)
                }
                .addToDisposable(disposables)

            shouldPresentPaymentSheet
                .compose(errorsV2())
                .subscribe {
                    // - Display error snackbar in case the SetupIntent was not successfully created
                    this.showError.onNext(it?.message ?: "")
                }
                .addToDisposable(disposables)

            this.paySheetPresented
                .subscribe {
                    this.pledgeButtonIsEnabled.onNext(true)
                    this.loadingState.onNext(State.DEFAULT)
                }
                .addToDisposable(disposables)

            this.continueButtonClicked
                .subscribe { this.startLoginToutActivity.onNext(it) }
                .addToDisposable(disposables)

            userIsLoggedIn
                .filter { it.isFalse() }
                .compose<Pair<Boolean, PledgeReason>>(combineLatestPair(pledgeReason))
                .filter { it.second == PledgeReason.PLEDGE }
                .compose<Pair<Pair<Boolean, PledgeReason>, Boolean>>(combineLatestPair(totalIsValid))
                .map { it.second }
                .subscribe { this.continueButtonIsEnabled.onNext(it) }
                .addToDisposable(disposables)

            // An observable of the ref tag stored in the cookie for the project
            val cookieRefTag = project
                .take(1)
                .map { p ->
                    RefTagUtils.storedCookieRefTagForProject(p, cookieManager, sharedPreferences)
                        ?: RefTag.Builder().build()
                }

            val locationId: Observable<String> = shippingRule
                .filter { it.location() != null }
                .map { it.location() }
                .map { it.id() ?: -1L }
                .map { it.toString() }
                .startWith("")

            val backingToUpdate = project
                .filter { it.isBacking() }
                .map { it.backing() }
                .ofType(Backing::class.java)
                .distinctUntilChanged()

            val paymentMethod: Observable<StoredCard> = selectedCardAndPosition.map { it.first }

            val extendedListForCheckOut = rewardAndAddOns
                .map { extendAddOns(it) }

            val pledgeButtonClicked = pledgeReason
                .compose<PledgeReason>(takeWhenV2(this.pledgeButtonClicked))
                .filter { it == PledgeReason.PLEDGE }
                .compose(ignoreValuesV2())

            val createBackingNotification = Observable.combineLatest(
                project,
                total.map { it.toString() },
                paymentMethod,
                locationId,
                extendedListForCheckOut,
                cookieRefTag
            ) { proj, amount, paymentMethod, locationId, rewards, cookieRefTag ->
                paymentMethod.getBackingData(proj, amount, locationId, rewards, cookieRefTag)
            }
                .compose<CreateBackingData>(takeWhenV2(pledgeButtonClicked))
                .switchMap {
                    this.apolloClient.createBacking(it)
                        .doOnSubscribe {
                            this.pledgeProgressIsGone.onNext(false)
                            this.pledgeButtonIsEnabled.onNext(false)
                        }
                        .materialize()
                }
                .share()

            val totalString: Observable<String> = total
                .map { it.toString() }
                .startWith("")

            val updatePaymentClick = pledgeReason
                .compose<PledgeReason>(takeWhenV2(this.pledgeButtonClicked))
                .filter { it == PledgeReason.UPDATE_PAYMENT }
                .compose(ignoreValuesV2())

            val fixPaymentClick = pledgeReason
                .compose<PledgeReason>(takeWhenV2(this.pledgeButtonClicked))
                .filter { it == PledgeReason.FIX_PLEDGE }
                .compose(ignoreValuesV2())

            val updatePledgeClick = pledgeReason
                .compose<PledgeReason>(takeWhenV2(this.pledgeButtonClicked))
                .filter { it == PledgeReason.UPDATE_PLEDGE || it == PledgeReason.UPDATE_REWARD }
                .compose(ignoreValuesV2())

            val updateBackingNotification = Observable.combineLatest(
                backingToUpdate,
                totalString,
                locationId,
                extendedListForCheckOut,
                paymentMethod,
                project
            ) { b, a, l, r, pMethod, pro ->
                if (pro.isBacking() && pro.backing()?.amount().toString() == a) {
                    Pair(this.getUpdateBackingData(b, null, l, r, pMethod), pro)
                } else {
                    Pair(this.getUpdateBackingData(b, a, l, r, pMethod), pro)
                }
            }
                .compose<Pair<UpdateBackingData, Project>>(takeWhenV2(Observable.merge(updatePledgeClick, updatePaymentClick, fixPaymentClick)))
                .switchMap {
                    this.apolloClient.updateBacking(it.first)
                        .doOnSubscribe {
                            this.pledgeProgressIsGone.onNext(false)
                            this.pledgeButtonIsEnabled.onNext(false)
                        }
                        .materialize()
                }
                .share()

            val checkoutResult = Observable.merge(createBackingNotification, updateBackingNotification)
                .compose(valuesV2())

            val successfulSCACheckout = checkoutResult
                .compose<Checkout>(takeWhenV2(this.stripeSetupResultSuccessful.filter { it == StripeIntentResult.Outcome.SUCCEEDED }))

            val successfulCheckout = checkoutResult
                .filter { it.backing().requiresAction().isFalse() }

            val successfulBacking = successfulCheckout
                .filter { it.backing().isNotNull() }
                .map { it.backing() }

            val successAndPledgeReason = Observable.merge(
                successfulBacking,
                this.stripeSetupResultSuccessful.filter { it == StripeIntentResult.Outcome.SUCCEEDED }
            )
                .compose<Pair<Any, PledgeReason>>(combineLatestPair(pledgeReason))

            Observable.combineLatest<Double, Double, String, Checkout, CheckoutData>(shippingAmountSelectedRw, total, this.bonusAmount, Observable.merge(successfulCheckout, successfulSCACheckout)) { s, t, b, c ->
                checkoutData(s, t, b.parseToDouble(), c)
            }
                .compose<Pair<CheckoutData, PledgeData>>(combineLatestPair(pledgeData))
                .filter { it.second.pledgeFlowContext() == PledgeFlowContext.NEW_PLEDGE }
                .subscribe { this.showPledgeSuccess.onNext(it) }
                .addToDisposable(disposables)

            successAndPledgeReason
                .filter { it.second == PledgeReason.UPDATE_PLEDGE || it.second == PledgeReason.UPDATE_REWARD }
                .compose(ignoreValuesV2())
                .subscribe { this.showUpdatePledgeSuccess.onNext(it) }
                .addToDisposable(disposables)

            successAndPledgeReason
                .filter { it.second == PledgeReason.UPDATE_PAYMENT || it.second == PledgeReason.FIX_PLEDGE }
                .compose(ignoreValuesV2())
                .subscribe { this.showUpdatePaymentSuccess.onNext(it) }
                .addToDisposable(disposables)

            Observable.merge(createBackingNotification, updateBackingNotification)
                .compose(valuesV2())
                .map { it.backing() }
                .filter { it.requiresAction().isTrue() }
                .map { it.clientSecret() }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }
                .subscribe { this.showSCAFlow.onNext(it) }
                .addToDisposable(disposables)

            val createOrUpdateError = Observable.merge(
                createBackingNotification.compose(errorsV2()),
                updateBackingNotification.compose(errorsV2())
            )

            val stripeSetupError = Observable.merge(
                this.stripeSetupResultUnsuccessful,
                this.stripeSetupResultSuccessful.filter { it != StripeIntentResult.Outcome.SUCCEEDED }
            )

            val errorAndPledgeReason = Observable.merge(createOrUpdateError, stripeSetupError)
                .compose(ignoreValuesV2())
                .compose<Pair<Unit, PledgeReason>>(combineLatestPair(pledgeReason))

            errorAndPledgeReason
                .filter { it.second == PledgeReason.PLEDGE }
                .compose(ignoreValuesV2())
                .subscribe {
                    this.pledgeProgressIsGone.onNext(true)
                    this.pledgeButtonIsEnabled.onNext(true)
                    this.showPledgeError.onNext(Unit)
                }
                .addToDisposable(disposables)

            errorAndPledgeReason
                .filter { it.second == PledgeReason.UPDATE_PLEDGE || it.second == PledgeReason.UPDATE_REWARD }
                .compose(ignoreValuesV2())
                .subscribe {
                    this.pledgeProgressIsGone.onNext(true)
                    this.pledgeButtonIsEnabled.onNext(true)
                    this.showUpdatePledgeError.onNext(Unit)
                }
                .addToDisposable(disposables)

            errorAndPledgeReason
                .filter { it.second == PledgeReason.UPDATE_PAYMENT || it.second == PledgeReason.FIX_PLEDGE }
                .compose(ignoreValuesV2())
                .subscribe {
                    this.pledgeProgressIsGone.onNext(true)
                    this.pledgeButtonIsEnabled.onNext(true)
                    this.showUpdatePaymentError.onNext(Unit)
                }
                .addToDisposable(disposables)

            this.baseUrlForTerms.onNext(this.environment.webEndpoint())

            this.linkClicked
                .withLatestFrom(this.loadingState.startWith(State.DEFAULT)) { link, state -> Pair(link, state) }
                .filter { it.second == State.DEFAULT }
                .map { it.first }
                .subscribe { this.startChromeTab.onNext(it) }
                .addToDisposable(disposables)

            pledgeReason
                .map { if (it == PledgeReason.PLEDGE) R.string.Pledge else R.string.Confirm }
                .subscribe { this.pledgeButtonCTA.onNext(it) }
                .addToDisposable(disposables)

            val checkoutAndPledgeData =
                Observable.combineLatest<Double, Double, String, CheckoutData>(
                    shippingAmountSelectedRw,
                    total,
                    this.bonusAmount
                ) { s, t, b ->
                    checkoutData(s, t, b.parseToDouble(), null)
                }
                    .compose<Pair<CheckoutData, PledgeData>>(combineLatestPair(pledgeData))

            checkoutAndPledgeData
                .take(1)
                .filter { it.second.pledgeFlowContext() == PledgeFlowContext.NEW_PLEDGE }
                .subscribe {
                    this.analyticEvents.trackCheckoutScreenViewed(it.first, it.second)
                }
                .addToDisposable(disposables)

            checkoutAndPledgeData
                .take(1)
                .filter { it.second.pledgeFlowContext() == PledgeFlowContext.MANAGE_REWARD }
                .subscribe {
                    this.analyticEvents.trackUpdatePledgePageViewed(it.first, it.second)
                }
                .addToDisposable(disposables)

            checkoutAndPledgeData
                .filter { shouldTrackPledgeSubmitButtonClicked(it.second.pledgeFlowContext()) }
                .compose<Pair<CheckoutData, PledgeData>>(takeWhenV2(this.pledgeButtonClicked))
                .subscribe {
                    this.analyticEvents.trackPledgeSubmitCTA(it.first, it.second)
                }
                .addToDisposable(disposables)

            // - Screen configuration Logic (Different configurations depending on: PledgeReason, Reward type, Shipping, AddOns)
            this.selectedReward
                .compose<Pair<Reward, PledgeReason>>(combineLatestPair(pledgeReason))
                .subscribe {
                    when (it.second) {
                        PledgeReason.PLEDGE,
                        PledgeReason.UPDATE_REWARD -> {
                            this.pledgeSummaryIsGone.onNext(true)
                            if (!RewardUtils.isNoReward(it.first)) {
                                this.headerSectionIsGone.onNext(false)
                                this.isBonusSupportSectionGone.onNext(false)
                                this.pledgeSectionIsGone.onNext(true)
                            } else {
                                this.pledgeSectionIsGone.onNext(false)
                                this.isPledgeMinimumSubtitleGone.onNext(true)
                                this.headerSectionIsGone.onNext(true)
                                this.isNoReward.onNext(true)
                                this.isBonusSupportSectionGone.onNext(true)
                            }
                        }
                        PledgeReason.UPDATE_PAYMENT,
                        PledgeReason.FIX_PLEDGE -> {
                            this.headerSectionIsGone.onNext(true)

                            if (RewardUtils.isNoReward(it.first)) {
                                this.pledgeSummaryIsGone.onNext(true)
                                this.shippingSummaryIsGone.onNext(true)
                                this.bonusSummaryIsGone.onNext(true)
                            } else {
                                this.shippingSummaryIsGone.onNext(!RewardUtils.isShippable(it.first))
                                this.pledgeSummaryIsGone.onNext(false)
                            }
                        }
                        else -> {}
                    }
                }
                .addToDisposable(disposables)

            // - Update visibility for shippingRules sections
            val shouldHideShippingSections = Observable.combineLatest(this.rewardAndAddOns, pledgeReason) { rwAndAddOns, reason ->
                return@combineLatest shippingRulesSectionShouldHide(rwAndAddOns, reason)
            }

            shouldHideShippingSections
                .subscribe {
                    this.shippingRulesSectionIsGone.onNext(it.first)
                    this.shippingRuleStaticIsGone.onNext(it.second)
                }
                .addToDisposable(disposables)

            pledgeReason
                .compose<Pair<PledgeReason, Backing>>(combineLatestPair(backing))
                .subscribe {
                    val hasBonus = it.second.bonusAmount() > 0
                    val isNoReward = it.second.reward() == null && hasBonus

                    when (it.first) {
                        PledgeReason.UPDATE_PLEDGE -> {
                            this.isBonusSupportSectionGone.onNext(isNoReward) // has bonus, sections is not gone
                            this.pledgeSectionIsGone.onNext(!isNoReward)
                            this.headerSectionIsGone.onNext(true)
                            this.pledgeSummaryIsGone.onNext(isNoReward) // Gone if No reward, Show if regular reward
                        }
                        PledgeReason.UPDATE_PAYMENT,
                        PledgeReason.FIX_PLEDGE -> {
                            if (!isNoReward) {
                                this.bonusSummaryIsGone.onNext(!hasBonus)
                            }
                        }
                        else -> {}
                    }
                }
                .addToDisposable(disposables)
        }

        /**
         * ThirdParty Analytic event sent when there is a change with the selected payment method
         * it does require pledgeAmount and shipping amount information plus the selected rewards/addOns
         *
         * @param project observable with the current project, should always emit
         * @param changeCard observable that will emit if the selected payment changes
         * @param pledgeData current user selection to make a pledge, should always emit
         * @param shippingAmount observable with shipping amount, will emit when reward or addon are shippable
         * @param total observable with the total amount of the plede, will always emit
         */
        private fun tPAddPaymentMethodEvent(
            project: Observable<Project>,
            changeCard: Observable<Boolean>,
            pledgeData: Observable<PledgeData>,
            shippingAmount: Observable<Double>,
            total: Observable<Double>
        ) {
            project
                .compose(takeWhenV2(changeCard))
                .withLatestFrom(pledgeData) { _, pData ->
                    pData
                }
                // - Start with 0 in case of digital reward/addon without shipping
                .withLatestFrom(shippingAmount.startWith(0.0)) { pData, shipAmount ->
                    Pair(pData, shipAmount)
                }
                .withLatestFrom(total) { data, totAmount ->
                    val pledData = data.first
                    val shipAmt = data.second
                    val pledgAmount = totAmount - shipAmt
                    val prject = pledData.projectData().project()
                    Triple(pledData, prject, Pair(pledgAmount, shipAmt))
                }
                .switchMap {
                    SendThirdPartyEventUseCaseV2(sharedPreferences, ffClient)
                        .sendThirdPartyEvent(
                            project = Observable.just(it.second),
                            currentUser = currentUser,
                            apolloClient = apolloClient,
                            draftPledge = it.third,
                            checkoutAndPledgeData = Observable.just(Pair(null, it.first)),
                            eventName = ThirdPartyEventValues.EventName.ADD_PAYMENT_INFO
                        )
                }
                .compose(neverErrorV2())
                .subscribe {
                    thirdpartyEventIsSuccessful.onNext(it.first)
                }
                .addToDisposable(disposables)
        }

        private fun shouldBeEnabled(changedValues: Boolean, hasCards: Boolean, pReason: PledgeReason): Boolean {
            val isEnabled = when (pReason) {
                PledgeReason.UPDATE_REWARD,
                PledgeReason.PLEDGE,
                PledgeReason.UPDATE_PAYMENT,
                PledgeReason.LATE_PLEDGE,
                PledgeReason.FIX_PLEDGE -> hasCards
                PledgeReason.UPDATE_PLEDGE -> changedValues && hasCards
            }

            return isEnabled
        }

        /**
         * The shipping rule from the backing object should be load on the next scenarios:
         * - Shippable Reward without available addOns
         * - Shippable Reward with available addOns but not backedAddOns
         *
         * Note: If pledgeData object contains shipping rule it comes from selecting addOns that's
         * the prioritary shippingRule ignore anything on the backingObject
         *
         * Note: If skipping addOns, pledgeData.shippingRule will be null but we will display
         * the shippingSelector for the user, so loading the backing shippingRule it's correct as
         * it can be edited.
         */
        private fun shouldLoadShippingRuleFromBacking(it: Pair<Backing, PledgeData>) =
            RewardUtils.isShippable(it.second.reward()) && it.first.locationId().isNotNull() &&
                !hasBackedAddOns(Pair(it.first, it.second.reward())) && !hasSelectedAddOns(it.second.addOns()) &&
                it.second.shippingRule() == null

        /**
         * If a user has selected addOns, we will know by checking field addOns from pledgeData input
         */
        private fun hasSelectedAddOns(addOns: List<Reward>?): Boolean = addOns?.isNotEmpty() ?: false

        /**
         * Determine if the user has backed addOns
         */
        private fun hasBackedAddOns(it: Pair<Backing, Reward>) =
            it.second.hasAddons() && it.first.addOns()?.isNotEmpty() ?: false

        private fun getAmountDigital(pledgeAmount: Double, bAmount: Double, pReason: PledgeReason) = pledgeAmount + bAmount

        /**
         *  Calculate the pledge amount for the selected reward + addOns
         */
        private fun getPledgeAmount(rewards: List<Reward>, isLatePledge: Boolean): Double {
            var totalPledgeAmount = 0.0
            rewards.forEach {
                totalPledgeAmount += if (isLatePledge) {
                    if (it.latePledgeAmount() > 0) {
                        if (RewardUtils.isNoReward(it) && !it.isAddOn()) it.latePledgeAmount() // - Cost of the selected Reward
                        else it.quantity()?.let { q -> (q * it.latePledgeAmount()) } ?: it.latePledgeAmount() // - Cost of each addOn
                    } else {
                        // We don't have a late pledge amount to work with, use the default minimum
                        if (RewardUtils.isNoReward(it) && !it.isAddOn()) it.minimum() // - Default cost of the selected Reward
                        else it.quantity()?.let { q -> (q * it.minimum()) } ?: it.minimum() // - Default cost of each addOn
                    }
                } else {
                    // We have a pledge amount to work with, use it
                    if (it.pledgeAmount() > 0.0) {
                        if (RewardUtils.isNoReward(it) && !it.isAddOn()) it.pledgeAmount() // - Cost of the selected Reward during the campaign
                        else it.quantity()?.let { q -> (q * it.pledgeAmount()) } ?: it.pledgeAmount() // - Cost of each addOn during the campaign
                    } else {
                        // We don't have a pledge amount to work with, use the default minimum
                        if (RewardUtils.isNoReward(it) && !it.isAddOn()) it.minimum() // - Default cost of the selected Reward
                        else it.quantity()?.let { q -> (q * it.minimum()) } ?: it.minimum() // - Default cost of each addOn
                    }
                }
            }
            return totalPledgeAmount
        }

        /**
         *  Logic to hide/show the shipping location sections
         *  @return Pair.first ShippingRulesSection -> This section shows/Hide the shippingSelector for shippable rewards without addOns
         *  @return Pair.second ShippingRulesStaticSection -> this section shows/hides the textview showing the current shipping location
         */
        private fun shippingRulesSectionShouldHide(rewardAndAddOns: List<Reward>, reason: PledgeReason): Pair<Boolean, Boolean> {
            var hideFlags = Pair(true, true)
            val rw = rewardAndAddOns.first()

            if (reason == PledgeReason.PLEDGE || reason == PledgeReason.UPDATE_REWARD || reason == PledgeReason.UPDATE_PLEDGE) {
                val showSelector = !hasSelectedAddons(rewardAndAddOns) && RewardUtils.isShippable(rw)
                val showStaticInfo = hasSelectedAddons(rewardAndAddOns) && RewardUtils.isShippable(rw)
                hideFlags = Pair(!showSelector, !showStaticInfo)
            }

            return hideFlags
        }

        /**
         *  Choose the correct shipping location in case of a just Reward Selected
         *  - If any location available for a reward matches our default configuration select that
         *  - In case no matching between our default and the locations available for a reward
         *  just select the fist one available.
         */
        private fun defaultConfigShippingRule(rules: MutableList<ShippingRule>, config: Config) =
            rules.firstOrNull { it.location()?.country() == config.countryCode() }?.let { it } ?: rules.first()

        /**
         *  Calculate the shipping amount in case of shippable reward and reward + AddOns
         */
        private fun getShippingAmount(rule: ShippingRule, reason: PledgeReason, bShippingAmount: Float? = null, listRw: List<Reward>): Double {
            val rw = listRw.first()

            return when (reason) {
                PledgeReason.UPDATE_REWARD,
                PledgeReason.PLEDGE -> if (rw.hasAddons()) shippingCostForAddOns(listRw, rule) + rule.cost() else rule.cost()
                PledgeReason.FIX_PLEDGE,
                PledgeReason.UPDATE_PAYMENT,
                PledgeReason.LATE_PLEDGE,
                PledgeReason.UPDATE_PLEDGE -> bShippingAmount?.toDouble() ?: rule.cost()
            }
        }

        private fun shippingCostForAddOns(listRw: List<Reward>, selectedRule: ShippingRule): Double {
            var shippingCost = 0.0
            listRw.filter {
                it.isAddOn()
            }.map { rw ->
                rw.shippingRules()?.filter { rule ->
                    rule.location()?.id() == selectedRule.location()?.id()
                }?.map { rule ->
                    shippingCost += rule.cost() * (rw.quantity() ?: 1)
                }
            }

            return shippingCost
        }

        /**
         * When retrieving the backing shipping information we do have available the LocationId on the backingObject
         * and the list of shippingRules available for the selected reward, the backing shippingRule will be
         * the one matching the backingObject field locationId
         */
        private fun selectedShippingRule(shippingInfo: Pair<Long, List<ShippingRule>>): ShippingRule =
            requireNotNull(shippingInfo.second.first { it.location()?.id() == shippingInfo.first })

        /** For the checkout we need to send a list repeating as much addOns items
         * as the user has selected:
         * User selection [R, 2xa, 3xb]
         * Checkout data  [R, a, a, b, b, b]
         */
        private fun extendAddOns(flattenedList: List<Reward>): List<Reward> {
            val mutableList = mutableListOf<Reward>()

            flattenedList.map {
                if (!it.isAddOn()) mutableList.add(it)
                else {
                    val q = it.quantity() ?: 1
                    for (i in 1..q) {
                        mutableList.add(it)
                    }
                }
            }

            return mutableList.toList()
        }

        private fun hasBeenUpdated(shippingUpdated: Boolean, pReason: PledgeReason?, bHasChanged: Boolean, aUpdated: Boolean): Boolean {
            var updated = false

            if (pReason == PledgeReason.PLEDGE) updated = true
            else if (pReason == PledgeReason.UPDATE_PLEDGE) {
                updated = (bHasChanged || aUpdated) || shippingUpdated
            }

            return updated
        }

        private fun hasSelectedAddons(itemsList: List<Reward>) = itemsList.size > 1

        private fun joinProject(items: Pair<List<Reward>, Project>?): List<Pair<Project, Reward>> {
            return items?.first?.map {
                Pair(items.second, it)
            } ?: emptyList()
        }
        private fun joinRewardAndAddOns(rw: Reward, addOns: List<Reward>): List<Reward> {
            val joinedList = addOns.toMutableList()
            joinedList.add(0, rw)
            return joinedList.toList()
        }

        private fun getAmount(pAmount: Double, shippingAmount: Double, bAmount: String, pReason: PledgeReason) = pAmount + shippingAmount + bAmount.parseToDouble()

        private fun checkoutData(shippingAmount: Double, total: Double, bonusAmount: Double?, checkout: Checkout?): CheckoutData {
            return CheckoutData.builder()
                .amount(total)
                .id(checkout?.id())
                .paymentType(CreditCardPaymentType.CREDIT_CARD)
                .bonusAmount(bonusAmount)
                .shippingAmount(shippingAmount)
                .build()
        }

        private fun initialCardSelection(storedCards: List<StoredCard>, project: Project): Pair<StoredCard, Int> {
            val defaultIndex = storedCards.indexOfFirst { project.acceptedCardType(it.type()) }
            val backingPaymentSourceIndex = storedCards.indexOfFirst { it.id() == project.backing()?.paymentSource()?.id() }
            return when {
                backingPaymentSourceIndex != -1 -> Pair(storedCards[backingPaymentSourceIndex], backingPaymentSourceIndex)
                storedCards.isNotEmpty() && defaultIndex != -1 -> Pair(storedCards[defaultIndex], defaultIndex)
                else -> Pair(StoredCard.builder().build(), -1)
            }
        }

        private fun rewardTitle(project: Project, reward: Reward): String {
            val projectName = project.name()
            return when {
                RewardUtils.isNoReward(reward) -> projectName
                else -> reward.title() ?: projectName
            }
        }

        private fun shouldTrackPledgeSubmitButtonClicked(pledgeFlowContext: PledgeFlowContext) =
            pledgeFlowContext == PledgeFlowContext.NEW_PLEDGE ||
                pledgeFlowContext == PledgeFlowContext.FIX_ERRORED_PLEDGE

        private fun storedCards(): Observable<List<StoredCard>> {
            return this.apolloClient.getStoredCards()
                .compose(neverErrorV2())
        }

        private fun userEmail(): Observable<String> {
            return this.apolloClient.userPrivacy()
                .compose(neverErrorV2())
                .map { it.email }
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        // - Inputs
        override fun addedCardPosition(position: Int) = this.addedCardPosition.onNext(position)

        override fun cardSaved(storedCard: StoredCard) = this.cardSaved.onNext(storedCard)

        override fun cardSelected(storedCard: StoredCard, position: Int) = this.cardSelected.onNext(Pair(storedCard, position))

        override fun continueButtonClicked() = this.continueButtonClicked.onNext(Unit)

        override fun decreasePledgeButtonClicked() = this.decreasePledgeButtonClicked.onNext(Unit)

        override fun increasePledgeButtonClicked() = this.increasePledgeButtonClicked.onNext(Unit)

        override fun decreaseBonusButtonClicked() = this.decreaseBonusButtonClicked.onNext(Unit)

        override fun increaseBonusButtonClicked() = this.increaseBonusButtonClicked.onNext(Unit)

        override fun onRiskMessageDismissed() = this.onRiskMessageDismissed.onNext(Unit)

        override fun linkClicked(url: String) = this.linkClicked.onNext(url)

        override fun miniRewardClicked() = this.miniRewardClicked.onNext(Unit)

        override fun newCardButtonClicked() = this.newCardButtonClicked.onNext(Unit)

        override fun pledgeInput(amount: String) = this.pledgeInput.onNext(amount)

        override fun pledgeButtonClicked() = this.pledgeButtonClicked.onNext(Unit)

        override fun shippingRuleSelected(shippingRule: ShippingRule) = this.shippingRule.onNext(shippingRule)

        override fun stripeSetupResultSuccessful(@StripeIntentResult.Outcome outcome: Int) = this.stripeSetupResultSuccessful.onNext(outcome)

        override fun stripeSetupResultUnsuccessful(exception: Exception) = this.stripeSetupResultUnsuccessful.onNext(exception)

        override fun paymentSheetResult(paymentResult: PaymentSheetResult) = this.paymentSheetResult.onNext(
            paymentResult
        )

        override fun paymentSheetPresented(isSuccesfullyPresented: Boolean) = this.paySheetPresented.onNext(isSuccesfullyPresented)

        // - Outputs
        override fun addedCard(): Observable<Pair<StoredCard, Project>> = this.addedCard

        override fun additionalPledgeAmount(): Observable<String> = this.additionalPledgeAmount

        override fun additionalPledgeAmountIsGone(): Observable<Boolean> = this.additionalPledgeAmountIsGone

        override fun baseUrlForTerms(): Observable<String> = this.baseUrlForTerms

        override fun cardsAndProject(): Observable<Pair<List<StoredCard>, Project>> = this.cardsAndProject

        override fun continueButtonIsEnabled(): Observable<Boolean> = this.continueButtonIsEnabled

        override fun continueButtonIsGone(): Observable<Boolean> = this.continueButtonIsGone

        override fun conversionTextViewIsGone(): Observable<Boolean> = this.conversionTextViewIsGone

        override fun conversionText(): Observable<String> = this.conversionText

        override fun decreasePledgeButtonIsEnabled(): Observable<Boolean> = this.decreasePledgeButtonIsEnabled

        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        override fun estimatedDeliveryInfoIsGone(): Observable<Boolean> = this.estimatedDeliveryInfoIsGone

        override fun increasePledgeButtonIsEnabled(): Observable<Boolean> = this.increasePledgeButtonIsEnabled

        override fun paymentContainerIsGone(): Observable<Boolean> = this.paymentContainerIsGone

        override fun pledgeAmount(): Observable<String> = this.pledgeAmount

        override fun pledgeButtonCTA(): Observable<Int> = this.pledgeButtonCTA

        override fun pledgeButtonIsEnabled(): Observable<Boolean> = this.pledgeButtonIsEnabled

        override fun pledgeButtonIsGone(): Observable<Boolean> = this.pledgeButtonIsGone

        override fun pledgeHint(): Observable<String> = this.pledgeHint

        override fun pledgeMaximum(): Observable<String> = this.pledgeMaximum

        override fun pledgeMaximumIsGone(): Observable<Boolean> = this.pledgeMaximumIsGone

        override fun pledgeMinimum(): Observable<String> = this.pledgeMinimum

        override fun pledgeProgressIsGone(): Observable<Boolean> = this.pledgeProgressIsGone

        override fun pledgeSectionIsGone(): Observable<Boolean> = this.pledgeSectionIsGone

        override fun pledgeSummaryAmount(): Observable<CharSequence> = this.pledgeSummaryAmount

        override fun pledgeSummaryIsGone(): Observable<Boolean> = this.pledgeSummaryIsGone

        override fun pledgeTextColor(): Observable<Int> = this.pledgeTextColor

        override fun projectCurrencySymbol(): Observable<Pair<SpannableString, Boolean>> = this.projectCurrencySymbol

        override fun rewardSummaryIsGone(): Observable<Boolean> = this.rewardSummaryIsGone

        override fun rewardTitle(): Observable<String> = this.rewardTitle

        override fun selectedShippingRule(): Observable<ShippingRule> = this.shippingRule

        override fun shippingAmount(): Observable<CharSequence> = this.shippingAmount

        override fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>> = this.shippingRulesAndProject

        override fun shippingRulesSectionIsGone(): BehaviorSubject<Boolean> = this.shippingRulesSectionIsGone

        override fun shippingSummaryAmount(): Observable<CharSequence> = this.shippingSummaryAmount

        override fun shippingSummaryLocation(): Observable<String> = this.shippingSummaryLocation

        override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone

        override fun showPledgeError(): Observable<Unit> = this.showPledgeError

        override fun showPledgeSuccess(): Observable<Pair<CheckoutData, PledgeData>> = this.showPledgeSuccess

        override fun showSelectedCard(): Observable<Pair<Int, CardState>> = this.showSelectedCard

        override fun showSCAFlow(): Observable<String> = this.showSCAFlow

        override fun showUpdatePaymentError(): Observable<Unit> = this.showUpdatePaymentError

        override fun showUpdatePaymentSuccess(): Observable<Unit> = this.showUpdatePaymentSuccess

        override fun showUpdatePledgeError(): Observable<Unit> = this.showUpdatePledgeError

        override fun showUpdatePledgeSuccess(): Observable<Unit> = this.showUpdatePledgeSuccess

        override fun startChromeTab(): Observable<String> = this.startChromeTab

        override fun startLoginToutActivity(): Observable<Unit> = this.startLoginToutActivity

        override fun totalAmount(): Observable<CharSequence> = this.totalAmount

        override fun totalAndDeadline(): Observable<Pair<String, String>> = this.totalAndDeadline

        override fun totalAndDeadlineIsVisible(): Observable<Unit> = this.totalAndDeadlineIsVisible

        override fun totalDividerIsGone(): Observable<Boolean> = this.totalDividerIsGone

        override fun headerSectionIsGone(): Observable<Boolean> = this.headerSectionIsGone

        override fun headerSelectedItems(): Observable<List<Pair<Project, Reward>>> = this.headerSelectedItems

        override fun isPledgeMinimumSubtitleGone(): Observable<Boolean> = this.isPledgeMinimumSubtitleGone

        override fun isBonusSupportSectionGone(): Observable<Boolean> = this.isBonusSupportSectionGone

        override fun bonusAmount(): Observable<String> = this.bonusAmount

        override fun decreaseBonusButtonIsEnabled(): Observable<Boolean> = this.decreaseBonusButtonIsEnabled

        override fun increaseBonusButtonIsEnabled(): Observable<Boolean> = this.increaseBonusButtonIsEnabled

        override fun bonusHint(): Observable<String> = this.bonusHint

        override fun isNoReward(): Observable<Boolean> = this.isNoReward

        override fun projectTitle(): Observable<String> = this.projectTitle

        override fun rewardAndAddOns(): Observable<List<Reward>> = this.rewardAndAddOns

        override fun shippingRuleStaticIsGone(): Observable<Boolean> = this.shippingRuleStaticIsGone
        override fun bonusSummaryAmount(): Observable<CharSequence> = this.bonusSummaryAmount

        override fun bonusSummaryIsGone(): Observable<Boolean> = this.bonusSummaryIsGone

        override fun pledgeAmountHeader(): Observable<CharSequence> = this.pledgeAmountHeader

        override fun shippingRule(): Observable<ShippingRule> = this.shippingRule

        override fun localPickUpIsGone(): Observable<Boolean> =
            localPickUpIsGone

        override fun localPickUpName(): Observable<String> =
            localPickUpName

        override fun presentPaymentSheet(): Observable<Pair<String, String>> =
            this.presentPaymentSheet

        override fun showError(): Observable<String> =
            this.showError

        override fun setState(): Observable<State> = this.loadingState

        override fun eventSent(): Observable<Boolean> = this.thirdpartyEventIsSuccessful
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val environment: Environment, private val bundle: Bundle? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PledgeFragmentViewModel(
                environment,
                bundle = bundle
            ) as T
        }
    }
}

/**
 * Obtain the data model input that will be send to UpdateBacking mutation
 * - When updating payment method with a new payment method using payment sheet
 * - When updating payment method with a previously existing payment source
 * - Updating any other parameter like location, amount or rewards
 */
fun PledgeFragmentViewModel.PledgeFragmentViewModel.getUpdateBackingData(
    backing: Backing,
    amount: String? = null,
    locationId: String? = null,
    rewardsList: List<Reward> = listOf(),
    pMethod: StoredCard? = null
): UpdateBackingData {
    return pMethod?.let { card ->
        // - Updating the payment method, a new one from PaymentSheet or already existing one
        if (card.isFromPaymentSheet()) UpdateBackingData(
            backing,
            amount,
            locationId,
            rewardsList,
            intentClientSecret = card.clientSetupId()
        )
        else UpdateBackingData(
            backing,
            amount,
            locationId,
            rewardsList,
            paymentSourceId = card.id()
        )
        // - Updating amount, location or rewards
    } ?: UpdateBackingData(
        backing,
        amount,
        locationId,
        rewardsList
    )
}
