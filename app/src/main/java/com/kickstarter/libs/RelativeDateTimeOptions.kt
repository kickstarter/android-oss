package com.kickstarter.libs

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.joda.time.DateTime

@Parcelize
class RelativeDateTimeOptions private constructor(
    private val abbreviated: Boolean,
    private val absolute: Boolean,
    private val relativeToDateTime: DateTime?,
    private val threshold: Int
) : Parcelable {
    /**
     * Abbreviates string, e.g.: "in 1 hr"
     */
    fun abbreviated() = this.abbreviated

    /**
     * Don't output tense, e.g.: "1 hour" instead of "in 1 hour"
     */
    fun absolute() = this.absolute

    /**
     * Compare against this date instead of the current time
     */
    fun relativeToDateTime() = this.relativeToDateTime

    /**
     * Number of seconds difference permitted before an attempt to describe the relative date is abandoned.
     * For example, "738 days ago" is not helpful to users. The threshold defaults to 30 days.
     */
    fun threshold() = this.threshold

    @Parcelize
    data class Builder(
        private var abbreviated: Boolean = false,
        private var absolute: Boolean = false,
        private var relativeToDateTime: DateTime? = null,
        private var threshold: Int = THIRTY_DAYS_IN_SECONDS
    ) : Parcelable {
        fun abbreviated(abbreviated: Boolean) = apply { this.abbreviated = abbreviated }
        fun absolute(absolute: Boolean) = apply { this.absolute = absolute }
        fun relativeToDateTime(relativeToDateTime: DateTime?) = apply { this.relativeToDateTime = relativeToDateTime }
        fun threshold(threshold: Int) = apply { this.threshold = threshold }
        fun build() = RelativeDateTimeOptions(
            abbreviated = abbreviated,
            absolute = absolute,
            relativeToDateTime = relativeToDateTime,
            threshold = threshold
        )
    }

    fun toBuilder() = Builder(
        abbreviated = abbreviated,
        absolute = absolute,
        relativeToDateTime = relativeToDateTime,
        threshold = threshold
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is RelativeDateTimeOptions) {
            equals = abbreviated() == obj.abbreviated() &&
                absolute() == obj.absolute() &&
                relativeToDateTime() == obj.relativeToDateTime() &&
                threshold() == obj.threshold()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()

        private const val THIRTY_DAYS_IN_SECONDS = 60 * 60 * 24 * 30
    }
}
