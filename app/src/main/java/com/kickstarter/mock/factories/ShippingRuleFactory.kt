package com.kickstarter.mock.factories

import com.kickstarter.models.ShippingRule

class ShippingRuleFactory private constructor() {
    companion object {
        fun usShippingRule(): ShippingRule {
            return ShippingRule.builder()
                    .id(1L)
                    .cost(30.0)
                    .location(LocationFactory.unitedStates())
                    .build()
        }

        fun germanyShippingRule(): ShippingRule {
            return ShippingRule.builder()
                    .id(2L)
                    .cost(40.0)
                    .location(LocationFactory.germany())
                    .build()
        }

        fun mexicoShippingRule(): ShippingRule {
            return ShippingRule.builder()
                    .id(3L)
                    .cost(10.0)
                    .location(LocationFactory.mexico())
                    .build()
        }

        fun emptyShippingRule(): ShippingRule {
            return ShippingRule.builder()
                    .id(-1L)
                    .location(LocationFactory.empty())
                    .cost(-1.0)
                    .build()
        }
    }
}
