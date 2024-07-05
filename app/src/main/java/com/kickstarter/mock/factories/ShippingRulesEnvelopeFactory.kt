package com.kickstarter.mock.factories

import com.kickstarter.services.apiresponses.ShippingRulesEnvelope

object ShippingRulesEnvelopeFactory {
    @JvmStatic
    fun shippingRules(): ShippingRulesEnvelope {
        return ShippingRulesEnvelope.builder()
            .shippingRules(
                listOf(
                    ShippingRuleFactory.usShippingRule(),
                    ShippingRuleFactory.germanyShippingRule(),
                    ShippingRuleFactory.mexicoShippingRule(),
                    ShippingRuleFactory.canadaShippingRule()
                )
            )
            .build()
    }

    @JvmStatic
    fun emptyShippingRules(): ShippingRulesEnvelope {
        return ShippingRulesEnvelope.builder()
            .shippingRules(listOf())
            .build()
    }
}
