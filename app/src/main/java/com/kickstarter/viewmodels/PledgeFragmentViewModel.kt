package com.kickstarter.viewmodels

import android.content.SharedPreferences
import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.CHECKOUT_PAYMENT_PAGE_VIEWED
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.NumberOptions
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.*
import com.kickstarter.models.*
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.services.mutations.UpdateBackingData
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.*
import com.kickstarter.ui.fragments.PledgeFragment
import com.stripe.android.StripeIntentResult
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import type.CreditCardPaymentType
import java.math.RoundingMode
import java.net.CookieManager
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

        /** Emits when we should show the [com.kickstarter.ui.fragments.NewCardFragment]. */
        fun showNewCardFragment(): Observable<Project>

        /**  Emits when the pledge call was unsuccessful. */
        fun showPledgeError(): Observable<Void>

        /** Emits when the creating backing mutation was successful. */
        fun showPledgeSuccess(): Observable<Pair<CheckoutData, PledgeData>>

        /** Emits when the cards adapter should update the selected position. */
        fun showSelectedCard(): Observable<Pair<Int, CardState>>

        /** Emits when we should show the SCA flow with the client secret. */
        fun showSCAFlow(): Observable<String>

        /**  Emits when the update payment source mutation was unsuccessful. */
        fun showUpdatePaymentError(): Observable<Void>

        /**  Emits when the update payment source mutation was successful. */
        fun showUpdatePaymentSuccess(): Observable<Void>

        /** Emits when the update pledge call was unsuccessful. */
        fun showUpdatePledgeError(): Observable<Void>

        /** Emits when the update pledge call was successful. */
        fun showUpdatePledgeSuccess(): Observable<Void>

        /** Emits when we should start a Chrome tab. */
        fun startChromeTab(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.LoginToutActivity]. */
        fun startLoginToutActivity(): Observable<Void>

        /** Emits the total amount string of the pledge. */
        fun totalAmount(): Observable<CharSequence>

        /** Emits the total pledge amount in the project's currency and the project's deadline. */
        fun totalAndDeadline(): Observable<Pair<String, String>>

        /** Emits when the total and deadline warning should be shown. */
        fun totalAndDeadlineIsVisible(): Observable<Void>

        /** Emits a boolean determining if the divider above the total should be hidden. */
        fun totalDividerIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the header whould be hidden */
        fun headerSectionIsGone(): Observable<Boolean>

        /** Emits a Pair containing reward/add-on title and the amount */
        fun titleAndAmount(): Observable<Pair<String, String>>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val addedCardPosition = PublishSubject.create<Int>()
        private val cardSaved = PublishSubject.create<StoredCard>()
        private val cardSelected = PublishSubject.create<Pair<StoredCard, Int>>()
        private val continueButtonClicked = PublishSubject.create<Void>()
        private val decreasePledgeButtonClicked = PublishSubject.create<Void>()
        private val increasePledgeButtonClicked = PublishSubject.create<Void>()
        private val linkClicked = PublishSubject.create<String>()
        private val miniRewardClicked = PublishSubject.create<Void>()
        private val newCardButtonClicked = PublishSubject.create<Void>()
        private val pledgeButtonClicked = PublishSubject.create<Void>()
        private val pledgeInput = PublishSubject.create<String>()
        private val shippingRule = PublishSubject.create<ShippingRule>()
        private val stripeSetupResultSuccessful = PublishSubject.create<Int>()
        private val stripeSetupResultUnsuccessful = PublishSubject.create<Exception>()

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
        private val selectedShippingRule = BehaviorSubject.create<ShippingRule>()
        private val shippingAmount = BehaviorSubject.create<CharSequence>()
        private val shippingRulesAndProject = BehaviorSubject.create<Pair<List<ShippingRule>, Project>>()
        private val shippingRulesSectionIsGone = BehaviorSubject.create<Boolean>()
        private val shippingSummaryAmount = BehaviorSubject.create<CharSequence>()
        private val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val shippingSummaryLocation = BehaviorSubject.create<String>()
        private val showNewCardFragment = PublishSubject.create<Project>()
        private val showPledgeError = PublishSubject.create<Void>()
        private val showPledgeSuccess = PublishSubject.create<Pair<CheckoutData, PledgeData>>()
        private val showSelectedCard = BehaviorSubject.create<Pair<Int, CardState>>()
        private val showSCAFlow = PublishSubject.create<String>()
        private val showUpdatePaymentError = PublishSubject.create<Void>()
        private val showUpdatePaymentSuccess = PublishSubject.create<Void>()
        private val showUpdatePledgeError = PublishSubject.create<Void>()
        private val showUpdatePledgeSuccess = PublishSubject.create<Void>()
        private val startChromeTab = PublishSubject.create<String>()
        private val startLoginToutActivity = PublishSubject.create<Void>()
        private val totalAmount = BehaviorSubject.create<CharSequence>()
        private val totalAndDeadline = BehaviorSubject.create<Pair<String, String>>()
        private val totalAndDeadlineIsVisible = BehaviorSubject.create<Void>()
        private val totalDividerIsGone = BehaviorSubject.create<Boolean>()
        private val headerSectionIsGone = BehaviorSubject.create<Boolean>()
        private val titleAndAmount = BehaviorSubject.create<Pair<String, String>>()

        private val apiClient = environment.apiClient()
        private val apolloClient = environment.apolloClient()
        private val optimizely = environment.optimizely()
        private val cookieManager: CookieManager = environment.cookieManager()
        private val currentConfig = environment.currentConfig()
        private val currentUser = environment.currentUser()
        private val ksCurrency = environment.ksCurrency()
        private val sharedPreferences: SharedPreferences = environment.sharedPreferences()
        private val variantSuggestedAmount = BehaviorSubject.create<Double>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val userIsLoggedIn = this.currentUser.isLoggedIn
                    .distinctUntilChanged()

            val pledgeData = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData? }
                    .ofType(PledgeData::class.java)

            val reward = pledgeData
                    .map { it.reward() }

            val projectData = pledgeData
                    .map { it.projectData() }

            val fullProjectDataAndPledgeData = projectData
                    .compose<Pair<ProjectData, PledgeData>>(combineLatestPair(pledgeData))

            reward
                .map { RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.CONTROL, it) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(variantSuggestedAmount)

            val project = projectData
                    .map { it.project() }

            val pledgeReason = arguments()
                    .map { it.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason }

            val updatingPayment = pledgeReason
                    .map { it == PledgeReason.UPDATE_PAYMENT || it == PledgeReason.FIX_PLEDGE }
                    .distinctUntilChanged()

            val updatingPaymentOrUpdatingPledge = pledgeReason
                    .map { it == PledgeReason.UPDATE_PAYMENT || it == PledgeReason.UPDATE_PLEDGE || it == PledgeReason.FIX_PLEDGE  }
                    .distinctUntilChanged()

            val projectAndReward = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))

            val backing = projectAndReward
                    .filter { BackingUtils.isBacked(it.first, it.second) }
                    .map { it.first.backing() }
                    .ofType(Backing::class.java)

            // Reward summary section
            projectAndReward
                    .map { rewardTitle(it.first, it.second) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardTitle)

            reward
                    .map { RewardUtils.isNoReward(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.headerSectionIsGone)

            reward
                    .map { it.estimatedDeliveryOn() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { dateTime -> dateTime?.let { DateTimeUtils.estimatedDeliveryOn(it) } }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.estimatedDelivery)

            reward
                    .map { ObjectUtils.isNull(it.estimatedDeliveryOn()) || RewardUtils.isNoReward(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.estimatedDeliveryInfoIsGone)

            //Base pledge amount
            val rewardMinimum = reward
                    .map { it.minimum() }
                    .distinctUntilChanged()

            rewardMinimum
                    .map { NumberUtils.format(it.toInt()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeHint)

            Observable.combineLatest(rewardMinimum, variantSuggestedAmount, reward, project)
            { rewardMinimum, variantSuggestedAmount, reward, project ->
                if (RewardUtils.isNoReward(reward)) {
                    return@combineLatest this.ksCurrency.format(variantSuggestedAmount, project)
                } else return@combineLatest this.ksCurrency.format(rewardMinimum, project)
            }
            .distinctUntilChanged()
            .compose(bindToLifecycle())
            .subscribe(this.pledgeMinimum)

            reward
                    .filter { !RewardUtils.isNoReward(it) }
                    .compose<Pair<Reward, String>>(combineLatestPair(this.pledgeMinimum))
                    .map { Pair(it.first.title()?: "", it.second) }
                    .compose(bindToLifecycle())
                    .subscribe(this.titleAndAmount)

            project
                    .map { ProjectViewUtils.currencySymbolAndPosition(it, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.projectCurrencySymbol)

            // Pledge stepper section
            val additionalPledgeAmount = BehaviorSubject.create(0.0)

            val additionalAmountOrZero = additionalPledgeAmount
                    .map { max(0.0, it) }

            val country = project
                    .map { Country.findByCurrencyCode(it.currency()) }
                    .filter { it != null }
                    .distinctUntilChanged()
                    .ofType(Country::class.java)

            val stepAmount = country
                    .map { it.minPledge }

            additionalAmountOrZero
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { this.ksCurrency.format(it.first, it.second, RoundingMode.HALF_UP) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.additionalPledgeAmount)

            additionalAmountOrZero
                    .map { it <= 0.0 }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.additionalPledgeAmountIsGone)

            val initialAmount = rewardMinimum
                    .compose<Pair<Double, Boolean>>(combineLatestPair(updatingPaymentOrUpdatingPledge))
                    .filter { BooleanUtils.isFalse(it.second) }
                    .map { it.first }

            val backingAmount = backing
                    .map { it.amount() - it.shippingAmount() }
                    .distinctUntilChanged()

            val pledgeInput = Observable.merge(initialAmount, backingAmount, this.pledgeInput.map { NumberUtils.parse(it) })
                    .distinctUntilChanged()

            pledgeInput
                    .compose<Double>(takeWhen(this.increasePledgeButtonClicked))
                    .compose<Pair<Double, Int>>(combineLatestPair(stepAmount))
                    .map { it.first + it.second }
                    .map { it.toString() }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeInput)

            pledgeInput
                    .compose<Double>(takeWhen(this.decreasePledgeButtonClicked))
                    .compose<Pair<Double, Int>>(combineLatestPair(stepAmount))
                    .map { it.first - it.second }
                    .map { it.toString() }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeInput)

            pledgeInput
                    .compose<Pair<Double, Double>>(combineLatestPair(rewardMinimum))
                    .map { it.first - it.second }
                    .compose(bindToLifecycle())
                    .subscribe { additionalPledgeAmount.onNext(it) }

            pledgeInput
                    .compose<Pair<Double, Double>>(combineLatestPair(rewardMinimum))
                    .map { max(it.first, it.second) > it.second }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.decreasePledgeButtonIsEnabled)

            pledgeInput
                    .map { NumberUtils.format(it.toFloat(), NumberOptions.builder().precision(NumberUtils.precision(it, RoundingMode.HALF_UP)).build()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeAmount)

            updatingPayment
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeSectionIsGone)

            // Shipping rules section
            val shippingRules = projectAndReward
                    .filter { RewardUtils.isShippable(it.second) }
                    .distinctUntilChanged()
                    .switchMap<ShippingRulesEnvelope> { this.apiClient.fetchShippingRules(it.first, it.second).compose(neverError()) }
                    .map { it.shippingRules() }
                    .share()

            val rulesAndProject = shippingRules
                    .compose<Pair<List<ShippingRule>, Project>>(combineLatestPair(project))

            rulesAndProject
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRulesAndProject)

            updatingPayment
                    .compose<Pair<Boolean, Reward>>(combineLatestPair(reward))
                    .map { it.first || !RewardUtils.isShippable(it.second) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRulesSectionIsGone)

            val defaultShippingRule = shippingRules
                    .filter { it.isNotEmpty() }
                    .compose<Pair<List<ShippingRule>, PledgeReason>>(combineLatestPair(pledgeReason))
                    .filter { it.second == PledgeReason.PLEDGE || it.second == PledgeReason.UPDATE_REWARD }
                    .switchMap { defaultShippingRule(it.first) }

            val backingShippingRule = shippingRules
                    .filter { it.isNotEmpty() }
                    .compose<Pair<List<ShippingRule>, Backing>>(combineLatestPair(backing))
                    .switchMap { backingShippingRule(it.first, it.second) }
                    .filter { it != null }

            val shippingRule = Observable.merge(this.shippingRule, defaultShippingRule, backingShippingRule)

            shippingRule
                    .compose(bindToLifecycle())
                    .subscribe(this.selectedShippingRule)

            val unshippableShippingAmount = reward
                    .filter { !RewardUtils.isShippable(it) || RewardUtils.isNoReward(it) }
                    .map { 0.0 }

            val shippingAmount = Observable.merge(unshippableShippingAmount, shippingRule.map { it.cost() })

            shippingAmount
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingAmount)

            // Total pledge section
            val total = pledgeInput
                    .compose<Pair<Double, Double>>(combineLatestPair(shippingAmount))
                    .map { it.first + it.second }
                    .distinctUntilChanged()

            total
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.totalAmount)

            total
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { Pair(this.ksCurrency.format(it.first, it.second, RoundingMode.HALF_UP), it.second) }
                    .filter { ObjectUtils.isNotNull(it.second.deadline()) }
                    .map { totalAndProject -> totalAndProject.second.deadline()?.let { Pair(totalAndProject.first, DateTimeUtils.longDate(it)) } }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.totalAndDeadline)

            this.totalAndDeadline
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.totalAndDeadlineIsVisible)

            total
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .filter { it.second.currency() != it.second.currentCurrency() }
                    .map { this.ksCurrency.formatWithUserPreference(it.first, it.second, RoundingMode.UP, 2) }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversionText)

            projectAndReward
                    .map { it.first.currency() != it.first.currentCurrency() }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.conversionTextViewIsGone)

            val minimumPledge = rewardMinimum
                    .compose<Pair<Double, Double>>(combineLatestPair(shippingAmount))
                    .map { it.first + it.second }
                    .distinctUntilChanged()

            val currencyMaximum = country
                    .map { it.maxPledge.toDouble() }
                    .distinctUntilChanged()

            val pledgeMaximum = currencyMaximum
                    .compose<Pair<Double, Double>>(combineLatestPair(shippingAmount))
                    .map { it.first - it.second }

            val minAndMaxPledge = rewardMinimum
                    .compose<Pair<Double, Double>>(combineLatestPair(pledgeMaximum))

            pledgeInput
                    .compose<Pair<Double, Pair<Double, Double>>>(combineLatestPair(minAndMaxPledge))
                    .map { it.first in it.second.first..it.second.second }
                    .map { if (it) R.color.ksr_green_500 else R.color.ksr_red_400 }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeTextColor)

            val pledgeMaximumIsGone = pledgeMaximum
                    .compose<Pair<Double, Double>>(combineLatestPair(pledgeInput))
                    .map { it.first >= it.second }
                    .distinctUntilChanged()

            pledgeMaximumIsGone
                    .compose<Pair<Boolean, Boolean>>(combineLatestPair(updatingPayment))
                    .filter { BooleanUtils.isFalse(it.second) }
                    .map { it.first }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeMaximumIsGone)

            pledgeMaximum
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { this.ksCurrency.format(it.first, it.second, RoundingMode.HALF_UP) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeMaximum)

            val stepAndMaxPledge = stepAmount
                    .map { it.toDouble() }
                    .compose<Pair<Double, Double>>(combineLatestPair(pledgeMaximum))

            pledgeInput
                    .compose<Pair<Double, Pair<Double, Double>>>(combineLatestPair(stepAndMaxPledge))
                    .map { it.second.second - it.first >= it.second.first }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.increasePledgeButtonIsEnabled)

            // Manage pledge section
            backingAmount
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeSummaryAmount)

            updatingPayment
                    .compose(bindToLifecycle())
                    .subscribe(this.totalDividerIsGone)

            updatingPayment
                    .map { BooleanUtils.negate(it) }
                    .compose<Pair<Boolean, Reward>>(combineLatestPair(reward))
                    .map { it.first || !RewardUtils.isShippable(it.second) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.pledgeSummaryIsGone.onNext(it)
                        this.shippingSummaryIsGone.onNext(it)
                    }

            backing
                    .map { it.shippingAmount().toDouble() }
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { ProjectViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingSummaryAmount)

            backingShippingRule
                    .map { it.location().displayableName() }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingSummaryLocation)

            val updatingPledge = pledgeReason
                    .map { it == PledgeReason.UPDATE_PLEDGE }

            val updatingReward = pledgeReason
                    .filter { it == PledgeReason.UPDATE_REWARD }
                    .map { true }

            val shippingRuleUpdated = this.selectedShippingRule
                    .compose<Pair<ShippingRule, Boolean>>(combineLatestPair(updatingPledge))
                    .filter { it.second }
                    .map { it.first }
                    .compose<Pair<ShippingRule, ShippingRule>>(combineLatestPair(backingShippingRule))
                    .map { it.first != it.second }
                    .startWith(false)

            val amountUpdated = pledgeInput
                    .compose<Pair<Double, Boolean>>(combineLatestPair(updatingPledge))
                    .filter { it.second }
                    .map { it.first }
                    .compose<Pair<Double, Double>>(combineLatestPair(backingAmount))
                    .map { it.first != it.second }
                    .startWith(false)

            val shippingOrAmountChanged = shippingRuleUpdated
                    .compose<Pair<Boolean, Boolean>>(combineLatestPair(amountUpdated))
                    .map { it.first || it.second }
                    .distinctUntilChanged()

            val minAndMaxTotal = minimumPledge
                    .compose<Pair<Double, Double>>(combineLatestPair(currencyMaximum))

            val totalIsValid = total
                    .compose<Pair<Double, Pair<Double, Double>>>(combineLatestPair(minAndMaxTotal))
                    .map { it.first in it.second.first..it.second.second }
                    .distinctUntilChanged()

            val validChange = shippingOrAmountChanged
                    .compose<Pair<Boolean, Boolean>>(combineLatestPair(totalIsValid))
                    .map { it.first && it.second }

            val changeDuringUpdatingPledge = validChange
                    .compose<Pair<Boolean, Boolean>>(combineLatestPair(updatingPledge))
                    .filter { BooleanUtils.isTrue(it.second) }
                    .map { it.first }

            Observable.merge(updatingReward, changeDuringUpdatingPledge)
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeButtonIsEnabled)

            // Payment section
            pledgeReason
                    .map { it == PledgeReason.UPDATE_PLEDGE || it == PledgeReason.UPDATE_REWARD }
                    .compose<Pair<Boolean, Boolean>>(combineLatestPair(userIsLoggedIn))
                    .map { it.first || !it.second }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.paymentContainerIsGone)

            userIsLoggedIn
                    .compose(bindToLifecycle())
                    .subscribe(this.continueButtonIsGone)

            userIsLoggedIn
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe { this.pledgeButtonIsGone.onNext(it) }

            val storedCards = BehaviorSubject.create<List<StoredCard>>()

            userIsLoggedIn
                    .filter { BooleanUtils.isTrue(it) }
                    .compose<Pair<Boolean, PledgeReason>>(combineLatestPair(pledgeReason))
                    .filter { it.second == PledgeReason.PLEDGE || it.second == PledgeReason.UPDATE_PAYMENT || it.second == PledgeReason.FIX_PLEDGE }
                    .take(1)
                    .switchMap { storedCards() }
                    .compose(bindToLifecycle())
                    .subscribe { storedCards.onNext(it) }

            val cardsAndProject = storedCards
                    .compose<Pair<List<StoredCard>, Project>>(combineLatestPair(project))

            cardsAndProject
                    .compose(bindToLifecycle())
                    .subscribe { this.cardsAndProject.onNext(it) }

            val initialCardSelection = cardsAndProject
                    .take(1)
                    .map { initialCardSelection(it.first, it.second) }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { it as Pair<StoredCard, Int> }

            this.cardSaved
                    .compose<Pair<StoredCard, Project>>(combineLatestPair(project))
                    .compose(bindToLifecycle())
                    .subscribe(this.addedCard)

            val selectedCardAndPosition = Observable.merge(initialCardSelection,
                    this.cardSelected,
                    this.cardSaved.compose<Pair<StoredCard, Int>>(zipPair(this.addedCardPosition)))

            selectedCardAndPosition
                    .map { it.second }
                    .compose(bindToLifecycle())
                    .subscribe { this.showSelectedCard.onNext(Pair(it, CardState.SELECTED)) }

            project
                    .compose<Project>(takeWhen(this.newCardButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.showNewCardFragment)

            this.continueButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.startLoginToutActivity)

            userIsLoggedIn
                    .filter { BooleanUtils.isFalse(it) }
                    .compose<Pair<Boolean, PledgeReason>>(combineLatestPair(pledgeReason))
                    .filter { it.second == PledgeReason.PLEDGE }
                    .compose<Pair<Pair<Boolean, PledgeReason>, Boolean>>(combineLatestPair(totalIsValid))
                    .map { it.second }
                    .compose(bindToLifecycle())
                    .subscribe(this.continueButtonIsEnabled)

            selectedCardAndPosition
                    .compose(ignoreValues())
                    .compose<Pair<Void, Boolean>>(combineLatestPair(totalIsValid))
                    .map { it.second }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeButtonIsEnabled)

            val pledgeButtonClicked = userIsLoggedIn
                    .compose<Pair<Boolean, PledgeReason>>(combineLatestPair(pledgeReason))
                    .filter { it.first && it.second == PledgeReason.PLEDGE }
                    .compose<Pair<Boolean, PledgeReason>>(takeWhen(this.pledgeButtonClicked))
                    .compose(ignoreValues())

            // An observable of the ref tag stored in the cookie for the project. Can emit `null`.
            val cookieRefTag = project
                    .take(1)
                    .map { p -> RefTagUtils.storedCookieRefTagForProject(p, this.cookieManager, this.sharedPreferences) }

            val locationId: Observable<String?> = shippingRule.map { it.location() }
                    .map { it.id() }
                    .map { it.toString() }
                    .startWith(null as String?)

            val backingToUpdate = project
                    .filter { it.isBacking }
                    .map { it.backing() }
                    .ofType(Backing::class.java)
                    .distinctUntilChanged()

            val paymentMethodId: Observable<String> = selectedCardAndPosition.map { it.first.id() }

            val createBackingNotification = Observable.combineLatest(project,
                    total.map { it.toString() },
                    paymentMethodId,
                    locationId,
                    reward,
                    cookieRefTag)
            { p, a, id, l, r, c -> CreateBackingData(p, a, id, l, r, c) }
                    .compose<CreateBackingData>(takeWhen(pledgeButtonClicked))
                    .switchMap {
                        this.apolloClient.createBacking(it)
                                .doOnSubscribe {
                                    this.pledgeProgressIsGone.onNext(false)
                                    this.pledgeButtonIsEnabled.onNext(false)
                                }
                                .materialize()
                    }
                    .share()

            val totalString: Observable<String?> = total
                    .map { it.toString() }
                    .startWith(null as String?)

            val updatePaymentClick = pledgeReason
                    .compose<PledgeReason>(takeWhen(this.pledgeButtonClicked))
                    .filter { it == PledgeReason.UPDATE_PAYMENT }
                    .compose(ignoreValues())

            val fixPaymentClick = pledgeReason
                    .compose<PledgeReason>(takeWhen(this.pledgeButtonClicked))
                    .filter { it == PledgeReason.FIX_PLEDGE }
                    .compose(ignoreValues())

            val updatePledgeClick = pledgeReason
                    .compose<PledgeReason>(takeWhen(this.pledgeButtonClicked))
                    .filter { it == PledgeReason.UPDATE_PLEDGE || it == PledgeReason.UPDATE_REWARD }
                    .compose(ignoreValues())

            val optionalPaymentMethodId: Observable<String?> = paymentMethodId
                    .startWith(null as String?)

            val updateBackingNotification = Observable.combineLatest(backingToUpdate,
                    totalString,
                    locationId,
                    reward,
                    optionalPaymentMethodId)
            { b, a, l, r, p -> UpdateBackingData(b, a, l, r, p) }
                    .compose<UpdateBackingData>(takeWhen(Observable.merge(updatePledgeClick, updatePaymentClick, fixPaymentClick)))
                    .switchMap {
                        this.apolloClient.updateBacking(it)
                                .doOnSubscribe {
                                    this.pledgeProgressIsGone.onNext(false)
                                    this.pledgeButtonIsEnabled.onNext(false)
                                }
                                .materialize()
                    }
                    .share()

            val checkoutResult = Observable.merge(createBackingNotification, updateBackingNotification)
                    .compose(values())

            val successfulSCACheckout = checkoutResult
                    .compose<Checkout>(takeWhen(this.stripeSetupResultSuccessful.filter { it == StripeIntentResult.Outcome.SUCCEEDED }))

            val successfulCheckout = checkoutResult
                    .filter { BooleanUtils.isFalse(it.backing().requiresAction()) }

            val successfulBacking = successfulCheckout
                    .map { it.backing() }

            val successAndPledgeReason = Observable.merge(successfulBacking,
                    this.stripeSetupResultSuccessful.filter { it == StripeIntentResult.Outcome.SUCCEEDED })
                    .compose<Pair<Any, PledgeReason>>(combineLatestPair(pledgeReason))

            Observable.combineLatest<Double, Double, Checkout, CheckoutData>(shippingAmount, total, Observable.merge(successfulCheckout, successfulSCACheckout))
            { s, t, c -> checkoutData(s, t, c) }
                    .compose<Pair<CheckoutData, PledgeData>>(combineLatestPair(pledgeData))
                    .filter { it.second.pledgeFlowContext() == PledgeFlowContext.NEW_PLEDGE }
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeSuccess)

            successAndPledgeReason
                    .filter { it.second == PledgeReason.UPDATE_PLEDGE || it.second == PledgeReason.UPDATE_REWARD }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledgeSuccess)

            successAndPledgeReason
                    .filter { it.second == PledgeReason.UPDATE_PAYMENT || it.second == PledgeReason.FIX_PLEDGE }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePaymentSuccess)

            Observable.merge(createBackingNotification, updateBackingNotification)
                    .compose(values())
                    .map { it.backing() }
                    .filter { BooleanUtils.isTrue(it.requiresAction()) }
                    .map { it.clientSecret() }
                    .compose(bindToLifecycle())
                    .subscribe(this.showSCAFlow)

            val createOrUpdateError = Observable.merge(createBackingNotification.compose(errors()),
                    updateBackingNotification.compose(errors()))

            val stripeSetupError = Observable.merge(this.stripeSetupResultUnsuccessful,
                    this.stripeSetupResultSuccessful.filter { it != StripeIntentResult.Outcome.SUCCEEDED })

            val errorAndPledgeReason = Observable.merge(createOrUpdateError, stripeSetupError)
                    .compose(ignoreValues())
                    .compose<Pair<Void, PledgeReason>>(combineLatestPair(pledgeReason))

            errorAndPledgeReason
                    .filter { it.second == PledgeReason.PLEDGE }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.pledgeProgressIsGone.onNext(true)
                        this.pledgeButtonIsEnabled.onNext(true)
                        this.showPledgeError.onNext(null)
                    }

            errorAndPledgeReason
                    .filter { it.second == PledgeReason.UPDATE_PLEDGE || it.second == PledgeReason.UPDATE_REWARD }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.pledgeProgressIsGone.onNext(true)
                        this.pledgeButtonIsEnabled.onNext(true)
                        this.showUpdatePledgeError.onNext(null)
                    }

            errorAndPledgeReason
                    .filter { it.second == PledgeReason.UPDATE_PAYMENT || it.second == PledgeReason.FIX_PLEDGE }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.pledgeProgressIsGone.onNext(true)
                        this.pledgeButtonIsEnabled.onNext(true)
                        this.showUpdatePaymentError.onNext(null)
                    }

            this.baseUrlForTerms.onNext(this.environment.webEndpoint())

            this.linkClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.startChromeTab)

            pledgeReason
                    .map { if (it == PledgeReason.PLEDGE) R.string.Pledge else R.string.Confirm }
                    .compose(bindToLifecycle())
                    .subscribe { this.pledgeButtonCTA.onNext(it) }

            //Tracking
            val projectAndTotal = project
                    .compose<Pair<Project, Double>>(combineLatestPair(total))

            val projectAndTotalForInitialPledges = pledgeReason
                    .filter { it == PledgeReason.PLEDGE }
                    .compose<Pair<PledgeReason, Pair<Project, Double>>>(combineLatestPair(projectAndTotal))
                    .map { it.second }

            projectAndTotalForInitialPledges
                    .take(1)
                    .map { it.first }
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackPledgeScreenViewed(it) }

            projectAndTotalForInitialPledges
                    .compose<Pair<Project, Double>>(takeWhen(this.newCardButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackAddNewCardButtonClicked(it.first, it.second) }

            projectAndTotalForInitialPledges
                    .compose<Pair<Project, Double>>(takeWhen(this.pledgeButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackPledgeButtonClicked(it.first, it.second) }

            projectAndTotal
                    .compose<Pair<Project, Double>>(takeWhen(updatePledgeClick))
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackUpdatePledgeButtonClicked(it.first, it.second) }

            project
                    .compose<Project>(takeWhen(updatePaymentClick))
                    .compose<Pair<Project, PledgeReason>>(combineLatestPair(pledgeReason))
                    .filter { it.second == PledgeReason.UPDATE_PAYMENT }
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackUpdatePaymentMethodButtonClicked(it.first) }

            pledgeData
                    .take(1)
                    .filter { it.pledgeFlowContext() == PledgeFlowContext.NEW_PLEDGE }
                    .compose(bindToLifecycle())
                    .subscribe { this.lake.trackCheckoutPaymentPageViewed(it) }

            fullProjectDataAndPledgeData
                    .take(1)
                    .filter { it.second.pledgeFlowContext() == PledgeFlowContext.NEW_PLEDGE }
                    .compose<Pair<Pair<ProjectData, PledgeData>, User?>>(combineLatestPair(this.currentUser.observable()))
                    .map { ExperimentData(it.second, it.first.first.refTagFromIntent(), it.first.first.refTagFromCookie()) }
                    .compose(bindToLifecycle())
                    .subscribe { this.optimizely.track(CHECKOUT_PAYMENT_PAGE_VIEWED, it) }

            Observable.combineLatest<Double, Double, CheckoutData>(shippingAmount, total)
            { s, t -> checkoutData(s, t, null) }
                    .compose<Pair<CheckoutData, PledgeData>>(combineLatestPair(pledgeData))
                    .filter { shouldTrackPledgeSubmitButtonClicked(it.second.pledgeFlowContext()) }
                    .compose<Pair<CheckoutData, PledgeData>>(takeWhen(this.pledgeButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe { this.lake.trackPledgeSubmitButtonClicked(it.first, it.second) }
        }

        private fun getMinimumRewardAmount(rewardAndVariant: Pair<Reward, Double>): Double {
            return if (RewardUtils.isNoReward(rewardAndVariant.first))
                    rewardAndVariant.second
                else rewardAndVariant.first.minimum()
        }

        private fun backingShippingRule(shippingRules: List<ShippingRule>, backing: Backing): Observable<ShippingRule> {
            return Observable.just(shippingRules.firstOrNull { it.location().id() == backing.locationId() })
        }

        private fun checkoutData(shippingAmount: Double, total: Double, checkout: Checkout?): CheckoutData {
            return CheckoutData.builder()
                    .amount(total)
                    .id(checkout?.id())
                    .paymentType(CreditCardPaymentType.CREDIT_CARD)
                    .shippingAmount(shippingAmount)
                    .build()
        }

        private fun defaultShippingRule(shippingRules: List<ShippingRule>): Observable<ShippingRule> {
            return this.currentConfig.observable()
                    .map { it.countryCode() }
                    .map { countryCode ->
                        shippingRules.firstOrNull { it.location().country() == countryCode }
                                ?: shippingRules.first()
                    }
        }

        private fun initialCardSelection(storedCards: List<StoredCard>, project: Project): Pair<StoredCard, Int>? {
            val defaultIndex = storedCards.indexOfFirst { ProjectUtils.acceptedCardType(it.type(), project) }
            val backingPaymentSourceIndex = storedCards.indexOfFirst { it.id() == project.backing()?.paymentSource()?.id() }
            return when {
                backingPaymentSourceIndex != -1 -> Pair(storedCards[backingPaymentSourceIndex], backingPaymentSourceIndex)
                storedCards.isNotEmpty() && defaultIndex != -1 -> Pair(storedCards[defaultIndex], defaultIndex)
                else -> null
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
                pledgeFlowContext == PledgeFlowContext.NEW_PLEDGE
                        || pledgeFlowContext == PledgeFlowContext.FIX_ERRORED_PLEDGE

        private fun storedCards(): Observable<List<StoredCard>> {
            return this.apolloClient.getStoredCards()
                    .compose(bindToLifecycle())
                    .compose(neverError())
        }

        override fun addedCardPosition(position: Int) = this.addedCardPosition.onNext(position)

        override fun cardSaved(storedCard: StoredCard) = this.cardSaved.onNext(storedCard)

        override fun cardSelected(storedCard: StoredCard, position: Int) = this.cardSelected.onNext(Pair(storedCard, position))

        override fun continueButtonClicked() = this.continueButtonClicked.onNext(null)

        override fun decreasePledgeButtonClicked() = this.decreasePledgeButtonClicked.onNext(null)

        override fun increasePledgeButtonClicked() = this.increasePledgeButtonClicked.onNext(null)

        override fun linkClicked(url: String) = this.linkClicked.onNext(url)

        override fun miniRewardClicked() = this.miniRewardClicked.onNext(null)

        override fun newCardButtonClicked() = this.newCardButtonClicked.onNext(null)

        override fun pledgeInput(amount: String) = this.pledgeInput.onNext(amount)

        override fun pledgeButtonClicked() = this.pledgeButtonClicked.onNext(null)

        override fun shippingRuleSelected(shippingRule: ShippingRule) = this.shippingRule.onNext(shippingRule)

        override fun stripeSetupResultSuccessful(@StripeIntentResult.Outcome outcome: Int) = this.stripeSetupResultSuccessful.onNext(outcome)

        override fun stripeSetupResultUnsuccessful(exception: Exception) = this.stripeSetupResultUnsuccessful.onNext(exception)

        @NonNull
        override fun addedCard(): Observable<Pair<StoredCard, Project>> = this.addedCard

        @NonNull
        override fun additionalPledgeAmount(): Observable<String> = this.additionalPledgeAmount

        @NonNull
        override fun additionalPledgeAmountIsGone(): Observable<Boolean> = this.additionalPledgeAmountIsGone

        @NonNull
        override fun baseUrlForTerms(): Observable<String> = this.baseUrlForTerms

        @NonNull
        override fun cardsAndProject(): Observable<Pair<List<StoredCard>, Project>> = this.cardsAndProject

        @NonNull
        override fun continueButtonIsEnabled(): Observable<Boolean> = this.continueButtonIsEnabled

        @NonNull
        override fun continueButtonIsGone(): Observable<Boolean> = this.continueButtonIsGone

        @NonNull
        override fun conversionTextViewIsGone(): Observable<Boolean> = this.conversionTextViewIsGone

        @NonNull
        override fun conversionText(): Observable<String> = this.conversionText

        @NonNull
        override fun decreasePledgeButtonIsEnabled(): Observable<Boolean> = this.decreasePledgeButtonIsEnabled

        @NonNull
        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        @NonNull
        override fun estimatedDeliveryInfoIsGone(): Observable<Boolean> = this.estimatedDeliveryInfoIsGone

        @NonNull
        override fun increasePledgeButtonIsEnabled(): Observable<Boolean> = this.increasePledgeButtonIsEnabled

        @NonNull
        override fun paymentContainerIsGone(): Observable<Boolean> = this.paymentContainerIsGone

        @NonNull
        override fun pledgeAmount(): Observable<String> = this.pledgeAmount

        @NonNull
        override fun pledgeButtonCTA(): Observable<Int> = this.pledgeButtonCTA

        @NonNull
        override fun pledgeButtonIsEnabled(): Observable<Boolean> = this.pledgeButtonIsEnabled

        @NonNull
        override fun pledgeButtonIsGone(): Observable<Boolean> = this.pledgeButtonIsGone

        @NonNull
        override fun pledgeHint(): Observable<String> = this.pledgeHint

        @NonNull
        override fun pledgeMaximum(): Observable<String> = this.pledgeMaximum

        @NonNull
        override fun pledgeMaximumIsGone(): Observable<Boolean> = this.pledgeMaximumIsGone

        @NonNull
        override fun pledgeMinimum(): Observable<String> = this.pledgeMinimum

        @NonNull
        override fun pledgeProgressIsGone(): Observable<Boolean> = this.pledgeProgressIsGone

        @NonNull
        override fun pledgeSectionIsGone(): Observable<Boolean> = this.pledgeSectionIsGone

        @NonNull
        override fun pledgeSummaryAmount(): Observable<CharSequence> = this.pledgeSummaryAmount

        @NonNull
        override fun pledgeSummaryIsGone(): Observable<Boolean> = this.pledgeSummaryIsGone

        @NonNull
        override fun pledgeTextColor(): Observable<Int> = this.pledgeTextColor

        @NonNull
        override fun projectCurrencySymbol(): Observable<Pair<SpannableString, Boolean>> = this.projectCurrencySymbol

        @NonNull
        override fun rewardSummaryIsGone(): Observable<Boolean> = this.rewardSummaryIsGone

        @NonNull
        override fun rewardTitle(): Observable<String> = this.rewardTitle

        @NonNull
        override fun selectedShippingRule(): Observable<ShippingRule> = this.selectedShippingRule

        @NonNull
        override fun shippingAmount(): Observable<CharSequence> = this.shippingAmount

        @NonNull
        override fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>> = this.shippingRulesAndProject

        @NonNull
        override fun shippingRulesSectionIsGone(): Observable<Boolean> = this.shippingRulesSectionIsGone

        @NonNull
        override fun shippingSummaryAmount(): Observable<CharSequence> = this.shippingSummaryAmount

        @NonNull
        override fun shippingSummaryLocation(): Observable<String> = this.shippingSummaryLocation

        @NonNull
        override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone

        @NonNull
        override fun showNewCardFragment(): Observable<Project> = this.showNewCardFragment

        @NonNull
        override fun showPledgeError(): Observable<Void> = this.showPledgeError

        @NonNull
        override fun showPledgeSuccess(): Observable<Pair<CheckoutData, PledgeData>> = this.showPledgeSuccess

        @NonNull
        override fun showSelectedCard(): Observable<Pair<Int, CardState>> = this.showSelectedCard

        @NonNull
        override fun showSCAFlow(): Observable<String> = this.showSCAFlow

        @NonNull
        override fun showUpdatePaymentError(): Observable<Void> = this.showUpdatePaymentError

        @NonNull
        override fun showUpdatePaymentSuccess(): Observable<Void> = this.showUpdatePaymentSuccess

        @NonNull
        override fun showUpdatePledgeError(): Observable<Void> = this.showUpdatePledgeError

        @NonNull
        override fun showUpdatePledgeSuccess(): Observable<Void> = this.showUpdatePledgeSuccess

        @NonNull
        override fun startChromeTab(): Observable<String> = this.startChromeTab

        @NonNull
        override fun startLoginToutActivity(): Observable<Void> = this.startLoginToutActivity

        @NonNull
        override fun totalAmount(): Observable<CharSequence> = this.totalAmount

        @NonNull
        override fun totalAndDeadline(): Observable<Pair<String, String>> = this.totalAndDeadline

        @NonNull
        override fun totalAndDeadlineIsVisible(): Observable<Void> = this.totalAndDeadlineIsVisible

        @NonNull
        override fun totalDividerIsGone(): Observable<Boolean> = this.totalDividerIsGone

        @NonNull
        override fun headerSectionIsGone(): Observable<Boolean> = this.headerSectionIsGone

        @NonNull
        override fun titleAndAmount(): Observable<Pair<String, String>> = this.titleAndAmount
    }
}
