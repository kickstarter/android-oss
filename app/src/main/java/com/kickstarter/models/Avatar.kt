package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Avatar Data Structure
 *
 * Will hold the different URL's for the different sizes the user's image avatar can have
 */
@Parcelize
class Avatar private constructor(
    private val medium: String,
    private val small: String,
    private val thumb: String
) : Parcelable {

    fun small() = this.small
    fun medium() = this.medium
    fun thumb() = this.thumb

    @Parcelize
    data class Builder(
        private var medium: String = "",
        private var small: String = "",
        private var thumb: String = ""
    ) : Parcelable {
        fun medium(med: String?) = apply { med?.let { this.medium = it } }
        fun small(sma: String?) = apply { sma?.let { this.small = it } }
        fun thumb(thu: String?) = apply { thu?.let { this.thumb = it } }
        fun build() = Avatar(medium = medium, small = small, thumb = thumb)
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    fun toBuilder() = Builder(medium = this.medium, small = this.small, thumb = this.thumb)

    override fun equals(other: Any?): Boolean =
        if (other is Avatar) {
            other.medium == this.medium && other.small == this.small &&
                other.thumb == this.thumb
        } else false
}
