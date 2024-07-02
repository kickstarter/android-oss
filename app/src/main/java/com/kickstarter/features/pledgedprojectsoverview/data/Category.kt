package com.kickstarter.features.pledgedprojectsoverview.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Category private constructor(
    val count: Int?,
    val slug: String?,
    val title: String?
) : Parcelable {

    fun count() = this.count

    fun slug() = this.slug

    fun title() = this.title

    @Parcelize
    data class Builder(
        private var count: Int? = null,
        private var slug: String? = null,
        private var title: String? = null,
    ) : Parcelable {
        fun count(count: Int?) = apply { this.count = count }
        fun slug(slug: String?) = apply { this.slug = slug }
        fun title(title: String?) = apply { this.title = title }
        fun build() = Category(
            count = count,
            slug = slug,
            title = title
        )
    }

    fun toBuilder() = Builder(
        count = count,
        slug = slug,
        title = title
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Category) {
            equals = count() == other.count() &&
                slug() == other.slug() &&
                title() == other.title()
        }
        return equals
    }
}
