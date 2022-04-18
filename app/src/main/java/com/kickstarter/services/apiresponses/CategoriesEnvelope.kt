package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.Category
import kotlinx.parcelize.Parcelize

@Parcelize
class CategoriesEnvelope private constructor(
    private val categories: List<Category>
) : Parcelable {
    fun categories() = this.categories

    @Parcelize
    data class Builder(
        private var categories: List<Category> = emptyList(),
    ) : Parcelable {
        fun categories(categories: List<Category>) = apply { this.categories = categories }
        fun build() = CategoriesEnvelope(
            categories = categories
        )
    }

    fun toBuilder() = Builder(
        categories = categories,
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is CategoriesEnvelope) {
            equals = categories() == obj.categories()
        }
        return equals
    }
}
