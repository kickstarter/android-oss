package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
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
        fun conversionTextViewText(): Observable<String>

        /** Set the description TextView's text.  */
        fun descriptionTextViewText(): Observable<String>

        /** Returns `true` if reward can be clicked, `false` otherwise.  */
        fun isClickable(): Observable<Boolean>

        /** Returns `true` if the limit TextView should be hidden, `false` otherwise.  */
        fun limitAndRemainingTextViewIsGone(): Observable<Boolean>

        /** Set the limit and remaining TextView's text.  */
        fun limitAndRemainingTextViewText(): Observable<Pair<String, String>>

        /** Returns `true` if the limit header should be hidden, `false` otherwise.  */
        fun limitHeaderIsGone(): Observable<Boolean>

        /** Set the minimum TextView's text.  */
        fun minimumTextViewText(): Observable<String>

        /** Returns `true` if the reward description is empty and should be hidden in the UI.  */
        fun rewardDescriptionIsGone(): Observable<Boolean>

        /** Returns the reward. */
        fun reward(): Observable<Reward>

        /** Returns true if the reward end date should be hidden,`false` otherwise. */
        fun rewardEndDateSectionIsGone(): Observable<Boolean>

        /** Returns `true` if the items section should be hidden, `false` otherwise.  */
        fun rewardsItemsAreGone(): Observable<Boolean>

        /** Start the [com.kickstarter.ui.activities.BackingActivity] with the project.  */
        fun startBackingActivity(): Observable<Project>

        /** Start [com.kickstarter.ui.activities.CheckoutActivity] with the project's reward selected.  */
        fun startCheckoutActivity(): Observable<Pair<Project, Reward>>

        /** Returns `true` if the title TextView should be hidden, `false` otherwise.  */
        fun titleTextViewIsGone(): Observable<Boolean>

        /** Use the reward's title to set the title text.  */
        fun titleTextViewText(): Observable<String>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<KSViewHolder>(environment), Inputs, Outputs {
        private val ksCurrency: KSCurrency = environment.ksCurrency()

        private val projectAndReward = PublishSubject.create<Pair<Project, Reward>>()
        private val rewardClicked = PublishSubject.create<Void>()

        private val backersTextViewIsGone: Observable<Boolean>
        private val backersTextViewText: Observable<Int>
        private val conversionTextViewText: Observable<String>
        private val conversionTextViewIsGone: Observable<Boolean>
        private val descriptionTextViewText: Observable<String>
        private val isClickable: Observable<Boolean>
        private val limitAndBackersSeparatorIsGone: Observable<Boolean>
        private val limitAndRemainingTextViewIsGone: Observable<Boolean>
        private val limitAndRemainingTextViewText: Observable<Pair<String, String>>
        private val limitHeaderIsGone: Observable<Boolean>
        private val minimumTextViewText: Observable<String>
        private val reward: Observable<Reward>
        private val rewardDescriptionIsGone: Observable<Boolean>
        private val rewardEndDateSectionIsGone: Observable<Boolean>
        private val rewardsItemsAreGone: Observable<Boolean>
        private val titleTextViewIsGone: Observable<Boolean>
        private val titleTextViewText: Observable<String>
        private val startBackingActivity: Observable<Project>
        private val startCheckoutActivity: Observable<Pair<Project, Reward>>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val formattedMinimum = this.projectAndReward
                    .map { pr -> this.ksCurrency.formatWithProjectCurrency(pr.second.minimum(), pr.first, RoundingMode.UP) }

            val isSelectable = this.projectAndReward
                    .map { pr -> isSelectable(pr.first, pr.second) }

            val reward = this.projectAndReward
                    .map { pr -> pr.second }

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
                    .compose(Transformers.takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))

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

            this.limitAndRemainingTextViewText = reward
                    .map { r -> Pair.create<Int, Int>(r.limit(), r.remaining()) }
                    .filter { lr -> lr.first != null && lr.second != null }
                    .map { rr -> Pair.create(NumberUtils.format(rr.first), NumberUtils.format(rr.second)) }

            // Hide limit header if reward is not limited, or reward has been backed by user.
            this.limitHeaderIsGone = this.projectAndReward
                    .map { pr -> !RewardUtils.isLimited(pr.second) || BackingUtils.isBacked(pr.first, pr.second) }
                    .distinctUntilChanged()

            this.minimumTextViewText = formattedMinimum

            this.rewardsItemsAreGone = reward
                    .map<Boolean> { RewardUtils.isItemized(it) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.reward = reward

            this.rewardEndDateSectionIsGone = reward
                    .map { RewardUtils.hasExpirationDate(it) }
                    .map { BooleanUtils.negate(it) }
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

        @NonNull
        override fun conversionTextViewIsGone(): Observable<Boolean> {
            return this.conversionTextViewIsGone
        }

        @NonNull
        override fun conversionTextViewText(): Observable<String> {
            return this.conversionTextViewText
        }

        @NonNull
        override fun descriptionTextViewText(): Observable<String> {
            return this.descriptionTextViewText
        }

        override fun isClickable(): Observable<Boolean> = this.isClickable

        @NonNull
        override fun limitAndRemainingTextViewIsGone(): Observable<Boolean> {
            return this.limitAndRemainingTextViewIsGone
        }

        @NonNull
        override fun limitAndRemainingTextViewText(): Observable<Pair<String, String>> {
            return this.limitAndRemainingTextViewText
        }

        @NonNull
        override fun limitHeaderIsGone(): Observable<Boolean> {
            return this.limitHeaderIsGone
        }

        @NonNull
        override fun minimumTextViewText(): Observable<String> {
            return this.minimumTextViewText
        }

        @NonNull
        override fun rewardDescriptionIsGone(): Observable<Boolean> {
            return this.rewardDescriptionIsGone
        }

        override fun reward(): Observable<Reward> = this.reward

        override fun rewardEndDateSectionIsGone(): Observable<Boolean> = this.rewardEndDateSectionIsGone

        @NonNull
        override fun rewardsItemsAreGone(): Observable<Boolean> {
            return this.rewardsItemsAreGone
        }

        @NonNull
        override fun startBackingActivity(): Observable<Project> {
            return this.startBackingActivity
        }

        @NonNull
        override fun startCheckoutActivity(): Observable<Pair<Project, Reward>> {
            return this.startCheckoutActivity
        }

        @NonNull
        override fun titleTextViewIsGone(): Observable<Boolean> {
            return this.titleTextViewIsGone
        }

        @NonNull
        override fun titleTextViewText(): Observable<String> {
            return this.titleTextViewText
        }
    }
}