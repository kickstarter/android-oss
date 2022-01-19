package com.kickstarter.libs.utils

/**
 * Represents the different context keys for properties sent to analytics
 *
 * @param contextName: The name of the context key.
 *
 * CONTEXT_CTA: Key that stores the button that was tapped.
 * CONTEXT_PAGE: Key that stores the page that was viewed.
 * CONTEXT_TYPE: Key that stores contextual details about an event that was
 * fired that aren't captured in other context properties
 */
enum class ContextPropertyKeyName(val contextName: String) {
    CONTEXT_CTA("context_cta"),
    CONTEXT_PAGE("context_page"),
    CONTEXT_SECTION("context_section"),
    CONTEXT_TYPE("context_type"),
    CONTEXT_LOCATION("context_location"),
    CONTEXT_DISCOVER_SORT("discover_sort"),
    COMMENT_BODY("comment_body"),
    COMMENT_CHARACTER_COUNT("comment_character_count"),
    COMMENT_ID("comment_id"),
    COMMENT_ROOT_ID("comment_root_id"),
    PROJECT_UPDATE_ID("project_update_id")
}
