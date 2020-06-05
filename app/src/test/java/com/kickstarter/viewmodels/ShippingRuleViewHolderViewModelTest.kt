package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.ShippingRuleFactory
import org.junit.Test
import rx.observers.TestSubscriber

class ShippingRuleViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ShippingRuleViewHolderViewModel.ViewModel

    private val shippingRuleText = TestSubscriber.create<String>()

    private fun setupEnvironment(environment: Environment) {
        this.vm = ShippingRuleViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.shippingRuleText().subscribe(this.shippingRuleText)
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
        val expectedCurrency = environment.ksCurrency().format(30.0, project)
        this.shippingRuleText.assertValue("Brooklyn, NY $expectedCurrency")
    }
}
