package com.kickstarter.services.apiresponses

import android.os.Parcelable
import androidx.annotation.StringDef
import com.kickstarter.services.ApiException
import kotlinx.parcelize.Parcelize
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Parcelize
class ErrorEnvelope private constructor(
    private val errorMessages: List<String> = emptyList(),
    private val httpCode: Int = 0,
    private val ksrCode: String = "",
    private val facebookUser: FacebookUser? = null // - Re-evaluate on https://kickstarter.atlassian.net/browse/MBL-815 the default value for this specific field
) : Parcelable {
    fun errorMessages() = this.errorMessages
    fun httpCode() = this.httpCode
    fun ksrCode() = this.ksrCode
    fun facebookUser() = this.facebookUser

    @Parcelize
    class FacebookUser private constructor(
        private val id: Long = 0L,
        private val name: String = "",
        private val email: String = ""
    ) : Parcelable {
        fun id() = this.id
        fun name() = this.name
        fun email() = this.email

        @Parcelize
        data class Builder(
            private var id: Long = 0L,
            private var name: String = "",
            private var email: String = ""
        ) : Parcelable {
            fun id(id: Long?) = apply { this.id = id ?: 0L }
            fun name(name: String?) = apply { this.name = name ?: "" }
            fun email(email: String?) = apply { this.email = email ?: "" }
            fun build() = FacebookUser(
                id = id,
                name = name,
                email = email
            )
        }

        fun toBuilder() = Builder(
            id = id,
            name = name,
            email = email
        )

        override fun equals(other: Any?): Boolean {
            var equals = super.equals(other)
            if (other is FacebookUser) {
                equals = id() == other.id() &&
                    name() == other.name() &&
                    email() == other.email()
            }
            return equals
        }

        companion object {
            @JvmStatic
            fun builder(): Builder {
                return Builder()
            }
        }
    }

    data class Builder(
        private var errorMessages: List<String> = emptyList(),
        private var httpCode: Int = 0,
        private var ksrCode: String = "",
        private var facebookUser: FacebookUser? = null
    ) {
        fun errorMessages(errorMessages: List<String?>?) = apply { errorMessages?.let { this.errorMessages = it.filterNotNull() } ?: emptyList<String>() }
        fun httpCode(httpCode: Int?) = apply { this.httpCode = httpCode ?: 0 }
        fun ksrCode(ksrCode: String?) = apply { this.ksrCode = ksrCode ?: "" }
        fun facebookUser(facebookUser: FacebookUser?) = apply { this.facebookUser = facebookUser }
        fun build() = ErrorEnvelope(
            errorMessages = errorMessages,
            httpCode = httpCode,
            ksrCode = ksrCode,
            facebookUser = facebookUser
        )
    }

    fun toBuilder() = Builder(
        errorMessages = errorMessages,
        httpCode = httpCode,
        ksrCode = ksrCode,
        facebookUser = facebookUser
    )

    @StringDef(INVALID_XAUTH_LOGIN, TFA_FAILED, TFA_REQUIRED)
    @Retention(RetentionPolicy.SOURCE)
    annotation class ErrorCode

    val isConfirmFacebookSignupError: Boolean
        get() = CONFIRM_FACEBOOK_SIGNUP == ksrCode()
    val isInvalidLoginError: Boolean
        get() = INVALID_XAUTH_LOGIN == ksrCode()
    val isTfaRequiredError: Boolean
        get() = TFA_REQUIRED == ksrCode()
    val isTfaFailedError: Boolean
        get() = TFA_FAILED == ksrCode()
    val isMissingFacebookEmailError: Boolean
        get() = MISSING_FACEBOOK_EMAIL == ksrCode()
    val isFacebookInvalidAccessTokenError: Boolean
        get() = FACEBOOK_INVALID_ACCESS_TOKEN == ksrCode()
    val isUnauthorizedError: Boolean
        get() = UNAUTHORIZED == ksrCode()

    /*
    When logging in the only two possible errors are INVALID_XAUTH_LOGIN
    and TFA_REQUIRED, so we consider anything else an unknown error.
   */
    val isGenericLoginError: Boolean
        get() = INVALID_XAUTH_LOGIN != ksrCode() &&
            TFA_REQUIRED != ksrCode()

    /**
     * Returns the first error message available, or "" if there are none.
     */
    fun errorMessage(): String = errorMessages()?.firstOrNull() ?: ""

    override fun equals(other: Any?): Boolean {
        var equals = super.equals(other)
        if (other is ErrorEnvelope) {
            equals = errorMessages() == other.errorMessages() &&
                httpCode() == other.httpCode() &&
                ksrCode() == other.ksrCode() &&
                facebookUser() == other.facebookUser()
        }
        return equals
    }

    companion object {
        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }

        const val CONFIRM_FACEBOOK_SIGNUP = "confirm_facebook_signup"
        const val INVALID_XAUTH_LOGIN = "invalid_xauth_login"
        const val TFA_FAILED = "tfa_failed"
        const val TFA_REQUIRED = "tfa_required"
        const val MISSING_FACEBOOK_EMAIL = "missing_facebook_email"
        const val FACEBOOK_INVALID_ACCESS_TOKEN = "facebook_invalid_access_token"
        const val UNAUTHORIZED = "unauthorized"

        /**
         * Tries to extract an [ErrorEnvelope] from an exception, and if it
         * can't returns Empty [ErrorEnvelope].
         */
        @JvmStatic
        fun fromThrowable(t: Throwable): ErrorEnvelope {
            if (t is ApiException) {
                return t.errorEnvelope()
            }

            return ErrorEnvelope.builder().errorMessages(listOf(t.message ?: "")).build()
        }
    }
}
