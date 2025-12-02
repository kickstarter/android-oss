package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
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

class WebViewViewModelTest : KSRobolectricTestCase() {

    lateinit var vm: WebViewViewModel

    fun setUpEnvironment(
        environment: Environment? = null,
        dispatcher: CoroutineDispatcher
    ) {
        this.vm = WebViewViewModel.Factory(environment ?: environment(), dispatcher).create(WebViewViewModel::class.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun emitsLoginEventWhenUserIsLoggedOut() = runTest {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2())
            .build()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val webViewEvent = mutableListOf<WebViewEvent>()

        backgroundScope.launch(dispatcher) {
            setUpEnvironment(env, dispatcher)
            vm.webViewUIState.toList(webViewEvent)
        }

        advanceUntilIdle()
        assertEquals(1, webViewEvent.size)
        assertEquals(webViewEvent.first(), WebViewEvent.SHOW_LOGIN)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun emitsLoginEventWhenUserIsLoggedIn() = runTest {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val webViewEvent = mutableListOf<WebViewEvent>()

        backgroundScope.launch(dispatcher) {
            setUpEnvironment(env, dispatcher)
            vm.webViewUIState.toList(webViewEvent)
        }

        advanceUntilIdle()
        assertEquals(2, webViewEvent.size)
        assertEquals(webViewEvent.first(), WebViewEvent.SHOW_LOGIN) // initial
        assertEquals(webViewEvent.last(), WebViewEvent.LOAD_WEBVIEW) // actual
    }
}
