package com.kickstarter.viewmodels

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
        var logInCallback = 0
        var continueCallback = 0

        // -  No user present on environment
        setUpEnvironment(environment())

        assertTrue(logInCallback == 0)
        assertTrue(continueCallback == 0)

        val state = mutableListOf<FlowUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            // - Call onConfirmDetailsContinueClicked with a VM loaded with Environment without user
            vm.onContinueClicked({ logInCallback++ }, { continueCallback++ })

            vm.flowUIState.toList(state)
        }

        // - make sure empty FlowUISate has been produced, `onConfirmDetailsContinueClicked` will produce states ONLY if user present
        assertEquals(state, listOf(FlowUIState()))
        assertNotSame(state, listOf(FlowUIState(currentPage = 2, expanded = true)))
        assertNotSame(state, listOf(FlowUIState(currentPage = 3, expanded = true)))
        assert(state.size == 1)

        // - Make sure the callback provided is called when no user present, `onConfirmDetailsContinueClicked` will produce states ONLY if user present
        assertTrue(logInCallback == 1)
        assertTrue(continueCallback == 0)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testProduceNextPageState_whenUser_LoggedIn() = runTest {
        var loginInCallback = 0
        var continueCallback = 0

        // - Environment with user present
        val environment = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()

        // - Make sure the callback is not called
        assertTrue(loginInCallback == 0)
        assertTrue(continueCallback == 0)

        val state = mutableListOf<FlowUIState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {

            setUpEnvironment(environment)

            // - Call VM loaded with Environment containing an user, so continueCallback it's executed
            vm.onContinueClicked({ loginInCallback++ }, continueCallback = { continueCallback++ })

            vm.flowUIState.toList(state)
        }

        // - make sure next page FlowUIState has been generated, not just the initial empty state
        assertEquals(state, listOf(FlowUIState(), FlowUIState(currentPage = 4, expanded = true)))
        assert(state.size == 2)
        assertTrue(loginInCallback == 0)
        assertTrue(continueCallback == 1)
    }
}
