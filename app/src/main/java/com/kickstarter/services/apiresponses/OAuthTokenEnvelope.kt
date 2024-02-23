package com.kickstarter.services.apiresponses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class OAuthTokenEnvelope private constructor(
    private val token: String,
) : Parcelable {
    fun accessToken() = this.token

    @Parcelize
    data class Builder(
        private var token: String = "",
    ) : Parcelable {
        fun accessToken(accessToken: String) = apply { this.token = accessToken }
        fun build() = OAuthTokenEnvelope(
            token = token
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is OAuthTokenEnvelope) {
            equals = accessToken() == obj.accessToken()
        }
        return equals
    }

    fun toBuilder() = Builder(
        token = token
    )

    override fun hashCode(): Int {
        return token.hashCode()
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
