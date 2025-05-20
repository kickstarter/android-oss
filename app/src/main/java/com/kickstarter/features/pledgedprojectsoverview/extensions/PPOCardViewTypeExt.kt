package com.kickstarter.features.pledgedprojectsoverview.extensions

import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType

fun PPOCardViewType.isTier1Alert(): Boolean {
    return when (this) {
        PPOCardViewType.CONFIRM_ADDRESS, PPOCardViewType.AUTHENTICATE_CARD, PPOCardViewType.OPEN_SURVEY, PPOCardViewType.FIX_PAYMENT -> true
        else -> false
    }
}

fun PPOCardViewType.isTier2Type(): Boolean {
    return when (this) {
        PPOCardViewType.ADDRESS_CONFIRMED,
        PPOCardViewType.PLEDGE_COLLECTED_REWARD,
        PPOCardViewType.PLEDGE_COLLECTED_NO_REWARD,
        PPOCardViewType.SUVERY_SUBMITTED_DIGITAL,
        PPOCardViewType.SUVERY_SUBMITTED_SHIPPABLE,
        PPOCardViewType.AWAITING_REWARD,
        PPOCardViewType.PLEDGE_REDEMPTION,
        PPOCardViewType.REWARD_RECEIVED -> true
        else -> false
    }
}