package com.kickstarter.viewmodels

import android.content.SharedPreferences
import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfigV2
import com.kickstarter.mock.MockFeatureFlagClient
import com.kickstarter.mock.factories.BackingFactory
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectDataFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.factories.ShippingRulesEnvelopeFactory
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.mock.services.MockApolloClientV2
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import com.kickstarter.ui.SharedPreferenceKey
import com.kickstarter.ui.data.PledgeData
import com.kickstarter.ui.data.PledgeFlowContext
import com.kickstarter.ui.data.PledgeReason
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.viewmodels.RewardsFragmentViewModel.Factory
import com.kickstarter.viewmodels.RewardsFragmentViewModel.RewardsFragmentViewModel
import com.kickstarter.viewmodels.usecases.GetShippingRulesUseCase
import com.kickstarter.viewmodels.usecases.ShippingRulesState
import com.kickstarter.viewmodels.usecases.TPEventInputData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.joda.time.DateTime
import org.junit.After
import org.junit.Test
import org.mockito.Mockito

class RewardsFragmentViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: RewardsFragmentViewModel
    private val backedRewardPosition = TestSubscriber.create<Int>()
    private val projectData = TestSubscriber.create<ProjectData>()
    private val showPledgeFragment = TestSubscriber<Pair<PledgeData, PledgeReason>>()
    private val showAddOnsFragment = TestSubscriber<Pair<PledgeData, PledgeReason>>()
    private val showAlert = TestSubscriber<Pair<PledgeData, PledgeReason>>()

    private val disposables = CompositeDisposable()
    private fun setUpEnvironment(
        @NonNull environment: Environment,
        useCase: GetShippingRulesUseCase? = null
    ) {
        this.vm = Factory(environment, useCase).create(RewardsFragmentViewModel::class.java)
        this.vm.outputs.backedRewardPosition().subscribe { this.backedRewardPosition.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.projectData().subscribe { this.projectData.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showPledgeFragment().subscribe { this.showPledgeFragment.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showAddOnsFragment().subscribe { this.showAddOnsFragment.onNext(it) }.addToDisposable(disposables)
        this.vm.outputs.showAlert().subscribe { this.showAlert.onNext(it) }.addToDisposable(disposables)
    }

    @After
    fun cleanUp() {
        disposables.clear()
    }
    @Test
    fun init_whenViewModelInstantiated_shouldSendPagedViewedEventToClient() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        this.vm.isExpanded(true)
        this.segmentTrack.assertValue(EventName.PAGE_VIEWED.eventName)
    }

    @Test
    fun init_whenViewModelInstantiated_shouldThirdPartyEvent() {
        val mockFeatureFlagClient: MockFeatureFlagClient =
            object : MockFeatureFlagClient() {
                override fun getBoolean(FlagKey: FlagKey): Boolean {
                    return true
                }
            }

        var sharedPreferences: SharedPreferences = Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPreferences.getBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false)).thenReturn(true)

        val environment = environment()
            .toBuilder()
            .featureFlagClient(mockFeatureFlagClient)
            .sharedPreferences(sharedPreferences)
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .apolloClientV2(object : MockApolloClientV2() {
                override fun triggerThirdPartyEvent(eventInput: TPEventInputData): Observable<Pair<Boolean, String>> {
                    return Observable.just(Pair(true, ""))
                }
            })
            .build()

        val project = ProjectFactory.project()
        setUpEnvironment(environment)

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        this.vm.isExpanded(true)

        assertTrue(this.vm.onThirdPartyEventSent.value!!)
    }

    @Test
    fun init_whenViewModelInstantiated_FragmentCollapsed_shouldNotSendPagedViewedEvent() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        this.vm.isExpanded(false)
        this.segmentTrack.assertNoValues()
    }

    @Test
    fun testBackedRewardPosition() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))
        this.backedRewardPosition.assertNoValues()

        val reward = RewardFactory.reward()
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(
                BackingFactory.backing()
                    .toBuilder()
                    .rewardId(reward.id())
                    .build()
            )
            .rewards(listOf(RewardFactory.noReward(), reward))
            .build()
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))
        this.backedRewardPosition.assertValue(1)

        val backedSuccessfulProject = backedProject
            .toBuilder()
            .state(Project.STATE_SUCCESSFUL)
            .build()
        this.vm.inputs.configureWith(ProjectDataFactory.project(backedSuccessfulProject))
        this.backedRewardPosition.assertValue(1)
    }

    @Test
    fun testProjectData() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        val projectData = ProjectDataFactory.project(project)
        this.vm.inputs.configureWith(projectData)
        this.projectData.assertValue(projectData)
    }

    @Test
    fun testShowPledgeFragment_whenBackingProject() {
        val project = ProjectFactory.project()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        val reward = RewardFactory.reward().toBuilder().hasAddons(false).build()
        this.vm.inputs.rewardClicked(reward)
        this.showPledgeFragment.assertValue(
            Pair(
                PledgeData.builder()
                    .pledgeFlowContext(PledgeFlowContext.NEW_PLEDGE)
                    .reward(reward)
                    .projectData(ProjectDataFactory.project(project))
                    .build(),
                PledgeReason.PLEDGE
            )
        )
        this.showAddOnsFragment.assertNoValues()
    }

    @Test
    fun testShowAlert_whenBackingProject_withAddOns_sameReward() {
        val reward = RewardFactory.rewardWithShipping().toBuilder().hasAddons(true).build()
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(
                BackingFactory.backing()
                    .toBuilder()
                    .reward(reward)
                    .rewardId(reward.id())
                    .build()
            )
            .rewards(listOf(RewardFactory.noReward(), reward))
            .build()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.vm.inputs.rewardClicked(reward)
        this.showPledgeFragment.assertNoValues()
        this.vm.outputs.showAddOnsFragment().subscribe {
            assertEquals(it.first.reward(), reward)
            assertEquals(it.first.projectData(), ProjectDataFactory.project(backedProject))
            assertEquals(it.second, PledgeReason.UPDATE_REWARD)
        }.addToDisposable(disposables)

        this.showAlert.assertNoValues()
    }

    @Test
    fun testShowAlert_whenBackingProject_withAddOns_otherReward() {
        val rewarda = RewardFactory.rewardWithShipping().toBuilder().id(4).hasAddons(true).build()
        val rewardb = RewardFactory.rewardHasAddOns().toBuilder().id(2).hasAddons(true).build()
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(
                BackingFactory.backing()
                    .toBuilder()
                    .reward(rewardb)
                    .rewardId(rewardb.id())
                    .build()
            )
            .rewards(listOf(RewardFactory.noReward(), rewarda, rewardb))
            .build()
        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))

        this.vm.inputs.rewardClicked(rewarda)
        this.showPledgeFragment.assertNoValues()
        this.showAddOnsFragment.assertNoValues()
        this.showAlert.assertValue(
            Pair(
                PledgeData.builder()
                    .pledgeFlowContext(PledgeFlowContext.CHANGE_REWARD)
                    .reward(rewarda)
                    .projectData(ProjectDataFactory.project(backedProject))
                    .build(),
                PledgeReason.UPDATE_REWARD
            )
        )

        this.vm.inputs.alertButtonPressed()
        this.showAddOnsFragment.assertValue(
            Pair(
                PledgeData.builder()
                    .pledgeFlowContext(PledgeFlowContext.CHANGE_REWARD)
                    .reward(rewarda)
                    .projectData(ProjectDataFactory.project(backedProject))
                    .build(),
                PledgeReason.UPDATE_REWARD
            )
        )
        this.showPledgeFragment.assertNoValues()
    }

    @Test
    fun testFilterOutRewards_whenRewardNotStarted_filtersOutReward() {
        val rwNotLimitedStart = RewardFactory.reward()
        val rwLimitedStartNotStartedYet = rwNotLimitedStart.toBuilder().startsAt(DateTime.now().plusDays(1)).build()
        val rwLimitedStartStarted = rwNotLimitedStart.toBuilder().startsAt(DateTime.now()).build()

        val rewards = listOf<Reward>(rwNotLimitedStart, rwLimitedStartNotStartedYet, rwLimitedStartStarted)

        val project = ProjectFactory.project().toBuilder().rewards(rewards).build()

        setUpEnvironment(environment())
        // - We configure the viewModel with a project that has rewards not started yet
        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        val filteredList = listOf(rwNotLimitedStart, rwLimitedStartStarted)
        val projWithFilteredRewards = project.toBuilder().rewards(filteredList).build()
        val modifiedPData = ProjectData.builder().project(projWithFilteredRewards).build()

        // - We check that the viewModel has filtered out the rewards not started yet
        this.projectData.assertValue(modifiedPData)
    }

    @Test
    fun testFilterAndSortRewards_whenRewardUnavailable_sortsRewardToEnd() {
        val rwNotLimitedStart = RewardFactory.reward()
        val rwLimitedStartNotStartedYet = rwNotLimitedStart.toBuilder().startsAt(DateTime.now().plusDays(1)).build()
        val rwLimitedStartStarted = rwNotLimitedStart.toBuilder().startsAt(DateTime.now()).build()
        val limited = RewardFactory.reward().toBuilder().startsAt(DateTime.now()).limit(5).build()
        val noRemaining = RewardFactory.limitReached()
        val expired = RewardFactory.ended()

        val rewards = listOf<Reward>(
            rwNotLimitedStart,
            rwLimitedStartNotStartedYet,
            noRemaining,
            limited,
            expired,
            rwLimitedStartStarted
        )

        val project = ProjectFactory.project().toBuilder().rewards(rewards).build()

        setUpEnvironment(environment())
        // - We configure the viewModel with a project that has rewards not started yet
        this.vm.inputs.configureWith(ProjectDataFactory.project(project))

        val filteredList = listOf(rwNotLimitedStart, limited, rwLimitedStartStarted, noRemaining, expired)
        val projWithFilteredRewards = project.toBuilder().rewards(filteredList).build()
        val modifiedPData = ProjectData.builder().project(projWithFilteredRewards).build()

        // - We check that the viewModel has filtered out the rewards not started yet
        this.projectData.assertValue(modifiedPData)
    }

    @Test
    fun `test countrySelectorRules state contains appropriate ShippingRules when reward shipping worldwide and default location Canada`() = runTest {

        val unlimitedReward = RewardFactory.rewardWithShipping()

        val rewards = listOf<Reward>(
            unlimitedReward
        )
        val project = ProjectFactory.project().toBuilder().rewards(rewards).build()

        val config = ConfigFactory.configForCA()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val testShippingRulesList = ShippingRulesEnvelopeFactory.shippingRules()
        val apolloClient = object : MockApolloClientV2() {
            override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
                return Observable.just(testShippingRulesList)
            }
        }

        val user = UserFactory.user()
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(user))
            .apolloClientV2(apolloClient)
            .currentConfig2(currentConfig)
            .build()

        val state = mutableListOf<ShippingRulesState>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(dispatcher) {
            val useCase = GetShippingRulesUseCase(apolloClient, project, config, this, dispatcher)
            setUpEnvironment(env, useCase)

            vm.inputs.configureWith(ProjectDataFactory.project(project))
            vm.countrySelectorRules().toList(state)
        }

        advanceUntilIdle() // wait until all state emissions completed

        assertEquals(state.size, 3)
        assertEquals(state[0], ShippingRulesState()) // Initialization
        assertEquals(state[1], ShippingRulesState(loading = true)) // starts loading
        assertEquals(
            state[2],
            ShippingRulesState(
                loading = false,
                selectedShippingRule = ShippingRuleFactory.canadaShippingRule(),
                shippingRules = testShippingRulesList.shippingRules()
            )
        ) // completed requests
    }

    @Test
    fun `test call vm-selectedShippingRule() with location US, pledgeData-shipping should be US `() = runTest {

        val unlimitedReward = RewardFactory.rewardWithShipping()

        val rewards = listOf<Reward>(
            unlimitedReward
        )
        val project = ProjectFactory.project().toBuilder().rewards(rewards).build()

        val config = ConfigFactory.configForCA()
        val currentConfig = MockCurrentConfigV2()
        currentConfig.config(config)

        val testShippingRulesList = ShippingRulesEnvelopeFactory.shippingRules()
        val apolloClient = object : MockApolloClientV2() {
            override fun getShippingRules(reward: Reward): Observable<ShippingRulesEnvelope> {
                return Observable.just(testShippingRulesList)
            }
        }

        val user = UserFactory.user()
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(user))
            .apolloClientV2(apolloClient)
            .currentConfig2(currentConfig)
            .build()

        val state = mutableListOf<ShippingRulesState>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(dispatcher) {
            val useCase = GetShippingRulesUseCase(apolloClient, project, config, this, dispatcher)
            setUpEnvironment(env, useCase)

            vm.inputs.configureWith(ProjectDataFactory.project(project))
            vm.countrySelectorRules().toList(state)
        }

        advanceUntilIdle() // wait until all state emissions completed

        assertEquals(state.size, 3)
        assertEquals(state[0], ShippingRulesState()) // Initialization
        assertEquals(state[1], ShippingRulesState(loading = true)) // starts loading
        assertEquals(
            state[2],
            ShippingRulesState(
                loading = false,
                selectedShippingRule = ShippingRuleFactory.canadaShippingRule(),
                shippingRules = testShippingRulesList.shippingRules()
            )
        ) // completed requests

        val usShippingRule = testShippingRulesList.shippingRules().first()
        backgroundScope.launch(dispatcher) {
            vm.inputs.configureWith(ProjectDataFactory.project(project))
            vm.inputs.selectedShippingRule(usShippingRule)
        }

        vm.showAddOnsFragment().subscribe {
            assertEquals(it.first.shippingRule()?.location(), usShippingRule.location())
        }.addToDisposable(disposables)
    }

    @Test
    fun `test DefaultShipping Rule is sent to PledgeFragment`() {
        val project = ProjectFactory.backedProject()
        val reward = RewardFactory.reward()
        val selectedShippingRule = ShippingRuleFactory.usShippingRule()

        setUpEnvironment(environment())
        vm.inputs.configureWith(ProjectDataFactory.project(project))
        vm.inputs.selectedShippingRule(selectedShippingRule)
        vm.inputs.rewardClicked(reward)

        vm.outputs.showPledgeFragment().subscribe {
            assertEquals(it.second, PledgeFlowContext.CHANGE_REWARD)
            assertEquals(it.first.reward(), reward)
            assertEquals(it.first.projectData(), ProjectDataFactory.project(project))
            assertEquals(it.first.shippingRule(), selectedShippingRule)
        }.addToDisposable(disposables)
        this.showAddOnsFragment.assertNoValues()
    }

    @Test
    fun `test DefaultShipping Rule is sent to AddOnsFragment`() {
        val reward = RewardFactory.rewardWithShipping().toBuilder().hasAddons(true).build()
        val backedProject = ProjectFactory.backedProject()
            .toBuilder()
            .backing(
                BackingFactory.backing()
                    .toBuilder()
                    .reward(reward)
                    .rewardId(reward.id())
                    .build()
            )
            .rewards(listOf(RewardFactory.noReward(), reward))
            .build()
        val selectedShippingRule = ShippingRuleFactory.usShippingRule()

        setUpEnvironment(environment())

        this.vm.inputs.configureWith(ProjectDataFactory.project(backedProject))
        this.vm.inputs.selectedShippingRule(selectedShippingRule)
        this.vm.inputs.rewardClicked(reward)

        this.showPledgeFragment.assertNoValues()
        this.vm.showAddOnsFragment().subscribe {
            assertEquals(it.first.shippingRule(), selectedShippingRule)
            assertEquals(it.first.reward(), reward)
        }.addToDisposable(disposables)
        this.showAlert.assertNoValues()
    }
}
