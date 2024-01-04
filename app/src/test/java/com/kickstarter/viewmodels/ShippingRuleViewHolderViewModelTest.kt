package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ShippingRuleViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ShippingRuleViewHolderViewModel.ViewModel

    private val shippingRuleText = TestSubscriber.create<String>()
    private val disposables = CompositeDisposable()

    private fun setupEnvironment(environment: Environment) {
        this.vm = ShippingRuleViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.shippingRuleText()
            .subscribe { this.shippingRuleText.onNext(it) }
            .addToDisposable(disposables)
    }

    @After
    fun clean() {
        disposables.clear()
    }

    @Test
    fun testShippingRuleText() {
        val config = ConfigFactory.configForUSUser()
        val currentConfig = MockCurrentConfig()
        currentConfig.config(config)

        val environment = environment().toBuilder()
            .currentConfig(currentConfig)
            .build()

        setupEnvironment(environment)

        val shippingRule = ShippingRuleFactory.usShippingRule()
        val project = ProjectFactory.project()

        this.vm.inputs.configureWith(shippingRule, project)
        this.shippingRuleText.assertValue("Brooklyn, NY")
    }
}
