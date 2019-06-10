package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.rx.transformers.Transformers.ignoreValues
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.ui.viewholders.HorizontalRewardViewHolder
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

        /** Set the preferred conversion text.  */
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

        /** Emits when the pledge button should display the reward unavailable copy.  */
        fun limitReachedButtonTextIsVisible(): Observable<Void>

        /** Returns the minimum pledge amount in the project's currency.  */
        fun minimumAmount(): Observable<String>

        /** Returns the minimum pledge amount in the project's currency.  */
        fun minimumAmountTitle(): Observable<SpannableString>

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
        private val isLimitReachedButtonTextVisible: Observable<Void>
        private val limitAndBackersSeparatorIsGone: Observable<Boolean>
        private val limitAndRemainingTextViewIsGone: Observable<Boolean>
        private val limitAndRemainingText: Observable<Pair<String, String>>
        private val limitHeaderIsGone: Observable<Boolean>
        private val minimumAmount: Observable<String>
        private val minimumAmountStyled: Observable<SpannableString>
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

            this.minimumAmountStyled = this.projectAndReward
                    .map { ViewUtils.styleCurrencyBottom(it.second.minimum(), it.first, this.ksCurrency) }

            val reward = this.projectAndReward
                    .map { it.second }

            this.isLimitReachedButtonTextVisible = reward
                    .filter { RewardUtils.isLimitReached(it) }
                    .compose(ignoreValues())

            this.minimumAmount = this.projectAndReward
                    .filter { !RewardUtils.isLimitReached(it.second) }
                    .map { this.ksCurrency.format(it.second.minimum(), it.first) }

            this.backersTextViewIsGone = reward
                    .map { !RewardUtils.hasBackers(it) }
                    .distinctUntilChanged()

            this.backersText = reward
                    .filter { RewardUtils.hasBackers(it) }
                    .map<Int>(Reward::backersCount)
                    .filter { ObjectUtils.isNotNull(it) }

            this.conversionTextViewIsGone = this.projectAndReward
                    .map { it.first.currency() != it.first.currentCurrency() }
                    .map { BooleanUtils.negate(it) }

            this.conversionText = this.projectAndReward
                    .map { this.ksCurrency.formatWithUserPreference(it.second.minimum(), it.first, RoundingMode.HALF_UP, 0) }

            this.descriptionText = reward
                    .map { it.description() }

            this.isClickable = this.projectAndReward
                    .map { isSelectable(it.first, it.second) }
                    .distinctUntilChanged()

            this.startBackingActivity = this.projectAndReward
                    .compose<Pair<Project, Reward>>(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))
                    .filter { ProjectUtils.isCompleted(it.first) && BackingUtils.isBacked(it.first, it.second) }
                    .map { it.first }

            this.limitAndBackersSeparatorIsGone = reward
                    .map { IntegerUtils.isNonZero(it.limit()) && IntegerUtils.isNonZero(it.backersCount()) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.limitAndRemainingTextViewIsGone = reward
                    .map<Boolean> { RewardUtils.isLimited(it) }
                    .map<Boolean> { BooleanUtils.negate(it) }
                    .distinctUntilChanged()

            this.limitAndRemainingText = reward
                    .map { Pair.create<Int, Int>(it.limit(), it.remaining()) }
                    .filter { it.first != null && it.second != null }
                    .map { Pair.create(NumberUtils.format(it.first), NumberUtils.format(it.second)) }

            // Hide limit header if reward is not limited, or reward has been backed by user.
            this.limitHeaderIsGone = this.projectAndReward
                    .map { !RewardUtils.isLimited(it.second) || BackingUtils.isBacked(it.first, it.second) }
                    .distinctUntilChanged()

            this.rewardItems = reward
                    .filter { RewardUtils.isItemized(it) }
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
                    .filter { isSelectable(it.first, it.second) && it.first.isLive }
                    .compose<Pair<Project, Reward>>(takeWhen<Pair<Project, Reward>, Void>(this.rewardClicked))

            this.titleTextViewIsGone = reward
                    .map { it.title() }
                    .map { ObjectUtils.isNull(it) }

            this.titleText = reward
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
        override fun conversionTextViewIsGone(): Observable<Boolean> = this.conversionTextViewIsGone

        @NonNull
        override fun conversionText(): Observable<String> = this.conversionText

        @NonNull
        override fun descriptionText(): Observable<String> = this.descriptionText

        @NonNull
        override fun isClickable(): Observable<Boolean> = this.isClickable

        @NonNull
        override fun limitReachedButtonTextIsVisible(): Observable<Void> = this.isLimitReachedButtonTextVisible

        @NonNull
        override fun limitAndRemainingTextViewIsGone(): Observable<Boolean> = this.limitAndRemainingTextViewIsGone

        @NonNull
        override fun limitAndRemainingText(): Observable<Pair<String, String>> = this.limitAndRemainingText

        @NonNull
        override fun limitHeaderIsGone(): Observable<Boolean> = this.limitHeaderIsGone

        @NonNull
        override fun minimumAmount(): Observable<String> = this.minimumAmount

        @NonNull
        override fun minimumAmountTitle(): Observable<SpannableString> = this.minimumAmountStyled

        @NonNull
        override fun rewardDescriptionIsGone(): Observable<Boolean> = this.rewardDescriptionIsGone

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
