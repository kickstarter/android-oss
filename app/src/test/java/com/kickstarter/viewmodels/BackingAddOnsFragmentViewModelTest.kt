package com.kickstarter.viewmodels

import android.os.Bundle
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.factories.ShippingRulesEnvelopeFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Location
import com.kickstarter.models.Reward
import com.kickstarter.models.ShippingRule
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.ArgumentsKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.BackingAddOnsFragmentViewModel.BackingAddOnsFragmentViewModel
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import junit.framework.TestCase
import org.junit.After
import org.junit.Test

class BackingAddOnsFragmentViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: BackingAddOnsFragmentViewModel
    private val shippingSelectorIsGone = TestSubscriber.create<Boolean>()
    private val addOnsList = TestSubscriber.create<Triple<ProjectData, List<Reward>, ShippingRule>>()
    private val showPledgeFragment = TestSubscriber.create<Pair<PledgeData, PledgeReason>>()
    private val isEnabledButton = TestSubscriber.create<Boolean>()
    private val totalSelectedAddOns = TestSubscriber.create<Int>()
    private val isEmptyState = TestSubscriber.create<Boolean>()
    private val showErrorDialog = TestSubscriber.create<Boolean>()
    private val selectedShippingRule = TestSubscriber.create<ShippingRule>()
    private val disposables = CompositeDisposable()

    @After
    fun cleanUp() {
        disposables.clear()
    }
    private fun setUpEnvironment(@NonNull environment: Environment, bundle: Bundle? = null) {
        this.vm = BackingAddOnsFragmentViewModel(environment, bundle)
        this.vm.outputs.addOnsList().subscribe { this.addOnsList.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.shippingSelectorIsGone().subscribe { this.shippingSelectorIsGone.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showPledgeFragment().subscribe { this.showPledgeFragment.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.isEnabledCTAButton().subscribe { this.isEnabledButton.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.totalSelectedAddOns().subscribe { this.totalSelectedAddOns.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.isEmptyState().subscribe { this.isEmptyState.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showErrorDialog().subscribe { this.showErrorDialog.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.selectedShippingRule().subscribe { this.selectedShippingRule.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun emptyAddOnsListForReward() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

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

        setUpEnvironment(buildEnvironmentWith(emptyList(), ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig), bundle)

        this.addOnsList.assertValue(Triple(projectData, emptyList(), ShippingRuleFactory.emptyShippingRule()))
        this.isEmptyState.assertValue(true)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
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
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

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

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig), bundle)
        this.addOnsList.assertValue(Triple(projectData, listAddons, shippingRule.shippingRules().first()))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
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
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

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
        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig), bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, shippingRule.shippingRules().first()))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun addOnsForRestricted_whenNoMatchingShippingRulesInReward() {
        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(listOf(shippingRuleAddOn, shippingRuleAddOn, shippingRuleAddOn))
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
            .shippingType(Reward.SHIPPING_TYPE_ANYWHERE)
            .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingRules(listOf(shippingRuleRw))
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 check this field
            .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS) // - Reward from V1 to check is digital use this field
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig), bundle)

        this.addOnsList.assertValue(Triple(projectData, emptyList(), shippingRuleRw))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun addOnsForRestrictedOneMatchingShippingRules() {
        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(listOf(shippingRuleAddOn, shippingRuleRw))
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

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

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig), bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, shippingRuleRw))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun addOnsForRestrictedFilterOutNoMatching() {
        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(listOf(shippingRuleRw))
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()
        val addOn2 = RewardFactory.rewardHasAddOns().toBuilder()
            .id(11)
            .shippingRules(listOf(shippingRuleAddOn))
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()
        val listAddons = listOf(addOn, addOn2, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
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
        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig), bundle)

        this.vm.outputs.addOnsList().subscribe {
            assertEquals(it.second.size, 1)
            val filteredAddOn = it.second.first()
            assertEquals(filteredAddOn, addOn2)
        }
            .addToDisposable(disposables)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun addOnsForRestricted_NoDigitalAddOn_ChangeSelectedShippingRule() {
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(listOf(shippingRuleRw))
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
            .shippingType(Reward.SHIPPING_TYPE_SINGLE_LOCATION)
            .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

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
        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig), bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, shippingRuleRw))

        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        this.vm.inputs.shippingRuleSelected(shippingRuleAddOn)

        this.addOnsList.assertValues(Triple(projectData, listAddons, shippingRuleRw), Triple(projectData, emptyList(), shippingRuleAddOn))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testDigitalAddOns_whenDigitalReward() {

        // DIGITAL AddOns
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING) // - // - Reward from V1 use this field
            .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        // - Digital Reward
        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.NOSHIPPING.name.toLowerCase())
            .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING) // - Reward from V1 use this field
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig), bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, ShippingRuleFactory.emptyShippingRule()))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testDigitalAddOns_whenLocalReceiptReward() {

        // DIGITAL AddOns
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING) // - // - Reward from V1 use this field
            .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        // - LocalReceipt Reward
        val rw = RewardFactory.localReceiptLocation().toBuilder()
            .hasAddons(true)
            .shippingType(Reward.ShippingPreference.LOCAL.name.toLowerCase())
            .shippingPreferenceType(Reward.ShippingPreference.LOCAL) // - Reward from GraphQL use this field
            .shippingType(Reward.SHIPPING_TYPE_LOCAL_PICKUP) // - Reward from V1 use this field
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig), bundle)

        this.shippingSelectorIsGone.assertValues(true)
        this.addOnsList.assertValue(Triple(projectData, listAddons, ShippingRuleFactory.emptyShippingRule()))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testDigitalAddOnsAndLocalReceipt_whenLocalReceiptReward() {

        // - LocalReceipt Reward
        val rw = RewardFactory.localReceiptLocation().toBuilder()
            .hasAddons(true)
            .build()

        // DIGITAL AddOn
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING) // - // - Reward from V1 use this field
            .build()

        val localReceipAddOn = RewardFactory.localReceiptLocation().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .build()

        val listAddons = listOf(addOn, addOn, addOn, localReceipAddOn, localReceipAddOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig))

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig), bundle)

        this.shippingSelectorIsGone.assertValues(true)
        this.addOnsList.assertValue(Triple(projectData, listAddons, ShippingRuleFactory.emptyShippingRule()))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testFilterOutShippableAddOns_whenLocalReceiptReward() {

        // - LocalReceipt Reward
        val rw = RewardFactory.localReceiptLocation().toBuilder()
            .hasAddons(true)
            .build()

        // DIGITAL AddOn
        val digitalAddOn = RewardFactory.addOn().toBuilder()
            .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING) // - // - Reward from V1 use this field
            .build()

        val localReceipAddOn = RewardFactory.localReceiptLocation().toBuilder()
            .isAddOn(true)
            .isAvailable(true)
            .build()

        val shippableAddOn = RewardFactory.addOn().toBuilder()
            .shippingRules(listOf(ShippingRuleFactory.usShippingRule(), ShippingRuleFactory.germanyShippingRule()))
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED)
            .shippingType(Reward.SHIPPING_TYPE_ANYWHERE)
            .build()

        val listAddons = listOf(shippableAddOn, digitalAddOn, shippableAddOn, localReceipAddOn, shippableAddOn)
        val outputTestList = listOf(digitalAddOn, localReceipAddOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig))

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig), bundle)

        this.shippingSelectorIsGone.assertValues(true)
        this.addOnsList.assertValue(Triple(projectData, outputTestList, ShippingRuleFactory.emptyShippingRule()))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testShippingSelectorGone_WhenNoAddOns_Shippable() {
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING)
            .shippingPreferenceType(Reward.ShippingPreference.NOSHIPPING) // - Reward from GraphQL use this field
            .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING)
            .shippingRules(listOf(shippingRuleRw))
            .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.NOSHIPPING.name.toLowerCase()) // - Reward from V1 use this field
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig), bundle)

        this.shippingSelectorIsGone.assertValues(true)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun testShippingSelectorGoneWhenBaseRewardIsNotShippable() {
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
            .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig))

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING)
            .shippingRules(listOf(shippingRuleRw))
            .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.NOSHIPPING.name.toLowerCase()) // - Reward from V1 use this field
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig), bundle)

        this.shippingSelectorIsGone.assertValues(true)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun continueButtonPressedNoAddOnsSelected() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val rw = RewardFactory
            .rewardHasAddOns()
            .toBuilder()
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name)
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED)
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)
        val pledgeData = PledgeData.with(pledgeReason, projectData, rw)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, pledgeData)
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        setUpEnvironment(buildEnvironmentWith(emptyList(), ShippingRulesEnvelopeFactory.emptyShippingRules(), currentConfig), bundle)

        val quantityPerId = Pair(0, rw.id())
        this.vm.inputs.quantityPerId(quantityPerId)
        this.vm.inputs.continueButtonPressed()

        this.addOnsList.assertValue(Triple(projectData, emptyList(), ShippingRuleFactory.emptyShippingRule()))
        this.vm.outputs.showPledgeFragment()
            .subscribe {
                assertEquals(it.first, pledgeData)
                assertEquals(it.second, pledgeReason)
            }
            .addToDisposable(disposables)

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun continueButtonPressedAddOnsFewAddOnsSelected() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()
        val addOn2 = addOn.toBuilder().id(8).build()
        val addOn3 = addOn.toBuilder().id(99).build()
        val listAddons = listOf(addOn, addOn2, addOn3)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)
        val pledgeData = PledgeData.with(pledgeReason, projectData, rw)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig), bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, shippingRule.shippingRules().first()))

        val quantityPerIdAddOn1 = Pair(7, addOn.id())
        val quantityPerIdAddOn2 = Pair(2, addOn2.id())
        val quantityPerIdAddOn3 = Pair(5, addOn3.id())

        this.vm.inputs.quantityPerId(quantityPerIdAddOn1)
        this.vm.inputs.quantityPerId(quantityPerIdAddOn2)
        this.vm.inputs.quantityPerId(quantityPerIdAddOn3)

        // - Comparison purposes the quantity of the add-ons has been updated in the previous vm.input.quantityPerId
        val listAddonsToCheck = listOf(
            addOn.toBuilder().quantity(7).build(),
            addOn2.toBuilder().quantity(2).build(),
            addOn3.toBuilder().quantity(5).build()
        )

        this.addOnsList.assertValues(Triple(projectData, listAddons, shippingRule.shippingRules().first()))
        this.totalSelectedAddOns.assertValues(0, 7, 9, 14)

        this.vm.inputs.continueButtonPressed()
        // - value only when updating pledge
        this.isEnabledButton.assertNoValues()

        this.vm.outputs.showPledgeFragment()
            .subscribe {
                assertEquals(it.first, pledgeData)
                assertEquals(it.second, pledgeReason)

                val selectedAddOnsList = pledgeData.addOns()
                assertEquals(selectedAddOnsList, listAddonsToCheck)
            }
            .addToDisposable(disposables)

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun givenBackedAddOns_whenUpdatingRewardReason_DisabledButton() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()
        val addOn2 = addOn.toBuilder().id(8).build()
        val addOn3 = addOn.toBuilder().id(99).build()
        val listAddons = listOf(addOn, addOn2, addOn3)
        val listAddonsBacked = listOf(addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())
        val combinedList = listOf(addOn, addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()

        // -Build the backing with location and list of AddOns
        val backing = BackingFactory.backing(project, UserFactory.user(), rw)
            .toBuilder()
            .locationId(ShippingRuleFactory.usShippingRule().location()?.id())
            .location(ShippingRuleFactory.usShippingRule().location())
            .addOns(listAddonsBacked)
            .build()
        val backedProject = project.toBuilder()
            .backing(backing)
            .build()

        val projectData = ProjectDataFactory.project(backedProject, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig), bundle)
        // - input from ViewHolder when building the item with the backed info
        this.vm.inputs.quantityPerId(Pair(2, addOn2.id()))
        this.vm.inputs.quantityPerId(Pair(1, addOn3.id()))

        this.isEnabledButton.assertValues(true, false)
        this.addOnsList.assertValue(Triple(projectData, combinedList, shippingRule.shippingRules().first()))

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun givenBackedAddOns_whenUpdatingRewardIncreaseQuantity_EnabledButtonAndResultList() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()

        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()
        val addOn2 = addOn.toBuilder().id(8).build()
        val addOn3 = addOn.toBuilder().id(99).build()

        val listAddons = listOf(addOn, addOn2, addOn3)
        val listAddonsBacked = listOf(addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())
        val combinedList = listOf(addOn, addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field.
            .shippingType(Reward.SHIPPING_TYPE_ANYWHERE) // - Reward from V1 use this field to check if is Digital
            .build()
        val project = ProjectFactory.project()

        // -Build the backing with location and list of AddOns
        val backing = BackingFactory.backing(project, UserFactory.user(), rw)
            .toBuilder()
            .locationId(ShippingRuleFactory.usShippingRule().location()?.id())
            .location(ShippingRuleFactory.usShippingRule().location())
            .addOns(listAddonsBacked)
            .build()
        val backedProject = project.toBuilder()
            .rewards(listOf(rw))
            .backing(backing)
            .build()

        val projectData = ProjectDataFactory.project(backedProject, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig), bundle)
        this.vm.inputs.quantityPerId(Pair(7, addOn3.id()))
        this.vm.inputs.quantityPerId(Pair(2, addOn2.id()))

        this.isEnabledButton.assertValues(true)
        this.addOnsList.assertValues(Triple(projectData, combinedList, shippingRule.shippingRules().first()))

        // - Always 0 first time, them summatory of all addOns quantity every time the list gets updated
        this.totalSelectedAddOns.assertValues(0, 7, 9)

        this.vm.inputs.continueButtonPressed()
        this.vm.outputs.showPledgeFragment()
            .subscribe {
                val updateList = listOf(addOn, addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(7).build())
                TestCase.assertEquals(it.first.addOns(), updateList)
            }
            .addToDisposable(disposables)

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun givenBackedAddOns_whenUpdatingRewardUnrestrictedChooseAnotherRewardDigital_AddOnsListNotBacked() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()

        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()
        val addOn2 = addOn.toBuilder().id(8).build()
        val addOn3 = addOn.toBuilder().id(99).build()

        val listAddons = listOf(addOn, addOn2, addOn3)
        val listAddonsBacked = listOf(addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val backedRw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase())
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.UNRESTRICTED.name.toLowerCase()) // - Reward from V1 use this field.
            .shippingType(Reward.SHIPPING_TYPE_ANYWHERE) // - Reward from V1 use this field to check if is Digital
            .build()

        // - Digital Reward
        val newRw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.NOSHIPPING.name.toLowerCase())
            .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING) // - Reward from V1 use this field
            .shippingRules(emptyList())
            .build()

        val project = ProjectFactory.project()

        // -Build the backing with location and list of AddOns
        val backing = BackingFactory.backing(project, UserFactory.user(), backedRw)
            .toBuilder()
            .locationId(ShippingRuleFactory.usShippingRule().location()?.id())
            .location(ShippingRuleFactory.usShippingRule().location())
            .addOns(listAddonsBacked)
            .build()

        val backedProject = project.toBuilder()
            .rewards(listOf(backedRw))
            .backing(backing)
            .build()

        val projectData = ProjectDataFactory.project(backedProject, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, newRw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig), bundle)

        this.shippingSelectorIsGone.assertValue(true)
        this.selectedShippingRule.assertValue(ShippingRuleFactory.emptyShippingRule())
        this.addOnsList.assertValues(Triple(projectData, listAddons, ShippingRuleFactory.emptyShippingRule()))

        // - Always 0 first time, them summatory of all addOns quantity every time the list gets updated
        this.totalSelectedAddOns.assertValues(0)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun givenBackedAddOns_whenUpdatingRewardDigitalChooseAnotherRewardLimited_AddOnsListNotBacked() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()

        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()
        val addOn2 = addOn.toBuilder().id(8).build()
        val addOn3 = addOn.toBuilder().id(99).build()

        val listAddons = listOf(addOn, addOn2, addOn3)
        val listAddonsBacked = listOf(addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val newRw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 use this field.
            .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS) // - Reward from V1 use this field to check if is Digital
            .build()

        // - Digital Reward
        val backedRw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.NOSHIPPING.name.toLowerCase())
            .shippingPreferenceType(Reward.ShippingPreference.NONE) // - Reward from GraphQL use this field
            .shippingType(Reward.SHIPPING_TYPE_NO_SHIPPING) // - Reward from V1 use this field
            .build()
        val project = ProjectFactory.project()

        // -Build the backing with location and list of AddOns
        val backing = BackingFactory.backing(project, UserFactory.user(), backedRw)
            .toBuilder()
            .locationId(ShippingRuleFactory.usShippingRule().location()?.id())
            .location(ShippingRuleFactory.usShippingRule().location())
            .addOns(listAddonsBacked)
            .build()

        val backedProject = project.toBuilder()
            .rewards(listOf(backedRw))
            .backing(backing)
            .build()

        val projectData = ProjectDataFactory.project(backedProject, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, newRw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig), bundle)

        this.shippingSelectorIsGone.assertNoValues()
        this.selectedShippingRule.assertValues(ShippingRuleFactory.emptyShippingRule(), shippingRule.shippingRules().first())
        this.addOnsList.assertValues(Triple(projectData, listAddons, shippingRule.shippingRules().first()))

        // - Always 0 first time, them summatory of all addOns quantity every time the list gets updated
        this.totalSelectedAddOns.assertValues(0)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun givenBackedAddOns_whenUpdatingSameReward_ChangeShippingRule() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()

        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()
        val addOn2 = addOn.toBuilder().id(8).build()
        val addOn3 = addOn.toBuilder().id(99).build()

        val listAddons = listOf(addOn, addOn2, addOn3)
        val listAddonsBacked = listOf(addOn2.toBuilder().quantity(2).build(), addOn3.toBuilder().quantity(1).build())

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        // - Backed Reward
        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED) // - Reward from GraphQL use this field
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase()) // - Reward from V1 use this field.
            .shippingType(Reward.SHIPPING_TYPE_MULTIPLE_LOCATIONS) // - Reward from V1 use this field to check if is Digital
            .build()

        val project = ProjectFactory.project()

        // -Build the backing with location and list of AddOns
        val backing = BackingFactory.backing(project, UserFactory.user(), rw)
            .toBuilder()
            .locationId(ShippingRuleFactory.usShippingRule().location()?.id())
            .location(ShippingRuleFactory.usShippingRule().location())
            .addOns(listAddonsBacked)
            .build()

        val backedProject = project.toBuilder()
            .rewards(listOf(rw))
            .backing(backing)
            .build()

        val projectData = ProjectDataFactory.project(backedProject, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.UPDATE_REWARD)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.UPDATE_REWARD)

        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig), bundle)

        this.shippingSelectorIsGone.assertNoValues()
        this.selectedShippingRule.assertValues(ShippingRuleFactory.emptyShippingRule(), shippingRule.shippingRules().first())

        // - Change shippingRule
        this.vm.inputs.shippingRuleSelected(ShippingRuleFactory.mexicoShippingRule())

        // - Test asserts
        this.selectedShippingRule.assertValues(
            ShippingRuleFactory.emptyShippingRule(),
            shippingRule.shippingRules().first(),
            ShippingRuleFactory.mexicoShippingRule()
        )

        this.vm.inputs.continueButtonPressed()

        this.vm.outputs.showPledgeFragment().subscribe {
            val shippingRuleSendToPledge = it.first.shippingRule()
            TestCase.assertEquals(shippingRuleSendToPledge, ShippingRuleFactory.mexicoShippingRule())
        }
            .addToDisposable(disposables)

        this.segmentTrack.assertValues(EventName.PAGE_VIEWED.eventName, EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun emptyState_whenNoAddOnsForShippingRule_shouldShowEmptyViewState() {
        val shippingRuleAddOn = ShippingRuleFactory.germanyShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(listOf(shippingRuleAddOn, shippingRuleAddOn, shippingRuleAddOn))
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
            .shippingType(Reward.SHIPPING_TYPE_SINGLE_LOCATION)
            .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
            .shippingRules(listOf(shippingRuleRw))
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig), bundle)

        this.addOnsList.assertValue(Triple(projectData, emptyList(), shippingRuleRw))
        this.isEmptyState.assertValue(true)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun emptyState_whenMatchingShippingRule_shouldNotShowEmptyState() {
        val shippingRuleAddOn = ShippingRuleFactory.usShippingRule()
        val shippingRuleRw = ShippingRuleFactory.usShippingRule()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(listOf(shippingRuleAddOn, shippingRuleAddOn, shippingRuleAddOn))
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
            .build()
        val listAddons = listOf(addOn, addOn, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .shippingType(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
            .shippingRules(listOf(shippingRuleRw))
            .shippingPreferenceType(Reward.ShippingPreference.RESTRICTED)
            .shippingPreference(Reward.ShippingPreference.RESTRICTED.name.toLowerCase())
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        setUpEnvironment(buildEnvironmentWith(listAddons, ShippingRulesEnvelope.builder().shippingRules(listOf(shippingRuleRw)).build(), currentConfig), bundle)

        this.addOnsList.assertValue(Triple(projectData, listAddons, shippingRuleRw))
        this.isEmptyState.assertValue(false)

        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun errorState_whenErrorReturned_shouldShowErrorAlertDialogAndHideShippingSelector() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val rw = RewardFactory.rewardHasAddOns().toBuilder()
            .build()

        val project = ProjectFactory.project().toBuilder().rewards(listOf(rw)).build()
        val projectData = ProjectDataFactory.project(project, null, null)
        val pledgeReason = PledgeFlowContext.forPledgeReason(PledgeReason.PLEDGE)

        val bundle = Bundle()
        bundle.putParcelable(ArgumentsKey.PLEDGE_PLEDGE_DATA, PledgeData.with(pledgeReason, projectData, rw))
        bundle.putSerializable(ArgumentsKey.PLEDGE_PLEDGE_REASON, PledgeReason.PLEDGE)
        setUpEnvironment(buildEnvironmentWithError(currentConfig), bundle)

        // Two values -> two failed network calls
        this.showErrorDialog.assertValues(true, true)
        this.shippingSelectorIsGone.assertValues(true, true, true)
    }

    fun addOnsList_whenUnavailable_FilteredOut() {
        val shippingRule = ShippingRulesEnvelopeFactory.shippingRules()
        val addOn = RewardFactory.addOn().toBuilder()
            .shippingRules(shippingRule.shippingRules())
            .shippingPreferenceType(Reward.ShippingPreference.UNRESTRICTED) // - Reward from GraphQL use this field
            .build()

        val addOns2 = addOn.toBuilder().isAvailable(false).build()
        val listAddons = listOf(addOn, addOns2, addOn)

        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

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
        setUpEnvironment(buildEnvironmentWith(listAddons, shippingRule, currentConfig))

        val filteredList = listOf(addOn, addOn)
        this.addOnsList.assertValue(Triple(projectData, filteredList, shippingRule.shippingRules().first()))
    }

    private fun buildEnvironmentWithError(currentConfig: MockCurrentConfigV2): Environment {

        return environment()
            .toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getProjectAddOns(slug: String, location: Location): Observable<List<Reward>> {
                    return Observable.error(ApiExceptionFactory.badRequestException())
                }
                override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
                    return Observable.error(ApiExceptionFactory.badRequestException())
                }
            })
            .currentConfig2(currentConfig)
            .build()
    }

    private fun buildEnvironmentWith(addOns: List<Reward>, shippingRule: ShippingRulesEnvelope, currentConfig: MockCurrentConfigV2): Environment {
        return environment()
            .toBuilder()
            .apolloClientV2(object : MockApolloClientV2() {
                override fun getProjectAddOns(slug: String, location: Location): Observable<List<Reward>> {
                    return Observable.just(addOns)
                }

                override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
                    return Observable.just(shippingRule)
                }
            })
            .currentConfig2(currentConfig)
            .build()
    }
}
