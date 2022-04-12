package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class PushTokenBody private constructor(
    private val pushServer: String?,
    private val token: String?
) : Parcelable {
    fun pushServer() = this.pushServer
    fun token() = this.token

    @Parcelize
    data class Builder(
        private var pushServer: String? = null,
        private var token: String? = null
    ) : Parcelable {
        fun pushServer(pushServer: String?) = apply { this.pushServer = pushServer }
        fun token(token: String?) = apply { this.token = token }
        fun build() = PushTokenBody(
            pushServer = pushServer,
            token = token
        )
    }

    fun toBuilder() = Builder(
        pushServer = pushServer,
        token = token
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is PushTokenBody) {
            equals = pushServer() == other.pushServer() &&
                token() == other.token()
        }
        return equals
    }
}
