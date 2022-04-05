package com.kickstarter.services.apiresponses

import android.os.Parcelable
import com.kickstarter.models.User
import kotlinx.parcelize.Parcelize

@Parcelize
class AccessTokenEnvelope private constructor(
    private val accessToken: String,
    private val user: User
) : Parcelable {
    fun accessToken() = this.accessToken
    fun user() = this.user

    @Parcelize
    data class Builder(
        private var accessToken: String = "",
        private var user: User = User.builder().build()
    ) : Parcelable {
        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }
        fun user(user: User) = apply { this.user = user }
        fun build() = AccessTokenEnvelope(
            accessToken = accessToken,
            user = user
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is AccessTokenEnvelope) {
            equals = accessToken() == obj.accessToken() &&
                user() == obj.user()
        }
        return equals
    }

    fun toBuilder() = Builder(
        accessToken = accessToken,
        user = user
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
