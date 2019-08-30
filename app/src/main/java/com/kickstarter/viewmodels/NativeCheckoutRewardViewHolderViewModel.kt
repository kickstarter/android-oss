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
import rx.Observable
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
        /**  Emits the string resource ID to set on the pledge button. */
        fun buttonCTA(): Observable<Int>

        /** Emits `true` if pledge button can be clicked, `false` otherwise.  */
        fun buttonIsEnabled(): Observable<Boolean>

        /** Emits a boolean determining if the pledge button should be shown. */
        fun buttonIsGone(): Observable<Boolean>

        /** Emits the color resource ID to tint the pledge button. */
        fun buttonTint(): Observable<Int>

        /** Emits the drawable resource ID to set as the check's background. */
        fun checkBackgroundDrawable(): Observable<Int>

        /** Emits `true` if the backed check should be hidden, `false` otherwise.  */
        fun checkIsInvisible(): Observable<Boolean>

        /** Emits the color resource ID to tint the check. */
        fun checkTintColor(): Observable<Int>

        /** Emits `true` if the conversion should be hidden, `false` otherwise.  */
        fun conversionIsGone(): Observable<Boolean>

        /** Emits the reward's minimum converted to the user's preference  */
        fun conversion(): Observable<String>

        /** Emits the reward's description.  */
        fun description(): Observable<String?>

        /** Emits `true` if the reward description is empty and should be hidden in the UI.  */
        fun descriptionIsGone(): Observable<Boolean>

        /** Emits `true` if the reward end date should be hidden,`false` otherwise. */
        fun endDateSectionIsGone(): Observable<Boolean>

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

        private val buttonCTA: Observable<Int>
        private val buttonIsEnabled: Observable<Boolean>
        private val buttonIsGone: Observable<Boolean>
        private val buttonTintColor: Observable<Int>
        private val checkBackgroundDrawable: Observable<Int>
        private val checkIsInvisible: Observable<Boolean>
        private val checkTintColor: Observable<Int>
        private val conversion: Observable<String>
        private val conversionIsGone: Observable<Boolean>
        private val description: Observable<String?>
        private val descriptionIsGone: Observable<Boolean>
        private val endDateSectionIsGone: Observable<Boolean>
        private val limitContainerIsGone: Observable<Boolean>
        private val minimumAmountTitle: Observable<SpannableString>
        private val remaining: Observable<String>
        private val remainingIsGone: Observable<Boolean>
        private val reward: Observable<Reward>
        private val rewardItems: Observable<List<RewardsItem>>
        private val rewardItemsAreGone: Observable<Boolean>
        private val shippingSummary: Observable<String>
        private val shippingSummaryIsGone: Observable<Boolean>
        private val showPledgeFragment: Observable<Pair<Project, Reward>>
        private val startBackingActivity: Observable<Project>
        private val titleForNoReward: Observable<Int>
        private val titleForReward: Observable<String?>
        private val titleIsGone: Observable<Boolean>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            this.minimumAmountTitle = this.projectAndReward
                    .map { RewardViewUtils.styleCurrency(it.second.minimum(), it.first, this.ksCurrency) }

            val reward = this.projectAndReward
                    .map { it.second }

            this.buttonIsGone = this.projectAndReward
                    .map { BackingUtils.isBacked(it.first, it.second) || it.first.isLive }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.buttonTintColor = this.projectAndReward
                    .map { RewardViewUtils.pledgeButtonColor(it.first, it.second) }
                    .distinctUntilChanged()

            this.checkTintColor = this.projectAndReward
                    .filter { BackingUtils.isBacked(it.first, it.second) }
                    .map { RewardViewUtils.pledgeButtonColor(it.first, it.second) }
                    .distinctUntilChanged()

            this.checkBackgroundDrawable = this.projectAndReward
                    .filter { BackingUtils.isBacked(it.first, it.second) }
                    .map { RewardViewUtils.checkBackgroundDrawable(it.first) }
                    .distinctUntilChanged()

            this.checkIsInvisible = this.projectAndReward
                    .map { !BackingUtils.isBacked(it.first, it.second) }
                    .distinctUntilChanged()

            this.buttonCTA = this.projectAndReward
                    .map { RewardViewUtils.pledgeButtonText(it.first, it.second) }
                    .distinctUntilChanged()

            this.conversionIsGone = this.projectAndReward
                    .map { it.first.currency() != it.first.currentCurrency() }
                    .map { BooleanUtils.negate(it) }

            this.conversion = this.projectAndReward
                    .map { this.ksCurrency.format(it.second.convertedMinimum(), it.first, RoundingMode.HALF_UP) }

            this.description = reward
                    .map { if (RewardUtils.isReward(it)) it.description() else null }
                    .distinctUntilChanged()

            this.descriptionIsGone = this.projectAndReward
                    .map { if (RewardUtils.isReward(it.second)) it.second.description().isNullOrEmpty() else BackingUtils.isBacked(it.first, it.second) }
                    .distinctUntilChanged()

            this.buttonIsEnabled = this.projectAndReward
                    .map { isSelectable(it.first, it.second) }
                    .distinctUntilChanged()

            this.startBackingActivity = this.projectAndReward
                    .compose<Pair<Project, Reward>>(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))
                    .filter { ProjectUtils.isCompleted(it.first) && BackingUtils.isBacked(it.first, it.second) }
                    .map { it.first }

            this.remainingIsGone = this.projectAndReward
                    .map<Boolean> { it.first.isLive && RewardUtils.isLimited(it.second) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.remaining = reward
                    .filter { RewardUtils.isLimited(it) }
                    .map { it.remaining() }
                    .map { remaining -> remaining?.let { NumberUtils.format(it) } }

            this.rewardItems = reward
                    .filter { RewardUtils.isItemized(it) }
                    .map { it.rewardsItems() }

            this.rewardItemsAreGone = reward
                    .map<Boolean> { RewardUtils.isItemized(it) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.reward = reward

            this.endDateSectionIsGone = this.projectAndReward
                    .map { expirationDateIsGone(it.first, it.second) }
                    .distinctUntilChanged()

            this.showPledgeFragment = this.projectAndReward
                    .filter { isSelectable(it.first, it.second) && it.first.isLive }
                    .compose<Pair<Project, Reward>>(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))

            this.titleForNoReward = this.projectAndReward
                    .filter { RewardUtils.isNoReward(it.second) }
                    .map { if(BackingUtils.isBacked(it.first, it.second)) R.string.Thank_you_for_supporting_this_project else R.string.Pledge_without_a_reward }

            this.titleForReward = reward
                    .filter { RewardUtils.isReward(it) }
                    .map { it.title() }

            this.titleIsGone = reward
                    .map {  RewardUtils.isReward(it) && it.title().isNullOrEmpty() }
                    .distinctUntilChanged()

            this.shippingSummary = reward
                    .filter { RewardUtils.isShippable(it) }
                    .map { it.shippingSummary() }

            this.shippingSummaryIsGone = this.projectAndReward
                    .map { it.first.isLive && RewardUtils.isShippable(it.second) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.limitContainerIsGone = Observable.combineLatest(this.endDateSectionIsGone, this.remainingIsGone, this.shippingSummaryIsGone)
            { endDateGone, remainingGone, shippingGone  -> endDateGone && remainingGone && shippingGone }
                    .distinctUntilChanged()

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
                return true
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
        override fun buttonCTA(): Observable<Int> = this.buttonCTA

        @NonNull
        override fun buttonIsGone(): Observable<Boolean> = this.buttonIsGone

        @NonNull
        override fun buttonTint(): Observable<Int> = this.buttonTintColor

        @NonNull
        override fun checkBackgroundDrawable(): Observable<Int> = this.checkBackgroundDrawable

        @NonNull
        override fun checkIsInvisible(): Observable<Boolean> = this.checkIsInvisible

        @NonNull
        override fun checkTintColor(): Observable<Int> = this.checkTintColor

        @NonNull
        override fun conversionIsGone(): Observable<Boolean> = this.conversionIsGone

        @NonNull
        override fun conversion(): Observable<String> = this.conversion

        @NonNull
        override fun description(): Observable<String?> = this.description

        @NonNull
        override fun buttonIsEnabled(): Observable<Boolean> = this.buttonIsEnabled

        @NonNull
        override fun remaining(): Observable<String> = this.remaining

        @NonNull
        override fun remainingIsGone(): Observable<Boolean> = this.remainingIsGone

        @NonNull
        override fun limitContainerIsGone(): Observable<Boolean> = this.limitContainerIsGone

        @NonNull
        override fun minimumAmountTitle(): Observable<SpannableString> = this.minimumAmountTitle

        @NonNull
        override fun descriptionIsGone(): Observable<Boolean> = this.descriptionIsGone

        @NonNull
        override fun reward(): Observable<Reward> = this.reward

        @NonNull
        override fun endDateSectionIsGone(): Observable<Boolean> = this.endDateSectionIsGone

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
