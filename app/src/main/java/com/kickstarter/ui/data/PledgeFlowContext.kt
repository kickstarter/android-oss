package com.kickstarter.ui.data

enum class PledgeFlowContext(val trackingString: String) {
    CHANGE_REWARD("change_reward"),
    MANAGE_REWARD("manage_reward"),
    NEW_PLEDGE("new_pledge");

    companion object {
        fun forPledgeReason(pledgeReason: PledgeReason) : PledgeFlowContext {
           return  when (pledgeReason) {
                PledgeReason.PLEDGE -> NEW_PLEDGE
                PledgeReason.UPDATE_REWARD -> CHANGE_REWARD
                else -> MANAGE_REWARD
            }
        }
    }
}
