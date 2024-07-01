package com.kickstarter.features.pledgedprojectsoverview.data

import android.os.Parcelable
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import kotlinx.parcelize.Parcelize

@Parcelize
class PledgedProjectsOverviewEnvelope private constructor(
    val ppoCardData : List<PPOCard>?,
    val pageInfoEnvelope: PageInfoEnvelope,
    val totalCount: Int?,
    val categories : List<Category>?
    ) : Parcelable {

        fun pledges() = this.ppoCardData
        fun pageInfoEnvelope() = this.pageInfoEnvelope
        fun totalCount() = this.totalCount
        fun categories() = this.categories
        @Parcelize
        data class Builder(
            var PPOCard: List<PPOCard>? = null,
            var pageInfoEnvelope: PageInfoEnvelope = PageInfoEnvelope.builder().build(),
            var totalCount: Int? = null,
            var categories: List<Category>? = null,
        ) : Parcelable {

            fun pledges(PPOCard : List<PPOCard>?) = apply { this.PPOCard = PPOCard }
            fun pageInfoEnvelope(pageInfoEnvelope: PageInfoEnvelope) = apply { this.pageInfoEnvelope = pageInfoEnvelope }
            fun totalCount(totalCount: Int?) = apply { this.totalCount = totalCount }

            fun categories(categories: List<Category>?) = apply { this.categories = categories }

            fun build() = PledgedProjectsOverviewEnvelope(
                ppoCardData = PPOCard,
                pageInfoEnvelope = pageInfoEnvelope,
                totalCount = totalCount,
                categories = categories
            )
        }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        PPOCard = ppoCardData,
        pageInfoEnvelope = pageInfoEnvelope,
        totalCount = totalCount,
        categories = categories
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PledgedProjectsOverviewEnvelope) {
            equals = pledges() == other.pledges() &&
                    categories() == other.categories() &&
                    pageInfoEnvelope() == other.pageInfoEnvelope() &&
                    totalCount() == other.totalCount()
        }
        return equals
    }
}