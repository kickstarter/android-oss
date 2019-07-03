package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.*
import com.kickstarter.models.*
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ActivityResult
import com.kickstarter.ui.data.CardState
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.ScreenLocation
import com.kickstarter.ui.fragments.PledgeFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode

interface PledgeFragmentViewModel {
    interface Inputs {
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

        /** Call when user clicks the pledge button. */
        fun pledgeButtonClicked(cardId: String)

        /** Call when user selects a card they want to pledge with. */
        fun selectCardButtonClicked(position: Int)

        /** Call when user selects a shipping location. */
        fun shippingRuleSelected(shippingRule: ShippingRule)
    }

    interface Outputs {
        /** Emits the additional pledge amount string. */
        fun additionalPledgeAmount(): Observable<String>

        /** Emits when the additional pledge amount should be hidden. */
        fun additionalPledgeAmountIsGone(): Observable<Boolean>

        /** Emits when the reward card should be animated. */
        fun animateRewardCard(): Observable<PledgeData>

        /** Emits the base URL to build terms URLs. */
        fun baseUrlForTerms(): Observable<String>

        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<List<StoredCard>>

        /**  Emits a boolean determining if the continue button should be hidden. */
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

        /**  Emits a boolean determining if the payment container should be hidden. */
        fun paymentContainerIsGone(): Observable<Boolean>

        /** Emits the pledge amount string of the reward. */
        fun pledgeAmount(): Observable<SpannableString>

        /** Emits the currently selected shipping rule. */
        fun selectedShippingRule(): Observable<ShippingRule>

        /** Emits the shipping amount of the selected shipping rule. */
        fun shippingAmount(): Observable<SpannableString>

        /** Emits a pair of list of shipping rules to be selected and the project. */
        fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>>

        /** Emits when the shipping rules section should be hidden. */
        fun shippingRulesSectionIsGone(): Observable<Boolean>

        /** Emits when the cards adapter should update the selected position. */
        fun showPledgeCard(): Observable<Pair<Int, CardState>>

        /**  Emits when the pledge call was unsuccessful. */
        fun showPledgeError(): Observable<Void>

        /** Emits when we should start a Chrome tab. */
        fun startChromeTab(): Observable<String>

        /** Emits when we should start the [com.kickstarter.ui.activities.LoginToutActivity]. */
        fun startLoginToutActivity(): Observable<Void>

        /** Emits when we should start the [com.kickstarter.ui.activities.NewCardActivity]. */
        fun startNewCardActivity(): Observable<Void>

        /** Emits when we the pledge was successful and should start the [com.kickstarter.ui.activities.ThanksActivity]. */
        fun startThanksActivity(): Observable<Project>

        /** Emits the total amount string of the pledge.*/
        fun totalAmount(): Observable<SpannableString>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val closeCardButtonClicked = PublishSubject.create<Int>()
        private val continueButtonClicked = PublishSubject.create<Void>()
        private val decreasePledgeButtonClicked = PublishSubject.create<Void>()
        private val increasePledgeButtonClicked = PublishSubject.create<Void>()
        private val linkClicked = PublishSubject.create<String>()
        private val newCardButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val pledgeButtonClicked = PublishSubject.create<String>()
        private val selectCardButtonClicked = PublishSubject.create<Int>()
        private val shippingRule = PublishSubject.create<ShippingRule>()

        private val animateRewardCard = BehaviorSubject.create<PledgeData>()
        private val additionalPledgeAmount = BehaviorSubject.create<String>()
        private val additionalPledgeAmountIsGone = BehaviorSubject.create<Boolean>()
        private val baseUrlForTerms = BehaviorSubject.create<String>()
        private val cards = BehaviorSubject.create<List<StoredCard>>()
        private val continueButtonIsGone = BehaviorSubject.create<Boolean>()
        private val conversionText = BehaviorSubject.create<String>()
        private val conversionTextViewIsGone = BehaviorSubject.create<Boolean>()
        private val decreasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val estimatedDeliveryInfoIsGone = BehaviorSubject.create<Boolean>()
        private val increasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val paymentContainerIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeAmount = BehaviorSubject.create<SpannableString>()
        private val shippingAmount = BehaviorSubject.create<SpannableString>()
        private val shippingRulesAndProject = BehaviorSubject.create<Pair<List<ShippingRule>, Project>>()
        private val selectedShippingRule = BehaviorSubject.create<ShippingRule>()
        private val shippingRulesSectionIsGone = BehaviorSubject.create<Boolean>()
        private val showPledgeCard = BehaviorSubject.create<Pair<Int, CardState>>()
        private val showPledgeError = BehaviorSubject.create<Void>()
        private val startChromeTab = PublishSubject.create<String>()
        private val startLoginToutActivity = PublishSubject.create<Void>()
        private val startNewCardActivity = PublishSubject.create<Void>()
        private val startThanksActivity = PublishSubject.create<Project>()
        private val totalAmount = BehaviorSubject.create<SpannableString>()

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

            reward
                    .map { it.estimatedDeliveryOn() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { dateTime -> dateTime?.let { DateTimeUtils.estimatedDeliveryOn(it) } }
                    .compose(bindToLifecycle())
                    .subscribe(this.estimatedDelivery)

            val projectAndReward = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))

            projectAndReward
                    .compose(bindToLifecycle())

            projectAndReward
                    .map { it.first.currency() != it.first.currentCurrency() }
                    .map { BooleanUtils.negate(it) }
                    .subscribe(this.conversionTextViewIsGone)

            val rewardMinimum = reward
                    .map { it.minimum() }

            rewardMinimum
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<SpannableString> { ViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.pledgeAmount)

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

            reward
                    .map { ObjectUtils.isNull(it.estimatedDeliveryOn()) || RewardUtils.isNoReward(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.estimatedDeliveryInfoIsGone)

            val defaultShippingRule = shippingRules
                    .filter { it.isNotEmpty() }
                    .switchMap { getDefaultShippingRule(it) }

            val additionalPledgeAmount = BehaviorSubject.create<Double>(0.0)

            this.increasePledgeButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { additionalPledgeAmount.onNext(additionalPledgeAmount.value + 1) }

            this.decreasePledgeButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { additionalPledgeAmount.onNext(additionalPledgeAmount.value - 1) }

            additionalPledgeAmount
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<String> { this.ksCurrency.format(it.first, it.second, RoundingMode.HALF_UP) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.additionalPledgeAmount)

            additionalPledgeAmount
                    .map { IntegerUtils.isZero(it.toInt()) }
                    .distinctUntilChanged()
                    .subscribe(this.additionalPledgeAmountIsGone)

            additionalPledgeAmount
                    .map { IntegerUtils.isZero(it.toInt()) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .subscribe(this.decreasePledgeButtonIsEnabled)

            Observable
                    .merge(rewardMinimum, rewardMinimum.compose<Pair<Double, Double>>(combineLatestPair(additionalPledgeAmount)).map { it.first + it.second })
                    .map { RewardUtils.isMaxRewardAmount(it) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .subscribe(this.increasePledgeButtonIsEnabled)

            Observable.combineLatest(screenLocation, reward, project, ::PledgeData)
                    .compose<PledgeData>(takeWhen(this.onGlobalLayout))
                    .compose(bindToLifecycle())
                    .subscribe { this.animateRewardCard.onNext(it) }

            userIsLoggedIn
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.paymentContainerIsGone)

            userIsLoggedIn
                    .compose(bindToLifecycle())
                    .subscribe(this.continueButtonIsGone)

            val shippingRule = Observable.merge(this.shippingRule, defaultShippingRule)

            shippingRule
                    .compose(bindToLifecycle())
                    .subscribe(this.selectedShippingRule)

            val shippingAmount = shippingRule
                    .map { it.cost() }

            shippingAmount
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<SpannableString> { ViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingAmount)

            val basePledgeAmount = rewardMinimum
                    .compose<Pair<Double, Double>>(combineLatestPair(additionalPledgeAmount))
                    .map { it.first + it.second }

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
                    .compose<Pair<Double, Project>>(combineLatestPair(project))

            total
                    .map<SpannableString> { ViewUtils.styleCurrency(it.first, it.second, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.totalAmount)

            total
                    .map { this.ksCurrency.formatWithUserPreference(it.first, it.second, RoundingMode.UP, 2) }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversionText)

            userIsLoggedIn
                    .filter { BooleanUtils.isTrue(it) }
                    .switchMap { getListOfStoredCards() }
                    .delaySubscription(total)
                    .compose(bindToLifecycle())
                    .subscribe(this.cards)

            val selectedPosition = BehaviorSubject.create(RecyclerView.NO_POSITION)

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
                    .subscribe(this.startNewCardActivity)

            this.continueButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe(this.startLoginToutActivity)

            activityResult()
                    .filter { it.isRequestCode(ActivityRequestCodes.SAVE_NEW_PAYMENT_METHOD) }
                    .filter(ActivityResult::isOk)
                    .switchMap { getListOfStoredCards() }
                    .compose(bindToLifecycle())
                    .subscribe(this.cards)

            val location: Observable<Location?> = Observable.merge(Observable.just(null as Location?), shippingRule.map { it.location() })

            val checkoutNotification = Observable.combineLatest(project,
                    total.map { it.first.toString() },
                    this.pledgeButtonClicked,
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

        private fun getDefaultShippingRule(shippingRules: List<ShippingRule>): Observable<ShippingRule> {
            return currentConfig.observable()
                    .map { it.countryCode() }
                    .map { countryCode ->
                        shippingRules.firstOrNull { it.location().country() == countryCode }
                                ?: shippingRules.first()
                    }
        }

        override fun closeCardButtonClicked(position: Int) = this.closeCardButtonClicked.onNext(position)

        override fun continueButtonClicked() = this.continueButtonClicked.onNext(null)

        override fun decreasePledgeButtonClicked() = this.decreasePledgeButtonClicked.onNext(null)

        override fun increasePledgeButtonClicked() = this.increasePledgeButtonClicked.onNext(null)

        override fun linkClicked(url: String) = this.linkClicked.onNext(url)

        override fun newCardButtonClicked() = this.newCardButtonClicked.onNext(null)

        override fun onGlobalLayout() = this.onGlobalLayout.onNext(null)

        override fun pledgeButtonClicked(cardId: String) = this.pledgeButtonClicked.onNext(cardId)

        override fun shippingRuleSelected(shippingRule: ShippingRule) = this.shippingRule.onNext(shippingRule)

        override fun selectCardButtonClicked(position: Int) = this.selectCardButtonClicked.onNext(position)

        @NonNull
        override fun additionalPledgeAmount(): Observable<String> = this.additionalPledgeAmount

        @NonNull
        override fun additionalPledgeAmountIsGone(): Observable<Boolean> = this.additionalPledgeAmountIsGone

        @NonNull
        override fun animateRewardCard(): Observable<PledgeData> = this.animateRewardCard

        @NonNull
        override fun baseUrlForTerms(): Observable<String> = this.baseUrlForTerms

        @NonNull
        override fun cards(): Observable<List<StoredCard>> = this.cards

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
        override fun pledgeAmount(): Observable<SpannableString> = this.pledgeAmount

        @NonNull
        override fun selectedShippingRule(): Observable<ShippingRule> = this.selectedShippingRule

        @NonNull
        override fun shippingAmount(): Observable<SpannableString> = this.shippingAmount

        @NonNull
        override fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>> = this.shippingRulesAndProject

        @NonNull
        override fun shippingRulesSectionIsGone(): Observable<Boolean> = this.shippingRulesSectionIsGone

        @NonNull
        override fun showPledgeCard(): Observable<Pair<Int, CardState>> = this.showPledgeCard

        @NonNull
        override fun showPledgeError(): Observable<Void> = this.showPledgeError

        @NonNull
        override fun startChromeTab(): Observable<String> = this.startChromeTab

        @NonNull
        override fun startLoginToutActivity(): Observable<Void> = this.startLoginToutActivity

        @NonNull
        override fun startNewCardActivity(): Observable<Void> = this.startNewCardActivity

        @NonNull
        override fun startThanksActivity(): Observable<Project> = this.startThanksActivity

        @NonNull
        override fun totalAmount(): Observable<SpannableString> = this.totalAmount

        private fun getListOfStoredCards(): Observable<List<StoredCard>> {
            return this.apolloClient.getStoredCards()
                    .compose(bindToLifecycle())
                    .compose(neverError())
        }

        data class Checkout(val project: Project, val amount: String, val paymentSourceId: String, val locationId: String?, val reward: Reward?)

    }
}
