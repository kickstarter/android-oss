package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class XauthBody private constructor(
    private val email: String,
    private val password: String,
    private val code: String?
) : Parcelable {
    fun email() = this.email
    fun password() = this.password
    fun code() = this.code

    @Parcelize
    data class Builder(
        private var email: String = "",
        private var password: String = "",
        private var code: String? = null
    ) : Parcelable {
        fun email(email: String) = apply { this.email = email }
        fun password(password: String) = apply { this.password = password }
        fun code(code: String?) = apply { this.code = code }
        fun build() = XauthBody(
            email = email,
            password = password,
            code = code
        )
    }

    fun toBuilder() = Builder(
        email = email,
        password = password,
        code = code
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is XauthBody) {
            equals = email == obj.email &&
                password == obj.password &&
                code == obj.code
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
