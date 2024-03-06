package com.kickstarter.libs

import com.kickstarter.libs.utils.AnalyticEventsUtils
import com.kickstarter.libs.utils.ContextPropertyKeyName
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventName
import com.kickstarter.ui.data.ProjectData
import timber.log.Timber

/*
Similar to the AnalyticEvents class but specifically for sending  KSR backend
 */
class AttributionEvents(private val trackingClient: TrackingClientType?) {
    /**
     * Sends data to the client when the projects screen is loaded.
     *
     * @param projectData: The selected project data.
     * @param pageSectionContext: The section of the project page being viewed.
     */
    fun trackProjectPageViewed(projectData: ProjectData, pageSectionContext: String) {
        val props: HashMap<String, Any> = hashMapOf(ContextPropertyKeyName.CONTEXT_PAGE.contextName to EventContextValues.ContextPageName.PROJECT.contextName)
        props[ContextPropertyKeyName.CONTEXT_SECTION.contextName] = pageSectionContext
        props["context_page_url"] = projectData.fullDeeplink().toString()

        Timber.tag("YC").d("Project page viewed properties deeplink: %s", props.toString())

        props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), trackingClient?.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        trackingClient?.track(EventName.PAGE_VIEWED.eventName, props)
    }
}