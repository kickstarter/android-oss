package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * AiDisclosure Data Structure
 *
 * Note: This data model is written in kotlin and using kotlin
 * parcelize, it is meant to be used only with GraphQL
 * networking client.
 */
@Parcelize
class AiDisclosure private constructor(
    val fundingForAiAttribution: Boolean,
    val fundingForAiConsent: Boolean,
    val fundingForAiOption: Boolean,
    val generatedByAiConsent: String,
    val generatedByAiDetails: String,
    val id: Long,
    val otherAiDetails: String
) : Parcelable {

    @Parcelize
    data class Builder(
        var fundingForAiAttribution: Boolean = false,
        var fundingForAiConsent: Boolean = false,
        var fundingForAiOption: Boolean = false,
        var generatedByAiConsent: String = "",
        var generatedByAiDetails: String = "",
        var id: Long = -1,
        var otherAiDetails: String = ""
    ) : Parcelable {
        fun id(id: Long) = apply { this.id = id }
        fun fundingForAiAttribution(aiAttribution: Boolean?) = apply { this.fundingForAiAttribution = aiAttribution ?: false }
        fun fundingForAiConsent(aiConsent: Boolean?) = apply { this.fundingForAiConsent = aiConsent ?: false }
        fun fundingForAiOption(aiOption: Boolean?) = apply { this.fundingForAiOption = aiOption ?: false }
        fun generatedByAiConsent(gByAiConsent: String?) = apply { this.generatedByAiConsent = gByAiConsent ?: "" }
        fun generatedByAiDetails(gByAiDetails: String?) = apply { this.generatedByAiDetails = gByAiDetails ?: "" }
        fun otherAiDetails(oAiDetails: String?) = apply { this.otherAiDetails = oAiDetails ?: "" }
        fun build() = AiDisclosure(
            id = id,
            fundingForAiAttribution = fundingForAiAttribution,
            fundingForAiConsent = fundingForAiConsent,
            fundingForAiOption = fundingForAiOption,
            generatedByAiConsent = generatedByAiConsent,
            generatedByAiDetails = generatedByAiDetails,
            otherAiDetails = otherAiDetails
        )
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        id = id,
        fundingForAiAttribution = fundingForAiAttribution,
        fundingForAiConsent = fundingForAiConsent,
        fundingForAiOption = fundingForAiOption,
        generatedByAiConsent = generatedByAiConsent,
        generatedByAiDetails = generatedByAiDetails,
        otherAiDetails = otherAiDetails
    )

    override fun equals(other: Any?): Boolean =
        if (other is AiDisclosure) {
            other.id == this.id &&
                other.fundingForAiAttribution == this.fundingForAiAttribution &&
                other.fundingForAiConsent == this.fundingForAiConsent &&
                other.fundingForAiOption == this.fundingForAiOption &&
                other.generatedByAiConsent == this.generatedByAiConsent &&
                other.generatedByAiDetails == this.generatedByAiDetails &&
                other.otherAiDetails == this.otherAiDetails
        } else false
}
