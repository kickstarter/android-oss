package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
import com.kickstarter.models.ShippingRule
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.viewholders.BackingAddOnViewHolder
import rx.Observable
import rx.subjects.PublishSubject
import java.math.RoundingMode

class BackingAddOnViewHolderViewModel {
    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward].
         * @param projectData we get the Project for currency
         * @param AddOn  the actual addOn item loading on the ViewHolder
         */
        fun configureWith(projectDataAndAddOn: Pair<ProjectData, Reward>)
    }

    interface Outputs {
        /** Emits a pair with the add on title and the quantity in order to build the stylized title  */
        fun titleForAddOn(): Observable<String>

        /** Emits the add on's description.  */
        fun description(): Observable<String>

        /** Emits the add on's minimum amount */
        fun minimum(): Observable<CharSequence>

        /** Emits the add on's converted minimum amount */
        fun convertedMinimum(): Observable<CharSequence>

        /** Emits whether or not the conversion text view is visible */
        fun conversionIsGone(): Observable<Boolean>

        /** Emits the conversion amount */
        fun conversion(): Observable<CharSequence>

        /** Emits the reward items */
        fun rewardItems(): Observable<List<RewardsItem>>

        /** Emits whether or not the remaining quantity pill is visible */
        fun remainingQuantityPillIsGone(): Observable<Boolean>

        /** Emits whether or not the backer limit pill is visible */
        fun backerLimitPillIsGone(): Observable<Boolean>

        /** Emits the backer limit */
        fun backerLimit(): Observable<String>

        /** Emits the remaining quantity*/
        fun remainingQuantity(): Observable<String>

        /** Emits whether or not the shipping amount is visible */
        fun shippingAmountIsGone(): Observable<Boolean>

        /** Emits the shipping amount */
        fun shippingAmount(): Observable<String>

        /** Emits whether or not the countdown pill is gone */
        fun deadlineCountdownIsGone(): Observable<Boolean>

        /** Emits the reward to be used to calculate the deadline countdown */
        fun deadlineCountdown(): Observable<Reward>

        /** Emits whether or not the reward items list is gone */
        fun rewardItemsAreGone(): Observable<Boolean>

    }

    /**
     *  Logic to handle the UI for backing `Add On` card
     *  Configuring the View for [BackingAddOnViewHolder]
     *  - No interaction with the user just displaying information
     *  - Loading in [BackingAddOnViewHolder] -> [BackingAddOnsAdapter] -> [BackingAddOnsFragment]
     */
    class ViewModel(@NonNull environment: Environment) : ActivityViewModel<BackingAddOnViewHolder>(environment), Inputs, Outputs {

        private val ksCurrency: KSCurrency = environment.ksCurrency()
        private val projectDataAndAddOn = PublishSubject.create<Pair<ProjectData, Reward>>()
        private val title = PublishSubject.create<String>()
        private val description = PublishSubject.create<String>()
        private val minimum = PublishSubject.create<CharSequence>()
        private val convertedMinimum = PublishSubject.create<CharSequence>()
        private val conversionIsGone = PublishSubject.create<Boolean>()
        private val conversion = PublishSubject.create<CharSequence>()
        private val rewardItems = PublishSubject.create<List<RewardsItem>>()
        private val remainingQuantityPillIsGone = PublishSubject.create<Boolean>()
        private val backerLimitPillIsGone = PublishSubject.create<Boolean>()
        private val remainingQuantity = PublishSubject.create<String>()
        private val backerLimit = PublishSubject.create<String>()
        private val shippingAmountIsGone = PublishSubject.create<Boolean>()
        private val shippingAmount = PublishSubject.create<String>()
        private val deadlineCountdown = PublishSubject.create<Reward>()
        private val deadlineCountdownIsGone = PublishSubject.create<Boolean>()
        private val rewardItemsAreGone = PublishSubject.create<Boolean>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {

            val addOn = projectDataAndAddOn
                    .map { it.second }

            addOn
                    .map { it.title() }
                    .subscribe(this.title)

            addOn
                    .map { it.description() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .subscribe(this.description)

            addOn.map { !RewardUtils.isItemized(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardItemsAreGone)


            addOn.filter { RewardUtils.isItemized(it) }
                    .map { it.rewardsItems() }
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardItems)

            projectDataAndAddOn.map { this.ksCurrency.format(it.second.minimum(), it.first.project()) }
                    .subscribe(this.minimum)


            projectDataAndAddOn.map { this.ksCurrency.format(it.second.convertedMinimum(), it.first.project()) }
                    .subscribe(this.convertedMinimum)

            projectDataAndAddOn.map { it.first.project() }
                    .map { it.currency() == it.currentCurrency() }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversionIsGone)

            projectDataAndAddOn
                    .map { this.ksCurrency.format(it.second.convertedMinimum(), it.first.project(), true, RoundingMode.HALF_UP, true) }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversion)

            projectDataAndAddOn
                    .map { !ObjectUtils.isNotNull(it.second.limit()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.backerLimitPillIsGone)

            addOn
                    .map { !ObjectUtils.isNotNull(it.remaining()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.remainingQuantityPillIsGone)

            addOn.map { it.limit().toString() }
                    .compose(bindToLifecycle())
                    .subscribe(this.backerLimit)

            addOn.map { it.remaining().toString() }
                    .compose(bindToLifecycle())
                    .subscribe(this.remainingQuantity)


            projectDataAndAddOn.map { ObjectUtils.isNotNull(it.second.endsAt()) }
                    .compose(bindToLifecycle())
                    .map { !it }
                    .subscribe(this.deadlineCountdownIsGone)

            projectDataAndAddOn.map { it.second }
                    .compose(bindToLifecycle())
                    .subscribe(this.deadlineCountdown)

            addOn.map { it.shippingRules()?.isEmpty() }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingAmountIsGone)


            projectDataAndAddOn.map { getShippingCost(it.second.shippingRules(), it.first.project()) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingAmount)


        }

        private fun getShippingCost(shippingRules: List<ShippingRule>?, project: Project): String {

            if (shippingRules != null) {
                return if (shippingRules.isEmpty()) {
                    ""
                } else {
                    this.ksCurrency.format(shippingRules?.get(0)?.cost()!!, project)

                }
            }

            return ""
        }


        override fun configureWith(projectDataAndAddOn: Pair<ProjectData, Reward>) = this.projectDataAndAddOn.onNext(projectDataAndAddOn)

        override fun titleForAddOn() = this.title

        override fun description() = this.description

        override fun minimum() = this.minimum

        override fun convertedMinimum() = this.convertedMinimum

        override fun conversionIsGone() = this.conversionIsGone

        override fun conversion() = this.conversion

        override fun rewardItems() = this.rewardItems

        override fun remainingQuantityPillIsGone() = this.remainingQuantityPillIsGone

        override fun backerLimitPillIsGone() = this.backerLimitPillIsGone

        override fun backerLimit() = this.backerLimit

        override fun remainingQuantity() = this.remainingQuantity

        override fun shippingAmountIsGone() = this.shippingAmountIsGone

        override fun shippingAmount() = this.shippingAmount

        override fun deadlineCountdown() = this.deadlineCountdown

        override fun deadlineCountdownIsGone() = this.deadlineCountdownIsGone

        override fun rewardItemsAreGone() = this.rewardItemsAreGone
    }
}