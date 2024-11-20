@file:JvmName("StoredCard")
package com.kickstarter.models.extensions

import com.kickstarter.R
import com.kickstarter.libs.RefTag
import com.kickstarter.models.PaymentSource
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.models.StoredCard
import com.kickstarter.services.mutations.CreateBackingData
import com.kickstarter.type.CreditCardTypes

fun StoredCard.getCardTypeDrawable(): Int {
    val cardType = this.type() ?: CreditCardTypes.UNKNOWN__
    val resourceId = this.expiration()?.let {
        getCardTypeDrawable(cardType)
    } ?: this.resourceId() ?: R.drawable.generic_bank_md

    return resourceId
}

fun StoredCard.isFromPaymentSheet(): Boolean {
    return this.type() == CreditCardTypes.UNKNOWN__ &&
        this.lastFourDigits()?.isNotEmpty() ?: false &&
        this.clientSetupId()?.isNotEmpty() ?: false
}

fun StoredCard.getBackingData(
    proj: Project,
    amount: String,
    locationId: String?,
    rewards: List<Reward>,
    cookieRefTag: RefTag?
): CreateBackingData {
    return if (this.isFromPaymentSheet()) {
        CreateBackingData(
            project = proj,
            amount = amount,
            setupIntentClientSecret = this.clientSetupId(),
            locationId = locationId,
            rewardsIds = rewards,
            refTag = if (cookieRefTag?.tag()?.isNotEmpty() == true) cookieRefTag else null,
            stripeCardId = this.stripeCardId()
        )
    } else {
        CreateBackingData(
            project = proj,
            amount = amount,
            paymentSourceId = this.id(),
            locationId = locationId,
            rewardsIds = rewards,
            refTag = if (cookieRefTag?.tag()?.isNotEmpty() == true) cookieRefTag else null,
            stripeCardId = this.stripeCardId()
        )
    }
}

fun PaymentSource.getCardTypeDrawable(): Int {
    val type = CreditCardTypes.safeValueOf(this.type() ?: "")
    return getCardTypeDrawable(type)
}

fun getCardTypeDrawable(cardType: CreditCardTypes): Int {
    return when (cardType) {
        CreditCardTypes.AMEX -> R.drawable.amex_md
        CreditCardTypes.DINERS -> R.drawable.diners_md
        CreditCardTypes.DISCOVER -> R.drawable.discover_md
        CreditCardTypes.JCB -> R.drawable.jcb_md
        CreditCardTypes.MASTERCARD -> R.drawable.mastercard_md
        CreditCardTypes.UNIONPAY -> R.drawable.union_pay_md
        CreditCardTypes.VISA -> R.drawable.visa_md
        else -> R.drawable.generic_bank_md
    }
}
