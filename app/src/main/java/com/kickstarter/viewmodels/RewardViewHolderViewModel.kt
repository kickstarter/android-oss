package com.kickstarter.viewmodels

import android.text.SpannableString
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.R
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.CurrentUserType
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.models.OptimizelyExperiment
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.BooleanUtils
import com.kickstarter.libs.utils.DateTimeUtils
import com.kickstarter.libs.utils.ExperimentData
import com.kickstarter.libs.utils.NumberUtils
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardViewUtils
import com.kickstarter.libs.utils.extensions.isBacked
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.models.User
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.RewardViewHolder
import org.joda.time.DateTime
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
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
        fun titleForReward(): Observable<String?>

        /** Emits if the reward has add-Ons available */
        fun hasAddOnsAvailable(): Observable<Boolean>

        /** Emits a boolean that determines if the minimum pledge amount should be shown **/
        fun isMinimumPledgeAmountGone(): Observable<Boolean>

        /** Emits a boolean that determines if the selected reward Tag should be shown **/
        fun selectedRewardTagIsGone(): Observable<Boolean>
    }

    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<RewardViewHolder>(environment), Inputs, Outputs {
        private val currentUser: CurrentUserType = environment.currentUser()
        private val ksCurrency: KSCurrency = environment.ksCurrency()
        private val optimizely = environment.optimizely()

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
        private val descriptionForReward = BehaviorSubject.create<String?>()
        private val descriptionIsGone = BehaviorSubject.create<Boolean>()
        private val endDateSectionIsGone = BehaviorSubject.create<Boolean>()
        private val estimatedDelivery = BehaviorSubject.create<String>()
        private val estimatedDeliveryIsGone = BehaviorSubject.create<Boolean>()
        private val limitContainerIsGone = BehaviorSubject.create<Boolean>()
        private val minimumAmountTitle = PublishSubject.create<SpannableString>()
        private val remaining = BehaviorSubject.create<String>()
        private val remainingIsGone = BehaviorSubject.create<Boolean>()
        private val reward = BehaviorSubject.create<Reward>()
        private val rewardItems = BehaviorSubject.create<List<RewardsItem>>()
        private val rewardItemsAreGone = BehaviorSubject.create<Boolean>()
        private val shippingSummary = BehaviorSubject.create<Pair<Int, String?>>()
        private val shippingSummaryIsGone = BehaviorSubject.create<Boolean>()
        private val showFragment = PublishSubject.create<Pair<Project, Reward>>()
        private val titleForNoReward = BehaviorSubject.create<Int>()
        private val titleForReward = BehaviorSubject.create<String?>()
        private val titleIsGone = BehaviorSubject.create<Boolean>()
        private val addOnsAvailable = BehaviorSubject.create<Boolean>()
        private val variantSuggestedAmount = BehaviorSubject.create<Int?>()
        private val isMinimumPledgeAmountGone = BehaviorSubject.create<Boolean>()
        private val selectedRewardTagIsGone = PublishSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            Observable.combineLatest(this.projectDataAndReward, this.currentUser.observable()) { data, user ->
                val experimentData = ExperimentData(user, data.first.refTagFromIntent(), data.first.refTagFromCookie())
                val variant = this.optimizely.variant(OptimizelyExperiment.Key.SUGGESTED_NO_REWARD_AMOUNT, experimentData)
                rewardAmountByVariant(variant)
            }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(variantSuggestedAmount)

            val reward = this.projectDataAndReward
                .map { it.second }
                .compose<Pair<Reward, Int?>>(combineLatestPair(variantSuggestedAmount))
                .distinctUntilChanged()
                .map { updateReward(it) }

            val project = this.projectDataAndReward
                .map { it.first.project() }

            val projectAndReward = project
                .compose<Pair<Project, Reward>>(combineLatestPair(reward))
                .map { Pair(it.first, it.second) }
                .distinctUntilChanged()

            projectAndReward
                .map { RewardViewUtils.styleCurrency(it.second.minimum(), it.first, this.ksCurrency) }
                .compose(bindToLifecycle())
                .subscribe(this.minimumAmountTitle)

            val userCreatedProject = this.currentUser.observable()
                .compose<Pair<User?, Project>>(combineLatestPair(project))
                .map { it.first?.id() == it.second.creator().id() }

            projectAndReward
                .compose<Pair<Pair<Project, Reward>, Boolean>>(combineLatestPair(userCreatedProject))
                .map { buttonIsGone(it.first.first, it.first.second, it.second) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.buttonIsGone)

            projectAndReward
                .map { RewardViewUtils.pledgeButtonText(it.first, it.second) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.buttonCTA.onNext(it)
                }

            projectAndReward
                .map { it.first }
                .map { it.currency() == it.currentCurrency() }
                .compose(bindToLifecycle())
                .subscribe(this.conversionIsGone)

            projectAndReward
                .map { this.ksCurrency.format(it.second.convertedMinimum(), it.first, true, RoundingMode.HALF_UP, true) }
                .compose(bindToLifecycle())
                .subscribe(this.conversion)

            projectAndReward
                .filter { RewardUtils.isNoReward(it.second) }
                .map { it.first.backing()?.isBacked(it.second) ?: false }
                .map {
                    when {
                        it -> R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality
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

            projectAndReward
                .map { RewardUtils.isReward(it.second) && it.second.description().isNullOrEmpty() }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.descriptionIsGone)

            projectAndReward
                .map { shouldContinueFlow(it.first, it.second) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.buttonIsEnabled.onNext(it)
                }

            projectAndReward
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

            reward
                .map { it.hasAddons() }
                .compose(bindToLifecycle())
                .subscribe(this.addOnsAvailable)

            projectAndReward
                .map { expirationDateIsGone(it.first, it.second) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.endDateSectionIsGone)

            projectAndReward
                .filter { shouldContinueFlow(it.first, it.second) && it.first.isLive }
                .compose<Pair<Project, Reward>>(takeWhen(this.rewardClicked))
                .compose(bindToLifecycle())
                .subscribe(this.showFragment)

            this.projectDataAndReward
                .filter { it.first.project().isLive && !it.first.project().isBacking }
                .compose<Pair<ProjectData, Reward>>(takeWhen(this.rewardClicked))
                .map { PledgeData.with(PledgeFlowContext.NEW_PLEDGE, it.first, it.second) }
                .compose(bindToLifecycle())
                .subscribe {
                    this.lake.trackSelectRewardButtonClicked(it)
                    this.lake.trackSelectRewardCTA(it)
                }

            projectAndReward
                .filter { RewardUtils.isNoReward(it.second) }
                .map { it.first.backing()?.isBacked(it.second) ?: false }
                .map {
                    when {
                        it -> R.string.You_pledged_without_a_reward
                        else -> R.string.Pledge_without_a_reward
                    }
                }
                .compose(bindToLifecycle())
                .subscribe(this.titleForNoReward)

            projectAndReward
                .map { it.first.backing()?.isBacked(it.second) ?: false }
                .compose(bindToLifecycle())
                .subscribe {
                    this.selectedRewardTagIsGone.onNext(!it)
                }

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
                .map { RewardUtils.shippingSummary(it) }
                .filter { ObjectUtils.isNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe(this.shippingSummary)

            projectAndReward
                .map { it.first.isLive && RewardUtils.isShippable(it.second) }
                .map { BooleanUtils.negate(it) }
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe(this.shippingSummaryIsGone)

            Observable.combineLatest(this.endDateSectionIsGone, this.remainingIsGone, this.shippingSummaryIsGone, this.addOnsAvailable) { endDateGone, remainingGone, shippingGone, addOnsAvailable -> endDateGone && remainingGone && shippingGone && !addOnsAvailable }
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

            reward.map { RewardUtils.isNoReward(it) }
                .compose(bindToLifecycle())
                .subscribe(this.isMinimumPledgeAmountGone)
        }

        /**
         * Use cases for enabling/disabling access to launch the next fragment
         * - If the selected reward has no addOns the CTA button will be enable if available
         * - If the previously selected reward has addOns but not BackedAddOns CTA button available is reward available
         * - If the previously selected reward has add-ons and has Backed addOns, CTA button available (they still can update the addOns selection)
         */
        private fun shouldContinueFlow(project: Project, rw: Reward): Boolean {
            val hasAddOns = rw.hasAddons()
            val backedRwId = project.backing()?.rewardId()
            val selectingOtherRw = backedRwId != rw.id()

            return when {
                !hasAddOns && isSelectable(project, rw) -> true
                hasAddOns && selectingOtherRw && RewardUtils.isAvailable(project, rw) -> true
                hasAddOns && !selectingOtherRw -> RewardUtils.isAvailable(project, rw)
                isUpdatingSameRewardWithBackedAddOns(hasAddOns, project, selectingOtherRw, rw) -> true
                else -> false
            }
        }

        private fun isUpdatingSameRewardWithBackedAddOns(hasAddOns: Boolean, project: Project, selectingOtherRw: Boolean, rw: Reward) =
            hasAddOns && hasBackedAddOns(project) && !selectingOtherRw && RewardUtils.hasStarted(rw) && project.isLive

        private fun rewardAmountByVariant(variant: OptimizelyExperiment.Variant?): Int? = when (variant) {
            OptimizelyExperiment.Variant.CONTROL -> 1
            OptimizelyExperiment.Variant.VARIANT_2 -> 10
            OptimizelyExperiment.Variant.VARIANT_3 -> 20
            OptimizelyExperiment.Variant.VARIANT_4 -> 50
            else -> null
        }

        /**
         * In case the `suggested_no_reward_amount` is active and the selected reward is no reward
         * update the value with one of the variants. Otherwise return the reward as it is
         *
         * @param rewardAndVariant contains the selected reward and the variant value
         *
         * @return reward if modified value if needed
         */
        private fun updateReward(rewardAndVariant: Pair<Reward, Int?>): Reward {
            val reward = rewardAndVariant.first

            val updatedMinimum = rewardAndVariant.second?.let {
                if (RewardUtils.isNoReward(reward)) it.toDouble()
                else reward.minimum()
            } ?: reward.minimum()

            return reward.toBuilder().minimum(updatedMinimum).build()
        }

        private fun buttonIsGone(project: Project, reward: Reward, userCreatedProject: Boolean): Boolean {
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

        private fun hasBackedAddOns(project: Project) = !project.backing()?.addOns().isNullOrEmpty()

        private fun isSelectable(@NonNull project: Project, @NonNull reward: Reward): Boolean {
            if (project.backing()?.isBacked(reward) == true) {
                return false
            }

            return RewardUtils.isAvailable(project, reward)
        }

        override fun configureWith(@NonNull projectData: ProjectData, @NonNull reward: Reward) {
            this.projectDataAndReward.onNext(Pair.create(projectData, reward))
        }

        override fun rewardClicked(position: Int) {
            this.rewardClicked.onNext(position)
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
        override fun shippingSummary(): Observable<Pair<Int, String?>> = this.shippingSummary

        @NonNull
        override fun shippingSummaryIsGone(): Observable<Boolean> = this.shippingSummaryIsGone

        @NonNull
        override fun showFragment(): Observable<Pair<Project, Reward>> = this.showFragment

        @NonNull
        override fun titleForNoReward(): Observable<Int> = this.titleForNoReward

        @NonNull
        override fun titleForReward(): Observable<String?> = this.titleForReward

        @NonNull
        override fun titleIsGone(): Observable<Boolean> = this.titleIsGone

        @NonNull
        override fun hasAddOnsAvailable(): Observable<Boolean> = this.addOnsAvailable

        @NonNull
        override fun isMinimumPledgeAmountGone(): Observable<Boolean> = this.isMinimumPledgeAmountGone

        @NonNull
        override fun selectedRewardTagIsGone(): Observable<Boolean> = this.selectedRewardTagIsGone
    }
}
