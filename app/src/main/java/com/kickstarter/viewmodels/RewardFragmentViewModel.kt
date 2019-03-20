package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.CurrentConfigType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.rx.transformers.Transformers.coalesce
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.ui.adapters.HorizontalRewardsAdapter
import com.kickstarter.ui.fragments.RewardsFragment
import rx.Observable
import rx.subjects.PublishSubject
import java.math.RoundingMode
import java.util.*

class RewardFragmentViewModel {
    interface Inputs {
        /** Call with a reward and project when data is bound to the view.  */
        fun projectAndReward(project: Project, reward: Reward)

        /** Call when the user clicks on a reward. */
        fun rewardClicked()
    }

    interface Outputs {

        /** Returns `true` if reward can be clicked, `false` otherwise.  */
        val isClickable: Observable<Boolean>

        /** Returns `true` if the all gone TextView should be gone, `false` otherwise.  */
        fun allGoneTextViewIsGone(): Observable<Boolean>

        /** Set the backers TextView's text.  */
        fun backersTextViewText(): Observable<Int>

        /** Returns `true` if the number of backers TextView should be hidden, `false` otherwise.  */
        fun backersTextViewIsGone(): Observable<Boolean>

        /** Returns `true` if the USD conversion section should be hidden, `false` otherwise.  */
        fun conversionTextViewIsGone(): Observable<Boolean>

        /** Set the USD conversion.  */
        fun conversionTextViewText(): Observable<String>

        fun deadlineCountdownTextViewText(): Observable<String>

        /** Set the description TextView's text.  */
        fun descriptionTextViewText(): Observable<String>

        /** Returns `true` if the separator between the limit and backers TextViews should be hidden, `false` otherwise.  */
        fun limitAndBackersSeparatorIsGone(): Observable<Boolean>

        /** Returns `true` if the limit TextView should be hidden, `false` otherwise.  */
        fun limitAndRemainingTextViewIsGone(): Observable<Boolean>

        /** Set the limit and remaining TextView's text.  */
        fun limitAndRemainingTextViewText(): Observable<Pair<String, String>>

        /** Returns `true` if the limit header should be hidden, `false` otherwise.  */
        fun limitHeaderIsGone(): Observable<Boolean>

        /** Set the minimum TextView's text.  */
        fun minimumTextViewText(): Observable<String>

        fun getProject(): Observable<Project>

        /** Returns `true` if the reward description is empty and should be hidden in the UI.  */
        fun rewardDescriptionIsGone(): Observable<Boolean>

        /** Show the rewards items.  */
        fun rewardsItemList(): Observable<List<RewardsItem>>

        /** Returns `true` if the items section should be hidden, `false` otherwise.  */
        fun rewardsItemsAreGone(): Observable<Boolean>

        /** Returns `true` if selected header should be hidden, `false` otherwise.  */
        fun selectedHeaderIsGone(): Observable<Boolean>

        /** Returns `true` if the shipping section should be hidden, `false` otherwise.  */
        fun shippingSummarySectionIsGone(): Observable<Boolean>

        /** Set the shipping summary TextView's text.  */
        fun shippingSummaryTextViewText(): Observable<String>

        /** Start the [com.kickstarter.ui.activities.BackingActivity] with the project.  */
        fun startBackingActivity(): Observable<Project>

        /** Start [com.kickstarter.ui.activities.CheckoutActivity] with the project's reward selected.  */
        fun startCheckoutActivity(): Observable<Pair<Project, Reward>>

        /** Returns `true` if the title TextView should be hidden, `false` otherwise.  */
        fun titleTextViewIsGone(): Observable<Boolean>

        /** Use the reward's title to set the title text.  */
        fun titleTextViewText(): Observable<String>

        /** Returns `true` if the white overlay indicating a reward is disabled should be invisible, `false` otherwise.  */
        fun whiteOverlayIsInvisible(): Observable<Boolean>
    }

    class ViewModel(@NonNull environment: Environment) : FragmentViewModel<RewardsFragment>(environment), HorizontalRewardsAdapter.Delegate ,Inputs, Outputs {
        private val currentConfig: CurrentConfigType = environment.currentConfig()
        private val ksCurrency: KSCurrency = environment.ksCurrency()

        private val projectAndReward = PublishSubject.create<Pair<Project, Reward>>()
        private val rewardClicked = PublishSubject.create<Void>()

        private val allGoneTextViewIsGone: Observable<Boolean>
        private val backersTextViewIsGone: Observable<Boolean>
        private val backersTextViewText: Observable<Int>
        private val conversionTextViewText: Observable<String>
        private val conversionTextViewIsGone: Observable<Boolean>
        private var deadlineCountdownTextViewText: Observable<String>
        private val descriptionTextViewText: Observable<String>
        @get:NonNull
        override val isClickable: Observable<Boolean>
        private val limitAndBackersSeparatorIsGone: Observable<Boolean>
        private val limitAndRemainingTextViewIsGone: Observable<Boolean>
        private val limitAndRemainingTextViewText: Observable<Pair<String, String>>
        private val limitHeaderIsGone: Observable<Boolean>
        private val minimumTextViewText: Observable<String>
        private val rewardDescriptionIsGone: Observable<Boolean>
        private val rewardsItemList: Observable<List<RewardsItem>>
        private val rewardsItemsAreGone: Observable<Boolean>
        private val titleTextViewIsGone: Observable<Boolean>
        private val titleTextViewText: Observable<String>
        private val selectedHeaderIsGone: Observable<Boolean>
        private val shippingSummarySectionIsGone: Observable<Boolean>
        private val shippingSummaryTextViewText: Observable<String>
        private val startBackingActivity: Observable<Project>
        private val startCheckoutActivity: Observable<Pair<Project, Reward>>
        private val whiteOverlayIsInvisible: Observable<Boolean>
        private val project: Observable<Project>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val formattedMinimum = this.projectAndReward
                    .map { pr -> this.ksCurrency.formatWithProjectCurrency(pr.second.minimum(), pr.first, RoundingMode.UP) }

            val isSelectable = this.projectAndReward
                    .map { pr -> isSelectable(pr.first, pr.second) }

            val project = this.projectAndReward
                    .map { pr -> pr.first }

            this.project = project

            this.deadlineCountdownTextViewText = project.map { ProjectUtils.deadlineCountdownValue(it) }.map { NumberUtils.format(it) }

            val reward = this.projectAndReward
                    .map { pr -> pr.second }

            val rewardIsSelected = this.projectAndReward
                    .map { pr -> BackingUtils.isBacked(pr.first, pr.second) }

            // Hide 'all gone' header if limit has not been reached, or reward has been backed by user.
            this.allGoneTextViewIsGone = this.projectAndReward
                    .map { pr -> !RewardUtils.isLimitReached(pr.second) || BackingUtils.isBacked(pr.first, pr.second) }
                    .distinctUntilChanged()

            this.backersTextViewIsGone = reward
                    .map { r -> RewardUtils.isNoReward(r) || !RewardUtils.hasBackers(r) }
                    .distinctUntilChanged()

            this.backersTextViewText = reward
                    .filter { r -> RewardUtils.isReward(r) || RewardUtils.hasBackers(r) }
                    .map<Int>(Reward::backersCount)
                    .filter { ObjectUtils.isNotNull(it) }

            this.conversionTextViewIsGone = this.projectAndReward
                    .map { p -> p.first.currency() != p.first.currentCurrency() }
                    .map { BooleanUtils.negate(it) }

            this.conversionTextViewText = this.projectAndReward
                    .map { pr -> this.ksCurrency.formatWithUserPreference(pr.second.minimum(), pr.first, RoundingMode.UP) }

            this.descriptionTextViewText = reward.map(Reward::description)

            this.isClickable = isSelectable.distinctUntilChanged()

            this.startCheckoutActivity = this.projectAndReward
                    .filter { pr -> isSelectable(pr.first, pr.second) && pr.first.isLive }
                    .compose(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))

            this.startBackingActivity = this.projectAndReward
                    .compose<Pair<Project, Reward>>(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))
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

            this.limitAndRemainingTextViewText = reward
                    .map { r -> Pair.create<Int, Int>(r.limit(), r.remaining()) }
                    .filter { lr -> lr.first != null && lr.second != null }
                    .map { rr -> Pair.create(NumberUtils.format(rr.first), NumberUtils.format(rr.second)) }

            // Hide limit header if reward is not limited, or reward has been backed by user.
            this.limitHeaderIsGone = this.projectAndReward
                    .map { pr -> !RewardUtils.isLimited(pr.second) || BackingUtils.isBacked(pr.first, pr.second) }
                    .distinctUntilChanged()

            this.minimumTextViewText = formattedMinimum

            this.rewardsItemList = reward
                    .map<List<RewardsItem>>{ it.rewardsItems() }
                    .compose(coalesce<List<RewardsItem>>(ArrayList()))

            this.rewardsItemsAreGone = reward
                    .map<Boolean>{ RewardUtils.isItemized(it) }
                    .map<Boolean>{ BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.selectedHeaderIsGone = rewardIsSelected
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.shippingSummaryTextViewText = reward
                    .filter { RewardUtils.isShippable(it) }
                    .map(Reward::shippingSummary)

            this.shippingSummarySectionIsGone = reward
                    .map<Boolean> { RewardUtils.isShippable(it) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.titleTextViewIsGone = reward
                    .map<String> { it.title() }
                    .map { ObjectUtils.isNull(it) }

            this.rewardDescriptionIsGone = reward
                    .map<String> { it.description() }
                    .map { it.isEmpty() }

            this.titleTextViewText = reward
                    .map<String> { it.title() }
                    .filter { ObjectUtils.isNotNull(it) }

            this.whiteOverlayIsInvisible = this.projectAndReward
                    .map { pr -> RewardUtils.isLimitReached(pr.second) && !BackingUtils.isBacked(pr.first, pr.second) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()
        }

        private fun isSelectable(@NonNull project: Project, @NonNull reward: Reward): Boolean {
            if (BackingUtils.isBacked(project, reward)) {
                return true
            }

            return if (!project.isLive) {
                false
            } else !RewardUtils.isLimitReached(reward)

        }

        override fun projectAndReward(@NonNull project: Project, @NonNull reward: Reward) {
            this.projectAndReward.onNext(Pair.create(project, reward))
        }

        override fun rewardClicked() {
            this.rewardClicked.onNext(null)
        }

        @NonNull override fun allGoneTextViewIsGone(): Observable<Boolean> {
            return this.allGoneTextViewIsGone
        }

        @NonNull override fun backersTextViewIsGone(): Observable<Boolean> {
            return this.backersTextViewIsGone
        }

        @NonNull override fun backersTextViewText(): Observable<Int> {
            return this.backersTextViewText
        }

        @NonNull override fun conversionTextViewIsGone(): Observable<Boolean> {
            return this.conversionTextViewIsGone
        }

        @NonNull override fun conversionTextViewText(): Observable<String> {
            return this.conversionTextViewText
        }

        override fun deadlineCountdownTextViewText(): Observable<String> = this.deadlineCountdownTextViewText

        @NonNull override fun descriptionTextViewText(): Observable<String> {
            return this.descriptionTextViewText
        }

        override fun getProject(): Observable<Project> = this.project

        @NonNull override fun limitAndBackersSeparatorIsGone(): Observable<Boolean> {
            return this.limitAndBackersSeparatorIsGone
        }

        @NonNull override fun limitAndRemainingTextViewIsGone(): Observable<Boolean> {
            return this.limitAndRemainingTextViewIsGone
        }

        @NonNull override fun limitAndRemainingTextViewText(): Observable<Pair<String, String>> {
            return this.limitAndRemainingTextViewText
        }

        @NonNull override fun limitHeaderIsGone(): Observable<Boolean> {
            return this.limitHeaderIsGone
        }

        @NonNull override fun minimumTextViewText(): Observable<String> {
            return this.minimumTextViewText
        }

        @NonNull override fun rewardDescriptionIsGone(): Observable<Boolean> {
            return this.rewardDescriptionIsGone
        }

        @NonNull override fun rewardsItemList(): Observable<List<RewardsItem>> {
            return this.rewardsItemList
        }

        @NonNull override fun rewardsItemsAreGone(): Observable<Boolean> {
            return this.rewardsItemsAreGone
        }

        @NonNull override fun selectedHeaderIsGone(): Observable<Boolean> {
            return this.selectedHeaderIsGone
        }

        @NonNull override fun shippingSummarySectionIsGone(): Observable<Boolean> {
            return this.shippingSummarySectionIsGone
        }

        @NonNull override fun shippingSummaryTextViewText(): Observable<String> {
            return this.shippingSummaryTextViewText
        }

        @NonNull override fun startBackingActivity(): Observable<Project> {
            return this.startBackingActivity
        }

        @NonNull override fun startCheckoutActivity(): Observable<Pair<Project, Reward>> {
            return this.startCheckoutActivity
        }

        @NonNull override fun titleTextViewIsGone(): Observable<Boolean> {
            return this.titleTextViewIsGone
        }

        @NonNull override fun titleTextViewText(): Observable<String> {
            return this.titleTextViewText
        }

        @NonNull override fun whiteOverlayIsInvisible(): Observable<Boolean> {
            return this.whiteOverlayIsInvisible
        }

    }
}