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
    val currencyCode : CurrencyCode?,
    val currencySymbol: String?,
    val projectName: String?,
    val projectId: String?,
    val projectSlug: String?,
    val imageUrl: String?,
    val creatorName: String?,
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
    fun creatorName() = this.creatorName
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
        var creatorName: String? = null,
        var viewType: PPOCardViewType? = null,
    ) : Parcelable {

        fun backingId(backingId : String?) = apply { this.backingId = backingId }
        fun address(address : AddressEnvelope?) = apply { this.address = address }
        fun amount(amount : String?) = apply { this.amount = amount }
        fun currencyCode(currencyCode : CurrencyCode?) = apply { this.currencyCode = currencyCode }
        fun currencySymbol(currencySymbol : String?) = apply { this.currencySymbol = currencySymbol }
        fun projectName(projectName : String?) = apply { this.projectName = projectName }
        fun projectId(projectId : String?) = apply { this.projectId = projectId }
        fun projectSlug(projectSlug : String?) = apply { this.projectSlug = projectSlug }
        fun imageUrl(imageUrl : String?) = apply { this.imageUrl = imageUrl }
        fun creatorName(creatorName : String?) = apply { this.creatorName = creatorName }
        fun viewType(viewType : PPOCardViewType?) = apply { this.viewType = viewType }

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
            creatorName = creatorName,
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
        creatorName = creatorName,
        viewType = viewType,
        )

    companion object {
        @JvmStatic
        fun builder() =  Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PPOCard) {
            equals =  backingId() == other.backingId() &&
                    address() == other.address() &&
                    amount() == other.amount() &&
                    currencyCode() == other.currencyCode() &&
                    currencySymbol() == other.currencySymbol() &&
                    projectName() == other.projectName() &&
                    projectId() == other.projectId() &&
                    projectSlug() == other.projectSlug() &&
                    creatorName() == other.creatorName() &&
                    imageUrl() == other.imageUrl() &&
                    viewType() == other.viewType()
        }
        return equals
    }

}