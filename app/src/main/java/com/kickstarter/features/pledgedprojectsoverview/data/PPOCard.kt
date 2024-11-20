package com.kickstarter.features.pledgedprojectsoverview.data

import android.os.Parcelable
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import com.kickstarter.type.CurrencyCode
import kotlinx.parcelize.Parcelize

@Parcelize
class PPOCard private constructor(
    val deliveryAddress: DeliveryAddress?,
    val amount: String?,
    val backingId: String?,
    val backingDetailsUrl: String?,
    val clientSecret: String?,
    val creatorID: String?,
    val creatorName: String?,
    val currencyCode: CurrencyCode?,
    val currencySymbol: String?,
    val flags: List<Flag?>?,
    val imageContentDescription: String?,
    val imageUrl: String?,
    val projectId: String?,
    val projectName: String?,
    val projectSlug: String?,
    val timeNumberForAction: Int,
    val surveyID: String?,
    val viewType: PPOCardViewType?

) : Parcelable {

    fun deliveryAddress() = this.deliveryAddress
    fun amount() = this.amount
    fun backingDetailsUrl() = this.backingDetailsUrl
    fun backingId() = this.backingId
    fun clientSecret() = this.clientSecret
    fun creatorID() = this.creatorID
    fun surveyID() = this.surveyID
    fun creatorName() = this.creatorName
    fun currencyCode() = this.currencyCode
    fun currencySymbol() = this.currencySymbol
    fun flags() = this.flags
    fun imageContentDescription() = this.imageContentDescription
    fun imageUrl() = this.imageUrl
    fun projectId() = this.projectId
    fun projectName() = this.projectName
    fun projectSlug() = this.projectSlug
    fun timeNumberForAction() = this.timeNumberForAction
    fun viewType() = this.viewType

    @Parcelize
    data class Builder(
        var deliveryAddress: DeliveryAddress? = null,
        var amount: String? = null,
        var backingDetailsUrl: String? = null,
        var backingId: String? = null,
        var clientSecret: String? = null,
        var creatorID: String? = null,
        var surveyID: String? = null,
        var creatorName: String? = null,
        var currencyCode: CurrencyCode? = null,
        var currencySymbol: String? = null,
        var flags: List<Flag?>? = null,
        var imageContentDescription: String? = null,
        var imageUrl: String? = null,
        var projectId: String? = null,
        var projectName: String? = null,
        var projectSlug: String? = null,
        var timeNumberForAction: Int = 0,
        var viewType: PPOCardViewType? = null,
    ) : Parcelable {

        fun deliveryAddress(deliveryAddress: DeliveryAddress?) = apply { this.deliveryAddress = deliveryAddress }
        fun amount(amount: String?) = apply { this.amount = amount }
        fun backingDetailsUrl(backingDetailsUrl: String?) = apply { this.backingDetailsUrl = backingDetailsUrl }
        fun backingId(backingId: String?) = apply { this.backingId = backingId }
        fun clientSecret(clientSecret: String?) = apply { this.clientSecret = clientSecret }
        fun creatorID(creatorName: String?) = apply { this.creatorID = creatorID }
        fun surveyID(surveyID: String?) = apply { this.surveyID = surveyID }
        fun creatorName(creatorName: String?) = apply { this.creatorName = creatorName }
        fun currencyCode(currencyCode: CurrencyCode?) = apply { this.currencyCode = currencyCode }
        fun currencySymbol(currencySymbol: String?) = apply { this.currencySymbol = currencySymbol }
        fun flags(flags: List<Flag?>?) = apply { this.flags = flags }
        fun imageContentDescription(imageContentDescription: String?) = apply { this.imageContentDescription = imageContentDescription }
        fun imageUrl(imageUrl: String?) = apply { this.imageUrl = imageUrl }
        fun projectId(projectId: String?) = apply { this.projectId = projectId }
        fun projectName(projectName: String?) = apply { this.projectName = projectName }
        fun projectSlug(projectSlug: String?) = apply { this.projectSlug = projectSlug }
        fun timeNumberForAction(timeNumberForAction: Int) = apply { this.timeNumberForAction = timeNumberForAction }
        fun viewType(viewType: PPOCardViewType?) = apply { this.viewType = viewType }

        fun build() = PPOCard(
            deliveryAddress = deliveryAddress,
            amount = amount,
            backingDetailsUrl = backingDetailsUrl,
            backingId = backingId,
            clientSecret = clientSecret,
            creatorID = creatorID,
            surveyID = surveyID,
            creatorName = creatorName,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            flags = flags,
            imageContentDescription = imageUrl,
            imageUrl = imageUrl,
            projectId = projectId,
            projectName = projectName,
            projectSlug = projectSlug,
            timeNumberForAction = timeNumberForAction,
            viewType = viewType,
        )
    }

    fun toBuilder() = Builder(
        deliveryAddress = deliveryAddress,
        amount = amount,
        backingDetailsUrl = backingDetailsUrl,
        backingId = backingId,
        clientSecret = clientSecret,
        creatorID = creatorID,
        surveyID = surveyID,
        creatorName = creatorName,
        currencyCode = currencyCode,
        currencySymbol = currencySymbol,
        flags = flags,
        imageContentDescription = imageContentDescription,
        imageUrl = imageUrl,
        projectId = projectId,
        projectName = projectName,
        projectSlug = projectSlug,
        timeNumberForAction = timeNumberForAction,
        viewType = viewType,
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PPOCard) {
            equals = backingId() == other.backingId() &&
                deliveryAddress() == other.deliveryAddress() &&
                amount() == other.amount() &&
                clientSecret() == other.clientSecret() &&
                currencyCode() == other.currencyCode() &&
                currencySymbol() == other.currencySymbol() &&
                flags() == other.flags() &&
                projectName() == other.projectName() &&
                projectId() == other.projectId() &&
                projectSlug() == other.projectSlug() &&
                creatorID() == other.creatorID() &&
                surveyID() == other.surveyID() &&
                creatorName() == other.creatorName() &&
                imageUrl() == other.imageUrl() &&
                imageContentDescription() == other.imageContentDescription() &&
                backingDetailsUrl() == other.backingDetailsUrl() &&
                timeNumberForAction() == other.timeNumberForAction() &&
                viewType() == other.viewType()
        }
        return equals
    }
}
