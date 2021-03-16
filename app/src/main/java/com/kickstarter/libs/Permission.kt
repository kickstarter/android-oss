package com.kickstarter.libs

enum class Permission(val permission: String) {
    COMMENT("comment"),
    EDIT_PROJECT("edit_project"),
    EDIT_FAQ("edit_faq"),
    FULFILLMENT("fulfillment"),
    POST("post"),
    VIEW_PLEDGES("view_pledges"),
    UNKNOWN("unknown")
}
