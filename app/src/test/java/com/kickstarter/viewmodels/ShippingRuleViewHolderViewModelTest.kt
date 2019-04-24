package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
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


    }

    @Test
    fun testShippingRuleText() {
        setupEnvironment(environment())
    }
}