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
    CONTEXT_TYPE("context_type")
}
