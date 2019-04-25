package com.kickstarter.mock.factories

import com.kickstarter.models.Location
import com.kickstarter.models.ShippingRule

class ShippingRuleFactory private constructor() {
    companion object {
        fun usShippingRule(): ShippingRule {
            return ShippingRule.builder()
                    .id(10)
                    .cost(30.0)
                    .location(LocationFactory.unitedStates())
                    .build()
        }

        fun germanyShippingRule(): ShippingRule {
            return ShippingRule.builder()
                    .id(10)
                    .cost(30.0)
                    .location(LocationFactory.unitedStates())
                    .build()
        }
    }
}
