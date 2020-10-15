package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.KSString
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.rx.transformers.Transformers.takeWhen
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RewardUtils
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
        private val continueButtonPressed = BehaviorSubject.create<Void>()
        private val isEnabledCTAButton = BehaviorSubject.create<Boolean>()
        private val apolloClient = this.environment.apolloClient()
        private val apiClient = environment.apiClient()
        private val currentConfig = environment.currentConfig()
        val ksString: KSString = this.environment.ksString()

        // - Current addOns selection
        private val totalSelectedAddOns = BehaviorSubject.create(0)
        private val quantityPerId = PublishSubject.create<Pair<Int, Long>>()
        private val currentSelection = BehaviorSubject.create(mutableMapOf<Long, Int>())

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
                        this.currentSelection.value?.clear()
                    }

            val filteredBackingReward = backingReward
                    .compose<Pair<Reward, Boolean>>(combineLatestPair(isSameReward))
                    .filter { it.second }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }
                    .map { it.first }

            val reward = Observable.merge(rewardPledge, filteredBackingReward)

            projectAndReward = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))

            // - If changing rewards do not emmit the backing information
            val backingShippingRule = backing
                    .compose<Pair<Backing, Boolean>>(combineLatestPair(isSameReward))
                    .filter { it.second }
                    .map { it.first }
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
                    .map { filterOutUnAvailableOrEndedExceptIfBacked(it) }
                    .distinctUntilChanged()

            shippingRules
                    .compose<Pair<List<ShippingRule>, Project>>(combineLatestPair(project))
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRulesAndProject)

            shippingRules
                    .filter { it.isNotEmpty() }
                    .compose<Pair<List<ShippingRule>, Reward>>(combineLatestPair(reward))
                    .filter { !isDigital(it.second) && isShippable(it.second) }
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
                    .combineLatest(this.retryButtonPressed.startWith(false), project, this.shippingRuleSelected) {
                        _, pj, shipRule ->

                        val projectSlug = pj.slug() ?: ""
                        val location = shipRule.location()
                        return@combineLatest this.apolloClient
                                .getProjectAddOns(projectSlug, location)
                                .doOnError {
                                    this.showErrorDialog.onNext(true)
                                    this.shippingSelectorIsGone.onNext(true)}
                                .onErrorResumeNext(Observable.empty())
                    }
                    .switchMap { it }
                    .filter { ObjectUtils.isNotNull(it) }
                    .subscribe(addOnsFromGraph)

            val filteredAddOns = Observable.combineLatest(addonsList, projectData, this.shippingRuleSelected, reward) {
                list, pData, rule, rw ->
                return@combineLatest filterByLocation(list, pData, rule, rw)
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
                        calculateTotal(it.second.second)
                    }

            // - .startWith(ShippingRuleFactory.emptyShippingRule()) because we need to trigger this validation every time the AddOns selection changes for digital rewards as well
            val isButtonEnabled = Observable.combineLatest(
                    backingShippingRule.startWith(ShippingRuleFactory.emptyShippingRule()),
                    addOnsFromBacking,
                    this.shippingRuleSelected,
                    this.currentSelection.take(1),
                    this.quantityPerId
            ) {
                backedRule, backedList, actualRule, currentSelection, _ ->
                return@combineLatest isDifferentLocation(backedRule, actualRule) || isDifferentSelection(backedList, currentSelection)
            }
                    .distinctUntilChanged()

            isButtonEnabled
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.isEnabledCTAButton.onNext(it)
                    }

            val updatedPledgeDataAndReason = getUpdatedPledgeData(
                    this.addOnsListFiltered,
                    pledgeData,
                    pledgeReason,
                    reward,
                    this.shippingRuleSelected,
                    this.currentSelection.take(1),
                    this.continueButtonPressed)

            updatedPledgeDataAndReason
                    .compose<Pair<PledgeData, PledgeReason>>(takeWhen(this.continueButtonPressed))
                    .compose(bindToLifecycle())
                    .subscribe {
                        this.lake.trackAddOnsContinueButtonClicked(it.first)
                        this.showPledgeFragment.onNext(it)
                    }
        }

        /**
         * Updates the pledgeData object if necessary. This observable should
         * emit only once, when the user press the continue button. As we will update
         * the selected quantity into the concrete items.
         *
         * @return updatedPledgeData depending on the selected shipping rule,
         * if any addOn has been selected
         */
        private fun getUpdatedPledgeData(
                filteredList: Observable<Triple<ProjectData, List<Reward>, ShippingRule>>,
                pledgeData: Observable<PledgeData>,
                pledgeReason: Observable<PledgeReason>,
                reward: Observable<Reward>,
                shippingRule: Observable<ShippingRule>,
                currentSelection: Observable<MutableMap<Long, Int>>,
                continueButtonPressed: Observable<Void>
        ): Observable<Pair<PledgeData, PledgeReason>> {
            return Observable.combineLatest(filteredList, pledgeData, pledgeReason, reward, shippingRule, currentSelection, continueButtonPressed) {
                listAddOns, pledgeData, pledgeReason, rw, shippingRule, currentSelection, _ ->

                val updatedList = updateQuantity(listAddOns.second, currentSelection)
                val selectedAddOns = getSelectedAddOns(updatedList)

                val updatedPledgeData = updatePledgeData(selectedAddOns, rw, pledgeData, shippingRule)
                return@combineLatest Pair(updatedPledgeData, pledgeReason)
            }
        }

        /**
         * Updated list filtering out the addOns with quantity higher than 1
         * @return selected addOns
         */
        private fun getSelectedAddOns(updatedList: List<Reward>): List<Reward> {
            return updatedList.filter { addOn ->
                addOn.quantity()?.let { it > 0 } ?: false
            }
        }

        /**
         * Update the pledgeData according to:
         * - The user has selected addOns, the reward is digital or shippable
         *
         * @param finalList
         * @param rw
         * @param pledgeData
         * @param shippingRule
         *
         * @return pledgeData
         */
        private fun updatePledgeData(finalList: List<Reward>, rw: Reward, pledgeData: PledgeData, shippingRule: ShippingRule) =
                if (finalList.isNotEmpty()) {
                    if (isShippable(rw) && !isDigital(rw)) {
                        pledgeData.toBuilder()
                            .addOns(finalList as java.util.List<Reward>)
                            .shippingRule(shippingRule)
                            .build()
                    } else pledgeData.toBuilder()
                        .addOns(finalList as java.util.List<Reward>)
                        .build()
                } else {
                    pledgeData.toBuilder()
                        .build()
                }

        /**
         *  Update the items in the list with the current selected amount.
         *
         *  This function should be called when the user hits the button
         *  either to continue or skip addOns.
         *  We update the amount at this point in order to avoid re-build the
         *  entire list every time the selection for some concrete addOns change,
         *  which leads to re-triggering all the subscriptions.
         *
         *  @param addOnsList -> actual addOns list
         *  @param currentSelection -> current selection of addOns
         */
        private fun updateQuantity(addOnsList: List<Reward>, currentSelection: MutableMap<Long, Int>): List<Reward> =
            addOnsList.map { addOn ->
                if (currentSelection.containsKey(addOn.id())) {
                    return@map addOn.toBuilder().quantity(currentSelection[addOn.id()]).build()
                } else return@map addOn
            }

        /**
         *  In case selecting the same reward, if any of the addOns is unavailable or
         *  has an invalid time range but has been backed do NOT filter out that addOn
         *  and allow to modify the selection.
         *
         *  In case selecting another reward or new pledge, filter out the unavailable/invalid time range ones
         *
         *  @param combinedList -> combinedList of Graph addOns and backed ones
         *  @return List<Reward> -> filtered list depending on availability and time range if new pledge
         *  @return List<Reward> -> not filtered if the addOn item was previously backed
         */
        private fun filterOutUnAvailableOrEndedExceptIfBacked(combinedList: List<Reward>): List<Reward> {
            return combinedList.filter { addOn ->
                addOn.quantity()?.let { it > 0 } ?: (addOn.isAvailable && RewardUtils.isValidTimeRange(addOn))
            }
        }

        /**
         *  Extract the ID:quantity from the original baked AddOns list
         *  in case the ID's of those addOns and the quantity are the same
         *  as the current selected ones the selection is the same as the
         *  backed one.
         *
         *  @param backedList -> addOns list from backing object
         *  @param currentSelection -> map holding addOns selection, on first load if backed addOns
         *  will hold the id and amount by id.
         *  @return Boolean -> true in case different selection or new item selected false otherwise
         */
        private fun isDifferentSelection(backedList: List<Reward>, currentSelection: MutableMap<Long, Int>): Boolean {

            val backedSelection: MutableMap<Long, Int> = mutableMapOf()
            backedList
                    .map {
                        backedSelection.put(it.id(), it.quantity() ?: 0)
                    }

            val isBackedItemList = currentSelection.map { item ->
                if (backedSelection.containsKey(item.key)) backedSelection[item.key] == item.value
                else false
            }

            val isNewItemSelected = currentSelection.map { item ->
                if (!backedSelection.containsKey(item.key)) item.value > 0
                else false
            }.any { it }

            val sameSelection = isBackedItemList.filter { it }.size == backedSelection.size

            return !sameSelection || isNewItemSelected
        }

        private fun isDifferentLocation(backedRule: ShippingRule, actualRule: ShippingRule) =
                backedRule.location().id() != actualRule.location().id()

        private fun calculateTotal(list: List<Reward>) =
            this.currentSelection
                    .take(1)
                    .subscribe { map ->
                        var total = 0
                        list.map { total += map[it.id()] ?: 0 }
                        this.totalSelectedAddOns.onNext(total)
                    }

        private fun joinSelectedWithAvailableAddOns(backingList: List<Reward>, graphList: List<Reward>): List<Reward> {
            return graphList
                    .map { graphAddOn ->
                        modifyIfBacked(backingList, graphAddOn)
                    }
        }

        /**
         *  If the addOn is previously backed, return the backedAddOn containing
         *  in the field quantity the amount of backed addOns.
         *  Modify it to hold the shippingRules from the graphAddOn, that information
         *  is not available in backing -> addOns graphQL schema.
         */
        private fun modifyIfBacked(backingList: List<Reward>, graphAddOn: Reward): Reward {
            return backingList.firstOrNull { it.id() == graphAddOn.id() }?.let {
                return@let it.toBuilder().shippingRules(graphAddOn.shippingRules()).build()
            }?: graphAddOn
        }

        private fun getBackingFromProjectData(pData: ProjectData?) = pData?.project()?.backing()
                ?: pData?.backing()

        private fun updateQuantityById(updated: Pair<Int, Long>) =
            this.currentSelection
                    .take(1)
                    .subscribe { selection ->
                        selection[updated.second] = updated.first
                    }


        private fun defaultShippingRule(shippingRules: List<ShippingRule>): Observable<ShippingRule> {
            return this.currentConfig.observable()
                    .map { it.countryCode() }
                    .map { countryCode ->
                        shippingRules.firstOrNull { it.location().country() == countryCode }
                                ?: shippingRules.first()
                    }
        }

        // - This will disappear when the query is ready in the backend [CT-649]
        private fun filterByLocation(addOns: List<Reward>, pData: ProjectData, rule: ShippingRule, rw: Reward): Triple<ProjectData, List<Reward>, ShippingRule> {
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

            return Triple(pData, filteredAddOns, rule)
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