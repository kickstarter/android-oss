package com.kickstarter.ui.data

enum class PledgeFlowContext(val trackingString: String) {
    CHANGE_REWARD("change_reward"),
    FIX_ERRORED_PLEDGE("fix_errored_pledge"),
    MANAGE_REWARD("manage_reward"),
    NEW_PLEDGE("new_pledge");

    companion object {
        fun forPledgeReason(pledgeReason: PledgeReason) : PledgeFlowContext {
           return when (pledgeReason) {
                PledgeReason.FIX_PLEDGE -> FIX_ERRORED_PLEDGE
                PledgeReason.PLEDGE -> NEW_PLEDGE
                PledgeReason.UPDATE_REWARD -> CHANGE_REWARD
                else -> MANAGE_REWARD
            }
        }
    }
}
