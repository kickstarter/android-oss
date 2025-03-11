package com.kickstarter.features.pledgedprojectsoverview.data

enum class PledgeTierType(val tierType: String) {
    FAILED_PAYMENT("Tier1PaymentFailed"),
    ADDRESS_LOCK("Tier1AddressLockingSoon"),
    SURVEY_OPEN("Tier1OpenSurvey"),
    PAYMENT_AUTHENTICATION("Tier1PaymentAuthenticationRequired"),
    PLEDGE_COLLECTED("PledgeCollected"),
    SUVERY_SUBMITTED("SurveySubmitted"),
    ADDRESS_CONFIRMED("AddressConfirmed"),
    AWAITING_REWARD("AwaitingReward"),
    PLEDGE_REDEMPTION("PledgeRedemption"),
    REWARD_RECEIVED("RewardReceived")
}
