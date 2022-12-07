package com.kickstarter.ui.data

enum class LoginReason {
    DEFAULT,
    ACTIVITY_FEED,
    CHANGE_PASSWORD,
    COMMENT_FEED,
    CREATE_PASSWORD,
    BACK_PROJECT,
    MESSAGE_CREATOR,
    RESET_PASSWORD,
    RESET_FACEBOOK_PASSWORD,
    STAR_PROJECT;

    val isDefaultFlow: Boolean
        get() = this == DEFAULT

    val isContextualFlow: Boolean
        get() = !isDefaultFlow
}
