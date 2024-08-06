package com.kickstarter.features.pledgedprojectsoverview.data

import android.os.Parcelable
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import kotlinx.parcelize.Parcelize

@Parcelize
class PledgedProjectsOverviewEnvelope private constructor(
    val ppoCards: List<PPOCard>?,
    val pageInfoEnvelope: PageInfoEnvelope,
    val totalCount: Int?,
) : Parcelable {

    fun pledges() = this.ppoCards
    fun pageInfoEnvelope() = this.pageInfoEnvelope
    fun totalCount() = this.totalCount
    @Parcelize
    data class Builder(
        var ppoCards: List<PPOCard>? = null,
        var pageInfoEnvelope: PageInfoEnvelope = PageInfoEnvelope.builder().build(),
        var totalCount: Int? = null,
    ) : Parcelable {

        fun pledges(ppoCards: List<PPOCard>?) = apply { this.ppoCards = ppoCards }
        fun pageInfoEnvelope(pageInfoEnvelope: PageInfoEnvelope) = apply { this.pageInfoEnvelope = pageInfoEnvelope }
        fun totalCount(totalCount: Int?) = apply { this.totalCount = totalCount }
        fun build() = PledgedProjectsOverviewEnvelope(
            ppoCards = ppoCards,
            pageInfoEnvelope = pageInfoEnvelope,
            totalCount = totalCount,
        )
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(
        ppoCards = ppoCards,
        pageInfoEnvelope = pageInfoEnvelope,
        totalCount = totalCount,
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PledgedProjectsOverviewEnvelope) {
            equals = pledges() == other.pledges() &&
                pageInfoEnvelope() == other.pageInfoEnvelope() &&
                totalCount() == other.totalCount()
        }
        return equals
    }
}
