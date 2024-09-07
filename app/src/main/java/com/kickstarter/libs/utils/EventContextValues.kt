package com.kickstarter.libs.utils

class EventContextValues {

    /**
     * Determines the button that was clicked or tapped by a user.
     *
     * @param contextName: The name of the event cta context.
     *
     * ADD_ONS_CONTINUE: The add-ons screen/carousel for a reward CTA.
     * PLEDGE_INITIATE: The checkout screen CTA.
     * PLEDGE_SUBMIT: The project screen CTA.
     * REWARD_CONTINUE: The Rewards carousel.
     * DISCOVER_SORT: The tab sort CTAs on the discover screen.
     * DISCOVER_FILTER: The filter CTAs in the overflow menu on the discover screen.
     * SEARCH: The search CTA on the discover screen.
     * DISCOVER: The discover projects CTA on the activity feed screen.
     * WATCH_PROJECT: The watch project heart CTA on the project screen.
     * LOGIN_INITIATE: The login CTA on the login/signup screen.
     * CAMPAIGN_DETAILS: The campaign details CTA on the project screen.
     * CREATOR_DETAILS:  The creator details CTA on the project screen.
     * LOGIN_OR_SIGN_UP: The login or signup cta on the discover screen.
     * LOGIN_SUBMIT: The login submit CTA on the login screen.
     * SIGN_UP_SUBMIT: The signup submit CTA on the signup screen.
     * SIGN_UP_INITIATE: The signup CTA on the login/signup screen.
     */
    enum class CtaContextName(val contextName: String) {
        ADD_ONS_CONTINUE("add_ons_continue"),
        PLEDGE_INITIATE("pledge_initiate"),
        PLEDGE_SUBMIT("pledge_submit"),
        PLEDGE_CONFIRM("pledge_confirm"),
        REWARD_CONTINUE("reward_continue"),
        DISCOVER_SORT("discover_sort"),
        DISCOVER_FILTER("discover_filter"),
        SEARCH("search"),
        DISCOVER("discover"),
        WATCH_PROJECT("watch_project"),
        LOGIN_INITIATE("log_in_initiate"),
        MESSAGE_CREATOR_INITIATE("message_creator_initiate"),
        FIX_PLEDGE_INITIATE("fix_pledge_initiate"),
        SURVEY_RESPONSE_INITIATE("survey_response_initiate"),
        CONFIRM_INITIATE("confirm_initiate"),
        CONFIRM_SUBMIT("confirm_submit"),
        CAMPAIGN_DETAILS("campaign_details"),
        CREATOR_DETAILS("creator_details"),
        LOGIN_OR_SIGN_UP("log_in_or_sign_up"),
        LOGIN_SUBMIT("log_in_submit"),
        SIGN_UP_SUBMIT("sign_up_submit"),
        SIGN_UP_INITIATE("sign_up_initiate"),
        COMMENT_POST("comment_post"),
        PROJECT_SELECT("project_select"),
        LATE_PLEDGE("late_pledge"),
        EDIT("edit"),
    }

    /**
     * Represents the current screen associated with the event.
     *
     * @param contextName: The name of the page context.
     *
     * ACTIVITY_FEED: The activity feed screen.
     * ADD_ONS: The add-ons screen/carousel for a reward.
     * CHECKOUT: The checkout screen.
     * DISCOVER: The discover screen.
     * PROJECT: The project screen.
     * REWARDS: The Rewards carousel.
     * THANKS: The Thanks page.
     * UPDATE_PLEDGE: The update pledge screen.
     * CHANGE_PAYMENT: The change payment screen.
     * LOGIN_SIGN_UP: The login/signup screen.
     * SIGN_UP: The signup screen.
     * LOGIN: The login screen.
     * MANAGE_PLEDGE: The manage pledge screen.
     * TWO_FACTOR_AUTH: The two-factor authentication screen.
     */
    enum class ContextPageName(val contextName: String) {
        ACTIVITY_FEED("activity_feed"),
        ADD_ONS("add_ons"),
        CHANGE_PAYMENT("change_payment"),
        CHECKOUT("checkout"),
        DISCOVER("discover"),
        LOGIN_SIGN_UP("log_in_sign_up"),
        PROFILE("profile"),
        PROJECT("project"),
        REWARDS("rewards"),
        SEARCH("search"),
        SIGN_UP("sign_up"),
        THANKS("thanks"),
        UPDATE_PLEDGE("update_pledge"),
        LOGIN("log_in"),
        MANAGE_PLEDGE("manage_pledge"),
        PROJECT_ALERTS("project_alerts"),
        TWO_FACTOR_AUTH("two_factor_auth")
    }

    /**
     * Represents contextual information about the section of a page firing the event.
     *
     * @param contextName: The name of the section context.
     *
     * CAMPAIGN: The campaign section of the project screen.
     * COMMENTS: The comments section of the project screen.
     * OVERVIEW: The overview section of the project screen.
     * UPDATES: The updates section of the project screen.
     */
    enum class ContextSectionName(val contextName: String) {
        CAMPAIGN("campaign"),
        COMMENTS("comments"),
        DASHBOARD("dashboard"),
        OVERVIEW("overview"),
        UPDATES("updates"),
        RISKS("risks"),
        STORY("campaign"),
        FAQS("faq"),
        ENVIRONMENT("environment"),
        AI("use_of_ai"),
    }

    /**
     * Indicates where on the page or screen the event fired based on the type of location.
     *
     * @param contextName: The name of the location context.
     *
     * DISCOVER_ADVANCED: The discover screen location.
     * DISCOVER_OVERLAY: The overflow menu on the discover screen.
     * GLOBAL_NAV: The global navigation location.
     */
    enum class LocationContextName(val contextName: String) {
        DISCOVER_ADVANCED("discover_advanced"),
        DISCOVER_OVERLAY("discover_overlay"),
        SEARCH_RESULTS("search_results"),
        GLOBAL_NAV("global_nav"),
        CURATED("curated"),
        COMMENTS("comments")
    }

    /**
     * Context type values specific to the discovery filters in the overflow menu on the discovery
     * screen
     *
     * @param contextName: The name of the discovery context.
     *
     * ALL: The name of the all projects filter.
     * CATEGORY_NAME: The name of the category filter.
     * PWL: The projects we love filter in the overflow menu.
     * RECOMMENDED: The recommended filter in the overflow menu.
     * SOCIAL: The social filter in the overflow menu.
     * SUBCATEGORY_NAME: The subcategory name under the category filter in the overflow menu.
     * WATCHED: The watched filter in the overflow menu.
     * RESULTS: Represents when a project is tapped from the results on the discover screen.
     */
    enum class DiscoveryContextType(val contextName: String) {
        ALL("all"),
        CATEGORY_NAME("category_name"),
        PWL("pwl"),
        RECOMMENDED("recommended"),
        SOCIAL("social"),
        SUBCATEGORY_NAME("subcategory_name"),
        WATCHED("watched"),
        RESULTS("results")
    }

    /**
     * Contextual detail valyes about an event that was fired that aren't captured in other context properties
     *
     * @param contextName: The name of the context type.
     *
     * CREDIT_CARD: Represents if the user has used a credit card during checkout.
     * WATCH: Represents when a user watches a project.
     * UNWATCH: Represents when a user unwatches a project.
     * FACEBOOK: Represents when a user logs in with facebook.
     */
    enum class ContextTypeName(val contextName: String) {
        CREDIT_CARD("credit_card"),
        WATCH("watch"),
        UNWATCH("unwatch"),
        FACEBOOK("facebook"),
        RESULTS("results"),
        ROOT("root"),
        REPLY("reply"),
        ADDRESS("address")
    }

    /**
     * Indicates where on the page or screen the event fired based on the type of video.
     *
     * @param contextName: The name of the video context.
     *
     * LENGTH: Length of video.
     * POSITION: Index position of the video playhead.
     */
    enum class VideoContextName(val contextName: String) {
        LENGTH("length"),
        POSITION("position")
    }
}
