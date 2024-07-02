package com.kickstarter.features.pledgedprojectsoverview.data

import android.os.Parcelable
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import kotlinx.parcelize.Parcelize
import type.CurrencyCode

@Parcelize
class PPOCard private constructor(
    val backingId: String?,
    val address: AddressEnvelope?,
    val amount: String?,
    val currencyCode: CurrencyCode?,
    val currencySymbol: String?,
    val projectName: String?,
    val projectId: String?,
    val projectSlug: String?,
    val imageUrl: String?,
    val imageContentDescription: String?,
    val creatorName: String?,
    val backingDetailsUrl: String?,
    val showBadge: Boolean,
    val timeNumberForAction: Int,
    val viewType: PPOCardViewType?

) : Parcelable {

    fun backingId() = this.backingId
    fun address() = this.address
    fun amount() = this.amount
    fun currencyCode() = this.currencyCode
    fun currencySymbol() = this.currencySymbol
    fun projectName() = this.projectName
    fun projectId() = this.projectId
    fun projectSlug() = this.projectSlug
    fun imageUrl() = this.imageUrl
    fun imageContentDescription() = this.imageContentDescription
    fun creatorName() = this.creatorName
    fun backingDetailsUrl() = this.backingDetailsUrl
    fun showBadge() = this.showBadge
    fun timeNumberForAction() = this.timeNumberForAction
    fun viewType() = this.viewType

    @Parcelize
    data class Builder(
        var backingId: String? = null,
        var address: AddressEnvelope? = null,
        var amount: String? = null,
        var currencyCode: CurrencyCode? = null,
        var currencySymbol: String? = null,
        var projectName: String? = null,
        var projectId: String? = null,
        var projectSlug: String? = null,
        var imageUrl: String? = null,
        var imageContentDescription: String? = null,
        var creatorName: String? = null,
        var backingDetailsUrl: String? = null,
        var showBadge: Boolean = false,
        var timeNumberForAction: Int = 0,
        var viewType: PPOCardViewType? = null,
    ) : Parcelable {

        fun backingId(backingId: String?) = apply { this.backingId = backingId }
        fun address(address: AddressEnvelope?) = apply { this.address = address }
        fun amount(amount: String?) = apply { this.amount = amount }
        fun currencyCode(currencyCode: CurrencyCode?) = apply { this.currencyCode = currencyCode }
        fun currencySymbol(currencySymbol: String?) = apply { this.currencySymbol = currencySymbol }
        fun projectName(projectName: String?) = apply { this.projectName = projectName }
        fun projectId(projectId: String?) = apply { this.projectId = projectId }
        fun projectSlug(projectSlug: String?) = apply { this.projectSlug = projectSlug }
        fun imageUrl(imageUrl: String?) = apply { this.imageUrl = imageUrl }
        fun imageContentDescription(imageContentDescription: String?) = apply { this.imageContentDescription = imageContentDescription }
        fun creatorName(creatorName: String?) = apply { this.creatorName = creatorName }
        fun backingDetailsUrl(backingDetailsUrl: String?) = apply { this.backingDetailsUrl = backingDetailsUrl }
        fun timeNumberForAction(timeNumberForAction: Int) = apply { this.timeNumberForAction = timeNumberForAction }
        fun showBadge(showBadge: Boolean) = apply { this.showBadge = showBadge }
        fun viewType(viewType: PPOCardViewType?) = apply { this.viewType = viewType }

        fun build() = PPOCard(
            backingId = backingId,
            address = address,
            amount = amount,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            projectName = projectName,
            projectId = projectId,
            projectSlug = projectSlug,
            imageUrl = imageUrl,
            imageContentDescription = imageUrl,
            creatorName = creatorName,
            backingDetailsUrl = backingDetailsUrl,
            showBadge = showBadge,
            timeNumberForAction = timeNumberForAction,
            viewType = viewType,
        )
    }

    fun toBuilder() = Builder(
        backingId = backingId,
        address = address,
        amount = amount,
        currencyCode = currencyCode,
        currencySymbol = currencySymbol,
        projectName = projectName,
        projectId = projectId,
        projectSlug = projectSlug,
        imageUrl = imageUrl,
        imageContentDescription = imageContentDescription,
        creatorName = creatorName,
        backingDetailsUrl = backingDetailsUrl,
        showBadge = showBadge,
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
                address() == other.address() &&
                amount() == other.amount() &&
                currencyCode() == other.currencyCode() &&
                currencySymbol() == other.currencySymbol() &&
                projectName() == other.projectName() &&
                projectId() == other.projectId() &&
                projectSlug() == other.projectSlug() &&
                creatorName() == other.creatorName() &&
                imageUrl() == other.imageUrl() &&
                imageContentDescription() == other.imageContentDescription() &&
                backingDetailsUrl() == other.backingDetailsUrl() &&
                showBadge() == other.showBadge() &&
                timeNumberForAction() == other.timeNumberForAction() &&
                viewType() == other.viewType()
        }
        return equals
    }
}
