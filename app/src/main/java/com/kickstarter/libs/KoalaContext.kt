package com.kickstarter.libs

class KoalaContext {

    /**
     * Determines the place from which the comments dialog was presented.
     *
     * PROJECT_ACTIVITY: The creator's project activity screen.
     * PROJECT_COMMENTS: The comments screen for a project.
     * UPDATE_COMMENTS:  The comments screen for an update.
     */
    enum class CommentDialog(val trackingString: String) {
        PROJECT_ACTIVITY("project_activity"),
        PROJECT_COMMENTS("project_comments"),
        UPDATE_COMMENTS("update_comments")
    }

    /**
     * Determines the place from which the comments were presented.
     *
     * PROJECT: The comments for a project.
     * UPDATE:  The comments for an update.
     */
    enum class Comments(val trackingString: String) {
        PROJECT("project"),
        UPDATE("update")
    }

    /**
     * Determines the place from which the external link was presented.
     *
     * PROJECT_UPDATE:  The project update page.
     * PROJECT_UPDATES: The project updates page.
     */
    enum class ExternalLink(val trackingString: String) {
        PROJECT_UPDATE("project_update"),
        PROJECT_UPDATES("project_updates")
    }

    /**
     * Determines the place from where the Mailbox was presented.
     *
     * CREATOR_DASHBOARD    The Messages from in the creator dashboard.
     * DRAWER:              The Discovery navigation drawer.
     * PROFILE:             The creator's activity feed.
     */
    enum class Mailbox(val trackingString: String) {
        CREATOR_DASHBOARD("creator_dashboard"),
        DRAWER("drawer"),
        PROFILE("profile"),
    }

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

    /**
     * Determines the place from which the share sheet was shown.
     *
     * CREATOR_DASHBOARD:   Sharing a creator's project from their dashboard.
     * DISCOVERY:           Sharing a project from the discovery page.
     * LIVE_STREAM:         Sharing a live stream from the countdown or stream itself.
     * PROJECT:             Sharing a project from the project screen.
     * THANKS:              Sharing a project from the checkout-thanks screen.
     * UPDATE:              Sharing an update from the update screen.
     */
    enum class Share(val trackingString: String) {
        CREATOR_DASHBOARD("creator_dashboard"),
        DISCOVERY("discovery"),
        LIVE_STREAM("live_stream"),
        PROJECT("project"),
        THANKS("thanks"),
        UPDATE("update"),
    }

    /**
     * Determines the place from which the Update was presented.
     *
     * UPDATES:           The updates index.
     * ACTIVITY:          The activity feed.
     * ACTIVITY_SAMPLE:   The activity sample.
     */
    enum class Update(val trackingString: String) {
        UPDATES("updates"),
        ACTIVITY("activity"),
        ACTIVITY_SAMPLE("activity_sample")
    }
}
