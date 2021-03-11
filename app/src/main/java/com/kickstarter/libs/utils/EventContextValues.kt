package com.kickstarter.libs.utils

class EventContextValues {

    /**
     * Determines the button that was clicked or tapped by a user.
     *
     * @param contextName: The name of the event context.
     *
     * ADD_ONS_CONTINUE: The add-ons screen/carousel for a reward.
     * PLEDGE_INITIATE: The checkout screen.
     * PLEDGE_SUBMIT: The project screen.
     * REWARD_CONTINUE: The Rewards carousel.
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
        CAMPAIGN_DETAILS("campaign_details")
    }

    /**
     * Determines which screen has been presented.
     *
     * @param contextName: The name of the event context.
     *
     * ADD_ONS: The add-ons screen/carousel for a reward.
     * CHECKOUT: The checkout screen.
     * PROJECT: The project screen.
     * REWARDS: The Rewards carousel.
     * THANKS: The Thanks page.
     */
    enum class PageViewedContextName(val contextName: String) {
        ACTIVITY_FEED("activity_feed"),
        ADD_ONS("add_ons"),
        CHECKOUT("checkout"),
        DISCOVER("discover"),
        EXPLORE("explore"),
        PROFILE("profile"),
        PROJECT("project"),
        REWARDS("rewards"),
        SEARCH("search"),
        THANKS("thanks"),
        UPDATE_PLEDGE("update_pledge")
    }

    enum class ProjectContextSectionName(val contextName: String) {
        CAMPAIGN("campaign"),
        COMMENTS("comments"),
        OVERVIEW("overview"),
        UPDATES("updates")
    }

    enum class LocationContextName(val contextName: String) {
        DISCOVER_ADVANCED("discover_advanced"),
        DISCOVER_OVERLAY("discover_overlay"),
        GLOBAL_NAV("global_nav")
    }

    enum class DiscoveryContextType(val contextName: String) {
        CATEGORY_NAME("category_name"),
        PWL("pwl"),
        RECOMMENDED("recommended"),
        SOCIAL("social"),
        SUBCATEGORY_NAME("subcategory_name"),
        WATCHED("watched")
    }

    enum class ContextTypeName(val contextName: String) {
        CREDIT_CARD("credit_card"),
        WATCH("watch"),
        UNWATCH("unwatch")
    }
}