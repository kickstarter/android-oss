package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityRequestCodes
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers.*
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.models.StoredCard
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.ActivityResult
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

        /** Call when the new card button is clicked. */
        fun newCardButtonClicked()

        /** Call when the view has been laid out. */
        fun onGlobalLayout()

        /** Call when user clicks the pledge button. */
        fun pledgeButtonClicked()

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

        /** Emits a list of stored cards for a user. */
        fun cards(): Observable<List<StoredCard>>

        /**  Emits a boolean determining if the continue button should be hidden. */
        fun continueButtonIsGone(): Observable<Boolean>

        /** Set the USD conversion.  */
        fun conversionText(): Observable<String>

        /** Returns `true` if the USD conversion section should be hidden, `false` otherwise.  */
        fun conversionTextViewIsGone(): Observable<Boolean>

        /**  Emits a boolean determining if the decrease pledge button should be enabled. */
        fun decreasePledgeButtonIsEnabled(): Observable<Boolean>

        /** Emits the estimated delivery date string of the reward. */
        fun estimatedDelivery(): Observable<String>

        /**  Emits a boolean determining if the increase pledge button should be enabled.*/
        fun increasePledgeButtonIsEnabled(): Observable<Boolean>

        /**  Emits a boolean determining if the payment container should be hidden. */
        fun paymentContainerIsGone(): Observable<Boolean>

        /** Emits the pledge amount string of the reward. */
        fun pledgeAmount(): Observable<SpannableString>

        /** Emits the currently selected shipping rule. */
        fun selectedShippingRule(): Observable<ShippingRule>

        /** Emits the shipping string of the selected shipping rule. */
        fun shippingAmount(): Observable<SpannableString>

        /** Emits a pair of list of shipping rules to be selected and the project. */
        fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>>

        /** Emits when the shipping rules section should be hidden. */
        fun shippingRulesSectionIsGone(): Observable<Boolean>

        /** Emits when the cards adapter should update selected position. */
        fun showPledgeCard(): Observable<Pair<Int, Boolean>>

        /** Emits when we should start the [com.kickstarter.ui.activities.LoginToutActivity]. */
        fun startLoginToutActivity(): Observable<Void>

        /** Emits when we should start the [com.kickstarter.ui.activities.NewCardActivity]. */
        fun startNewCardActivity(): Observable<Void>

        /** Emits the total amount string of the pledge.*/
        fun totalAmount(): Observable<SpannableString>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<PledgeFragment>(environment), Inputs, Outputs {

        private val closeCardButtonClicked = PublishSubject.create<Int>()
        private val continueButtonClicked = PublishSubject.create<Void>()
        private val decreasePledgeButtonClicked = PublishSubject.create<Void>()
        private val increasePledgeButtonClicked = PublishSubject.create<Void>()
        private val newCardButtonClicked = PublishSubject.create<Void>()
        private val onGlobalLayout = PublishSubject.create<Void>()
        private val pledgeButtonClicked = PublishSubject.create<Void>()
        private val selectCardButtonClicked = PublishSubject.create<Int>()
        private val shippingRule = PublishSubject.create<ShippingRule>()

        private val animateRewardCard = BehaviorSubject.create<PledgeData>()
        private val additionalPledgeAmount = BehaviorSubject.create<String>()
        private val additionalPledgeAmountIsGone = BehaviorSubject.create<Boolean>()
        private val cards = BehaviorSubject.create<List<StoredCard>>()
        private val continueButtonIsGone = BehaviorSubject.create<Boolean>()
        private val conversionText = BehaviorSubject.create<String>()
        private val conversionTextViewIsGone = BehaviorSubject.create<Boolean>()
        private val decreasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val increasePledgeButtonIsEnabled = BehaviorSubject.create<Boolean>()
        private val paymentContainerIsGone = BehaviorSubject.create<Boolean>()
        private val pledgeAmount = BehaviorSubject.create<SpannableString>()
        private val shippingAmount = BehaviorSubject.create<SpannableString>()
        private val shippingRules = BehaviorSubject.create<List<ShippingRule>>()
        private val shippingRulesAndProject = BehaviorSubject.create<Pair<List<ShippingRule>, Project>>()
        private val selectedShippingRule = BehaviorSubject.create<ShippingRule>()
        private val shippingRulesSectionIsGone = BehaviorSubject.create<Boolean>()
        private val showPledgeCard = BehaviorSubject.create<Pair<Int, Boolean>>()
        private val startLoginToutActivity = PublishSubject.create<Void>()
        private val startNewCardActivity = PublishSubject.create<Void>()
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
                    .map { dateTime -> dateTime?.let { DateTimeUtils.estimatedDeliveryOn(it) } }
                    .compose(bindToLifecycle())
                    .subscribe { this.estimatedDelivery.onNext(it) }

            val rewardAmount = reward
                    .map { it.minimum() }

            rewardAmount
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<SpannableString> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP, 0) }
                    .compose(bindToLifecycle())
                    .subscribe { this.pledgeAmount.onNext(it) }

            val shippingRules = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))
                    .switchMap<ShippingRulesEnvelope> { this.apiClient.fetchShippingRules(it.first, it.second) }
                    .map { it.shippingRules() }
                    .distinctUntilChanged()

            shippingRules
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRules)

            val rulesAndProject = shippingRules
                    .compose<Pair<List<ShippingRule>, Project>>(combineLatestPair(project))

            rulesAndProject
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRulesAndProject)

            rulesAndProject
                    .compose(bindToLifecycle())
                    .map { ObjectUtils.isNull(it.first) || it.first.isEmpty() }
                    .subscribe(this.shippingRulesSectionIsGone)

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
                    .map<String> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP) }
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

            val rewardMinimum = reward
                    .map { it.minimum() }

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

            userIsLoggedIn
                    .filter { BooleanUtils.isTrue(it) }
                    .switchMap { getListOfStoredCards() }
                    .compose(bindToLifecycle())
                    .subscribe(this.cards)

            val shippingRule = Observable.merge(this.shippingRule, defaultShippingRule)

            shippingRule
                    .compose(bindToLifecycle())
                    .subscribe(this.selectedShippingRule)

            val shippingAmount = shippingRule
                    .map { it.cost() }

            shippingAmount
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<SpannableString> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP, 2) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingAmount)

            val rulesAndReward = shippingRules
                    .compose<Pair<List<ShippingRule>, Reward>>(combineLatestPair(reward))

            val initialTotalAmount = rulesAndReward
                    .filter { ObjectUtils.isNull(it.first) || it.first.isEmpty() }
                    .map { it.second.minimum() }
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<SpannableString> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP, 2) }
                    .compose(bindToLifecycle())

            val totalWithShippingRule = rewardAmount
                    .compose<Pair<Double, Double>>(combineLatestPair(shippingAmount))
                    .map { it.first + it.second }
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map<SpannableString> { this.ksCurrency.formatWithProjectCurrency(it.first, it.second, RoundingMode.UP, 2) }
                    .compose(bindToLifecycle())

            Observable.merge(initialTotalAmount, totalWithShippingRule)
                    .compose(bindToLifecycle())
                    .subscribe(this.totalAmount)

            val projectAndReward = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))

            projectAndReward
                    .compose(bindToLifecycle())

            projectAndReward
                    .map { p -> p.first.currency() != p.first.currentCurrency() || RewardUtils.isNoReward(p.second) }
                    .map { BooleanUtils.negate(it) }
                    .subscribe { this.conversionTextViewIsGone.onNext(it) }

            val initialTotalConversionAmount = rewardAmount
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { this.ksCurrency.formatWithUserPreference(it.first, it.second, RoundingMode.UP, 2) }
                    .compose(bindToLifecycle())

            val totalConversionAmount = rewardAmount
                    .compose<Pair<Double, Double>>(combineLatestPair(shippingAmount))
                    .map { it.first + it.second }
                    .compose<Pair<Double, Project>>(combineLatestPair(project))
                    .map { this.ksCurrency.formatWithUserPreference(it.first, it.second, RoundingMode.UP, 2) }

            Observable.merge(initialTotalConversionAmount, totalConversionAmount)
                    .compose(bindToLifecycle())
                    .subscribe { this.conversionText.onNext(it) }

            this.selectCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.showPledgeCard.onNext(Pair(it, true)) }

            this.closeCardButtonClicked
                    .compose(bindToLifecycle())
                    .subscribe { this.showPledgeCard.onNext(Pair(it, false)) }

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
                    .subscribe { this.cards.onNext(it) }
        }

        private fun getDefaultShippingRule(shippingRules: List<ShippingRule>): Observable<ShippingRule> {
            return currentConfig.observable()
                    .map { it.countryCode() }
                    .map { countryCode ->
                        shippingRules.firstOrNull { it.location().country() == countryCode }
                                ?: shippingRules.first()
                    }
        }

        override fun closeCardButtonClicked(position: Int) {
            this.closeCardButtonClicked.onNext(position)
        }

        override fun continueButtonClicked() {
            this.continueButtonClicked.onNext(null)
        }

        override fun decreasePledgeButtonClicked() {
            this.decreasePledgeButtonClicked.onNext(null)
        }

        override fun increasePledgeButtonClicked() {
            this.increasePledgeButtonClicked.onNext(null)
        }

        override fun newCardButtonClicked() {
            this.newCardButtonClicked.onNext(null)
        }

        override fun onGlobalLayout() {
            this.onGlobalLayout.onNext(null)
        }

        override fun pledgeButtonClicked() {
            this.pledgeButtonClicked.onNext(null)
        }

        override fun shippingRuleSelected(shippingRule: ShippingRule) {
            this.shippingRule.onNext(shippingRule)
        }

        override fun selectCardButtonClicked(position: Int) {
            this.selectCardButtonClicked.onNext(position)
        }

        override fun additionalPledgeAmount(): Observable<String> = this.additionalPledgeAmount

        override fun additionalPledgeAmountIsGone(): Observable<Boolean> = this.additionalPledgeAmountIsGone

        override fun animateRewardCard(): Observable<PledgeData> = this.animateRewardCard

        @NonNull
        override fun cards(): Observable<List<StoredCard>> = this.cards

        @NonNull
        override fun continueButtonIsGone(): Observable<Boolean> = this.continueButtonIsGone

        @NonNull
        override fun conversionTextViewIsGone(): Observable<Boolean> {
            return this.conversionTextViewIsGone
        }

        @NonNull
        override fun conversionText(): Observable<String> {
            return this.conversionText
        }

        override fun decreasePledgeButtonIsEnabled(): Observable<Boolean> = this.decreasePledgeButtonIsEnabled

        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        override fun increasePledgeButtonIsEnabled(): Observable<Boolean> = this.increasePledgeButtonIsEnabled

        override fun paymentContainerIsGone(): Observable<Boolean> = this.paymentContainerIsGone

        @NonNull
        override fun pledgeAmount(): Observable<SpannableString> = this.pledgeAmount

        @NonNull
        override fun selectedShippingRule(): Observable<ShippingRule> = this.selectedShippingRule

        @NonNull
        override fun shippingAmount(): Observable<SpannableString> = this.shippingAmount

        override fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>> = this.shippingRulesAndProject

        @NonNull
        override fun shippingRulesSectionIsGone(): Observable<Boolean> = this.shippingRulesSectionIsGone

        @NonNull
        override fun showPledgeCard(): Observable<Pair<Int, Boolean>> = this.showPledgeCard

        @NonNull
        override fun startLoginToutActivity(): Observable<Void> = this.startLoginToutActivity

        override fun startNewCardActivity(): Observable<Void> = this.startNewCardActivity

        @NonNull
        override fun totalAmount(): Observable<SpannableString> = this.totalAmount

        private fun getListOfStoredCards(): Observable<List<StoredCard>> {
            return this.apolloClient.getStoredCards()
                    .compose(bindToLifecycle())
                    .compose(neverError())
        }

    }
}
