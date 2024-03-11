package com.kickstarter.libs

import android.annotation.SuppressLint
import com.kickstarter.libs.utils.AnalyticEventsUtils
import com.kickstarter.libs.utils.ContextPropertyKeyName
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventName
import com.kickstarter.models.Project
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.mutations.CreateAttributionEventData
import com.kickstarter.services.transformers.encodeRelayId
import com.kickstarter.ui.data.ProjectData
import timber.log.Timber

/**
 * Similar to the [AnalyticEvents] class but specifically for sending attribution events to KSR
 * event attribution backend.
 *
 */
class AttributionEvents(
    val apolloClient: ApolloClientTypeV2
) {
    /**
     * Sends data to the backend for event attribution when the projects screen is loaded.
     *
     * @param projectData: The selected project data.
     */
    fun trackProjectPageViewed(projectData: ProjectData) {
        val props: HashMap<String, Any> = hashMapOf(ContextPropertyKeyName.CONTEXT_PAGE.contextName to EventContextValues.ContextPageName.PROJECT.contextName)
        props["context_page_url"] = projectData.fullDeeplink()?.toString() ?: ""
        props["session_device_type"] = "phone" // TODO Remove when https://kickstarter.atlassian.net/browse/MBL-1275 is complete
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        track(EventName.PROJECT_PAGE_VIEWED.eventName, props, projectData.project())
    }

    @SuppressLint("CheckResult")
    private fun track(eventName: String, eventProperties: Map<String, Any?>, project: Project) {
        val attributionEventData = CreateAttributionEventData(
            eventName = eventName,
            eventProperties = eventProperties,
            projectId = encodeRelayId(project)
        )
        createAttributionEvent(attributionEventData)
            .subscribe {
                Timber.tag("Event Attribution").d("Project page viewed sent to backend: %s", it)
            }
    }

    private fun createAttributionEvent(eventInput: CreateAttributionEventData) =
        this.apolloClient.createAttributionEvent(eventInput)
            .materialize()
            .share()
}
