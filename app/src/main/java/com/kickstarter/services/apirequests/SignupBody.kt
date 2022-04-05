package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SignupBody private constructor(
    private val name: String,
    private val email: String,
    private val newsletterOptIn: Boolean,
    private val password: String,
    private val passwordConfirmation: String,
    private val sendNewsletters: Boolean
) : Parcelable {
    fun name() = this.name
    fun email() = this.email
    fun password() = this.password
    fun passwordConfirmation() = this.passwordConfirmation
    fun newsletterOptIn() = this.newsletterOptIn
    fun sendNewsletters() = this.sendNewsletters

    @Parcelize
    data class Builder(
        private var name: String = "",
        private var email: String = "",
        private var password: String = "",
        private var passwordConfirmation: String = "",
        private var newsletterOptIn: Boolean = false,
        private var sendNewsletters: Boolean = false
    ) : Parcelable {
        fun name(name: String) = apply { this.name = name }
        fun email(email: String) = apply { this.email = email }
        fun password(password: String) = apply { this.password = password }
        fun passwordConfirmation(passwordConfirmation: String) = apply { this.passwordConfirmation = passwordConfirmation }
        fun newsletterOptIn(newsletterOptIn: Boolean) = apply { this.newsletterOptIn = newsletterOptIn }
        fun sendNewsletters(sendNewsletters: Boolean) = apply { this.sendNewsletters = sendNewsletters }

        fun build() = SignupBody(
            name = name,
            email = email,
            password = password,
            passwordConfirmation = passwordConfirmation,
            newsletterOptIn = newsletterOptIn,
            sendNewsletters = sendNewsletters
        )
    }

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is SignupBody) {
            equals = name == obj.name &&
                email == obj.email &&
                password == obj.password &&
                passwordConfirmation == obj.passwordConfirmation &&
                newsletterOptIn == obj.newsletterOptIn &&
                sendNewsletters == obj.sendNewsletters
        }
        return equals
    }

    fun toBuilder() = Builder(
        name = name,
        email = email,
        password = password,
        passwordConfirmation = passwordConfirmation,
        newsletterOptIn = newsletterOptIn,
        sendNewsletters = sendNewsletters
    )

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
