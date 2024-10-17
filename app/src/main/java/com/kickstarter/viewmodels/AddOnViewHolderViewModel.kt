package com.kickstarter.viewmodels

import android.util.Pair
import com.kickstarter.R
import com.kickstarter.libs.Environment
import com.kickstarter.libs.models.Country
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.negate
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.math.RoundingMode

interface AddOnViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward].
         * @param projectData we get the Project for currency
         * @param reward the actual reward, add on, no reward loading on the ViewHolder
         */
        fun configureWith(projectData: ProjectData, reward: Reward)
    }

    interface Outputs {

        /** Emits `true` if the title for addons should be hidden, `false` otherwise.  */
        fun isAddonTitleGone(): Observable<Boolean>

        /** Emits the reward's minimum converted to the user's preference  */
        fun conversion(): Observable<String>

        /** Emits `true` if the conversion should be hidden, `false` otherwise.  */
        fun conversionIsGone(): Observable<Boolean>

        /** Emits the reward's description when `isNoReward` is true. */
        fun descriptionForNoReward(): Observable<Int>

        /** Emits the reward's description.  */
        fun descriptionForReward(): Observable<String?>

        /** Emits the minimum pledge amount in the project's currency.  */
        fun minimumAmountTitle(): Observable<String>

        /** Emits the reward's items.  */
        fun rewardItems(): Observable<List<RewardsItem>>

        /** Emits `true` if the items section should be hidden, `false` otherwise.  */
        fun rewardItemsAreGone(): Observable<Boolean>

        /** Emits the reward's title when `isReward` is true.  */
        fun titleForReward(): Observable<String?>

        /** Emits the reward's title when `noReward` is true.  */
        fun titleForNoReward(): Observable<Int>

        /** Emits a pait with the add on title and the quantity in order to build the stylized title  */
        fun titleForAddOn(): Observable<Pair<String, Int>>

        /** Emits a boolean that determines if the local PickUp section should be hidden **/
        fun localPickUpIsGone(): Observable<Boolean>

        /** Emits the String with the Local Pickup Displayable name **/
        fun localPickUpName(): Observable<String>
    }

    /**
     *  Logic to handle the UI for `Reward`, `No Reward` and `Add On`
     *  Configuring the View for [AddOnViewHolder]
     *  - No interaction with the user just displaying information
     *  - Loading in [AddOnViewHolder] -> [RewardAndAddOnsAdapter] -> [BackingFragment]
     */
    class ViewModel(environment: Environment) : Inputs, Outputs {

        private val ksCurrency = requireNotNull(environment.ksCurrency())
        private val isAddonTitleGone = BehaviorSubject.create<Boolean>()
        private val projectDataAndReward = PublishSubject.create<Pair<ProjectData, Reward>>()
        private val conversion = BehaviorSubject.create<String>()
        private val conversionIsGone = BehaviorSubject.create<Boolean>()
        private val descriptionForNoReward = BehaviorSubject.create<Int>()
        private val titleForNoReward = BehaviorSubject.create<Int>()
        private val descriptionForReward = BehaviorSubject.create<String?>()
        private val minimumAmountTitle = PublishSubject.create<String>()
        private val rewardItems = BehaviorSubject.create<List<RewardsItem>>()
        private val rewardItemsAreGone = BehaviorSubject.create<Boolean>()
        private val titleForReward = BehaviorSubject.create<String?>()
        private val titleForAddOn = BehaviorSubject.create<Pair<String, Int>>()
        private val titleIsGone = BehaviorSubject.create<Boolean>()
        private val localPickUpIsGone = BehaviorSubject.create<Boolean>()
        private val localPickUpName = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()

        init {
            val reward = this.projectDataAndReward
                .map { it.second }

            val projectAndReward = this.projectDataAndReward
                .map { Pair(it.first.project(), it.second) }

            projectAndReward
                .map { buildCurrency(it.first, it.second) }
                .subscribe { this.minimumAmountTitle.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { it.first }
                .map { it.currency() == it.currentCurrency() }
                .subscribe { this.conversionIsGone.onNext(it) }
                .addToDisposable(disposables)

            projectAndReward
                .map { getCurrency(it) }
                .subscribe { this.conversion.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isReward(it) }
                .filter { it.description().isNotNull() }
                .map { it.description() }
                .map { it }
                .subscribe { this.descriptionForReward.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { !it.isAddOn() && RewardUtils.isNoReward(it) }
                .subscribe {
                    this.descriptionForNoReward.onNext(R.string.Thanks_for_bringing_this_project_one_step_closer_to_becoming_a_reality)
                    this.titleForNoReward.onNext(R.string.You_pledged_without_a_reward)
                }
                .addToDisposable(disposables)

            reward
                .filter { RewardUtils.isItemized(it) }
                .filter { if (it.isAddOn()) it.addOnsItems().isNotNull() else it.rewardsItems().isNotNull() }
                .map { if (it.isAddOn()) it.addOnsItems() else it.rewardsItems() }
                .map { it }
                .subscribe { this.rewardItems.onNext(it) }
                .addToDisposable(disposables)

            reward
                .map { RewardUtils.isItemized(it) }
                .map { it.negate() }
                .distinctUntilChanged()
                .subscribe { this.rewardItemsAreGone.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { !it.isAddOn() && RewardUtils.isReward(it) }
                .filter { it.title().isNotNull() }
                .map { it.title() }
                .map { it }
                .subscribe { this.titleForReward.onNext(it) }
                .addToDisposable(disposables)

            reward
                .map { !it.isAddOn() }
                .subscribe { this.titleIsGone.onNext(it) }
                .addToDisposable(disposables)

            reward
                .filter { it.isAddOn() && it.quantity()?.let { q -> q > 0 } ?: false }
                .map { reward -> parametersForTitle(reward) }
                .subscribe { this.titleForAddOn.onNext(it) }
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
                .map { it.localReceiptLocation()?.displayableName() }
                .map { it }
                .subscribe { this.localPickUpName.onNext(it) }
                .addToDisposable(disposables)
        }

        private fun getCurrency(it: Pair<Project, Reward>) =
            this.ksCurrency.format(it.second.convertedMinimum(), it.first, true, RoundingMode.HALF_UP, true)

        private fun buildCurrency(project: Project, reward: Reward): String {
            val completeCurrency = if (project.backing()?.isPostCampaign() == true) {
                if (reward.latePledgeAmount() > 0) {
                    ksCurrency.format(reward.latePledgeAmount(), project, RoundingMode.HALF_UP)
                } else {
                    ksCurrency.format(reward.minimum(), project, RoundingMode.HALF_UP)
                }
            } else {
                if (reward.pledgeAmount() > 0) {
                    ksCurrency.format(reward.pledgeAmount(), project, RoundingMode.HALF_UP)
                } else {
                    ksCurrency.format(reward.minimum(), project, RoundingMode.HALF_UP)
                }
            }

            val country = Country.findByCurrencyCode(project.currency()) ?: ""

            return completeCurrency.removePrefix(country.toString())
        }

        private fun parametersForTitle(reward: Reward?): Pair<String, Int> {
            val title = reward?.title()?.let { it } ?: ""
            val quantity = reward?.quantity()?.let { it } ?: -1

            return Pair(title, quantity)
        }

        override fun configureWith(projectData: ProjectData, reward: Reward) = this.projectDataAndReward.onNext(Pair.create(projectData, reward))

        override fun isAddonTitleGone(): Observable<Boolean> = this.isAddonTitleGone

        override fun conversion(): Observable<String> = this.conversion

        override fun conversionIsGone(): Observable<Boolean> = this.conversionIsGone

        override fun descriptionForNoReward(): Observable<Int> = this.descriptionForNoReward

        override fun titleForNoReward(): Observable<Int> = this.titleForNoReward

        override fun descriptionForReward(): Observable<String?> = this.descriptionForReward

        override fun minimumAmountTitle(): Observable<String> = this.minimumAmountTitle

        override fun rewardItems(): Observable<List<RewardsItem>> = this.rewardItems

        override fun rewardItemsAreGone(): Observable<Boolean> = this.rewardItemsAreGone

        override fun titleForReward(): Observable<String?> = this.titleForReward

        override fun titleForAddOn(): Observable<Pair<String, Int>> = this.titleForAddOn

        override fun localPickUpIsGone(): Observable<Boolean> = localPickUpIsGone

        override fun localPickUpName(): Observable<String> = localPickUpName

        fun clear() = disposables.clear()
    }
}
