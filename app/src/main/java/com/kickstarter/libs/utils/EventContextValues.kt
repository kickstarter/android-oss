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
     */
    enum class CtaContextName(val contextName: String) {
        ADD_ONS_CONTINUE("add_ons_continue"),
        PLEDGE_INITIATE("pledge_initiate"),
        PLEDGE_SUBMIT("pledge_submit"),
        REWARD_CONTINUE("reward_continue"),
        DISCOVER_SORT("discover_sort"),
        DISCOVER_FILTER("discover_filter"),
        SEARCH("search"),
        DISCOVER("discover"),
        WATCH_PROJECT("watch_project"),
        LOGIN_INITIATE("log_in_initiate"),
        CAMPAIGN_DETAILS("campaign_details"),
        CREATOR_DETAILS("creator_details"),
        LOGIN_OR_SIGN_UP("log_in_or_sign_up"),
        LOGIN_SUBMIT("log_in_submit"),
        SIGN_UP_SUBMIT("sign_up_submit"),
        SIGN_UP_INITIATE("sign_up_initiate"),
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
     */
    enum class ContextPageName(val contextName: String) {
        ACTIVITY_FEED("activity_feed"),
        ADD_ONS("add_ons"),
        CHECKOUT("checkout"),
        DISCOVER("discover"),
        PROFILE("profile"),
        PROJECT("project"),
        REWARDS("rewards"),
        SEARCH("search"),
        THANKS("thanks"),
        UPDATE_PLEDGE("update_pledge"),
        CHANGE_PAYMENT("change_payment"),
        LOGIN_SIGN_UP("log_in_sign_up"),
        SIGN_UP("sign_up"),
        LOGIN("log_in")
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
        OVERVIEW("overview"),
        UPDATES("updates")
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
        GLOBAL_NAV("global_nav")
    }

    /**
     * Context type values specific to the discovery filters in the overflow menu on the discovery
     * screen
     *
     * @param contextName: The name of the discovery context.
     *
     * CATEGORY_NAME: The name of the category filter.
     * PWL: The projects we love filter in the overflow menu.
     * RECOMMENDED: The recommended filter in the overflow menu.
     * SOCIAL: The social filter in the overflow menu.
     * SUBCATEGORY_NAME: The subcategory name under the category filter in the overflow menu.
     * WATCHED: The watched filter in the overflow menu.
     */
    enum class DiscoveryContextType(val contextName: String) {
        CATEGORY_NAME("category_name"),
        PWL("pwl"),
        RECOMMENDED("recommended"),
        SOCIAL("social"),
        SUBCATEGORY_NAME("subcategory_name"),
        WATCHED("watched")
    }

    /**
     * Contextual detail valyes about an event that was fired that aren't captured in other context properties
     *
     * @param contextName: The name of the context type.
     *
     * CREDIT_CARD: Represents if the user has used a credit card during checkout.
     * WATCH: Represents when a user watches a project.
     * WATCH: Represents when a user unwatches a project.
     */
    enum class ContextTypeName(val contextName: String) {
        CREDIT_CARD("credit_card"),
        WATCH("watch"),
        UNWATCH("unwatch"),
        FACEBOOK("facebook")
    }
}
