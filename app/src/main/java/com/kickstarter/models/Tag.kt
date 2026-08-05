package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a single Open Call / Creative Prompt tag (e.g. Make 100, Zine Quest, Witchstarter).
 *
 * [id] is the numeric tag id, decoded from the GraphQL Relay `ID!` via `decodeRelayId`. It is
 * applied to a search through [com.kickstarter.services.DiscoveryParams.tagId] (an `Int`), so
 * callers convert with `id.toInt()` at that boundary.
 */
@Parcelize
data class Tag(
    val id: Long,
    val name: String,
    val url: String,
    val slug: String,
) : Parcelable {
    fun id() = this.id
    fun name() = this.name
    fun url() = this.url
    fun slug() = this.slug

    @Parcelize
    data class Builder(
        var id: Long = 0L,
        var name: String = "",
        var url: String = "",
        var slug: String = "",
    ) : Parcelable {
        fun id(id: Long) = apply { this.id = id }
        fun name(name: String) = apply { this.name = name }
        fun url(url: String) = apply { this.url = url }
        fun slug(slug: String) = apply { this.slug = slug }

        fun build() = Tag(
            id = id,
            name = name,
            url = url,
            slug = slug,
        )
    }

    fun toBuilder() = Builder(
        id = id,
        name = name,
        url = url,
        slug = slug,
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
