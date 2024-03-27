package com.kickstarter.viewmodels.usecases

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.viewmodels.projectpage.CheckoutFlowViewModel
import com.kickstarter.viewmodels.projectpage.FlowUIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CheckoutFlowViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: CheckoutFlowViewModel

    private fun setUpEnvironment(environment: Environment) {
        this.vm = CheckoutFlowViewModel.Factory(environment).create(CheckoutFlowViewModel::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testLaunchLogInCallBack_whenNoUser_loggedIn() = runTest {
        var callbackCalled = 0

        // -  No user present on environment
        setUpEnvironment(environment())

        // - Call onConfirmDetailsContinueClicked with a VM loaded with Environment without user
        vm.onConfirmDetailsContinueClicked { callbackCalled++ }

        // - Make sure the callback provided is called when no user present, `onConfirmDetailsContinueClicked` will produce states ONLY if user present
        assertTrue(callbackCalled == 1)

        val state = mutableListOf<FlowUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.flowUIState.toList(state)
        }

        // - make sure empty FlowUISate has been produced, `onConfirmDetailsContinueClicked` will produce states ONLY if user present
        assertEquals(state, listOf(FlowUIState()))
        assertNotSame(state, listOf(FlowUIState(currentPage = 3, expanded = true)))
        assert(state.size == 1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testProduceNextPageState_whenUser_LoggedIn() = runTest {
        var callbackCalled = 0

        // - Environment with user present
        val environment = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()

        setUpEnvironment(environment)

        // - Call onConfirmDetailsContinueClicked with a VM loaded with Environment containing an user
        vm.onConfirmDetailsContinueClicked { callbackCalled++ }

        // - Make sure the callback is not called
        assertTrue(callbackCalled == 0)

        val state = mutableListOf<FlowUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.flowUIState.toList(state)
        }

        // - make sure next page FlowUIState has been generated, not just the initial empty state
        assertEquals(state, listOf(FlowUIState(), FlowUIState(currentPage = 3, expanded = true)))
        assert(state.size == 2)
    }
}
