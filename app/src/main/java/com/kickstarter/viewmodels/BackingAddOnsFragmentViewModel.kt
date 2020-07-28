package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.libs.utils.RewardUtils
import com.kickstarter.libs.utils.RewardUtils.isDigital
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
import rx.subjects.PublishSubject

class BackingAddOnsFragmentViewModel {

    interface Inputs {
        /** Configure with the current [ProjectData] and [Reward].
         * @param projectData we get the Project for currency
         */
        fun configureWith(pledgeDataAndReason: Pair<PledgeData, PledgeReason>)

        /** Call when user selects a shipping location. */
        fun shippingRuleSelected(shippingRule: ShippingRule)

        /** Call when the user updates the quantity for one add-on */
        fun selectedAddonsQuantity(quantity: Int)
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
        fun totalSelectedAddOns(): Observable<String>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<BackingAddOnsFragment>(environment), Outputs, Inputs {
        val inputs = this
        val outputs = this

        private val pledgeDataAndReason = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val shippingRuleSelected = PublishSubject.create<ShippingRule>()
        private val shippingRulesAndProject = PublishSubject.create<Pair<List<ShippingRule>, Project>>()

        private val showPledgeFragment = PublishSubject.create<Pair<PledgeData, PledgeReason>>()
        private val addOnsList = PublishSubject.create<Triple<ProjectData, List<Reward>, ShippingRule>>()
        private val selectedAddOns = PublishSubject.create<Int>()
        private val totalSelectedAddOns = PublishSubject.create<String>()
        private var totalAmount = 0

        private val apolloClient = this.environment.apolloClient()
        private val apiClient = environment.apiClient()
        private val currentConfig = environment.currentConfig()

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

            val reward = pledgeData
                    .map { it.reward() }

            this.pledgeDataAndReason
                    .compose(bindToLifecycle())
                    .subscribe(this.showPledgeFragment)

            val addonsList = project
                    .switchMap { pj -> this.apolloClient.getProjectAddOns(pj.slug()?.let { it }?: "") }
                    .compose(bindToLifecycle())
                    .filter { ObjectUtils.isNotNull(it) }
                    .share()

            val projectAndReward = project
                    .compose<Pair<Project, Reward>>(combineLatestPair(reward))

            val shippingRules = projectAndReward
                    .filter { RewardUtils.isShippable(it.second) }
                    .distinctUntilChanged()
                    .switchMap<ShippingRulesEnvelope> { this.apiClient.fetchShippingRules(it.first, it.second).compose(Transformers.neverError()) }
                    .map { it.shippingRules() }
                    .share()

            shippingRules
                    .compose<Pair<List<ShippingRule>, Project>>(combineLatestPair(project))
                    .compose(bindToLifecycle())
                    .subscribe(this.shippingRulesAndProject)

            shippingRules
                    .filter { it.isNotEmpty() }
                    .compose<Pair<List<ShippingRule>, PledgeReason>>(combineLatestPair(pledgeReason))
                    .filter { it.second == PledgeReason.PLEDGE || it.second == PledgeReason.UPDATE_REWARD }
                    .switchMap { defaultShippingRule(it.first) }
                    .subscribe(this.shippingRuleSelected)

            Observable.combineLatest(addonsList, projectData, this.shippingRuleSelected, reward) { list, pData, rule, rw ->
                return@combineLatest filterAddOnsByLocation(list, pData, rule, rw)
            }
                    .distinctUntilChanged()
                    .compose(bindToLifecycle())
                    .subscribe(this.addOnsList)

            this.selectedAddOns
                    .compose(bindToLifecycle())
                    .subscribe {
                        totalAmount += it
                        this.totalSelectedAddOns.onNext(totalAmount.toString())
                    }
        }

        private fun defaultShippingRule(shippingRules: List<ShippingRule>): Observable<ShippingRule> {
            return this.currentConfig.observable()
                    .map { it.countryCode() }
                    .map { countryCode ->
                        shippingRules.firstOrNull { it.location().country() == countryCode }
                                ?: shippingRules.first()
                    }
        }

        private fun filterAddOnsByLocation(addOns: List<Reward>, pData: ProjectData, rule: ShippingRule, rw: Reward): Triple<ProjectData, List<Reward>, ShippingRule> {
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

            return Triple(pData, filteredAddOns, rule)
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
        override fun selectedAddonsQuantity(quantity: Int) = this.selectedAddOns.onNext(quantity)

        // - Outputs
        @NonNull
        override fun showPledgeFragment(): Observable<Pair<PledgeData, PledgeReason>> = this.showPledgeFragment
        override fun addOnsList() = this.addOnsList
        override fun selectedShippingRule(): Observable<ShippingRule> = this.shippingRuleSelected
        override fun shippingRulesAndProject(): Observable<Pair<List<ShippingRule>, Project>> = this.shippingRulesAndProject
        override fun totalSelectedAddOns(): Observable<String> = this.totalSelectedAddOns
    }
}