package com.kickstarter.ui.data

enum class PledgeFlowContext(val trackingString: String) {
    CHANGE_REWARD("change_reward"),
    MANAGE_REWARD("manage_reward"),
    NEW_PLEDGE("new_pledge")
}