package com.kickstarter.services.mutations

/***
 * CreateAttributionEventData is the corresponding internal data model to the GraphQL model
 * [CreateAttributionEventInput]
 */
data class CreateAttributionEventData(
    val eventName: String,
    val eventProperties: Map<String, String>? = null,
    val projectId: String? = null,
    val clientMutationId: String? = null
)
