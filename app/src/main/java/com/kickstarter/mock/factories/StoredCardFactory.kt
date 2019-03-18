package com.kickstarter.mock.factories

import com.kickstarter.models.StoredCard
import type.CreditCardTypes
import java.util.*

class StoredCardFactory private constructor() {
    companion object {
        fun storedCard(): StoredCard {
            return StoredCard.builder()
                    .id(IdFactory.id().toString())
                    .expiration(Date())
                    .lastFourDigits("1234")
                    .type(CreditCardTypes.DISCOVER)
                    .build()
        }
    }
}
