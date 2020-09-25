package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RewardUtils.isDigital
import com.kickstarter.libs.utils.RewardUtils.isShippable
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.models.Backing
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
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

        /** Invoked when the retry button on the add-on Error alert dialog is pressed */
        fun retryButtonPressed()
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

        /** Emits an alert dialog when add-ons request results in error **/
        fun showErrorDialog(): Observable<Boolean>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<BackingAddOnsFragment>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        private val shippingRules = PublishSubject.create<List<ShippingRule>>()
        private val addOnsFromGraph = PublishSubject.create<List<Reward>>()
        private var pledgeDataAndReason = BehaviorSubject.create<Pair<PledgeData, PledgeReason>>()
        private val shippingRuleSelected = PublishSubject.create<ShippingRule>()
        private val shippingRulesAndProject = PublishSubject.create<Pair<List<ShippingRule>, Project>>()

        private val projectAndReward: Observable<Pair<Project, Reward>>
        private val retryButtonPressed = BehaviorSubject.create<Boolean>()

        private val showPledgeFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val shippingSelectorIsGone = BehaviorSubject.create<Boolean>()
        private val addOnsListFiltered = PublishSubject.create<Triple<ProjectData, List<Reward>, ShippingRule>>()
        private val isEmptyState = PublishSubject.create<Boolean>()
        private val showErrorDialog = BehaviorSubject.create<Boolean>()
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

            pledgeData
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe { this.lake.trackAddOnsPageViewed(it) }

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

            val isSameReward = rewardPledge
                    .compose<Pair<Reward, Reward>>(combineLatestPair(backingReward))
                    .map { it.first.id() == it.second.id() }

            isSameReward
                    .filter { !it }
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.currentSelection.clear()
                    }

            val reward = Observable.merge(rewardPledge, backingReward)

            projectAndReward = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))

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
                    .filter { isDigital(it) || !isShippable(it) }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.shippingSelectorIsGone.onNext(true)
                        this.shippingRuleSelected.onNext(ShippingRuleFactory.emptyShippingRule())
                    }

            val addOnsFromBacking = backing
                    .compose<Pair<Backing, Boolean>>(combineLatestPair(isSameReward))
                    .filter { it.second }
                    .map { it.first }
                    .map { it.addOns()?.toList() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }
                    .distinctUntilChanged()

            val combinedList = addOnsFromBacking
                    .compose<Pair<List<Reward>, List<Reward>>>(combineLatestPair(addOnsFromGraph))
                    .map { joinSelectedWithAvailableAddOns(it.first, it.second) }
                    .distinctUntilChanged()

            val addonsList = Observable.merge(addOnsFromGraph, combinedList)
                    .distinctUntilChanged()

            shippingRules
                    .compose<Pair<List<ShippingRule>, Project>>(combineLatestPair(project))
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRulesAndProject)

            shippingRules
                    .filter { it.isNotEmpty() }
                    .compose<Pair<List<ShippingRule>, PledgeReason>>(combineLatestPair(pledgeReason))
                    .switchMap { defaultShippingRule(it.first) }
                    .subscribe(this.shippingRuleSelected)

            Observable
                    .combineLatest(this.retryButtonPressed.startWith(false), projectAndReward) { _, projectAndReward ->
                        return@combineLatest this.apiClient
                                .fetchShippingRules(projectAndReward.first, projectAndReward.second)
                                .doOnError {
                                    this.showErrorDialog.onNext(true)
                                    this.shippingSelectorIsGone.onNext(true)
                    }
                                .onErrorResumeNext(Observable.empty())
                    }
                    .switchMap { it }
                    .map { it.shippingRules() }
                    .compose(bindToLifecycle())
                    .subscribe {
                        shippingRules.onNext(it)
                    }

            Observable
                    .combineLatest(this.retryButtonPressed.startWith(false), project) { _, pj ->
                        return@combineLatest this.apolloClient
                                .getProjectAddOns(pj.slug()?.let { it } ?: "")
                                .doOnError {
                                    this.showErrorDialog.onNext(true)
                                    this.shippingSelectorIsGone.onNext(true)}
                                .onErrorResumeNext(Observable.empty())
                    }
                    .switchMap { it }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { listAddOns ->
                        listAddOns.filter{ it.isAvailable }
                    }
                    .subscribe(addOnsFromGraph)

            val filteredAddOns = Observable.combineLatest(addonsList, projectData, this.shippingRuleSelected, reward, this.totalSelectedAddOns) { list, pData, rule, rw,
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

            this.quantityPerId
                    .compose<Pair<Pair<Int, Long>, Triple<ProjectData, List<Reward>, ShippingRule>>>(combineLatestPair(this.addOnsListFiltered))
                    .compose(bindToLifecycle())
                    .distinctUntilChanged()
                    .subscribe {
                        updateQuantityById(it.first)
                        this.totalSelectedAddOns.onNext(calculateTotal(it.second.second))
                    }
            // - .startWith(Pair(-1,-1L) because we need to trigger this validation everytime the AddOns selection changes
            // - .startWith(ShippingRuleFactory.emptyShippingRule()) because we need to trigger this validation every time the AddOns selection changes for digital rewards as well
            val isButtonEnabled = Observable.combineLatest(
                    backingShippingRule.startWith(ShippingRuleFactory.emptyShippingRule()),
                    addOnsFromBacking,
                    this.shippingRuleSelected,
                    this.quantityPerId.startWith(Pair(0, 0L))) {
                backedRule, backedList, actualRule, _  ->
                return@combineLatest isDifferentLocation(backedRule, actualRule) || isDifferentSelection(backedList)
            }
                    .distinctUntilChanged()

            isButtonEnabled
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.isEnabledCTAButton.onNext(it)
                    }

            // - Update pledgeData and reason each time there is a change (location, quantity of addons, filtered by location, refreshed ...)
            val updatedPledgeDataAndReason = Observable.combineLatest(this.addOnsListFiltered, pledgeData, pledgeReason, reward, this.shippingRuleSelected) { listAddOns, pledgeData, pledgeReason, rw, shippingRule ->
                val finalList = listAddOns.second.filter { addOn ->
                    addOn.quantity()?.let { it > 0 } ?: false
                }

                val updatedPledgeData = when (finalList.isNotEmpty()) {
                    isDigital(rw) -> {
                        pledgeData.toBuilder()
                                .addOns(finalList as java.util.List<Reward>)
                                .build()
                    }
                    isShippable(rw) -> {
                        pledgeData.toBuilder()
                                .addOns(finalList as java.util.List<Reward>)
                                .shippingRule(shippingRule)
                                .build()
                    }
                    else -> {
                        pledgeData.toBuilder()
                                .build()
                    }
                }
                return@combineLatest Pair(updatedPledgeData, pledgeReason)
            }

            updatedPledgeDataAndReason
                    .compose<Pair<PledgeData, PledgeReason>>(takeWhen(this.continueButtonPressed))
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.lake.trackAddOnsContinueButtonClicked(it.first)
                        this.showPledgeFragment.onNext(it)
                    }
        }

        /**
         *  Extract the ID:quantity from the original baked AddOns list
         *  in case the ID's of those addOns and the quantity are the same
         *  as the current selected ones the selection is the same as the
         *  backed one.
         *
         *  @param backedList -> addOns list from backing object
         *  @return Boolean -> true in case different selection or new item selected false otherwise
         */
        private fun isDifferentSelection(backedList: List<Reward>): Boolean {

            val backedSelection: MutableMap<Long, Int> = mutableMapOf()
            backedList
                    .map {
                        backedSelection.put(it.id(), it.quantity() ?: 0)
                    }

            val isBackedItemList = this.currentSelection.map { item ->
                if (backedSelection.containsKey(item.key)) backedSelection[item.key] == item.value
                else false
            }

            val isNewItemSelected = this.currentSelection.map { item ->
                if (!backedSelection.containsKey(item.key)) item.value > 0
                else false
            }.any { it }

            val sameSelection = isBackedItemList.filter { it }.size == backedSelection.size

            return !sameSelection || isNewItemSelected
        }

        private fun isDifferentLocation(backedRule: ShippingRule, actualRule: ShippingRule) =
                backedRule.location().id() != actualRule.location().id()

        private fun calculateTotal(list: List<Reward>): Int {
            var total = 0
            list.map { total += this.currentSelection[it.id()] ?: 0 }
            return total
        }

        private fun joinSelectedWithAvailableAddOns(backingList: List<Reward>, graphList: List<Reward>): List<Reward> {
            return graphList
                    .map { graphAddOn ->
                        modifyOrSelect(backingList, graphAddOn)
                    }
        }

        private fun modifyOrSelect(backingList: List<Reward>, graphAddOn: Reward): Reward {
            return backingList.firstOrNull { it.id() == graphAddOn.id() }?.let {
                val update = Pair(it.quantity() ?: 0, it.id())
                if (update.first > 0)
                    updateQuantityById(update)
                return@let it
            } ?: graphAddOn
        }

        private fun getBackingFromProjectData(pData: ProjectData?) = pData?.project()?.backing()
                ?: pData?.backing()

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
            val filteredAddOns = when (rw.shippingPreference()) {
                Reward.ShippingPreference.UNRESTRICTED.name,
                Reward.ShippingPreference.UNRESTRICTED.toString().toLowerCase() -> {
                    addOns.filter {
                        it.shippingPreferenceType() == Reward.ShippingPreference.UNRESTRICTED || containsLocation(rule, it) || isDigital(it)
                    }
                }
                Reward.ShippingPreference.RESTRICTED.name,
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
                        return@map if (amount == -1) it else it.toBuilder().quantity(amount).build()
                    }

            return Triple(pData, updatedQuantity, rule)
        }

        private fun containsLocation(rule: ShippingRule, reward: Reward): Boolean {
            val idLocations = reward
                    .shippingRules()
                    ?.map {
                        it.location().id()
                    } ?: emptyList()

            return idLocations.contains(rule.location().id())
        }

        // - Inputs
        override fun configureWith(pledgeDataAndReason: Pair<PledgeData, PledgeReason>) = this.pledgeDataAndReason.onNext(pledgeDataAndReason)
        override fun shippingRuleSelected(shippingRule: ShippingRule) = this.shippingRuleSelected.onNext(shippingRule)
        override fun continueButtonPressed() = this.continueButtonPressed.onNext(null)
        override fun quantityPerId(quantityPerId: Pair<Int, Long>) = this.quantityPerId.onNext(quantityPerId)
        override fun retryButtonPressed() = this.retryButtonPressed.onNext(true)

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
        override fun showErrorDialog(): Observable<Boolean> = this.showErrorDialog
    }
}