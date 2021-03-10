package com.kickstarter.libs.utils

/**
 * Represents the type of event being sent
 *
 * @param eventName: The name of the event.
 *
 * CTA_CLICKED: Event when a CTA is tapped.
 * PAGE_VIEWED: Event when a screen is loaded.
 */
enum class EventName(val eventName: String) {
    CTA_CLICKED("CTA Clicked"),
    CARD_CLICKED("Card Clicked"),
    PAGE_VIEWED("Page Viewed")
}