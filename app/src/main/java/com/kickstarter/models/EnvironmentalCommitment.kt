package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * EnvironmentalCommitments Data Structure
 *
 * Note: This data model is written in kotlin and using kotlin
 * parcelize because it's meant to be used only with GraphQL
 * networking client.
 */
@Parcelize
class EnvironmentalCommitment private constructor(
    val id: Long,
    val description: String,
    val category: String
) : Parcelable {

    @Parcelize
    data class Builder(
        var id: Long = -1,
        var description: String = "",
        var category: String = ""
    ) : Parcelable {
        fun id(id: Long) = apply { this.id = id }
        fun description(description: String) = apply { this.description = description }
        fun category(category: String) = apply { this.category = category }
        fun build() = EnvironmentalCommitment(id = id, description = description, category = category)
    }

    companion object {
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(id = this.id, description = this.description, category = category)

    override fun equals(other: Any?): Boolean =
        if (other is EnvironmentalCommitment) {
            other.id == this.id && other.description == this.description &&
                other.category == this.category
        } else false
}
