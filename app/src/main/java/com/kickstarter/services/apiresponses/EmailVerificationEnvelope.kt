package com.kickstarter.services.apiresponses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class EmailVerificationEnvelope private constructor(
    private val message: String,
    private val code: Int
) : Parcelable {
    fun message() = this.message
    fun code() = this.code

    @Parcelize
    data class Builder(
        private var message: String = "",
        private var code: Int = 0
    ) : Parcelable {
        fun message(message: String) = apply { this.message = message }
        fun code(code: Int) = apply { this.code = code }
        fun build() = EmailVerificationEnvelope(
            message = message,
            code = code
        )
    }

    fun toBuilder() = Builder(
        message = message,
        code = code
    )

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is EmailVerificationEnvelope) {
            equals = other.code() == this.code() && other.message() == this.message()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
