package com.kickstarter.services.apirequests

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
class PKCEBody private constructor(
    private val code: String,
    private val code_verifier: String,
    private val client_id: String
) : Parcelable {

    fun code() = this.code
    fun codeVerifier() = this.code_verifier

    @Parcelize
    data class Builder(
        private var code: String = "",
        private var codeVerifier: String = "",
        private var clientId: String = ""
    ) : Parcelable {
        fun codeVerifier(codeVerifier: String) = apply { this.codeVerifier = codeVerifier }
        fun code(code: String) = apply { this.code = code }

        fun clientId(clientId: String) = apply { this.clientId = clientId }
        fun build() = PKCEBody(
            code_verifier = codeVerifier,
            code = code,
            client_id = clientId
        )
    }

    fun toBuilder() = Builder(
        codeVerifier = code_verifier,
        code = code,
        clientId = client_id
    )

    override fun equals(obj: Any?): Boolean {
        var equals = super.equals(obj)
        if (obj is PKCEBody) {
            equals = code_verifier == obj.code_verifier &&
                code == obj.code &&
                client_id == obj.client_id
        }
        return equals
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + code_verifier.hashCode()
        result = 31 * result + client_id.hashCode()
        return result
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
    }
}
