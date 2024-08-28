package com.kickstarter.mock.factories

import com.kickstarter.models.ShippingRule

object ShippingRuleFactory {
    @JvmStatic
    fun usShippingRule(): ShippingRule {
        return ShippingRule.builder()
            .id(1L)
            .cost(30.0)
            .location(LocationFactory.unitedStates())
            .build()
    }

    @JvmStatic
    fun germanyShippingRule(): ShippingRule {
        return ShippingRule.builder()
            .id(2L)
            .cost(40.0)
            .location(LocationFactory.germany())
            .build()
    }

    @JvmStatic
    fun mexicoShippingRule(): ShippingRule {
        return ShippingRule.builder()
            .id(3L)
            .cost(10.0)
            .location(LocationFactory.mexico())
            .build()
    }

    fun canadaShippingRule(): ShippingRule {
        return ShippingRule.builder()
            .id(4L)
            .cost(10.0)
            .location(LocationFactory.canada())
            .build()
    }

    @JvmStatic
    fun emptyShippingRule(): ShippingRule {
        return ShippingRule.builder()
            .id(-1L)
            .location(LocationFactory.empty())
            .cost(-1.0)
            .build()
    }
}
