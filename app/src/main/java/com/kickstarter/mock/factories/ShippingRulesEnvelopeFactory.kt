package com.kickstarter.mock.factories

import com.kickstarter.models.ShippingRule
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope

class ShippingRulesEnvelopeFactory private constructor() {

    companion object {

        fun shippingRules(): ShippingRulesEnvelope {
            return listOf(ShippingRule.builder()
                    .cost(30.00)
                    .id(1)
                    .location(LocationFactory.unitedStates())
                    .build()) as ShippingRulesEnvelope
        }

    }
}

