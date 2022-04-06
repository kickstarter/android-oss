package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class ResetPasswordBody private constructor(
    private val email: String
) : Parcelable {
    fun email() = this.email

    @Parcelize
    data class Builder(
        private var email: String = ""
    ) : Parcelable {
        fun email(email: String) = apply { this.email = email }
        fun build() = ResetPasswordBody(
            email = email
        )
    }

    fun toBuilder() = Builder(
        email = email
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is ResetPasswordBody) {
            equals = email() == obj.email()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
