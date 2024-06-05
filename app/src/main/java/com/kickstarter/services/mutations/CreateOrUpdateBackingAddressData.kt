package com.kickstarter.services.mutations

/***
 * CreateOrUpdateBackingAddressData is the corresponding internal data model to the GraphQL model
 * [CreateOrUpdateBackingAddressInput]
 */
data class CreateOrUpdateBackingAddressData(
    val backingId : String,
    val addressID : String,
    val clientMutationId : String? = null
)