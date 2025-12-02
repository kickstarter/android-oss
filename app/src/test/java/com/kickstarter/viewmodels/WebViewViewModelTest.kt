package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.MockCurrentUserV2
import com.kickstarter.mock.factories.UserFactory
import com.kickstarter.ui.activities.WebViewEvent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test

class WebViewViewModelTest : KSRobolectricTestCase() {

    lateinit var vm: WebViewViewModel

    fun setUpEnvironment(
        environment: Environment? = null,
    ) {
        this.vm = WebViewViewModel.Factory(environment ?: environment()).create(WebViewViewModel::class.java)
    }

    @After
    fun clear() {
    }

    @Test
    fun emitsLoginEventWhenUserIsLoggedOut() = runTest {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2())
            .build()
        val vm = WebViewViewModel(env)

        assert(vm.events.value is WebViewEvent.ShowLogin)
    }

    @Test
    fun emitsLoadWebPageEventWhenLoggedIn() = runTest {
        val env = environment()
            .toBuilder()
            .currentUserV2(MockCurrentUserV2(UserFactory.user()))
            .build()
        val vm = WebViewViewModel(env)

        assert(vm.events.value is WebViewEvent.LoadWebPage)
    }
}
