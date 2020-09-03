package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RewardUtils.isDigital
import com.kickstarter.libs.utils.RewardUtils.isShippable
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.BackingAddOnsFragment
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BackingAddOnsFragmentViewModel {

    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward].
         * @param projectData we get the Project for currency
         */
        fun configureWith(pledgeDataAndReason: Pair<PledgeData, PledgeReason>)

        /** Call when user selects a shipping location. */
        fun shippingRuleSelected(shippingRule: ShippingRule)

        /** Emits when the CTA button has been pressed */
        fun continueButtonPressed()

        /** Emits the quantity per AddOn Id selected */
        fun quantityPerId(quantityPerId: Pair<Int, Long>)
    }

    interface Outputs {
        /** Emits a Pair containing the projectData and the pledgeReason. */
        fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>>

        /** Emits a Pair containing the projectData and the list for Add-ons associated to that project. */
        fun addOnsList(): Observable<Triple<ProjectData, List<Reward>, ShippingRule>>

        /** Emits the current selected shipping rule. */
        fun selectedShippingRule(): Observable<ShippingRule>

        /** Emits a pair of list of shipping rules to be selected and the project. */
        fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>>

        /** Emits the total sum of addOns selected in each item of the addOns list. */
        fun totalSelectedAddOns(): Observable<Int>

        /** Emits whether or not the shipping selector is visible **/
        fun shippingSelectorIsGone(): Observable<Boolean>

        /** Emits whether or not the continue button should be enabled **/
        fun isEnabledCTAButton(): Observable<Boolean>

        /** Emits whether or not the empty state should be shown **/
        fun isEmptyState(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<BackingAddOnsFragment>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        private val pledgeDataAndReason = BehaviorSubject.create<Pair<PledgeData, PledgeReason>>()
        private val shippingRuleSelected = PublishSubject.create<ShippingRule>()
        private val shippingRulesAndProject = PublishSubject.create<Pair<List<ShippingRule>, Project>>()

        private val showPledgeFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val shippingSelectorIsGone = BehaviorSubject.create<Boolean>()
        private val addOnsListFiltered = PublishSubject.create<Triple<ProjectData, List<Reward>, ShippingRule>>()
        private val isEmptyState = PublishSubject.create<Boolean>()
        private val totalSelectedAddOns = BehaviorSubject.create(0)
        private val continueButtonPressed = BehaviorSubject.create<Void>()
        private val quantityPerId = PublishSubject.create<Pair<Int, Long>>()
        private val currentSelection: MutableMap<Long, Int> = mutableMapOf()
        private val isEnabledCTAButton = BehaviorSubject.create<Boolean>()

        private val apolloClient = this.environment.apolloClient()
        private val apiClient = environment.apiClient()
        private val currentConfig = environment.currentConfig()
        val ksString: KSString = this.environment.ksString()

        init {

            val pledgeData = arguments()
                    .map { it.getParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA) as PledgeData? }
                    .ofType(PledgeData::class.java)

            val pledgeReason = arguments()
                    .map { it.getSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON) as PledgeReason }

            val projectData = pledgeData
                    .map { it.projectData() }

            val project = projectData
                    .map { it.project() }

            val rewardPledge = pledgeData
                    .map { it.reward() }

            val backing = projectData
                    .map { getBackingFromProjectData(it) }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }

            val backingReward = backing
                    .map { it.reward() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }

            val reward = Observable.merge(rewardPledge, backingReward)

            val projectAndReward = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))

            val shippingRules = projectAndReward
                    .filter { isShippable(it.second) }
                    .distinctUntilChanged()
                    .switchMap<ShippingRulesEnvelope> { this.apiClient.fetchShippingRules(it.first, it.second).compose(Transformers.neverError()) }
                    .map { it.shippingRules() }
                    .share()

            val backingShippingRule = backing
                    .compose<Pair<Backing, List<ShippingRule>>>(combineLatestPair(shippingRules))
                    .map {
                        it.second.first { rule ->
                            rule.location().id() == it.first.locationId()
                        }
                    }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }

            backingShippingRule
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.shippingRuleSelected.onNext(it)
                    }

            // - In case of digital Reward to follow the same flow as the rest of use cases use and empty shippingRule
            reward
                    .filter { isDigital(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.shippingRuleSelected.onNext(ShippingRuleFactory.emptyShippingRule())
                    }

            val addOnsFromBacking = backing
                    .map { it.addOns()?.toList() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }

            val addOnsFromGraph = project
                    .switchMap { pj -> this.apolloClient.getProjectAddOns(pj.slug()?.let { it }?: "") }
                    .compose(bindToLifecycle())
                    .filter { ObjectUtils.isNotNull(it) }
                    .share()

            val combinedList = addOnsFromBacking
                   .compose<Pair<List<Reward>, List<Reward>>>(combineLatestPair(addOnsFromGraph))
                   .map { joinSelectedWithAvailableAddOns(it.first, it.second) }

            val addonsList = Observable.merge(addOnsFromGraph, combinedList)

            shippingRules
                    .compose<Pair<List<ShippingRule>, Project>>(combineLatestPair(project))
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRulesAndProject)

            shippingRules
                    .filter { it.isNotEmpty() }
                    .compose<Pair<List<ShippingRule>, PledgeReason>>(combineLatestPair(pledgeReason))
                    .filter { it.second == PledgeReason.PLEDGE }
                    .switchMap { defaultShippingRule(it.first) }
                    .subscribe(this.shippingRuleSelected)

            val filteredAddOns = Observable.combineLatest(addonsList, projectData, this.shippingRuleSelected, reward, this.totalSelectedAddOns) { list, pData, rule, rw ,
                _ ->
                return@combineLatest filterByLocationAndUpdateQuantity(list, pData, rule, rw)
            }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())

            filteredAddOns
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.addOnsListFiltered)

            filteredAddOns
                    .map { it.second.isEmpty() }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.isEmptyState)

            reward
                    .map{ !isShippable(it) }
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingSelectorIsGone)

            this.quantityPerId
                    .compose<Pair<Pair<Int, Long>, Triple<ProjectData, List<Reward>, ShippingRule>>>(combineLatestPair(this.addOnsListFiltered))
                    .compose(bindToLifecycle())
                    .distinctUntilChanged()
                    .subscribe {
                        updateQuantityById(it.first)
                        this.totalSelectedAddOns.onNext(calculateTotal(it.second.second))
                    }

            // - this.quantityPerId.startWith(Pair(-1,-1L) because we need to trigger this validation everytime the AddOns selection changes
            val isButtonEnabled = Observable.combineLatest(backingShippingRule, addOnsFromBacking, this.shippingRuleSelected, this.quantityPerId.startWith(Pair(-1,-1L))) {
                backedRule, backedList, actualRule,_  ->
                return@combineLatest isDifferentLocation(backedRule, actualRule) || isDifferentSelection(backedList)
            }
                    .distinctUntilChanged()

            isButtonEnabled
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.isEnabledCTAButton.onNext(it)
                    }

            Observable.combineLatest(this.continueButtonPressed, this.addOnsListFiltered, pledgeData, pledgeReason, reward, this.shippingRuleSelected) {
                _, listAddOns, pledgeData, pledgeReason, rw, shippingRule ->
                val finalList = listAddOns.second

                val updatedPledgeData = when {
                    isDigital(rw) && finalList.isNotEmpty() -> {
                        pledgeData.toBuilder()
                                .addOns(finalList as java.util.List<Reward>)
                                .build()
                    }
                    isShippable(rw) && finalList.isEmpty() -> {
                        pledgeData.toBuilder()
                                .addOns(finalList as java.util.List<Reward>)
                                .shippingRule(shippingRule)
                                .build()
                    }
                    else ->  {
                        pledgeData.toBuilder()
                                .build()
                    }
                }

                return@combineLatest Pair(updatedPledgeData, pledgeReason)
            }
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.showPledgeFragment.onNext(it)
                    }
        }

        private fun isDifferentSelection(backedList: List<Reward>): Boolean {
            val backedSelection: MutableMap<Long, Int> = mutableMapOf()
                    backedList
                    .map {
                        backedSelection.put(it.id(), it.quantity() ?: 0)
                    }

            return backedSelection != this.currentSelection
        }

        private fun isDifferentLocation(backedRule: ShippingRule, actualRule: ShippingRule) =
                backedRule.location().id() != actualRule.location().id()

        private fun calculateTotal(list: List<Reward>): Int {
            var total = 0
            list.map { total += this.currentSelection[it.id()]?: 0 }
            return total
        }

        private fun joinSelectedWithAvailableAddOns(backingList: List<Reward>, graphList: List<Reward>):List<Reward> {
            return graphList
                    .map { graphAddOn ->
                        modifyOrSelect(backingList, graphAddOn)
                    }
        }

        private fun modifyOrSelect(backingList: List<Reward>, graphAddOn: Reward): Reward {
            return backingList.firstOrNull { it.id() == graphAddOn.id() }?.let {
                val update = Pair(it.quantity()?: 0 , it.id())
                if (update.first > 0 )
                    updateQuantityById(update)
                return@let it
            } ?: graphAddOn
        }

        private fun getBackingFromProjectData(pData: ProjectData?) = pData?.project()?.backing() ?: pData?.backing()

        private fun updateQuantityById(it: Pair<Int, Long>) {
            this.currentSelection[it.second] = it.first
        }

        private fun defaultShippingRule(shippingRules: List<ShippingRule>): Observable<ShippingRule> {
            return this.currentConfig.observable()
                    .map { it.countryCode() }
                    .map { countryCode ->
                        shippingRules.firstOrNull { it.location().country() == countryCode }
                                ?: shippingRules.first()
                    }
        }

        private fun filterByLocationAndUpdateQuantity(addOns: List<Reward>, pData: ProjectData, rule: ShippingRule, rw: Reward): Triple<ProjectData, List<Reward>, ShippingRule> {
           val filteredAddOns = when (rw.shippingPreference()){
                Reward.ShippingPreference.UNRESTRICTED.toString().toLowerCase() -> {
                    addOns.filter {
                        it.shippingPreferenceType() ==  Reward.ShippingPreference.UNRESTRICTED || isDigital(it)
                    }
                }
                Reward.ShippingPreference.RESTRICTED.toString().toLowerCase() -> {
                    addOns.filter { containsLocation(rule, it) || isDigital(it) }
                }
                else -> {
                    if (isDigital(rw))
                        addOns.filter { isDigital(it) }
                    else emptyList()
                }
            }

            val updatedQuantity = filteredAddOns
                    .map {
                        val amount = this.currentSelection[it.id()] ?: -1
                        return@map  if (amount == -1 ) it else it.toBuilder().quantity(amount).build()
                    }

            return Triple(pData, updatedQuantity, rule)
        }

        private fun containsLocation(rule: ShippingRule, reward: Reward): Boolean {
            val idLocations = reward
                    .shippingRules()
                    ?.map {
                        it.location().id()
                    }?: emptyList()

            return idLocations.contains(rule.location().id())
        }

        // - Inputs
        override fun configureWith(pledgeDataAndReason: Pair<PledgeData, PledgeReason>) = this.pledgeDataAndReason.onNext(pledgeDataAndReason)
        override fun shippingRuleSelected(shippingRule: ShippingRule) = this.shippingRuleSelected.onNext(shippingRule)
        override fun continueButtonPressed() = this.continueButtonPressed.onNext(null)
        override fun quantityPerId(quantityPerId: Pair<Int, Long>) = this.quantityPerId.onNext(quantityPerId)

        // - Outputs
        @NonNull
        override fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showPledgeFragment
        override fun addOnsList(): Observable<Triple<ProjectData, List<Reward>, ShippingRule>> = this.addOnsListFiltered
        override fun selectedShippingRule(): Observable<ShippingRule> = this.shippingRuleSelected
        override fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>> = this.shippingRulesAndProject
        override fun totalSelectedAddOns(): Observable<Int> = this.totalSelectedAddOns
        override fun shippingSelectorIsGone(): Observable<Boolean> = this.shippingSelectorIsGone
        override fun isEnabledCTAButton(): Observable<Boolean> = this.isEnabledCTAButton
        override fun isEmptyState(): Observable<Boolean> = this.isEmptyState
    }
}