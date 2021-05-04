package com.kickstarter.libs

class KoalaContext {

    /**
     * Determines the place from which Messages were presented.
     *
     * BACKER_MODAL:        The backing view, usually seen by pressing "View pledge" on the project page.
     * CREATOR_ACTIVITY:    The creator's activity feed.
     * CREATOR_BIO_MODAL:   The creator bio.
     * MESSAGES:            The messages inbox.
     * PROJECT_MESSAGES:    The messages inbox for a particular project of a creator's.
     * PROJECT_PAGE:        The project page.
     */
    enum class Message(val trackingString: String) {
        BACKER_MODAL("backer_modal"),
        CREATOR_ACTIVITY("creator_activity"),
        CREATOR_BIO_MODAL("creator_bio_modal"),
        MESSAGES("messages"),
        PROJECT_MESSAGES("project_messages"),
        PROJECT_PAGE("project_page"),
        PUSH("push")
    }

}
