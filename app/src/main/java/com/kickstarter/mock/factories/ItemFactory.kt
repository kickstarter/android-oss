package com.kickstarter.mock.factories

import com.kickstarter.models.Item
import com.kickstarter.models.Item.Companion.builder

object ItemFactory {
    fun item(): Item {
        return builder()
            .amount(10.0f)
            .id(IdFactory.id().toLong())
            .name("T-Shirt")
            .projectId(IdFactory.id().toLong())
            .build()
    }
}
