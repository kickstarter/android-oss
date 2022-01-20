package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Photo private constructor(
    private val ed: String,
    private val full: String,
    private val little: String,
    private val med: String,
    private val small: String,
    private val thumb: String,
) : Parcelable {
    fun ed() = this.ed
    fun full() = this.full
    fun little() = this.little
    fun med() = this.med
    fun small() = this.small
    fun thumb() = this.thumb

    @Parcelize
    data class Builder(
        private var ed: String = "",
        private var full: String = "",
        private var little: String = "",
        private var med: String = "",
        private var small: String = "",
        private var thumb: String = "",
    ) : Parcelable {
        fun ed(ed: String?) = apply { this.ed = ed ?: "" }
        fun full(full: String?) = apply { this.full = full ?: "" }
        fun little(little: String?) = apply { this.little = little ?: "" }
        fun med(med: String?) = apply { this.med = med ?: "" }
        fun small(small: String?) = apply { this.small = small ?: "" }
        fun thumb(thumb: String?) = apply { this.thumb = thumb ?: "" }
        fun build() = Photo(
            ed = ed,
            full = full,
            little = little,
            med = med,
            small = small,
            thumb = thumb
        )
    }

    fun toBuilder() = Builder(
        ed = ed,
        full = full,
        little = little,
        med = med,
        small = small,
        thumb = thumb
    )

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is Photo) {
            equals = ed() == other.ed() &&
                full() == other.full() &&
                little() == other.little() &&
                med() == other.med() &&
                small() == other.small() &&
                thumb() == other.thumb()
        }
        return equals
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
