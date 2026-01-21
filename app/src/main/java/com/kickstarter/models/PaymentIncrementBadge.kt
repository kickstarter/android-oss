package com.kickstarter.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentIncrementBadge(
    val copy: String,
    val variant: PaymentIncrementBadgeVariant
) : Parcelable {
    fun copy() = this.copy
    fun variant() = this.variant

    @Parcelize
    data class Builder(
        var copy: String = "",
        var variant: PaymentIncrementBadgeVariant = PaymentIncrementBadgeVariant.GRAY,
    ) : Parcelable {
        fun copy(copy: String) = apply { this.copy = copy }
        fun variant(variant: PaymentIncrementBadgeVariant) = apply { this.variant = variant }

        fun build() = PaymentIncrementBadge(
            copy = copy,
            variant = variant,
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is PaymentIncrementBadge) {
            equals = copy() == obj.copy() &&
                variant() == obj.variant()
        }
        return equals
    }

    fun toBuilder() = Builder(
        copy = copy,
        variant = variant,
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
