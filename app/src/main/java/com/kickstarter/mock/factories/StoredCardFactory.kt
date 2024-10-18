package com.kickstarter.mock.factories

import com.kickstarter.models.StoredCard
import com.kickstarter.type.CreditCardTypes
import java.util.Date

object StoredCardFactory {

    fun fromPaymentSheetCard(): StoredCard {
        return StoredCard.builder()
            .lastFourDigits("1234")
            .resourceId(1234)
            .clientSetupId("ClientSetupId")
            .build()
    }

    @JvmStatic
    fun discoverCard(): StoredCard {
        return StoredCard.builder()
            .id(IdFactory.id().toString())
            .expiration(Date())
            .lastFourDigits("1234")
            .type(CreditCardTypes.DISCOVER)
            .build()
    }

    @JvmStatic
    fun visa(): StoredCard {
        return StoredCard.builder()
            .id(IdFactory.id().toString())
            .expiration(Date())
            .lastFourDigits("4321")
            .type(CreditCardTypes.VISA)
            .build()
    }
}
