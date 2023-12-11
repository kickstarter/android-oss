package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.VisibleForTesting
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhenV2
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.KsOptional
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isNull
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.models.User
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import java.math.RoundingMode

interface RewardViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward]. */
        fun configureWith(projectData: ProjectData, reward: Reward)

        /** Call when the user clicks on a reward. */
        fun rewardClicked(position: Int)
    }

    interface Outputs {
        /**  Emits the count of backers who have pledged this reward. */
        fun backersCount(): Observable<Int>

        /** Emits a boolean determining if the backers count should be shown. */
        fun backersCountIsGone(): Observable<Boolean>

        /**  Emits the string resource ID to set on the pledge button. */
        fun buttonCTA(): Observable<Int>

        /** Emits `true` if pledge button can be clicked, `false` otherwise.  */
        fun buttonIsEnabled(): Observable<Boolean>

        /** Emits a boolean determining if the pledge button should be shown. */
        fun buttonIsGone(): Observable<Boolean>

        /** Emits the reward's minimum converted to the user's preference  */
        fun conversion(): Observable<String>

        /** Emits `true` if the conversion should be hidden, `false` otherwise.  */
        fun conversionIsGone(): Observable<Boolean>

        /** Emits the reward's description when `isNoReward` is true. */
        fun descriptionForNoReward(): Observable<Int>

        /** Emits the reward's description.  */
        fun descriptionForReward(): Observable<String>

        /** Emits `true` if the reward description is empty and should be hidden in the UI.  */
        fun descriptionIsGone(): Observable<Boolean>

        /** Emits `true` if the reward end date should be hidden,`false` otherwise. */
        fun endDateSectionIsGone(): Observable<Boolean>

        /**  Emits the reward's localized estimated delivery date. */
        fun estimatedDelivery(): Observable<String>

        /** Emits a boolean determining if the estimated delivery should be shown. */
        fun estimatedDeliveryIsGone(): Observable<Boolean>

        /** Emits `true` if the limits container should be hidden, `false` otherwise. */
        fun limitContainerIsGone(): Observable<Boolean>

        /** Emits the minimum pledge amount in the project's currency.  */
        fun minimumAmountTitle(): Observable<SpannableString>

        /** Emits the remaining count of the reward.  */
        fun remaining(): Observable<Int>

        /** Emits `true` if the remaining count should be hidden, `false` otherwise.  */
        fun remainingIsGone(): Observable<Boolean>

        /** Emits the reward to use to display the reward's expiration. */
        fun reward(): Observable<Reward>

        /** Emits the reward's items.  */
        fun rewardItems(): Observable<List<RewardsItem>>

        /** Emits `true` if the items section should be hidden, `false` otherwise.  */
        fun rewardItemsAreGone(): Observable<Boolean>

        /** Set the shipping summary TextView's text.  */
        fun shippingSummary(): Observable<Pair<Int, String?>>

        /** Returns `true` if the shipping summary should be hidden, `false` otherwise.  */
        fun shippingSummaryIsGone(): Observable<Boolean>

        /** Show [com.kickstarter.ui.fragments.PledgeFragment] || [com.kickstarter.ui.fragments.BackingAddOnsFragment]  with the project's reward selected.  */
        fun showFragment(): Observable<Pair<Project, Reward>>

        /** Emits `true` if the title should be hidden, `false` otherwise.  */
        fun titleIsGone(): Observable<Boolean>

        /** Emits the reward's title when `isNoReward` is true.  */
        fun titleForNoReward(): Observable<Int>

        /** Emits the reward's title when `isReward` is true.  */
        fun titleForReward(): Observable<String>

        /** Emits if the reward has add-Ons available */
        fun hasAddOnsAvailable(): Observable<Boolean>

        /** Emits a boolean that determines if the minimum pledge amount should be shown **/
        fun isMinimumPledgeAmountGone(): Observable<Boolean>

        /** Emits a boolean that determines if the selected reward Tag should be shown **/
        fun selectedRewardTagIsGone(): Observable<Boolean>

        /** Emits a boolean that determines if the local PickUp section should be hidden **/
        fun localPickUpIsGone(): Observable<Boolean>

        /** Emits the String with the Local Pickup Displayable name **/
        fun localPickUpName(): Observable<String>
    }

    class ViewModel(environment: Environment) : Inputs, Outputs {
        private val currentUser = requireNotNull(environment.currentUserV2())
        private val ksCurrency = requireNotNull(environment.ksCurrency())

        private val projectDataAndReward = PublishSubject.create<Pair<ProjectData, Reward>>()
        private val rewardClicked = PublishSubject.create<Int>()

        private val backersCount = BehaviorSubject.create<Int>()
        private val backersCountIsGone = BehaviorSubject.create<Boolean>()
        private val buttonCTA = BehaviorSubject.create<Int>()
        private val buttonIsEnabled = BehaviorSubject.create<Boolean>()
        private val buttonIsGone = BehaviorSubject.create<Boolean>()
        private val conversion = BehaviorSubject.create<String>()
        private val conversionIsGone = BehaviorSubject.create<Boolean>()
        private val descriptionForNoReward = BehaviorSubject.create<Int>()
        private val descriptionForReward = BehaviorSubject.create<String>()
        private val descriptionIsGone = BehaviorSubject.create<Boolean>()
        private val endDateSectionIsGone = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val estimatedDeliveryIsGone = BehaviorSubject.create<Boolean>()
        private val limitContainerIsGone = BehaviorSubject.create<Boolean>()
        private val minimumAmountTitle = PublishSubject.create<SpannableString>()
        private val remaining = BehaviorSubject.create<Int>()
        private val remainingIsGone = BehaviorSubject.create<Boolean>()
        private val reward = BehaviorSubject.create<Reward>()
        private val rewardItems = BehaviorSubject.create<List<RewardsItem>>()
        private val rewardItemsAreGone = BehaviorSubject.create<Boolean>()
        private val shippingSummary = BehaviorSubject.create<Pair<Int, String?>>()
        private val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val showFragment = PublishSubject.create<Pair<Project, Reward>>()
        private val titleForNoReward = BehaviorSubject.create<Int>()
        private val titleForReward = BehaviorSubject.create<String>()
        private val titleIsGone = BehaviorSubject.create<Boolean>()
        private val addOnsAvailable = BehaviorSubject.create<Boolean>()
        private val isMinimumPledgeAmountGone = BehaviorSubject.create<Boolean>()
        private val selectedRewardTagIsGone = PublishSubject.create<Boolean>()
        private val localPickUpIsGone = BehaviorSubject.create<Boolean>()
        private val localPickUpName = BehaviorSubject.create<String>()

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val onCAPIEventSent = BehaviorSubject.create<Boolean?>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        val disposables = CompositeDisposable()

        init {

            val reward = this.projectDataAndReward
                .map { it.second }
                .distinctUntilChanged()

            val project = this.projectDataAndReward
                .map { it.first.project() }

            val projectAndReward = project
                .compose<Pair<Project, Reward>>(combineLatestPair(reward))
                .map { Pair(it.first, it.second) }
                .distinctUntilChanged()

            projectAndReward
                .map {
                    RewardViewUtils.styleCurrency(
                        it.second.minimum(),
                        it.first,
                        this.ksCurrency
                    )
                }
                .subscribe { this.minimumAmountTitle.onNext(it) }
                .addToDisposable(disposables)

            val userCreatedProject = this.currentUser.observable()
                .compose<Pair<KsOptional<User>?, Project>>(combineLatestPair(project))
                .map { it.first?.getValue()?.id() == it.second.creator().id() }

            projectAndReward
                .compose<Pair<Pair<Project, Reward>, Boolean>>(combineLatestPair(userCreatedProject))
                .map { buttonIsGone(it.first.first, it.first.second, it.second) }
                .distinctUntilChanged()
                .subscribe { this.buttonIsGone.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { RewardViewUtils.pledgeButtonText(it.first, it.second) }
                .distinctUntilChanged()
                .subscribe {
                    this.buttonCTA.onNext(it)
                }
                .addToDisposable(disposables)

            projectAndReward
                .map { it.first }
                .map { it.currency() == it.currentCurrency() }
                .subscribe { this.conversionIsGone.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map {
                    this.ksCurrency.format(
                        it.second.convertedMinimum(),
                        it.first,
                        true,
                        RoundingMode.HALF_UP,
                        true
                    )
                }
                .subscribe { this.conversion.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .filter { RewardUtils.isNoReward(it.second) }
                .map { it.first.backing()?.isBacked(it.second) ?: false }
                .map {
                    when {
                        it -> R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality
                        else -> R.string.Back_it_because_you_believe_in_it
                    }
                }
                .subscribe { this.descriptionForNoReward.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isReward(it) }
                .filter { it.description().isNotNull() }
                .map { requireNotNull(it.description()) }
                .subscribe { this.descriptionForReward.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { RewardUtils.isReward(it.second) && it.second.description().isNullOrEmpty() }
                .distinctUntilChanged()
                .subscribe { this.descriptionIsGone.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { shouldContinueFlow(it.first, it.second) }
                .distinctUntilChanged()
                .subscribe {
                    this.buttonIsEnabled.onNext(it)
                }
                .addToDisposable(disposables)

            projectAndReward
                .map { it.first.isLive && RewardUtils.isLimited(it.second) }
                .map { it.negate() }
                .distinctUntilChanged()
                .subscribe { this.remainingIsGone.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isLimited(it) }
                .map { it.remaining() ?: -1 }
                .subscribe { this.remaining.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isItemized(it) }
                .filter { it.rewardsItems().isNotNull() }
                .map { requireNotNull(it.rewardsItems()) }
                .subscribe { this.rewardItems.onNext(it) }
                .addToDisposable(disposables)

            reward
                .map { RewardUtils.isItemized(it) }
                .map { it.negate() }
                .distinctUntilChanged()
                .subscribe { this.rewardItemsAreGone.onNext(it) }
                .addToDisposable(disposables)

            reward
                .subscribe { this.reward.onNext(it) }
                .addToDisposable(disposables)

            reward
                .map { it.hasAddons() }
                .subscribe { this.addOnsAvailable.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { expirationDateIsGone(it.first, it.second) }
                .distinctUntilChanged()
                .subscribe { this.endDateSectionIsGone.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .filter { shouldContinueFlow(it.first, it.second) && it.first.isLive }
                .compose<Pair<Project, Reward>>(takeWhenV2(this.rewardClicked))
                .subscribe { this.showFragment.onNext(it) }
                .addToDisposable(disposables)

            this.projectDataAndReward
                .filter { it.first.project().isLive && !it.first.project().isBacking() }
                .compose(takeWhenV2(this.rewardClicked))
                .filter {
                    PledgeData.with(PledgeFlowContext.NEW_PLEDGE, it.first, it.second).isNotNull()
                }
                .map { PledgeData.with(PledgeFlowContext.NEW_PLEDGE, it.first, it.second) }
                .subscribe {
                    environment.analytics()?.trackSelectRewardCTA(it)
                }
                .addToDisposable(disposables)

            projectAndReward
                .filter { RewardUtils.isNoReward(it.second) }
                .map { it.first.backing()?.isBacked(it.second) ?: false }
                .map {
                    when {
                        it -> R.string.You_pledged_without_a_reward
                        else -> R.string.Pledge_without_a_reward
                    }
                }
                .subscribe { this.titleForNoReward.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { it.first.backing()?.isBacked(it.second) ?: false }
                .subscribe {
                    this.selectedRewardTagIsGone.onNext(!it)
                }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isReward(it) }
                .filter { it.title().isNotNull() }
                .map { requireNotNull(it.title()) }
                .subscribe { this.titleForReward.onNext(it) }
                .addToDisposable(disposables)

            reward
                .map { RewardUtils.isReward(it) && it.title().isNullOrEmpty() }
                .distinctUntilChanged()
                .subscribe { this.titleIsGone.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isShippable(it) }
                .filter { RewardUtils.shippingSummary(it).isNotNull() }
                .map { requireNotNull(RewardUtils.shippingSummary(it)) }
                .subscribe { this.shippingSummary.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { !RewardUtils.isShippable(it) }
                .map {
                    RewardUtils.isLocalPickup(it)
                }
                .subscribe {
                    this.localPickUpIsGone.onNext(!it)
                }
                .addToDisposable(disposables)

            reward
                .filter { !RewardUtils.isShippable(it) }
                .filter { RewardUtils.isLocalPickup(it) }
                .filter { it.localReceiptLocation()?.displayableName().isNotNull() }
                .map { requireNotNull(it.localReceiptLocation()?.displayableName()) }
                .subscribe { this.localPickUpName.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { it.first.isLive && RewardUtils.isShippable(it.second) }
                .map { it.negate() }
                .distinctUntilChanged()
                .subscribe { this.shippingSummaryIsGone.onNext(it) }
                .addToDisposable(disposables)

            Observable.combineLatest(
                this.endDateSectionIsGone,
                this.remainingIsGone,
                this.shippingSummaryIsGone,
                this.addOnsAvailable
            ) { endDateGone, remainingGone, shippingGone, addOnsAvailable -> endDateGone && remainingGone && shippingGone && !addOnsAvailable }
                .distinctUntilChanged()
                .subscribe { this.limitContainerIsGone.onNext(it) }
                .addToDisposable(disposables)

            reward
                .map { RewardUtils.isNoReward(it) || !RewardUtils.hasBackers(it) }
                .distinctUntilChanged()
                .subscribe { this.backersCountIsGone.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isReward(it) && RewardUtils.hasBackers(it) }
                .filter { it.backersCount().isNotNull() }
                .map { requireNotNull(it.backersCount()) }
                .subscribe { this.backersCount.onNext(it) }
                .addToDisposable(disposables)

            reward
                .map { RewardUtils.isNoReward(it) || it.estimatedDeliveryOn().isNull() }
                .distinctUntilChanged()
                .subscribe { this.estimatedDeliveryIsGone.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isReward(it) && it.estimatedDeliveryOn().isNotNull() }
                .map<DateTime> { it.estimatedDeliveryOn() }
                .map { DateTimeUtils.estimatedDeliveryOn(it) }
                .subscribe { this.estimatedDelivery.onNext(it) }
                .addToDisposable(disposables)

            reward.map { RewardUtils.isNoReward(it) }
                .subscribe { this.isMinimumPledgeAmountGone.onNext(it) }
                .addToDisposable(disposables)
        }

        /**
         * Use cases for enabling/disabling access to launch the next fragment
         * - If the selected reward has no addOns the CTA button will be enable if available
         * - If selecting other reward CTA button available if reward available
         * - If the previously selected reward has addOns, baked AddOns or not CTA button available
         */
        private fun shouldContinueFlow(project: Project, rw: Reward): Boolean {
            val hasAddOns = rw.hasAddons()
            val backedRwId = project.backing()?.rewardId()
            val selectingOtherRw = backedRwId != rw.id()

            return when {
                !hasAddOns && isSelectable(project, rw) -> true
                selectingOtherRw && RewardUtils.isAvailable(project, rw) -> true
                hasAddOns && !selectingOtherRw && project.isLive -> true
                else -> false
            }
        }

        private fun buttonIsGone(
            project: Project,
            reward: Reward,
            userCreatedProject: Boolean
        ): Boolean {
            return when {
                userCreatedProject -> true
                project.backing()?.isBacked(reward) ?: false || project.isLive -> false
                else -> true
            }
        }

        private fun expirationDateIsGone(project: Project, reward: Reward): Boolean {
            return when {
                !project.isLive -> true
                RewardUtils.isTimeLimitedEnd(reward) -> RewardUtils.isExpired(reward)
                else -> true
            }
        }

        private fun isSelectable(project: Project, reward: Reward): Boolean {
            if (project.backing()?.isBacked(reward) == true) {
                return false
            }

            return RewardUtils.isAvailable(project, reward)
        }

        override fun configureWith(projectData: ProjectData, reward: Reward) {
            this.projectDataAndReward.onNext(Pair.create(projectData, reward))
        }

        override fun rewardClicked(position: Int) {
            this.rewardClicked.onNext(position)
        }

        override fun backersCount(): Observable<Int> = this.backersCount

        override fun backersCountIsGone(): Observable<Boolean> = this.backersCountIsGone

        override fun buttonCTA(): Observable<Int> = this.buttonCTA

        override fun buttonIsEnabled(): Observable<Boolean> = this.buttonIsEnabled

        override fun buttonIsGone(): Observable<Boolean> = this.buttonIsGone

        override fun conversion(): Observable<String> = this.conversion

        override fun conversionIsGone(): Observable<Boolean> = this.conversionIsGone

        override fun descriptionForNoReward(): Observable<Int> = this.descriptionForNoReward

        override fun descriptionForReward(): Observable<String> = this.descriptionForReward

        override fun descriptionIsGone(): Observable<Boolean> = this.descriptionIsGone

        override fun endDateSectionIsGone(): Observable<Boolean> = this.endDateSectionIsGone

        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        override fun estimatedDeliveryIsGone(): Observable<Boolean> = this.estimatedDeliveryIsGone

        override fun limitContainerIsGone(): Observable<Boolean> = this.limitContainerIsGone

        override fun minimumAmountTitle(): Observable<SpannableString> = this.minimumAmountTitle

        override fun remaining(): Observable<Int> = this.remaining

        override fun remainingIsGone(): Observable<Boolean> = this.remainingIsGone

        override fun reward(): Observable<Reward> = this.reward

        override fun rewardItems(): Observable<List<RewardsItem>> = this.rewardItems

        override fun rewardItemsAreGone(): Observable<Boolean> = this.rewardItemsAreGone

        override fun shippingSummary(): Observable<Pair<Int, String?>> = this.shippingSummary

        override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone

        override fun showFragment(): Observable<Pair<Project, Reward>> = this.showFragment

        override fun titleForNoReward(): Observable<Int> = this.titleForNoReward

        override fun titleForReward(): Observable<String> = this.titleForReward

        override fun titleIsGone(): Observable<Boolean> = this.titleIsGone

        override fun hasAddOnsAvailable(): Observable<Boolean> = this.addOnsAvailable

        override fun isMinimumPledgeAmountGone(): Observable<Boolean> =
            this.isMinimumPledgeAmountGone

        override fun selectedRewardTagIsGone(): Observable<Boolean> = this.selectedRewardTagIsGone

        override fun localPickUpIsGone(): Observable<Boolean> = this.localPickUpIsGone

        override fun localPickUpName(): Observable<String> = this.localPickUpName

        fun onCleared() {
            disposables.clear()
        }
    }
}
