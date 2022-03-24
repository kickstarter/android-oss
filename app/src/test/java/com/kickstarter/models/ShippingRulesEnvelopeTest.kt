package com.kickstarter.models

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ShippingRuleFactory
import com.kickstarter.mock.factories.ShippingRulesEnvelopeFactory
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope
import org.junit.Test

class ShippingRulesEnvelopeTest : KSRobolectricTestCase() {

    @Test
    fun testDefaultInit() {
        val shippingRulesList = listOf(
            ShippingRuleFactory.usShippingRule(),
            ShippingRuleFactory.germanyShippingRule(),
            ShippingRuleFactory.mexicoShippingRule()
        )
        val shippingRulesEnvelope = ShippingRulesEnvelope.builder()
            .shippingRules(
                shippingRulesList
            )
            .build()

        assertEquals(shippingRulesEnvelope.shippingRules(), shippingRulesList)
    }

    @Test
    fun testDefaultToBuilder() {
        val shippingRulesList = listOf(
            ShippingRuleFactory.usShippingRule(),
            ShippingRuleFactory.germanyShippingRule(),
            ShippingRuleFactory.mexicoShippingRule()
        )
        val shippingRulesEnvelope = ShippingRulesEnvelope.builder().build().toBuilder().shippingRules(shippingRulesList).build()

        assertEquals(shippingRulesEnvelope.shippingRules(), shippingRulesList)
    }

    @Test
    fun testShippingRulesEnvelope_equalFalse() {
        val shippingRulesEnvelope = ShippingRulesEnvelope.builder().build()
        val shippingRulesEnvelope2 = ShippingRulesEnvelopeFactory.shippingRules()

        assertFalse(shippingRulesEnvelope == shippingRulesEnvelope2)
    }

    @Test
    fun testShippingRulesEnvelope_equalTrue() {
        val shippingRulesEnvelope1 = ShippingRulesEnvelope.builder().build()
        val shippingRulesEnvelope2 = ShippingRulesEnvelope.builder().build()

        assertEquals(shippingRulesEnvelope1, shippingRulesEnvelope2)
    }
}
