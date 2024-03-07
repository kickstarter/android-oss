package com.kickstarter.libs

import android.annotation.SuppressLint
import com.kickstarter.libs.utils.AnalyticEventsUtils
import com.kickstarter.libs.utils.ContextPropertyKeyName
import com.kickstarter.libs.utils.EventContextValues
import com.kickstarter.libs.utils.EventName
import com.kickstarter.services.ApolloClientTypeV2
import com.kickstarter.services.mutations.CreateAttributionEventData
import com.kickstarter.ui.data.ProjectData
import timber.log.Timber

/**
 * Similar to the [AnalyticEvents] class but specifically for sending attribution events to KSR
 * event attribution backend. The trackingClient is [KsEventAttributionClient].
 *
 */
class AttributionEvents(
    val apolloClient: ApolloClientTypeV2
) {
    /**
     * Sends data to the backend for event attribution when the projects screen is loaded.
     *
     * @param projectData: The selected project data.
     * @param pageSectionContext: The section of the project page being viewed.
     */
    fun trackProjectPageViewed(projectData: ProjectData, pageSectionContext: String) {
        val props: HashMap<String, Any> = hashMapOf(ContextPropertyKeyName.CONTEXT_PAGE.contextName to EventContextValues.ContextPageName.PROJECT.contextName)
        props[ContextPropertyKeyName.CONTEXT_SECTION.contextName] = pageSectionContext
        props["context_page_url"] = projectData.fullDeeplink().toString()

        //props.putAll(AnalyticEventsUtils.projectProperties(projectData.project(), trackingClient?.loggedInUser()))
        props.putAll(AnalyticEventsUtils.refTagProperties(projectData.refTagFromIntent(), projectData.refTagFromCookie()))
        track(EventName.PROJECT_PAGE_VIEWED.eventName, props)
    }

    @SuppressLint("CheckResult")
    private fun track(eventName: String, eventProperties: Map<String, Any?>) {
        val attributionEventData = CreateAttributionEventData(
            eventName = eventName,
            eventProperties =  eventProperties,
            clientMutationId = "TBD" //TODO
        )
        createAttributionEvent(attributionEventData)
            .subscribe {
                Timber.tag("YC").d("YC project page viewed sent to backend: %s", it)
            }
    }

    private fun createAttributionEvent(eventInput: CreateAttributionEventData) =
        this.apolloClient.createAttributionEvent(eventInput)
            .doOnSubscribe {
                //TODO
            }
            .doAfterTerminate {
                //TODO
            }
            .materialize()
            .share()
}