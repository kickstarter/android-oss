package com.kickstarter.mock.factories

import com.kickstarter.models.ShippingRule
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope

import java.util.Collections

object ShippingRulesEnvelopeFactory {

    fun shippingRules(): ShippingRulesEnvelope {
        return listOf<ShippingRule>(ShippingRule.builder()
                .cost(30.0)
                .id(1)
                .location(LocationFactory.germany())
                .build()) as ShippingRulesEnvelope
    }
}
