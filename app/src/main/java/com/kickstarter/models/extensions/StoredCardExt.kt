@file:JvmName("StoredCard")
package com.kickstarter.models.extensions

import com.kickstarter.R
import com.kickstarter.models.PaymentSource
import com.kickstarter.models.StoredCard
import type.CreditCardTypes

fun StoredCard.getCardTypeDrawable(): Int {
    return this.type()?.let {
        getCardTypeDrawable(it)
    } ?: this.resourceId() ?: R.drawable.generic_bank_md
}

fun PaymentSource.getCardTypeDrawable(): Int {
    val type = CreditCardTypes.safeValueOf(this.type())
    return getCardTypeDrawable(type)
}

fun getCardTypeDrawable(cardType: CreditCardTypes): Int {
    return when (cardType) {
        CreditCardTypes.AMEX -> R.drawable.amex_md
        CreditCardTypes.DINERS -> R.drawable.diners_md
        CreditCardTypes.DISCOVER -> R.drawable.discover_md
        CreditCardTypes.JCB -> R.drawable.jcb_md
        CreditCardTypes.MASTERCARD -> R.drawable.mastercard_md
        CreditCardTypes.UNION_PAY -> R.drawable.union_pay_md
        CreditCardTypes.VISA -> R.drawable.visa_md
        else -> R.drawable.generic_bank_md
    }
}
