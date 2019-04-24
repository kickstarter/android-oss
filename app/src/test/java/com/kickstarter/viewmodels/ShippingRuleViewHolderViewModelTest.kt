package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.MockCurrentConfig
import com.kickstarter.mock.factories.ConfigFactory
import com.kickstarter.mock.factories.ProjectFactory
import com.kickstarter.mock.factories.ShippingRulesEnvelopeFactory
import com.kickstarter.models.ShippingRule
import org.junit.Test
import rx.observers.TestSubscriber

class ShippingRuleViewHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ShippingRuleViewHolderViewModel.ViewModel

    private val shippingRule = TestSubscriber.create<ShippingRule>()
    private val shippingRuleText = TestSubscriber.create<String>()

    private fun setupEnvironment(environment: Environment) {
        this.vm = ShippingRuleViewHolderViewModel.ViewModel(environment)

        this.vm.outputs.shippingRule().subscribe(this.shippingRule)
        this.vm.outputs.shippingRuleText().subscribe(this.shippingRuleText)
    }

    @Test
    fun testShippingRule() {
        setupEnvironment(environment())

        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules().single()
        val project = ProjectFactory.project()

        this.vm.inputs.configureWith(shippingRules, project)
        this.vm.inputs.shippingRuleClicked()
        this.shippingRule.assertValueCount(1)
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

        val shippingRules = ShippingRulesEnvelopeFactory.shippingRules().shippingRules().single()
        val project = ProjectFactory.project()

        this.vm.inputs.configureWith(shippingRules, project)
        this.shippingRuleText.assertValue("Brooklyn, NY $30.00")
    }
}
