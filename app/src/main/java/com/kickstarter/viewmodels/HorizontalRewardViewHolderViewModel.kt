package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.ui.viewholders.HorizontalRewardViewHolder
import com.kickstarter.ui.viewholders.KSViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.math.RoundingMode

interface HorizontalRewardViewHolderViewModel {
    interface Inputs {
        /** Call with a reward and project when data is bound to the view.  */
        fun projectAndReward(project: Project, reward: Reward)

        /** Call when the user clicks on a reward. */
        fun rewardClicked()
    }

    interface Outputs {
        /** Returns `true` if the USD conversion section should be hidden, `false` otherwise.  */
        fun conversionTextViewIsGone(): Observable<Boolean>

        /** Set the USD conversion.  */
        fun conversionText(): Observable<String>

        /** Set the description TextView's text.  */
        fun descriptionText(): Observable<String>

        /** Returns `true` if reward can be clicked, `false` otherwise.  */
        fun isClickable(): Observable<Boolean>

        /** Returns `true` if the limit TextView should be hidden, `false` otherwise.  */
        fun limitAndRemainingTextViewIsGone(): Observable<Boolean>

        /** Set the limit and remaining TextView's text.  */
        fun limitAndRemainingText(): Observable<Pair<String, String>>

        /** Returns `true` if the limit header should be hidden, `false` otherwise.  */
        fun limitHeaderIsGone(): Observable<Boolean>

        /** Set the minimum TextView's text.  */
        fun minimumText(): Observable<String>

        /** Returns `true` if the reward description is empty and should be hidden in the UI.  */
        fun rewardDescriptionIsGone(): Observable<Boolean>

        /** Returns the reward. */
        fun reward(): Observable<Reward>

        /** Returns true if the reward end date should be hidden,`false` otherwise. */
        fun rewardEndDateSectionIsGone(): Observable<Boolean>

        /** Show the rewards items.  */
        fun rewardItems(): Observable<List<RewardsItem>>

        /** Returns `true` if the items section should be hidden, `false` otherwise.  */
        fun rewardItemsAreGone(): Observable<Boolean>

        /** Show [com.kickstarter.ui.fragments.PledgeFragment] with the project's reward selected.  */
        fun showPledgeFragment(): Observable<Pair<Project, Reward>>

        /** Start the [com.kickstarter.ui.activities.BackingActivity] with the project.  */
        fun startBackingActivity(): Observable<Project>

        /** Returns `true` if the title TextView should be hidden, `false` otherwise.  */
        fun titleTextViewIsGone(): Observable<Boolean>

        /** Use the reward's title to set the title text.  */
        fun titleText(): Observable<String>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<HorizontalRewardViewHolder>(environment), Inputs, Outputs {
        private val ksCurrency: KSCurrency = environment.ksCurrency()

        private val projectAndReward = PublishSubject.create<Pair<Project, Reward>>()
        private val rewardClicked = PublishSubject.create<Void>()

        private val backersTextViewIsGone: Observable<Boolean>
        private val backersText: Observable<Int>
        private val conversionText: Observable<String>
        private val conversionTextViewIsGone: Observable<Boolean>
        private val descriptionText: Observable<String>
        private val isClickable: Observable<Boolean>
        private val limitAndBackersSeparatorIsGone: Observable<Boolean>
        private val limitAndRemainingTextViewIsGone: Observable<Boolean>
        private val limitAndRemainingText: Observable<Pair<String, String>>
        private val limitHeaderIsGone: Observable<Boolean>
        private val minimumText: Observable<String>
        private val reward: Observable<Reward>
        private val rewardDescriptionIsGone: Observable<Boolean>
        private val rewardEndDateSectionIsGone: Observable<Boolean>
        private val rewardItems: Observable<List<RewardsItem>>
        private val rewardItemsAreGone: Observable<Boolean>
        private val titleTextViewIsGone: Observable<Boolean>
        private val titleText: Observable<String>
        private val showPledgeFragment: Observable<Pair<Project, Reward>>
        private val startBackingActivity: Observable<Project>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val formattedMinimum = this.projectAndReward
                    .filter { RewardUtils.isReward(it.second) }
                    .map { pr -> this.ksCurrency.formatWithProjectCurrency(pr.second.minimum(), pr.first, RoundingMode.UP) }

            val isSelectable = this.projectAndReward
                    .map { pr -> isSelectable(pr.first, pr.second) }

            val reward = this.projectAndReward
                    .map { pr -> pr.second }

            this.backersTextViewIsGone = reward
                    .map { r -> RewardUtils.isNoReward(r) || !RewardUtils.hasBackers(r) }
                    .distinctUntilChanged()

            this.backersText = reward
                    .filter { r -> RewardUtils.isReward(r) || RewardUtils.hasBackers(r) }
                    .map<Int>(Reward::backersCount)
                    .filter { ObjectUtils.isNotNull(it) }

            this.conversionTextViewIsGone = this.projectAndReward
                    .map { p -> p.first.currency() != p.first.currentCurrency() || RewardUtils.isNoReward(p.second) }
                    .map { BooleanUtils.negate(it) }

            this.conversionText = this.projectAndReward
                    .filter { RewardUtils.isReward(it.second) }
                    .map { pr -> this.ksCurrency.formatWithUserPreference(pr.second.minimum(), pr.first, RoundingMode.UP) }

            this.descriptionText = reward
                    .filter { RewardUtils.isReward(it) }
                    .map { it.description() }

            this.isClickable = isSelectable.distinctUntilChanged()

            this.startBackingActivity = this.projectAndReward
                    .compose<Pair<Project, Reward>>(Transformers.takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))
                    .filter { pr -> ProjectUtils.isCompleted(pr.first) && BackingUtils.isBacked(pr.first, pr.second) }
                    .map { pr -> pr.first }

            this.limitAndBackersSeparatorIsGone = reward
                    .map { r -> IntegerUtils.isNonZero(r.limit()) && IntegerUtils.isNonZero(r.backersCount()) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.limitAndRemainingTextViewIsGone = reward
                    .map<Boolean> { RewardUtils.isLimited(it) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.limitAndRemainingText = reward
                    .map { r -> Pair.create<Int, Int>(r.limit(), r.remaining()) }
                    .filter { lr -> lr.first != null && lr.second != null }
                    .map { rr -> Pair.create(NumberUtils.format(rr.first), NumberUtils.format(rr.second)) }

            // Hide limit header if reward is not limited, or reward has been backed by user.
            this.limitHeaderIsGone = this.projectAndReward
                    .map { pr -> !RewardUtils.isLimited(pr.second) || BackingUtils.isBacked(pr.first, pr.second) }
                    .distinctUntilChanged()

            this.minimumText = formattedMinimum

            this.rewardItems = reward
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { it.rewardsItems() }

            this.rewardItemsAreGone = reward
                    .map<Boolean> { RewardUtils.isItemized(it) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.reward = reward

            this.rewardEndDateSectionIsGone = reward
                    .map { RewardUtils.hasExpirationDate(it) }
                    .map { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.rewardDescriptionIsGone = reward
                    .map<String> { it.description() }
                    .map { it.isEmpty() }

            this.showPledgeFragment = this.projectAndReward
                    .filter { isSelectable(it.first, it.second) && it.first.isLive}
                    .compose<Pair<Project, Reward>>(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))

            this.titleTextViewIsGone = reward
                    .map { it.title() }
                    .map { ObjectUtils.isNull(it) }

            this.titleText = reward
                    .filter { RewardUtils.isReward(it) }
                    .map<String> { it.title() }
                    .filter { ObjectUtils.isNotNull(it) }
        }

        private fun isSelectable(@NonNull project: Project, @NonNull reward: Reward): Boolean {
            if (BackingUtils.isBacked(project, reward)) {
                return true
            }

            return if (!project.isLive) {
                false
            } else {
                !RewardUtils.isLimitReached(reward)
            }

        }

        override fun projectAndReward(@NonNull project: Project, @NonNull reward: Reward) {
            this.projectAndReward.onNext(Pair.create(project, reward))
        }

        override fun rewardClicked() {
            this.rewardClicked.onNext(null)
        }

        @NonNull
        override fun conversionTextViewIsGone(): Observable<Boolean> {
            return this.conversionTextViewIsGone
        }

        @NonNull
        override fun conversionText(): Observable<String> {
            return this.conversionText
        }

        @NonNull
        override fun descriptionText(): Observable<String> {
            return this.descriptionText
        }

        override fun isClickable(): Observable<Boolean> = this.isClickable

        @NonNull
        override fun limitAndRemainingTextViewIsGone(): Observable<Boolean> {
            return this.limitAndRemainingTextViewIsGone
        }

        @NonNull
        override fun limitAndRemainingText(): Observable<Pair<String, String>> {
            return this.limitAndRemainingText
        }

        @NonNull
        override fun limitHeaderIsGone(): Observable<Boolean> {
            return this.limitHeaderIsGone
        }

        @NonNull
        override fun minimumText(): Observable<String> {
            return this.minimumText
        }

        @NonNull
        override fun rewardDescriptionIsGone(): Observable<Boolean> {
            return this.rewardDescriptionIsGone
        }

        @NonNull
        override fun reward(): Observable<Reward> = this.reward

        @NonNull
        override fun rewardEndDateSectionIsGone(): Observable<Boolean> = this.rewardEndDateSectionIsGone

        @NonNull
        override fun rewardItems(): Observable<List<RewardsItem>> = this.rewardItems

        @NonNull
        override fun rewardItemsAreGone(): Observable<Boolean> = this.rewardItemsAreGone

        @NonNull
        override fun showPledgeFragment(): Observable<Pair<Project, Reward>> = this.showPledgeFragment

        @NonNull
        override fun startBackingActivity(): Observable<Project> = this.startBackingActivity

        @NonNull
        override fun titleTextViewIsGone(): Observable<Boolean> = this.titleTextViewIsGone

        @NonNull
        override fun titleText(): Observable<String> = this.titleText

    }
}
