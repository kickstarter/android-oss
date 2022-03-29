package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class LoginWithFacebookBody private constructor(
    private val accessToken: String,
    private val code: String?,
) : Parcelable {
    fun accessToken() = this.accessToken
    fun code() = this.code

    @Parcelize
    data class Builder(
        private var accessToken: String = "",
        private var code: String? = null
    ) : Parcelable {
        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }
        fun code(code: String?) = apply { this.code = code }
        fun build() = LoginWithFacebookBody(
            accessToken = accessToken,
            code = code
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is LoginWithFacebookBody) {
            equals = accessToken() == obj.accessToken() &&
                code() == obj.code()
        }
        return equals
    }

    fun toBuilder() = Builder(
        accessToken = accessToken,
        code = code
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
