package com.kickstarter.libs

/**
 * Determines the place from which Messages were presented.
 *
 * BACKER_MODAL:        The backing view, usually seen by pressing "View pledge" on the project page.
 * CREATOR_BIO_MODAL:   The creator bio.
 * MESSAGES:            The messages inbox.
 * PROJECT_PAGE:        The project page.
 */
enum class MessagePreviousScreenType(val trackingString: String) {
    BACKER_MODAL("backer_modal"),
    CREATOR_BIO_MODAL("creator_bio_modal"),
    MESSAGES("messages"),
    PROJECT_PAGE("project_page"),
    PUSH("push"),
    PLEDGED_PROJECTS_OVERVIEW("pledged_projects_overview")
}
