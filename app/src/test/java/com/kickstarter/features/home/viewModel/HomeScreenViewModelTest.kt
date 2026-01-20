package com.kickstarter.features.home.viewModel

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.home.viewmodel.HomeScreenUIState
import com.kickstarter.features.home.viewmodel.HomeScreenViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.UserFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HomeScreenViewModelTest : KSRobolectricTestCase() {

    private lateinit var viewModel: HomeScreenViewModel

    private fun setUpEnvironment(environment: Environment, dispatcher: CoroutineDispatcher) {
        viewModel = HomeScreenViewModel.Factory(environment, dispatcher)
            .create(HomeScreenViewModel::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Logged Out user state`() = runTest {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2())
            .build()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val state = mutableListOf<HomeScreenUIState>()

        backgroundScope.launch(dispatcher) {
            setUpEnvironment(env, dispatcher)
            viewModel.homeUIState.toList(state)
        }

        advanceUntilIdle()
        assertEquals(1, state.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Logged In user state`() = runTest {
        val user = UserFactory.user()
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(user))
            .build()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val state = mutableListOf<HomeScreenUIState>()

        backgroundScope.launch(dispatcher) {
            setUpEnvironment(env, dispatcher)
            viewModel.homeUIState.toList(state)
        }

        advanceUntilIdle()
        assertEquals(2, state.size)
        assertEquals(HomeScreenUIState(true, user.avatar().medium()), state.last())
    }
}
