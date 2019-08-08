package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.NumberOptions
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.*
import com.kickstarter.models.*
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.fragments.PledgeFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode
import kotlin.math.sign

interface PledgeFragmentViewModel {
    interface Inputs {
        /** Call when a card has been inserted into the stored cards list. */
        fun addedCardPosition(position: Int)

        /** Call when user clicks the cancel pledge button. */
        fun cancelPledgeButtonClicked()

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
    }

    interface Outputs {
        /** Emits a newly added stored card and the project. */
        fun addedCard(): Observable<Pair<StoredCard, Project>>

        /** Emits the additional pledge amount string. */
        fun additionalPledgeAmount(): Observable<String>

        /** Emits when the additional pledge amount should be hidden. */
        fun additionalPledgeAmountIsGone(): Observable<Boolean>

        /** Emits when the reward card should be animated. */
        fun animateRewardCard(): Observable<PledgeData>

        /** Emits the base URL to build terms URLs. */
        fun baseUrlForTerms(): Observable<String>

        /** Emits a boolean determining if the cancel pledge button should be hidden. */
        fun cancelPledgeButtonIsGone(): Observable<Boolean>

        /** Emits a list of stored cards for a user. */
        fun cardsAndProject(): Observable<Pair<List<StoredCard>, Project>>

        /** Emits a boolean determining if the change payment method pledge button should be hidden. */
        fun changePaymentMethodButtonIsGone(): Observable<Boolean>

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

        /** Emits the pledge amount string of the reward. */
        fun pledgeAmount(): Observable<String>

        /** Emits the hint text for the pledge amount. */
        fun pledgeHint(): Observable<String>

        /** Emits the color resource ID of the pledge amount. */
        fun pledgeTextColor(): Observable<Int>

        /** Emits the currency symbol string of the project. */
        fun projectCurrencySymbol(): Observable<Pair<SpannableString, Boolean>>

        /** Emits the currently selected shipping rule. */
        fun selectedShippingRule(): Observable<ShippingRule>

        /** Emits the shipping amount of the selected shipping rule. */
        fun shippingAmount(): Observable<String>

        /** Emits a pair of list of shipping rules to be selected and the project. */
        fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>>

        /** Emits when the shipping rules section should be hidden. */
        fun shippingRulesSectionIsGone(): Observable<Boolean>

        /** Emits when we should start the [com.kickstarter.ui.fragments.CancelPledgeFragment]. */
        fun showCancelPledge(): Observable<Project>

        /** Emits when we should the user a warning about not satisfying the reward's minimum. */
        fun showMinimumWarning(): Observable<String>

        /** Emits when we should show the [com.kickstarter.ui.fragments.NewCardFragment]. */
        fun showNewCardFragment(): Observable<Void>

        /** Emits when the cards adapter should update the selected position. */
        fun showPledgeCard(): Observable<Pair<Int, CardState>>

        /**  Emits when the pledge call was unsuccessful. */
        fun showPledgeError(): Observable<Void>

        /** Emits when we should start a Chrome tab. */
        fun startChromeTab(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.LoginToutActivity]. */
        fun startLoginToutActivity(): Observable<Void>

        /** Emits when we the pledge was successful and should start the [com.kickstarter.ui.activities.ThanksActivity]. */
        fun startThanksActivity(): Observable<Project>

        /** Emits the total amount string of the pledge. */
        fun totalAmount(): Observable<SpannableString>

        /** Emits a boolean determining if the total container should be hidden. */
        fun totalContainerIsGone(): Observable<Boolean>

        /** Emits the color resource ID of the total amount. */
        fun totalTextColor(): Observable<Int>

        /** Emits a boolean determining if the update pledge button should be hidden. */
        fun updatePledgeButtonIsGone(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val addedCardPosition = PublishSubject.create<Int>()
        private val cancelPledgeButtonClicked = PublishSubject.create<Void>()
        private val cardSaved = PublishSubject.create<StoredCard>()
        private val closeCardButtonClicked = PublishSubject.create<Int>()
        private val continueButtonClicked = PublishSubject.create<Void>()
        private val decreasePledgeButtonClicked = PublishSubject.create<Void>()
        private val increasePledgeButtonClicked = PublishSubject.create<Void>()
        private val linkClicked = PublishSubject.create<String>()
        private val newCardButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val pledgeButtonClicked = PublishSubject.create<String>()
        private val pledgeInput = PublishSubject.create<String>()
        private val selectCardButtonClicked = PublishSubject.create<Int>()
        private val shippingRule = PublishSubject.create<ShippingRule>()

        private val addedCard = BehaviorSubject.create<Pair<StoredCard, Project>>()
        private val additionalPledgeAmount = BehaviorSubject.create<String>()
        private val additionalPledgeAmountIsGone = BehaviorSubject.create<Boolean>()
        private val animateRewardCard = BehaviorSubject.create<PledgeData>()
        private val baseUrlForTerms = BehaviorSubject.create<String>()
        private val cancelPledgeButtonIsGone = BehaviorSubject.create<Boolean>()
        private val cardsAndProject = BehaviorSubject.create<Pair<List<StoredCard>, Project>>()
        private val changePaymentMethodButtonIsGone = BehaviorSubject.create<Boolean>()
        private val continueButtonIsGone = BehaviorSubject.create<Boolean>()
        private val conversionText = BehaviorSubject.create<String>()
        private val conversionTextViewIsGone = BehaviorSubject.create<Boolean>()
        private val decreasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val estimatedDeliveryInfoIsGone = BehaviorSubject.create<Boolean>()
        private val increasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val paymentContainerIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeAmount = BehaviorSubject.create<String>()
        private val pledgeHint = BehaviorSubject.create<String>()
        private val pledgeTextColor = BehaviorSubject.create<Int>()
        private val projectCurrencySymbol = BehaviorSubject.create<Pair<SpannableString, Boolean>>()
        private val selectedShippingRule = BehaviorSubject.create<ShippingRule>()
        private val shippingAmount = BehaviorSubject.create<String>()
        private val shippingRulesAndProject = BehaviorSubject.create<Pair<List<ShippingRule>, Project>>()
        private val shippingRulesSectionIsGone = BehaviorSubject.create<Boolean>()
        private val showCancelPledge = PublishSubject.create<Project>()
        private val showMinimumWarning = PublishSubject.create<String>()
        private val showNewCardFragment = PublishSubject.create<Void>()
        private val showPledgeCard = BehaviorSubject.create<Pair<Int, CardState>>()
        private val showPledgeError = BehaviorSubject.create<Void>()
        private val startChromeTab = PublishSubject.create<String>()
        private val startLoginToutActivity = PublishSubject.create<Void>()
        private val startThanksActivity = PublishSubject.create<Project>()
        private val totalAmount = BehaviorSubject.create<SpannableString>()
        private val totalContainerIsGone = BehaviorSubject.create<Boolean>()
        private val totalTextColor = BehaviorSubject.create<Int>()
        private val updatePledgeButtonIsGone = BehaviorSubject.create<Boolean>()

        private val apiClient = environment.apiClient()
        private val apolloClient = environment.apolloClient()
        private val currentConfig = environment.currentConfig()
        private val currentUser = environment.currentUser()
        private val ksCurrency = environment.ksCurrency()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val userIsLoggedIn = this.currentUser.isLoggedIn
                    .distinctUntilChanged()

            val reward = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_REWARD) as Reward }

            val screenLocation = arguments()
                    .map { it.getSerializable(ArgumentsKey.PLEDGE_SCREEN_LOCATION) as ScreenLocation }

            val project = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_PROJECT) as Project }

            val projectAndReward = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))

            val isBackedReward = projectAndReward
                    .map { BackingUtils.isBacked(it.first, it.second) }

            val backing = projectAndReward
                    .filter { BackingUtils.isBacked(it.first, it.second) }
                    .map { it.first.backing() }
                    .ofType(Backing::class.java)

            // Mini reward card
            Observable.combineLatest(screenLocation, reward, project, ::PledgeData)
                    .compose<PledgeData>(takeWhen(this.onGlobalLayout))
                    .compose(bindToLifecycle())
                    .subscribe { this.animateRewardCard.onNext(it) }

            // Estimated delivery section
            reward
                    .map { it.estimatedDeliveryOn() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { dateTime -> dateTime?.let { DateTimeUtils.estimatedDeliveryOn(it) } }
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
                    .map<String> { NumberUtils.format(it.toInt()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeHint)

            project
                    .map { ProjectViewUtils.currencySymbolAndPosition(it, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.projectCurrencySymbol)

            // Pledge stepper section
            val additionalPledgeAmount = BehaviorSubject.create<Double>(0.0)

            val additionalAmountOrZero = additionalPledgeAmount
                    .map { Math.max(0.0, it) }

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

            val pledgeInput = Observable.merge(rewardMinimum, this.pledgeInput.map { NumberUtils.parse(it) })
                    .distinctUntilChanged()
                    .compose<Pair<Double, Country>>(combineLatestPair(country.distinctUntilChanged()))
                    .map { Math.min(it.first, it.second.maxPledge.toDouble()) }

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

            Observable.merge(rewardMinimum, pledgeInput)
                    .distinctUntilChanged()
                    .map { NumberUtils.format(it.toFloat(), NumberOptions.builder().precision(NumberUtils.precision(it, RoundingMode.HALF_UP)).build()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeAmount)

            // Shipping rules section
            val shippingRules = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))
                    .filter { RewardUtils.isShippable(it.second) }
                    .switchMap<ShippingRulesEnvelope> { this.apiClient.fetchShippingRules(it.first, it.second).compose(neverError()) }
                    .map { it.shippingRules() }
                    .share()

            val rulesAndProject = shippingRules
                    .compose<Pair<List<ShippingRule>, Project>>(combineLatestPair(project))

            rulesAndProject
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRulesAndProject)

            reward
                    .map { RewardUtils.isShippable(it) }
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRulesSectionIsGone)

            val defaultShippingRule = shippingRules
                    .compose<Pair<List<ShippingRule>, Boolean>>(combineLatestPair(isBackedReward))
                    .filter { it.first.isNotEmpty() && !it.second }
                    .switchMap { defaultShippingRule(it.first) }

            val backingShippingRule = shippingRules
                    .compose<Pair<List<ShippingRule>, Boolean>>(combineLatestPair(isBackedReward))
                    .filter { it.first.isNotEmpty() && it.second }
                    .map { it.first }
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
                    .map<String> { NumberUtils.format(it.toInt()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingAmount)

            // Total pledge section
            val basePledgeAmount = pledgeInput

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
                    .map<SpannableString> { ProjectViewUtils.styleCurrency(it) }
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
                    .subscribe(this.conversionTextViewIsGone)

            // Manage pledge section
            projectAndReward
                    .map { it.first.isLive && BackingUtils.isBacked(it.first, it.second) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .subscribe(this.updatePledgeButtonIsGone)

            projectAndReward
                    .map { it.first.isLive && BackingUtils.isBacked(it.first, it.second) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .subscribe(this.changePaymentMethodButtonIsGone)

            projectAndReward
                    .map { it.first.isLive && BackingUtils.isBacked(it.first, it.second) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .subscribe(this.cancelPledgeButtonIsGone)

            projectAndReward
                    .map { it.first.isLive && it.first.isBacking && BackingUtils.isBacked(it.first, it.second) }
                    .distinctUntilChanged()
                    .subscribe(this.totalContainerIsGone)

            project
                    .compose<Project>(takeWhen(this.cancelPledgeButtonClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.showCancelPledge)

            // Payment section
            userIsLoggedIn
                    .compose<Pair<Boolean, Pair<Project, Reward>>>(combineLatestPair(projectAndReward))
                    .map {
                        when {
                            !it.first -> true
                            it.second.first.isLive && BackingUtils.isBacked(it.second.first, it.second.second) -> true
                            else -> false
                        }
                    }
                    .compose(bindToLifecycle())
                    .subscribe(this.paymentContainerIsGone)

            userIsLoggedIn
                    .compose(bindToLifecycle())
                    .subscribe(this.continueButtonIsGone)

            userIsLoggedIn
                    .filter { BooleanUtils.isTrue(it) }
                    .switchMap { storedCards() }
                    .delaySubscription(total)
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

            this.newCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.showNewCardFragment)

            pledgeLessThanMinimum
                    .compose<Boolean>(takeWhen(this.continueButtonClicked))
                    .filter { BooleanUtils.isFalse(it) }
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe(this.startLoginToutActivity)

            val pledgeAttempt = Observable.merge(this.continueButtonClicked, this.pledgeButtonClicked)

            pledgeLessThanMinimum
                    .compose<Boolean>(takeWhen(pledgeAttempt))
                    .filter { BooleanUtils.isTrue(it) }
                    .compose<Pair<Boolean, Pair<Project, Reward>>>(combineLatestPair(projectAndReward))
                    .map { it.second }
                    .map { this.ksCurrency.format(it.second.minimum(), it.first, RoundingMode.HALF_UP) }
                    .compose(bindToLifecycle())
                    .subscribe(this.showMinimumWarning)

            val location: Observable<Location?> = Observable.merge(Observable.just(null as Location?), shippingRule.map { it.location() })

            val validPledgeClick = pledgeLessThanMinimum
                    .compose<Pair<Boolean, String>>(takePairWhen(this.pledgeButtonClicked))
                    .filter { BooleanUtils.isFalse(it.first) }
                    .map { it.second }

            val checkoutNotification = Observable.combineLatest(project,
                    total.map { it.toString() },
                    validPledgeClick,
                    location.map { it?.id()?.toString() },
                    reward)
            { p, a, id, l, r -> Checkout(p, a, id, l, r) }
                    .switchMap {
                        this.apolloClient.checkout(it.project, it.amount, it.paymentSourceId, it.locationId, it.reward)
                            .doOnSubscribe { this.showPledgeCard.onNext(Pair(selectedPosition.value, CardState.LOADING)) }
                            .materialize()
                    }
                    .share()

            val checkoutValues = checkoutNotification
                    .compose(values())

            Observable.merge(checkoutNotification.compose(errors()), checkoutValues.filter { BooleanUtils.isFalse(it) })
                    .compose(ignoreValues())
                    .compose(bindToLifecycle())
                    .subscribe{
                        this.showPledgeError.onNext(null)
                        this.showPledgeCard.onNext(Pair(selectedPosition.value, CardState.PLEDGE))
                    }

            project
                    .compose<Project>(takeWhen(checkoutValues.filter { BooleanUtils.isTrue(it) }))
                    .compose(bindToLifecycle())
                    .subscribe(this.startThanksActivity)

            this.baseUrlForTerms.onNext(this.environment.webEndpoint())

            this.linkClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.startChromeTab)
        }

        private fun backingShippingRule(shippingRules: List<ShippingRule>, backing: Backing): Observable<ShippingRule> {
            return Observable.just(shippingRules.firstOrNull { it.location().id() == backing.locationId() })
        }

        private fun defaultShippingRule(shippingRules: List<ShippingRule>): Observable<ShippingRule> {
            return currentConfig.observable()
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

        data class Checkout(val project: Project, val amount: String, val paymentSourceId: String, val locationId: String?, val reward: Reward?)

        override fun addedCardPosition(position: Int) = this.addedCardPosition.onNext(position)

        override fun cancelPledgeButtonClicked() = this.cancelPledgeButtonClicked.onNext(null)

        override fun cardSaved(storedCard: StoredCard) = this.cardSaved.onNext(storedCard)

        override fun closeCardButtonClicked(position: Int) = this.closeCardButtonClicked.onNext(position)

        override fun continueButtonClicked() = this.continueButtonClicked.onNext(null)

        override fun decreasePledgeButtonClicked() = this.decreasePledgeButtonClicked.onNext(null)

        override fun increasePledgeButtonClicked() = this.increasePledgeButtonClicked.onNext(null)

        override fun linkClicked(url: String) = this.linkClicked.onNext(url)

        override fun newCardButtonClicked() = this.newCardButtonClicked.onNext(null)

        override fun onGlobalLayout() = this.onGlobalLayout.onNext(null)

        override fun pledgeInput(amount: String) = this.pledgeInput.onNext(amount)

        override fun pledgeButtonClicked(cardId: String) = this.pledgeButtonClicked.onNext(cardId)

        override fun shippingRuleSelected(shippingRule: ShippingRule) = this.shippingRule.onNext(shippingRule)

        override fun selectCardButtonClicked(position: Int) = this.selectCardButtonClicked.onNext(position)

        @NonNull
        override fun addedCard(): Observable<Pair<StoredCard, Project>> = this.addedCard

        @NonNull
        override fun additionalPledgeAmount(): Observable<String> = this.additionalPledgeAmount

        @NonNull
        override fun additionalPledgeAmountIsGone(): Observable<Boolean> = this.additionalPledgeAmountIsGone

        @NonNull
        override fun animateRewardCard(): Observable<PledgeData> = this.animateRewardCard

        @NonNull
        override fun baseUrlForTerms(): Observable<String> = this.baseUrlForTerms

        @NonNull
        override fun cancelPledgeButtonIsGone(): Observable<Boolean> = this.cancelPledgeButtonIsGone

        @NonNull
        override fun cardsAndProject(): Observable<Pair<List<StoredCard>, Project>> = this.cardsAndProject

        @NonNull
        override fun changePaymentMethodButtonIsGone(): Observable<Boolean> = this.changePaymentMethodButtonIsGone

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
        override fun pledgeHint(): Observable<String> = this.pledgeHint

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
        override fun showCancelPledge(): Observable<Project> = this.showCancelPledge

        @NonNull
        override fun showMinimumWarning(): Observable<String> = this.showMinimumWarning

        @NonNull
        override fun showNewCardFragment(): Observable<Void> = this.showNewCardFragment

        @NonNull
        override fun showPledgeCard(): Observable<Pair<Int, CardState>> = this.showPledgeCard

        @NonNull
        override fun showPledgeError(): Observable<Void> = this.showPledgeError

        @NonNull
        override fun startChromeTab(): Observable<String> = this.startChromeTab

        @NonNull
        override fun startLoginToutActivity(): Observable<Void> = this.startLoginToutActivity

        @NonNull
        override fun startThanksActivity(): Observable<Project> = this.startThanksActivity

        @NonNull
        override fun totalAmount(): Observable<SpannableString> = this.totalAmount

        @NonNull
        override fun totalContainerIsGone(): Observable<Boolean> = this.totalContainerIsGone

        @NonNull
        override fun totalTextColor(): Observable<Int> = this.totalTextColor

        @NonNull
        override fun updatePledgeButtonIsGone(): Observable<Boolean> = this.updatePledgeButtonIsGone

    }
}
