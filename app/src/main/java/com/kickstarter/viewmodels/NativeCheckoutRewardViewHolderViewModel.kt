package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.ui.viewholders.NativeCheckoutRewardViewHolder
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode

interface NativeCheckoutRewardViewHolderViewModel {
    interface Inputs {
        /** Call with a reward and project when data is bound to the view.  */
        fun projectAndReward(project: Project, reward: Reward)

        /** Call when the user clicks on a reward. */
        fun rewardClicked()
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
        fun descriptionForReward(): Observable<String?>

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
        fun remaining(): Observable<String>

        /** Emits `true` if the remaining count should be hidden, `false` otherwise.  */
        fun remainingIsGone(): Observable<Boolean>

        /** Emits the reward to use to display the reward's expiration. */
        fun reward(): Observable<Reward>

        /** Emits the reward's items.  */
        fun rewardItems(): Observable<List<RewardsItem>>

        /** Emits `true` if the items section should be hidden, `false` otherwise.  */
        fun rewardItemsAreGone(): Observable<Boolean>

        /** Set the shipping summary TextView's text.  */
        fun shippingSummary(): Observable<String>

        /** Returns `true` if the shipping summary should be hidden, `false` otherwise.  */
        fun shippingSummaryIsGone(): Observable<Boolean>

        /** Show [com.kickstarter.ui.fragments.PledgeFragment] with the project's reward selected.  */
        fun showPledgeFragment(): Observable<Pair<Project, Reward>>

        /** Start the [com.kickstarter.ui.activities.BackingActivity] with the project.  */
        fun startBackingActivity(): Observable<Project>

        /** Emits `true` if the title should be hidden, `false` otherwise.  */
        fun titleIsGone(): Observable<Boolean>

        /** Emits the reward's title when `isNoReward` is true.  */
        fun titleForNoReward(): Observable<Int>

        /** Emits the reward's title when `isReward` is true.  */
        fun titleForReward(): Observable<String?>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<NativeCheckoutRewardViewHolder>(environment), Inputs, Outputs {
        private val ksCurrency: KSCurrency = environment.ksCurrency()

        private val projectAndReward = PublishSubject.create<Pair<Project, Reward>>()
        private val rewardClicked = PublishSubject.create<Void>()

        private val backersCount = BehaviorSubject.create<Int>()
        private val backersCountIsGone = BehaviorSubject.create<Boolean>()
        private val buttonCTA = BehaviorSubject.create<Int>()
        private val buttonIsEnabled = BehaviorSubject.create<Boolean>()
        private val buttonIsGone = BehaviorSubject.create<Boolean>()
        private val conversion = BehaviorSubject.create<String>()
        private val conversionIsGone = BehaviorSubject.create<Boolean>()
        private val descriptionForNoReward = BehaviorSubject.create<Int>()
        private val descriptionForReward = BehaviorSubject.create<String?>()
        private val descriptionIsGone = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val estimatedDeliveryIsGone = BehaviorSubject.create<Boolean>()
        private val endDateSectionIsGone = BehaviorSubject.create<Boolean>()
        private val limitContainerIsGone = BehaviorSubject.create<Boolean>()
        private val minimumAmountTitle = PublishSubject.create<SpannableString>()
        private val remaining = BehaviorSubject.create<String>()
        private val remainingIsGone = BehaviorSubject.create<Boolean>()
        private val reward = BehaviorSubject.create<Reward>()
        private val rewardItems = BehaviorSubject.create<List<RewardsItem>>()
        private val rewardItemsAreGone = BehaviorSubject.create<Boolean>()
        private val shippingSummary = BehaviorSubject.create<String>()
        private val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val showPledgeFragment = PublishSubject.create<Pair<Project, Reward>>()
        private val startBackingActivity = PublishSubject.create<Project>()
        private val titleForNoReward = BehaviorSubject.create<Int>()
        private val titleForReward = BehaviorSubject.create<String?>()
        private val titleIsGone = BehaviorSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.projectAndReward
                    .map { RewardViewUtils.styleCurrency(it.second.minimum(), it.first, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.minimumAmountTitle)

            val reward = this.projectAndReward
                    .map { it.second }

            this.projectAndReward
                    .map { BackingUtils.isBacked(it.first, it.second) || it.first.isLive }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.buttonIsGone)

            this.projectAndReward
                    .map { RewardViewUtils.pledgeButtonText(it.first, it.second) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.buttonCTA)

            this.projectAndReward
                    .map { it.first.currency() != it.first.currentCurrency() }
                    .map { BooleanUtils.negate(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversionIsGone)

            this.projectAndReward
                    .map { this.ksCurrency.format(it.second.convertedMinimum(), it.first, true, RoundingMode.HALF_UP, true) }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversion)

            this.projectAndReward
                    .filter { RewardUtils.isNoReward(it.second) }
                    .map {
                        val backed = BackingUtils.isBacked(it.first, it.second)
                        when {
                            backed -> R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality
                            else -> R.string.Back_it_because_you_believe_in_it
                        }
                    }
                    .compose(bindToLifecycle())
                    .subscribe(this.descriptionForNoReward)

            reward
                    .filter { RewardUtils.isReward(it) }
                    .map { it.description() }
                    .compose(bindToLifecycle())
                    .subscribe(this.descriptionForReward)

            this.projectAndReward
                    .map { RewardUtils.isReward(it.second) && it.second.description().isNullOrEmpty() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.descriptionIsGone)

            this.projectAndReward
                    .map { isSelectable(it.first, it.second) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.buttonIsEnabled)

            this.projectAndReward
                    .compose<Pair<Project, Reward>>(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))
                    .filter { ProjectUtils.isCompleted(it.first) && BackingUtils.isBacked(it.first, it.second) }
                    .map { it.first }
                    .compose(bindToLifecycle())
                    .subscribe(this.startBackingActivity)

            this.projectAndReward
                    .map { it.first.isLive && RewardUtils.isLimited(it.second) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.remainingIsGone)

            reward
                    .filter { RewardUtils.isLimited(it) }
                    .map { it.remaining() }
                    .map { remaining -> remaining?.let { NumberUtils.format(it) } }
                    .compose(bindToLifecycle())
                    .subscribe(this.remaining)

            reward
                    .filter { RewardUtils.isItemized(it) }
                    .map { it.rewardsItems() }
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardItems)

            reward
                    .map { RewardUtils.isItemized(it) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardItemsAreGone)

            reward
                    .compose(bindToLifecycle())
                    .subscribe(this.reward)

            this.projectAndReward
                    .map { expirationDateIsGone(it.first, it.second) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.endDateSectionIsGone)

            this.projectAndReward
                    .filter { isSelectable(it.first, it.second) && it.first.isLive }
                    .compose<Pair<Project, Reward>>(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeFragment)

            this.projectAndReward
                    .filter { RewardUtils.isNoReward(it.second) }
                    .map {
                        val backed = BackingUtils.isBacked(it.first, it.second)
                        when {
                            backed -> R.string.You_pledged_without_a_reward
                            else -> R.string.Pledge_without_a_reward
                        }
                    }
                    .compose(bindToLifecycle())
                    .subscribe(this.titleForNoReward)

            reward
                    .filter { RewardUtils.isReward(it) }
                    .map { it.title() }
                    .compose(bindToLifecycle())
                    .subscribe(this.titleForReward)

            reward
                    .map { RewardUtils.isReward(it) && it.title().isNullOrEmpty() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.titleIsGone)

            reward
                    .filter { RewardUtils.isShippable(it) }
                    .map { it.shippingSummary() }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingSummary)

            this.projectAndReward
                    .map { it.first.isLive && RewardUtils.isShippable(it.second) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingSummaryIsGone)

            Observable.combineLatest(this.endDateSectionIsGone, this.remainingIsGone, this.shippingSummaryIsGone)
            { endDateGone, remainingGone, shippingGone -> endDateGone && remainingGone && shippingGone }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.limitContainerIsGone)

            reward
                    .map { RewardUtils.isNoReward(it) || !RewardUtils.hasBackers(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.backersCountIsGone)

            reward
                    .filter { RewardUtils.isReward(it) && RewardUtils.hasBackers(it) }
                    .map { it.backersCount() as Int }
                    .compose(bindToLifecycle())
                    .subscribe(this.backersCount)

            reward
                    .map { RewardUtils.isNoReward(it) || ObjectUtils.isNull(it.estimatedDeliveryOn()) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.estimatedDeliveryIsGone)

            reward
                    .filter { RewardUtils.isReward(it) && ObjectUtils.isNotNull(it.estimatedDeliveryOn()) }
                    .map<DateTime> { it.estimatedDeliveryOn() }
                    .map { DateTimeUtils.estimatedDeliveryOn(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.estimatedDelivery)

        }

        private fun expirationDateIsGone(project: Project, reward: Reward): Boolean {
            return when {
                !project.isLive -> true
                RewardUtils.isTimeLimited(reward) -> RewardUtils.isExpired(reward)
                else -> true
            }
        }

        private fun isSelectable(@NonNull project: Project, @NonNull reward: Reward): Boolean {
            if (BackingUtils.isBacked(project, reward)) {
                return false
            }

            return RewardUtils.isAvailable(project, reward)
        }

        override fun projectAndReward(@NonNull project: Project, @NonNull reward: Reward) {
            this.projectAndReward.onNext(Pair.create(project, reward))
        }

        override fun rewardClicked() {
            this.rewardClicked.onNext(null)
        }

        @NonNull
        override fun backersCount(): Observable<Int> = this.backersCount

        @NonNull
        override fun backersCountIsGone(): Observable<Boolean> = this.backersCountIsGone

        @NonNull
        override fun buttonCTA(): Observable<Int> = this.buttonCTA

        @NonNull
        override fun buttonIsEnabled(): Observable<Boolean> = this.buttonIsEnabled

        @NonNull
        override fun buttonIsGone(): Observable<Boolean> = this.buttonIsGone

        @NonNull
        override fun conversion(): Observable<String> = this.conversion

        @NonNull
        override fun conversionIsGone(): Observable<Boolean> = this.conversionIsGone

        @NonNull
        override fun descriptionForNoReward(): Observable<Int> = this.descriptionForNoReward

        @NonNull
        override fun descriptionForReward(): Observable<String?> = this.descriptionForReward

        @NonNull
        override fun descriptionIsGone(): Observable<Boolean> = this.descriptionIsGone

        @NonNull
        override fun endDateSectionIsGone(): Observable<Boolean> = this.endDateSectionIsGone

        @NonNull
        override fun estimatedDelivery(): Observable<String> = this.estimatedDelivery

        @NonNull
        override fun estimatedDeliveryIsGone(): Observable<Boolean> = this.estimatedDeliveryIsGone

        @NonNull
        override fun limitContainerIsGone(): Observable<Boolean> = this.limitContainerIsGone

        @NonNull
        override fun minimumAmountTitle(): Observable<SpannableString> = this.minimumAmountTitle

        @NonNull
        override fun remaining(): Observable<String> = this.remaining

        @NonNull
        override fun remainingIsGone(): Observable<Boolean> = this.remainingIsGone

        @NonNull
        override fun reward(): Observable<Reward> = this.reward

        @NonNull
        override fun rewardItems(): Observable<List<RewardsItem>> = this.rewardItems

        @NonNull
        override fun rewardItemsAreGone(): Observable<Boolean> = this.rewardItemsAreGone

        @NonNull
        override fun shippingSummary(): Observable<String> = this.shippingSummary

        @NonNull
        override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone

        @NonNull
        override fun showPledgeFragment(): Observable<Pair<Project, Reward>> = this.showPledgeFragment

        @NonNull
        override fun startBackingActivity(): Observable<Project> = this.startBackingActivity

        @NonNull
        override fun titleForNoReward(): Observable<Int> = this.titleForNoReward

        @NonNull
        override fun titleForReward(): Observable<String?> = this.titleForReward

        @NonNull
        override fun titleIsGone(): Observable<Boolean> = this.titleIsGone
    }
}
