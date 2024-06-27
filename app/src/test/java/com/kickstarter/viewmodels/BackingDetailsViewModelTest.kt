package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.ui.IntentKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackingDetailsViewModelTest  : KSRobolectricTestCase() {
    private lateinit var vm: BackingDetailsViewModel

    private fun setUpEnvironment(environment: Environment, intent: Intent) {
        this.vm = BackingDetailsViewModel.Factory(environment, intent).create(BackingDetailsViewModel::class.java)
    }

    @Test
    fun `test VM init state that the backing details url is emitted from intent`() = runTest {
        val testUrl = "https://www.kickstarter.com/"
        val intent = Intent().apply {
            putExtra(IntentKey.URL, testUrl)
        }
        setUpEnvironment(environment(), intent = intent)

        val emittedUrls = mutableListOf<String>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            vm.url.toList(emittedUrls)
        }

        assertEquals(emittedUrls.size, 2)
        assertEquals(emittedUrls.last(), testUrl)
    }
}
