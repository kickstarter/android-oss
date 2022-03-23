package com.kickstarter.libs

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.RoundingMode

@Parcelize
class NumberOptions private constructor(
    private val bucketAbove: Float?,
    private val bucketPrecision: Int?,
    private val currencyCode: String?,
    private val currencySymbol: String?,
    private val precision: Int?,
    private val roundingMode: RoundingMode?
) : Parcelable {
    fun bucketAbove() = this.bucketAbove
    fun bucketPrecision() = this.bucketPrecision
    fun currencyCode() = this.currencyCode
    fun currencySymbol() = this.currencySymbol
    fun precision() = this.precision
    fun roundingMode() = this.roundingMode

    @Parcelize
    data class Builder(
        private var bucketAbove: Float? = null,
        private var bucketPrecision: Int? = null,
        private var currencyCode: String? = null,
        private var currencySymbol: String? = null,
        private var precision: Int? = null,
        private var roundingMode: RoundingMode? = null
    ) : Parcelable {
        fun bucketAbove(bucketAbove: Float?) = apply { this.bucketAbove = bucketAbove }
        fun bucketPrecision(bucketPrecision: Int?) = apply { this.bucketPrecision = bucketPrecision }
        fun currencyCode(currencyCode: String?) = apply { this.currencyCode = currencyCode }
        fun currencySymbol(currencySymbol: String?) = apply { this.currencySymbol = currencySymbol }
        fun precision(precision: Int?) = apply { this.precision = precision }
        fun roundingMode(roundingMode: RoundingMode?) = apply { this.roundingMode = roundingMode }

        fun build() = NumberOptions(
            bucketAbove = bucketAbove,
            bucketPrecision = bucketPrecision,
            currencyCode = currencyCode,
            currencySymbol = currencySymbol,
            precision = precision,
            roundingMode = roundingMode
        )
    }

    fun toBuilder() = Builder(
        bucketAbove = bucketAbove,
        bucketPrecision = bucketPrecision,
        currencyCode = currencyCode,
        currencySymbol = currencySymbol,
        precision = precision,
        roundingMode = roundingMode
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is NumberOptions) {
            equals = bucketAbove() == obj.bucketAbove() &&
                bucketPrecision() == obj.bucketPrecision() &&
                currencyCode() == obj.currencyCode() &&
                currencySymbol() == obj.currencySymbol() &&
                precision() == obj.precision() &&
                roundingMode() == obj.roundingMode()
        }
        return equals
    }

    val isCurrency: Boolean
        get() = currencySymbol() != null

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
