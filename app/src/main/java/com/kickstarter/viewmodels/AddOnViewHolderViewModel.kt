package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.RewardViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.math.RoundingMode

interface AddOnViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward]. */
        fun configureWith(projectData: ProjectData, reward: Reward)
    }

    interface Outputs {

        /** Emits `true` if the quantity should be hidden, `false` otherwise.  */
        fun quantityIsGone(): Observable<Boolean>

        /** Emits the reward's minimum converted to the user's preference  */
        fun conversion(): Observable<String>

        /** Emits `true` if the conversion should be hidden, `false` otherwise.  */
        fun conversionIsGone(): Observable<Boolean>

        /** Emits the reward's description when `isNoReward` is true. */
        fun descriptionForNoReward(): Observable<Int>

        /** Emits the reward's description.  */
        fun descriptionForReward(): Observable<String?>

        /** Emits the minimum pledge amount in the project's currency.  */
        fun minimumAmountTitle(): Observable<SpannableString>

        /** Emits the reward's items.  */
        fun rewardItems(): Observable<List<RewardsItem>>

        /** Emits `true` if the items section should be hidden, `false` otherwise.  */
        fun rewardItemsAreGone(): Observable<Boolean>

        /** Emits `true` if the title should be hidden, `false` otherwise.  */
        fun titleIsGone(): Observable<Boolean>

        /** Emits the reward's title when `isReward` is true.  */
        fun titleForReward(): Observable<String?>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<RewardViewHolder>(environment), Inputs, Outputs{

        private val ksCurrency: KSCurrency = environment.ksCurrency()

        private val projectDataAndReward = PublishSubject.create<Pair<ProjectData, Reward>>()
        private val quantityIsGone = BehaviorSubject.create<Boolean>()
        private val conversion = BehaviorSubject.create<String>()
        private val conversionIsGone = BehaviorSubject.create<Boolean>()
        private val descriptionForNoReward = BehaviorSubject.create<Int>()
        private val descriptionForReward = BehaviorSubject.create<String?>()
        private val minimumAmountTitle = PublishSubject.create<SpannableString>()
        private val rewardItems = BehaviorSubject.create<List<RewardsItem>>()
        private val rewardItemsAreGone = BehaviorSubject.create<Boolean>()
        private val titleForReward = BehaviorSubject.create<String?>()
        private val titleIsGone = BehaviorSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            val reward = this.projectDataAndReward
                    .map { it.second }

            val projectAndReward = this.projectDataAndReward
                    .map { Pair(it.first.project(), it.second) }

            projectAndReward
                    .map { RewardViewUtils.styleCurrency(it.second.minimum(), it.first, this.ksCurrency) }
                    .compose(bindToLifecycle())
                    .subscribe(this.minimumAmountTitle)

            projectAndReward
                    .map { it.first }
                    .map { it.currency() == it.currentCurrency() }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversionIsGone)

            projectAndReward
                    .map { this.ksCurrency.format(it.second.convertedMinimum(), it.first, true, RoundingMode.HALF_UP, true) }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversion)

            reward
                    .filter { RewardUtils.isReward(it) }
                    .map { it.description() }
                    .compose(bindToLifecycle())
                    .subscribe(this.descriptionForReward)

            reward
                    .map { it.quantity()?.let { it <= 0 } ?: true }
                    .compose(bindToLifecycle())
                    .subscribe(this.quantityIsGone)

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
                    .filter { RewardUtils.isReward(it) }
                    .map { it.title() }
                    .compose(bindToLifecycle())
                    .subscribe(this.titleForReward)

            reward
                    .map { RewardUtils.isReward(it) && it.title().isNullOrEmpty() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.titleIsGone)
        }

        override fun configureWith(projectData: ProjectData, reward: Reward) {
            this.projectDataAndReward.onNext(Pair.create(projectData, reward))
        }

        override fun quantityIsGone(): Observable<Boolean> = this.quantityIsGone

        override fun conversion(): Observable<String> = this.conversion

        override fun conversionIsGone(): Observable<Boolean> = this.conversionIsGone

        override fun descriptionForNoReward(): Observable<Int> = this.descriptionForNoReward

        override fun descriptionForReward(): Observable<String?> = this.descriptionForReward

        override fun minimumAmountTitle(): Observable<SpannableString> = this.minimumAmountTitle

        override fun rewardItems(): Observable<List<RewardsItem>> = this.rewardItems

        override fun rewardItemsAreGone(): Observable<Boolean> = this.rewardItemsAreGone

        override fun titleIsGone(): Observable<Boolean> = this.titleIsGone

        override fun titleForReward(): Observable<String?> = this.titleForReward
    }
}
