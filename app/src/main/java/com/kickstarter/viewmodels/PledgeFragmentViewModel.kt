package com.kickstarter.viewmodels

import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.NumberOptions
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.*
import com.kickstarter.models.*
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.fragments.PledgeFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode
import java.net.CookieManager
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

interface PledgeFragmentViewModel {
    interface Inputs {
        /** Call when a card has been inserted into the stored cards list. */
        fun addedCardPosition(position: Int)

        /** Call when user clicks the back button. */
        fun backPressed()

        /** Call when a card has successfully saved. */
        fun cardSaved(storedCard: StoredCard)

        /** Call when user deselects a card they want to pledge with. */
        fun closeCardButtonClicked(position: Int)

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

        /** Call when the view has been laid out. */
        fun onGlobalLayout()

        /** Call when the user updates the pledge amount. */
        fun pledgeInput(amount: String)

        /** Call when user clicks the pledge button. */
        fun pledgeButtonClicked(cardId: String)

        /** Call when user selects a card they want to pledge with. */
        fun selectCardButtonClicked(position: Int)

        /** Call when user selects a shipping location. */
        fun shippingRuleSelected(shippingRule: ShippingRule)

        /** Call when user clicks the update pledge button. */
        fun updatePledgeButtonClicked()
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

        /** Emits a boolean determining if the continue button should be hidden. */
        fun continueButtonIsGone(): Observable<Boolean>

        /** Emits a string representing the total pledge amount in the user's preferred currency.  */
        fun conversionText(): Observable<String>

        /** Returns `true` if the conversion should be hidden, `false` otherwise.  */
        fun conversionTextViewIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the decrease pledge button should be enabled. */
        fun decreasePledgeButtonIsEnabled(): Observable<Boolean>

        /** Emits a boolean determining if the divider below the delivery section should be hidden. */
        fun deliveryDividerIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the delivery section should be hidden. */
        fun deliverySectionIsGone(): Observable<Boolean>

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

        /** Emits the hint text for the pledge amount. */
        fun pledgeHint(): Observable<String>

        /** Emits a boolean determining if the pledge section should be hidden. */
        fun pledgeSectionIsGone(): Observable<Boolean>

        /** Emits the pledge amount string of the backing. */
        fun pledgeSummaryAmount(): Observable<String>

        /** Emits a boolean determining if the pledge summary section should be hidden. */
        fun pledgeSummaryIsGone(): Observable<Boolean>

        /** Emits the color resource ID of the pledge amount. */
        fun pledgeTextColor(): Observable<Int>

        /** Emits the currency symbol string of the project. */
        fun projectCurrencySymbol(): Observable<Pair<SpannableString, Boolean>>

        /** Emits when we should reverse the reward card animation. */
        fun startRewardExpandAnimation(): Observable<Void>

        /** Emits when the reward card shrink animation should start. */
        fun startRewardShrinkAnimation(): Observable<PledgeData>

        /** Emits the currently selected shipping rule. */
        fun selectedShippingRule(): Observable<ShippingRule>

        /** Emits the shipping amount of the selected shipping rule. */
        fun shippingAmount(): Observable<String>

        /** Emits a pair of list of shipping rules to be selected and the project. */
        fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>>

        /** Emits when the shipping rules section should be hidden. */
        fun shippingRulesSectionIsGone(): Observable<Boolean>

        /** Emits the shipping amount of the selected shipping rule. */
        fun shippingSummaryAmount(): Observable<String>

        /** Emits a boolean determining if the shipping summary should be hidden. */
        fun shippingSummaryIsGone(): Observable<Boolean>

        /** Emits the displayable name of the backing's location. */
        fun shippingSummaryLocation(): Observable<String>

        /** Emits when we should the user a warning about not satisfying the reward's minimum. */
        fun showMinimumWarning(): Observable<String>

        /** Emits when we should show the [com.kickstarter.ui.fragments.NewCardFragment]. */
        fun showNewCardFragment(): Observable<Project>

        /** Emits when the cards adapter should update the selected position. */
        fun showPledgeCard(): Observable<Pair<Int, CardState>>

        /**  Emits when the pledge call was unsuccessful. */
        fun showPledgeError(): Observable<Void>

        /**  Emits when the update payment source mutation was unsuccessful. */
        fun showUpdatePaymentError(): Observable<Void>

        /**  Emits when the update payment source mutation was successful. */
        fun showUpdatePaymentSuccess(): Observable<Void>

        /** Emits when the update pledge call was unsuccessful. */
        fun showUpdatePledgeError(): Observable<Void>

        /** Emits when the update pledge call was successful. */
        fun showUpdatePledgeSuccess(): Observable<Void>

        /** Emits a boolean determining if the reward snapshot should be hidden. */
        fun snapshotIsGone(): Observable<Boolean>

        /** Emits when we should start a Chrome tab. */
        fun startChromeTab(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.LoginToutActivity]. */
        fun startLoginToutActivity(): Observable<Void>

        /** Emits when we the pledge was successful and should start the [com.kickstarter.ui.activities.ThanksActivity]. */
        fun startThanksActivity(): Observable<Project>

        /** Emits the total amount string of the pledge. */
        fun totalAmount(): Observable<SpannableString>

        /** Emits a boolean determining if the divider above the total should be hidden. */
        fun totalDividerIsGone(): Observable<Boolean>

        /** Emits the color resource ID of the total amount. */
        fun totalTextColor(): Observable<Int>

        /** Emits a boolean determining if the update pledge button should be enabled. */
        fun updatePledgeButtonIsEnabled(): Observable<Boolean>

        /** Emits a boolean determining if the update pledge button should be hidden. */
        fun updatePledgeButtonIsGone(): Observable<Boolean>

        /** Emits a boolean determining if the update pledge progress bar should be hidden. */
        fun updatePledgeProgressIsGone(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val addedCardPosition = PublishSubject.create<Int>()
        private val backPressed = PublishSubject.create<Void>()
        private val cardSaved = PublishSubject.create<StoredCard>()
        private val closeCardButtonClicked = PublishSubject.create<Int>()
        private val continueButtonClicked = PublishSubject.create<Void>()
        private val decreasePledgeButtonClicked = PublishSubject.create<Void>()
        private val increasePledgeButtonClicked = PublishSubject.create<Void>()
        private val linkClicked = PublishSubject.create<String>()
        private val miniRewardClicked = PublishSubject.create<Void>()
        private val newCardButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val pledgeButtonClicked = PublishSubject.create<String>()
        private val pledgeInput = PublishSubject.create<String>()
        private val selectCardButtonClicked = PublishSubject.create<Int>()
        private val shippingRule = PublishSubject.create<ShippingRule>()
        private val updatePledgeButtonClicked = PublishSubject.create<Void>()

        private val addedCard = BehaviorSubject.create<Pair<StoredCard, Project>>()
        private val additionalPledgeAmount = BehaviorSubject.create<String>()
        private val additionalPledgeAmountIsGone = BehaviorSubject.create<Boolean>()
        private val baseUrlForTerms = BehaviorSubject.create<String>()
        private val cardsAndProject = BehaviorSubject.create<Pair<List<StoredCard>, Project>>()
        private val continueButtonIsGone = BehaviorSubject.create<Boolean>()
        private val conversionText = BehaviorSubject.create<String>()
        private val conversionTextViewIsGone = BehaviorSubject.create<Boolean>()
        private val decreasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val deliveryDividerIsGone = BehaviorSubject.create<Boolean>()
        private val deliverySectionIsGone = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val estimatedDeliveryInfoIsGone = BehaviorSubject.create<Boolean>()
        private val increasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val paymentContainerIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeAmount = BehaviorSubject.create<String>()
        private val pledgeHint = BehaviorSubject.create<String>()
        private val pledgeSectionIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeSummaryAmount = BehaviorSubject.create<String>()
        private val pledgeSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeTextColor = BehaviorSubject.create<Int>()
        private val projectCurrencySymbol = BehaviorSubject.create<Pair<SpannableString, Boolean>>()
        private val selectedShippingRule = BehaviorSubject.create<ShippingRule>()
        private val shippingAmount = BehaviorSubject.create<String>()
        private val shippingRulesAndProject = BehaviorSubject.create<Pair<List<ShippingRule>, Project>>()
        private val shippingRulesSectionIsGone = BehaviorSubject.create<Boolean>()
        private val shippingSummaryAmount = BehaviorSubject.create<String>()
        private val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val shippingSummaryLocation = BehaviorSubject.create<String>()
        private val showMinimumWarning = PublishSubject.create<String>()
        private val showNewCardFragment = PublishSubject.create<Project>()
        private val showPledgeCard = BehaviorSubject.create<Pair<Int, CardState>>()
        private val showPledgeError = PublishSubject.create<Void>()
        private val showUpdatePaymentError = PublishSubject.create<Void>()
        private val showUpdatePaymentSuccess = PublishSubject.create<Void>()
        private val showUpdatePledgeError = PublishSubject.create<Void>()
        private val showUpdatePledgeSuccess = PublishSubject.create<Void>()
        private val snapshotIsGone = BehaviorSubject.create<Boolean>()
        private val startChromeTab = PublishSubject.create<String>()
        private val startLoginToutActivity = PublishSubject.create<Void>()
        private val startRewardExpandAnimation = BehaviorSubject.create<Void>()
        private val startRewardShrinkAnimation = BehaviorSubject.create<PledgeData>()
        private val startThanksActivity = PublishSubject.create<Project>()
        private val totalAmount = BehaviorSubject.create<SpannableString>()
        private val totalDividerIsGone = BehaviorSubject.create<Boolean>()
        private val totalTextColor = BehaviorSubject.create<Int>()
        private val updatePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val updatePledgeButtonIsGone = BehaviorSubject.create<Boolean>()
        private val updatePledgeProgressIsGone = BehaviorSubject.create<Boolean>()

        private val apiClient = environment.apiClient()
        private val apolloClient = environment.apolloClient()
        private val cookieManager: CookieManager = environment.cookieManager()
        private val currentConfig = environment.currentConfig()
        private val currentUser = environment.currentUser()
        private val ksCurrency = environment.ksCurrency()
        private val sharedPreferences: SharedPreferences = environment.sharedPreferences()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val userIsLoggedIn = this.currentUser.isLoggedIn
                    .distinctUntilChanged()

            val arguments = arguments()
                    .compose<Bundle>(takeWhen(this.onGlobalLayout))

            val reward = arguments
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_REWARD) as Reward }

            val screenLocation = arguments
                    .map { it.getSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION) as ScreenLocation? }

            val project = arguments
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_PROJECT) as Project }

            val pledgeReason = arguments
                    .map { it.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason }

            val updatingPayment = pledgeReason
                    .map { it == PledgeReason.UPDATE_PAYMENT }
                    .distinctUntilChanged()

            val updatingPaymentOrUpdatingPledge = pledgeReason
                    .map { it == PledgeReason.UPDATE_PAYMENT || it == PledgeReason.UPDATE_PLEDGE }
                    .distinctUntilChanged()

            val projectAndReward = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))

            val backing = projectAndReward
                    .filter { BackingUtils.isBacked(it.first, it.second) }
                    .map { it.first.backing() }
                    .ofType(Backing::class.java)

            // Mini reward card
            Observable.combineLatest(screenLocation, reward, project, ::PledgeData)
                    .compose(bindToLifecycle())
                    .subscribe(this.startRewardShrinkAnimation)

            Observable.merge(this.backPressed, this.miniRewardClicked)
                    .compose(bindToLifecycle())
                    .subscribe(this.startRewardExpandAnimation)

            updatingPaymentOrUpdatingPledge
                    .compose(bindToLifecycle())
                    .subscribe(this.snapshotIsGone)

            // Estimated delivery section
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

            updatingPaymentOrUpdatingPledge
                    .compose(bindToLifecycle())
                    .subscribe(this.deliverySectionIsGone)

            updatingPaymentOrUpdatingPledge
                    .compose(bindToLifecycle())
                    .subscribe(this.deliveryDividerIsGone)

            //Base pledge amount
            val rewardMinimum = reward
                    .map { it.minimum() }
                    .distinctUntilChanged()

            rewardMinimum
                    .map { NumberUtils.format(it.toInt()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeHint)

            project
                    .map { ProjectViewUtils.currencySymbolAndPosition(it, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.projectCurrencySymbol)

            // Pledge stepper section
            val additionalPledgeAmount = BehaviorSubject.create(0.0)

            val additionalAmountOrZero = additionalPledgeAmount
                    .map { max(0.0, it) }

            val pledgeLessThanMinimum = additionalPledgeAmount
                    .map { it.sign == -1.0 }

            pledgeLessThanMinimum
                    .map { if (it) R.color.ksr_red_400 else R.color.ksr_green_500 }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.pledgeTextColor.onNext(it)
                        this.totalTextColor.onNext(it)
                    }

            val country = project
                    .map { Country.findByCurrencyCode(it.currency()) }
                    .filter { it != null }
                    .distinctUntilChanged()
                    .ofType(Country::class.java)

            val stepAmount = country
                    .map { it.minPledge }

            additionalAmountOrZero
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<String> { this.ksCurrency.format(it.first, it.second, RoundingMode.HALF_UP) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.additionalPledgeAmount)

            additionalAmountOrZero
                    .map { IntegerUtils.isZero(it.toInt()) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.additionalPledgeAmountIsGone)

            val maxAndMinPledge = country
                    .map { it.maxPledge }
                    .compose<Pair<Int, Double>>(combineLatestPair(rewardMinimum))

            val initialAmount = rewardMinimum
                    .compose<Pair<Double, PledgeReason>>(combineLatestPair(pledgeReason))
                    .filter { it.second == PledgeReason.PLEDGE || it.second == PledgeReason.UPDATE_REWARD }
                    .map { it.first }

            val pledgeInput = Observable.merge(initialAmount, this.pledgeInput.map { NumberUtils.parse(it) })
                    .distinctUntilChanged()
                    .compose<Pair<Double, Country>>(combineLatestPair(country.distinctUntilChanged()))
                    .map { min(it.first, it.second.maxPledge.toDouble()) }

            pledgeInput
                    .compose<Double>(takeWhen(this.increasePledgeButtonClicked))
                    .compose<Pair<Double,Int>>(combineLatestPair(stepAmount))
                    .map { it.first + it.second }
                    .map { it.toString() }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeInput)

            pledgeInput
                    .compose<Double>(takeWhen(this.decreasePledgeButtonClicked))
                    .compose<Pair<Double,Int>>(combineLatestPair(stepAmount))
                    .map { it.first - it.second }
                    .map { it.toString() }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeInput)

            pledgeInput
                    .compose<Pair<Double, Pair<Int, Double>>>(combineLatestPair(maxAndMinPledge))
                    .filter { it.first <= it.second.first }
                    .map { it.first - it.second.second }
                    .compose(bindToLifecycle())
                    .subscribe { additionalPledgeAmount.onNext(it) }

            pledgeInput
                    .compose<Pair<Double, Country>>(combineLatestPair(country))
                    .map { it.first < it.second.maxPledge }
                    .distinctUntilChanged()
                    .subscribe(this.increasePledgeButtonIsEnabled)

            pledgeInput
                    .compose<Pair<Double, Double>>(combineLatestPair(rewardMinimum))
                    .map { Math.max(it.first, it.second) > it.second }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.decreasePledgeButtonIsEnabled)

            val backingAmount = backing
                    .map { it.amount() - it.shippingAmount() }

            val pledgeAmount = Observable.merge(backingAmount, pledgeInput)
                    .distinctUntilChanged()

            pledgeAmount
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

            val shippingAmount = shippingRule
                    .map { it.cost() }

            shippingAmount
                    .map { NumberUtils.format(it.toInt()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingAmount)

            // Total pledge section
            val basePledgeAmount = pledgeAmount

            val unshippableTotal = basePledgeAmount
                    .compose<Pair<Double, Reward>>(combineLatestPair(reward))
                    .filter { RewardUtils.isNoReward(it.second) || !RewardUtils.isShippable(it.second) }
                    .map<Double> { it.first }
                    .distinctUntilChanged()

            val shippableTotal = basePledgeAmount
                    .compose<Pair<Double, Double>>(combineLatestPair(shippingAmount))
                    .map { it.first + it.second }
                    .compose<Pair<Double, Reward>>(combineLatestPair(reward))
                    .filter { RewardUtils.isReward(it.second) && RewardUtils.isShippable(it.second) }
                    .map<Double> { it.first }
                    .distinctUntilChanged()

            val total = Observable.merge(unshippableTotal, shippableTotal)

            total
                    .map { ProjectViewUtils.styleCurrency(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.totalAmount)

            total
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { this.ksCurrency.formatWithUserPreference(it.first, it.second, RoundingMode.UP, 2) }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversionText)

            projectAndReward
                    .map { it.first.currency() != it.first.currentCurrency() }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .subscribe(this.conversionTextViewIsGone)

            // Manage pledge section
            pledgeReason
                    .map { it == PledgeReason.PLEDGE || it == PledgeReason.UPDATE_PAYMENT }
                    .distinctUntilChanged()
                    .subscribe(this.updatePledgeButtonIsGone)

            backingAmount
                    .map { NumberUtils.format(it.toFloat(), NumberOptions.builder().precision(NumberUtils.precision(it, RoundingMode.HALF_UP)).build()) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeSummaryAmount)

            updatingPayment
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeSummaryIsGone)

            updatingPayment
                    .compose(bindToLifecycle())
                    .subscribe(this.totalDividerIsGone)

            updatingPayment
                    .map { BooleanUtils.negate(it) }
                    .compose<Pair<Boolean, Reward>>(combineLatestPair(reward))
                    .map { it.first || !RewardUtils.isShippable(it.second) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingSummaryIsGone)

            backing
                    .map { NumberUtils.format(it.shippingAmount()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingSummaryAmount)

            backingShippingRule
                    .map { it.location().displayableName() }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingSummaryLocation)

            val updatingPledge = pledgeReason
                    .map { it == PledgeReason.UPDATE_PLEDGE }

            val updatingReward = pledgeReason
                    .map { it == PledgeReason.UPDATE_REWARD }

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

            Observable.merge(updatingReward, shippingOrAmountChanged.skip(1))
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.updatePledgeButtonIsEnabled)

            val validUpdatePledgeClick = pledgeLessThanMinimum
                    .compose<Pair<Boolean, Void>>(takePairWhen(this.updatePledgeButtonClicked))
                    .filter { BooleanUtils.isFalse(it.first) }

            val location: Observable<Location?> = Observable.merge(Observable.just(null as Location?), shippingRule.map { it.location() })

            val backingForMutation = project
                    .filter { it.isBacking }
                    .map { it.backing() }
                    .ofType(Backing::class.java)
                    .distinctUntilChanged()

            val updateBackingNotification = Observable.combineLatest(backingForMutation,
                    total.map { it.toString() },
                    location.map { it?.id()?.toString() },
                    reward)
            { b, a, l, r -> UpdateBacking(b, a, l, r) }
                    .compose<UpdateBacking>(takeWhen(validUpdatePledgeClick))
                    .switchMap {
                        this.apolloClient.updateBacking(it.backing, it.amount, it.locationId, it.reward)
                                .doOnSubscribe { this.updatePledgeProgressIsGone.onNext(false) }
                                .materialize()
                    }
                    .share()

            val updateBackingNotificationValues = updateBackingNotification
                    .compose(values())

            Observable.merge(updateBackingNotification.compose(errors()), updateBackingNotificationValues.filter { BooleanUtils.isFalse(it) })
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.showUpdatePledgeError.onNext(null)
                        this.updatePledgeProgressIsGone.onNext(true)
                    }

            updateBackingNotificationValues
                    .filter { BooleanUtils.isTrue(it) }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePledgeSuccess)

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
                    .filter { BooleanUtils.isTrue(it) }
                    .compose<Boolean>(waitUntil(total))
                    .switchMap { storedCards() }
                    .compose<Pair<List<StoredCard>, Project>>(combineLatestPair(project))
                    .compose(bindToLifecycle())
                    .subscribe(this.cardsAndProject)

            this.cardSaved
                    .compose<Pair<StoredCard, Project>>(combineLatestPair(project))
                    .compose(bindToLifecycle())
                    .subscribe(this.addedCard)

            val selectedPosition = BehaviorSubject.create(RecyclerView.NO_POSITION)

            this.cardSaved
                    .compose<Pair<StoredCard, Int>>(zipPair(this.addedCardPosition))
                    .map { it.second }
                    .compose(bindToLifecycle())
                    .subscribe(this.selectCardButtonClicked)

            this.showPledgeCard
                    .map { it.first }
                    .compose(bindToLifecycle())
                    .subscribe(selectedPosition)

            this.selectCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.showPledgeCard.onNext(Pair(it, CardState.PLEDGE)) }

            this.closeCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.showPledgeCard.onNext(Pair(it, CardState.SELECT)) }

            project
                    .compose<Project>(takeWhen(this.newCardButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.showNewCardFragment)

            pledgeLessThanMinimum
                    .compose<Boolean>(takeWhen(this.continueButtonClicked))
                    .filter { BooleanUtils.isFalse(it) }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.startLoginToutActivity)

            val pledgeAttempt = Observable.merge(this.continueButtonClicked, this.pledgeButtonClicked, this.updatePledgeButtonClicked)

            pledgeLessThanMinimum
                    .compose<Boolean>(takeWhen(pledgeAttempt))
                    .filter { BooleanUtils.isTrue(it) }
                    .compose<Pair<Boolean, Pair<Project, Reward>>>(combineLatestPair(projectAndReward))
                    .map { it.second }
                    .map { this.ksCurrency.format(it.second.minimum(), it.first, RoundingMode.HALF_UP) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showMinimumWarning)

            val validPledgeClick = pledgeLessThanMinimum
                    .compose<Pair<Boolean, PledgeReason>>(combineLatestPair(pledgeReason))
                    .filter { it.second == PledgeReason.PLEDGE }
                    .map { it.first }
                    .compose<Pair<Boolean, String>>(takePairWhen(this.pledgeButtonClicked))
                    .filter { BooleanUtils.isFalse(it.first) }

            // An observable of the ref tag stored in the cookie for the project. Can emit `null`.
            val cookieRefTag = project
                    .take(1)
                    .map { p -> RefTagUtils.storedCookieRefTagForProject(p, this.cookieManager, this.sharedPreferences) }

            val createBackingNotification = Observable.combineLatest(project,
                    total.map { it.toString() },
                    this.pledgeButtonClicked,
                    location.map { it?.id()?.toString() },
                    reward,
                    cookieRefTag)
            { p, a, id, l, r, c -> CreateBacking(p, a, id, l, r, c) }
                    .compose<CreateBacking>(takeWhen(validPledgeClick))
                    .switchMap {
                        this.apolloClient.createBacking(it.project, it.amount, it.paymentSourceId, it.locationId, it.reward, it.refTag)
                            .doOnSubscribe { this.showPledgeCard.onNext(Pair(selectedPosition.value, CardState.LOADING)) }
                            .materialize()
                    }
                    .share()

            val createBackingValues = createBackingNotification
                    .compose(values())

            Observable.merge(createBackingNotification.compose(errors()), createBackingValues.filter { BooleanUtils.isFalse(it) })
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe{
                        this.showPledgeError.onNext(null)
                        this.showPledgeCard.onNext(Pair(selectedPosition.value, CardState.PLEDGE))
                    }

            project
                    .compose<Project>(takeWhen(createBackingValues.filter { BooleanUtils.isTrue(it) }))
                    .compose(bindToLifecycle())
                    .subscribe(this.startThanksActivity)

            val updatePaymentClick = pledgeReason
                    .compose<Pair<PledgeReason, String>>(takePairWhen(this.pledgeButtonClicked))
                    .filter { it.first == PledgeReason.UPDATE_PAYMENT }
                    .map { it.second }

            val updatePaymentNotification = Observable.combineLatest(backingForMutation,
                    updatePaymentClick)
            { b, id -> UpdateBackingPayment(b, id) }
                    .switchMap {
                        this.apolloClient.updateBackingPayment(it.backing, it.paymentSourceId)
                                .doOnSubscribe { this.showPledgeCard.onNext(Pair(selectedPosition.value, CardState.LOADING)) }
                                .materialize()
                    }
                    .share()

            val updatePaymentNotificationValues = updatePaymentNotification
                    .compose(values())

            Observable.merge(updatePaymentNotification.compose(errors()), updatePaymentNotificationValues.filter { BooleanUtils.isFalse(it) })
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe{
                        this.showUpdatePaymentError.onNext(null)
                        this.showPledgeCard.onNext(Pair(selectedPosition.value, CardState.PLEDGE))
                    }

            updatePaymentNotificationValues
                    .filter { BooleanUtils.isTrue(it) }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.showUpdatePaymentSuccess)

            this.baseUrlForTerms.onNext(this.environment.webEndpoint())

            this.linkClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.startChromeTab)

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
                    .compose<Pair<Project, Double>>(takeWhen(this.updatePledgeButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackUpdatePledgeButtonClicked(it.first, it.second) }

            project
                    .compose<Project>(takeWhen(updatePaymentClick))
                    .compose(bindToLifecycle())
                    .subscribe { this.koala.trackUpdatePaymentMethodButtonClicked(it) }
        }

        private fun backingShippingRule(shippingRules: List<ShippingRule>, backing: Backing): Observable<ShippingRule> {
            return Observable.just(shippingRules.firstOrNull { it.location().id() == backing.locationId() })
        }

        private fun defaultShippingRule(shippingRules: List<ShippingRule>): Observable<ShippingRule> {
            return this.currentConfig.observable()
                    .map { it.countryCode() }
                    .map { countryCode ->
                        shippingRules.firstOrNull { it.location().country() == countryCode }
                                ?: shippingRules.first()
                    }
        }

        private fun storedCards(): Observable<List<StoredCard>> {
            return this.apolloClient.getStoredCards()
                    .compose(bindToLifecycle())
                    .compose(neverError())
        }

        data class CreateBacking(val project: Project, val amount: String, val paymentSourceId: String, val locationId: String?, val reward: Reward?, val refTag: RefTag?)
        data class UpdateBacking(val backing: Backing, val amount: String, val locationId: String?, val reward: Reward?)
        data class UpdateBackingPayment(val backing: Backing, val paymentSourceId: String)

        override fun addedCardPosition(position: Int) = this.addedCardPosition.onNext(position)

        override fun backPressed() = this.backPressed.onNext(null)

        override fun cardSaved(storedCard: StoredCard) = this.cardSaved.onNext(storedCard)

        override fun closeCardButtonClicked(position: Int) = this.closeCardButtonClicked.onNext(position)

        override fun continueButtonClicked() = this.continueButtonClicked.onNext(null)

        override fun decreasePledgeButtonClicked() = this.decreasePledgeButtonClicked.onNext(null)

        override fun increasePledgeButtonClicked() = this.increasePledgeButtonClicked.onNext(null)

        override fun linkClicked(url: String) = this.linkClicked.onNext(url)

        override fun miniRewardClicked() = this.miniRewardClicked.onNext(null)

        override fun newCardButtonClicked() = this.newCardButtonClicked.onNext(null)

        override fun onGlobalLayout() = this.onGlobalLayout.onNext(null)

        override fun pledgeInput(amount: String) = this.pledgeInput.onNext(amount)

        override fun pledgeButtonClicked(cardId: String) = this.pledgeButtonClicked.onNext(cardId)

        override fun shippingRuleSelected(shippingRule: ShippingRule) = this.shippingRule.onNext(shippingRule)

        override fun selectCardButtonClicked(position: Int) = this.selectCardButtonClicked.onNext(position)

        override fun updatePledgeButtonClicked() = this.updatePledgeButtonClicked.onNext(null)

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
        override fun continueButtonIsGone(): Observable<Boolean> = this.continueButtonIsGone

        @NonNull
        override fun conversionTextViewIsGone(): Observable<Boolean> = this.conversionTextViewIsGone

        @NonNull
        override fun conversionText(): Observable<String> = this.conversionText

        @NonNull
        override fun decreasePledgeButtonIsEnabled(): Observable<Boolean> = this.decreasePledgeButtonIsEnabled

        @NonNull
        override fun deliveryDividerIsGone(): Observable<Boolean> = this.deliveryDividerIsGone

        @NonNull
        override fun deliverySectionIsGone(): Observable<Boolean> = this.deliverySectionIsGone

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
        override fun pledgeHint(): Observable<String> = this.pledgeHint

        @NonNull
        override fun pledgeSectionIsGone(): Observable<Boolean> = this.pledgeSectionIsGone

        @NonNull
        override fun pledgeSummaryAmount(): Observable<String> = this.pledgeSummaryAmount

        @NonNull
        override fun pledgeSummaryIsGone(): Observable<Boolean> = this.pledgeSummaryIsGone

        @NonNull
        override fun pledgeTextColor(): Observable<Int> = this.pledgeTextColor

        @NonNull
        override fun projectCurrencySymbol(): Observable<Pair<SpannableString, Boolean>> = this.projectCurrencySymbol

        @NonNull
        override fun selectedShippingRule(): Observable<ShippingRule> = this.selectedShippingRule

        @NonNull
        override fun shippingAmount(): Observable<String> = this.shippingAmount

        @NonNull
        override fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>> = this.shippingRulesAndProject

        @NonNull
        override fun shippingRulesSectionIsGone(): Observable<Boolean> = this.shippingRulesSectionIsGone

        @NonNull
        override fun shippingSummaryAmount(): Observable<String> = this.shippingSummaryAmount

        @NonNull
        override fun shippingSummaryLocation(): Observable<String> = this.shippingSummaryLocation

        @NonNull
        override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone

        @NonNull
        override fun showMinimumWarning(): Observable<String> = this.showMinimumWarning

        @NonNull
        override fun showNewCardFragment(): Observable<Project> = this.showNewCardFragment

        @NonNull
        override fun showPledgeCard(): Observable<Pair<Int, CardState>> = this.showPledgeCard

        @NonNull
        override fun showPledgeError(): Observable<Void> = this.showPledgeError

        @NonNull
        override fun showUpdatePaymentError(): Observable<Void> = this.showUpdatePaymentError

        @NonNull
        override fun showUpdatePaymentSuccess(): Observable<Void> = this.showUpdatePaymentSuccess

        @NonNull
        override fun showUpdatePledgeError(): Observable<Void> = this.showUpdatePledgeError

        @NonNull
        override fun showUpdatePledgeSuccess(): Observable<Void> = this.showUpdatePledgeSuccess

        @NonNull
        override fun snapshotIsGone(): Observable<Boolean> = this.snapshotIsGone

        @NonNull
        override fun startChromeTab(): Observable<String> = this.startChromeTab

        @NonNull
        override fun startRewardExpandAnimation(): Observable<Void> = this.startRewardExpandAnimation

        @NonNull
        override fun startRewardShrinkAnimation(): Observable<PledgeData> = this.startRewardShrinkAnimation

        @NonNull
        override fun startLoginToutActivity(): Observable<Void> = this.startLoginToutActivity

        @NonNull
        override fun startThanksActivity(): Observable<Project> = this.startThanksActivity

        @NonNull
        override fun totalAmount(): Observable<SpannableString> = this.totalAmount

        @NonNull
        override fun totalDividerIsGone(): Observable<Boolean> = this.totalDividerIsGone

        @NonNull
        override fun totalTextColor(): Observable<Int> = this.totalTextColor

        @NonNull
        override fun updatePledgeButtonIsEnabled(): Observable<Boolean> = this.updatePledgeButtonIsEnabled

        @NonNull
        override fun updatePledgeButtonIsGone(): Observable<Boolean> = this.updatePledgeButtonIsGone

        @NonNull
        override fun updatePledgeProgressIsGone(): Observable<Boolean> = this.updatePledgeProgressIsGone

    }
}
