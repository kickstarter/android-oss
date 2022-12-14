package com.kickstarter.services.apiresponses.updatesresponse

import android.os.Parcelable
import com.kickstarter.models.ApolloEnvelope
import com.kickstarter.models.Update
import com.kickstarter.services.apiresponses.commentresponse.PageInfoEnvelope
import kotlinx.android.parcel.Parcelize

@Parcelize
class UpdatesGraphQlEnvelope(
    val updates: List<Update>?,
    val pageInfoEnvelope: PageInfoEnvelope?,
    val totalCount: Int?
) : Parcelable, ApolloEnvelope {

    @Parcelize
    data class Builder(
        var updates: List<Update>? = null,
        var pageInfoEnvelope: PageInfoEnvelope? = null,
        var totalCount: Int? = null
    ) : Parcelable {

        fun updates(updates: List<Update>?) = apply { this.updates = updates }
        fun pageInfoEnvelope(pageInfoEnvelope: PageInfoEnvelope?) = apply { this.pageInfoEnvelope = pageInfoEnvelope }
        fun totalCount(totalCount: Int?) = apply { this.totalCount = totalCount }
        fun build() = UpdatesGraphQlEnvelope(updates, pageInfoEnvelope, totalCount)
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(this.updates, this.pageInfoEnvelope, this.totalCount)
    override fun pageInfoEnvelope(): PageInfoEnvelope? = this.pageInfoEnvelope
}
