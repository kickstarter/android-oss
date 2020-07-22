package com.kickstarter.viewmodels

import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.*
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.mock.services.MockApolloClient
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class BackingAddOnsFragmentViewModelTest: KSRobolectricTestCase() {
    private lateinit var vm: BackingAddOnsFragmentViewModel.ViewModel
    private val addOnsList = TestSubscriber.create<Pair<ProjectData, List<Reward>>>()

    private fun setUpEnvironment(@NonNull environment: Environment) {
        this.vm = BackingAddOnsFragmentViewModel.ViewModel(environment)
        this.vm.outputs.addOnsList().subscribe(this.addOnsList)
    }

    @Test
    fun emptyAddOnsListForReward() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(emptyList(), ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig))

        val rw = RewardFactory
                .rewardHasAddOns()
                .toBuilder()
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED)
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertNoValues()
    }

    @Test
    fun addOnsForUnrestrictedSameShippingRules() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Pair(projectData,listAddons))
    }

    @Test
    fun addOnsForRestrictedSameShippingRules() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .shippingRules(shippingRule.shippingRules())
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Pair(projectData,listAddons))
    }

    @Test
    fun addOnsForRestrictedNoMatchingShippingRules() {
        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
                .shippingRules(listOf(shippingRuleAddOn,shippingRuleAddOn,shippingRuleAddOn ))
                .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
                .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
                .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
                .shippingRules(listOf(shippingRuleRw))
                .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
                .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
                .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        this.vm.arguments(bundle)

        this.addOnsList.assertValue(Pair(projectData, emptyList()))
    }

    @Test
    fun addOnsForRestrictedOneMatchingShippingRules() {

    }

    @Test
    fun addOnsForRestrictedChangeSelectedShippingRule() {

    }

    @Test
    fun addOnsForDigitalNoShipping(){

    }


    private fun buildEnvironmentWith(addOns: List<Reward>, shippingRule: ShippingRulesEnvelope, currentConfig: MockCurrentConfig): Environment {

         return environment()
                .toBuilder()
                .apolloClient(object : MockApolloClient() {
                    override fun getProjectAddOns(slug: String): Observable<List<Reward>> {
                        return Observable.just(addOns)
                    }
                })
                 .apiClient(object  : MockApiClient() {
                     override fun fetchShippingRules(project: Project, reward: Reward): Observable<ShippingRulesEnvelope> {
                         return Observable.just(shippingRule)
                     }
                 })
                 .currentConfig(currentConfig)
                .build()
    }
}