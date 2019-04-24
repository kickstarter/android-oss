package com.kickstarter.mock.factories

import com.kickstarter.models.ShippingRule
import com.kickstarter.services.apiresponses.ShippingRulesEnvelope

class ShippingRulesEnvelopeFactory private constructor() {

    companion object {

        fun shippingRules(): ShippingRulesEnvelope {
            return ShippingRulesEnvelope.builder()
                    .shippingRules(shippingRules)
                    .build()
        }

        private val shippingRules = listOf(ShippingRule
                .builder()
                .id(1)
                .cost(30.0)
                .location(LocationFactory.unitedStates())
                .build())
    }
}
