package com.kickstarter.services.mutations

/***
 * UpdateBackerCompletedData is the corresponding internal data model to the GraphQL model
 * [UpdateBackerCompletedInput]
 */
data class UpdateBackerCompletedData(
    val backingID: String,
    val backerCompleted: Boolean,
    val clientMutationId: String? = null
)