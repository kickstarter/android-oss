package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.KSCurrency
import com.kickstarter.libs.utils.*
import com.kickstarter.models.Reward
import com.kickstarter.models.RewardsItem
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

        fun conversionIsGone(): Observable<Boolean>

        fun conversion(): Observable<CharSequence>

        fun rewardItems(): Observable<List<RewardsItem>>

        fun deadlinePillIsGone(): Observable<Boolean>

        fun remainingQuantityPillIsGone(): Observable<Boolean>

        fun backerLimitPillIsGone(): Observable<Boolean>

        fun timeRemaining(): Observable<String>

        fun backerLimit(): Observable<String>

        fun remainingQuantity(): Observable<String>

        fun shippingAmountIsGone(): Observable<Boolean>

        fun shippingAmount(): Observable<Double>

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
        private val timeRemaining = PublishSubject.create<String>()
        private val deadlinePillIsGone = PublishSubject.create<Boolean>()
        private val remainingQuantityPillIsGone = PublishSubject.create<Boolean>()
        private val backerLimitPillIsGone = PublishSubject.create<Boolean>()
        private val remainingQuantity = PublishSubject.create<String>()
        private val backerLimit = PublishSubject.create<String>()
        private val shippingAmountIsGone = PublishSubject.create<Boolean>()
        private val shippingAmount = PublishSubject.create<Double>()

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

            addOn.filter { RewardUtils.isItemized(it) }
                    .map { it.rewardsItems() }
                    .compose(bindToLifecycle())
                    .subscribe(this.rewardItems)

            projectDataAndAddOn.map { ProjectViewUtils.styleCurrency(it.second.minimum(), it.first.project(), this.ksCurrency) }
                    .subscribe(this.minimum)


            projectDataAndAddOn.map { ProjectViewUtils.styleCurrency(it.second.convertedMinimum(), it.first.project(), this.ksCurrency) }
                    .subscribe(this.convertedMinimum)

            projectDataAndAddOn.map { it.first.project() }
                    .map { it.currency() == it.currentCurrency() }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversionIsGone)

            projectDataAndAddOn
                    .map { this.ksCurrency.format(it.second.convertedMinimum(), it.first.project(), true, RoundingMode.HALF_UP, true) }
                    .compose(bindToLifecycle())
                    .subscribe(this.conversion)

            projectDataAndAddOn.map { it.first.project() }
                    .map { ProjectUtils.isCompleted(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.deadlinePillIsGone)

            addOn.map { it.limit() }
                    .map { !ObjectUtils.isNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe { this.backerLimitPillIsGone }

            addOn.map { it.quantity() }
                    .map { !ObjectUtils.isNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe { this.remainingQuantityPillIsGone }

            addOn.map { it.limit().toString() }
                    .compose(bindToLifecycle())
                    .subscribe(this.backerLimit)

            addOn.map { it.remaining().toString() }
                    .compose(bindToLifecycle())
                    .subscribe (this.remainingQuantity)

            //addOn.map { it.shippingRules()?.get(0)?.cost() }


        }


        override fun configureWith(projectDataAndAddOn: Pair<ProjectData, Reward>) = this.projectDataAndAddOn.onNext(projectDataAndAddOn)

        override fun titleForAddOn() = this.title

        override fun description() = this.description

        override fun minimum() = this.minimum

        override fun convertedMinimum() = this.convertedMinimum

        override fun conversionIsGone() = this.conversionIsGone

        override fun conversion() = this.conversion

        override fun rewardItems() = this.rewardItems

        override fun timeRemaining() = this.timeRemaining

        override fun deadlinePillIsGone() = this.deadlinePillIsGone

        override fun remainingQuantityPillIsGone() = this.remainingQuantityPillIsGone

        override fun backerLimitPillIsGone() = this.backerLimitPillIsGone

        override fun backerLimit() = this.backerLimit

        override fun remainingQuantity() = this.remainingQuantity

        override fun shippingAmountIsGone() = this.shippingAmountIsGone

        override fun shippingAmount() = this.shippingAmount
    }
}