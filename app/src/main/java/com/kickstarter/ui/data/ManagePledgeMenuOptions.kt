package com.kickstarter.ui.data

data class ManagePledgeMenuOptions(
    val showEditPledge: Boolean,
    val showChooseAnotherReward: Boolean,
    val showUpdatePayment: Boolean,
    val showSeeRewards: Boolean,
    val showCancelPledge: Boolean,
    val showContactCreator: Boolean = true
)
