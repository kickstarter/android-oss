package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.ui.IntentKey
import org.junit.Test
import rx.observers.TestSubscriber

class WebViewViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: WebViewViewModel.ViewModel
    private val toolbarTitle = TestSubscriber<String>()
    private val url = TestSubscriber<String>()

    private fun setUpEnvironment(environment: Environment) {
        vm = WebViewViewModel.ViewModel(environment)

        vm.outputs.toolbarTitle().subscribe(toolbarTitle)
        vm.outputs.url().subscribe(url)
    }

    @Test
    fun testToolbarTitle() {
        val toolbarTitle = "some body once told me"
        setUpEnvironment(environment())

        vm.intent(Intent().putExtra(IntentKey.TOOLBAR_TITLE, toolbarTitle))
        this.toolbarTitle.assertValues(toolbarTitle)
    }

    @Test
    fun testUrl() {
        val url = "d.rip"
        setUpEnvironment(environment())

        vm.intent(Intent().putExtra(IntentKey.URL, url))
        this.url.assertValues(url)
    }
}
