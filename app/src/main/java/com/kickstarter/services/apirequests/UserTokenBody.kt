package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class UserTokenBody private constructor(
    private val oauth_token: String?
) : Parcelable {
    fun token() = this.oauth_token

    @Parcelize
    data class Builder(
        private var pushServer: String? = null,
        private var token: String? = null
    ) : Parcelable {
        fun token(token: String?) = apply { this.token = token }
        fun build() = UserTokenBody(
            oauth_token = token
        )
    }

    fun toBuilder() = Builder(
        token = oauth_token
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is UserTokenBody) {
            equals =
                token() == other.token()
        }
        return equals
    }

    override fun hashCode(): Int {
        return oauth_token?.hashCode() ?: 0
    }
}
