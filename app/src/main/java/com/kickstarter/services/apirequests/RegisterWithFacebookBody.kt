package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class RegisterWithFacebookBody private constructor(
    private val accessToken: String,
    private val newsletterOptIn: Boolean,
    private val sendNewsletters: Boolean,
) : Parcelable {
    fun accessToken() = this.accessToken
    fun newsletterOptIn() = this.newsletterOptIn
    fun sendNewsletters() = this.sendNewsletters

    @Parcelize
    data class Builder(
        private var accessToken: String = "",
        private var newsletterOptIn: Boolean = false,
        private var sendNewsletters: Boolean = false
    ) : Parcelable {
        fun accessToken(accessToken: String) = apply { this.accessToken = accessToken }
        fun newsletterOptIn(newsletterOptIn: Boolean) = apply { this.newsletterOptIn = newsletterOptIn }
        fun sendNewsletters(sendNewsletters: Boolean) = apply { this.sendNewsletters = sendNewsletters }
        fun build() = RegisterWithFacebookBody(
            accessToken = accessToken,
            newsletterOptIn = newsletterOptIn,
            sendNewsletters = sendNewsletters
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is RegisterWithFacebookBody) {
            equals = accessToken() == obj.accessToken() &&
                newsletterOptIn() == obj.newsletterOptIn() &&
                sendNewsletters() == obj.sendNewsletters()
        }
        return equals
    }

    fun toBuilder() = Builder(
        accessToken = accessToken,
        newsletterOptIn = newsletterOptIn,
        sendNewsletters = sendNewsletters
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
