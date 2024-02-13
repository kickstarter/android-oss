package com.kickstarter.libs.utils

import android.util.Base64
import com.kickstarter.libs.utils.CodeVerifier.Companion.DEFAULT_CODE_VERIFIER_ENTROPY
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.regex.Pattern

/**
 * Generates code verifiers and challenges for PKCE exchange.
 *
 * @see [Proof Key for Code Exchange by OAuth Public Clients](https://datatracker.ietf.org/doc/html/rfc7636)
 */
interface PKCE {
    fun generateRandomCodeVerifier(entropy: Int = DEFAULT_CODE_VERIFIER_ENTROPY): String
    fun generateCodeChallenge(codeVerifier: String): String
}
open class CodeVerifier : PKCE {

    override fun generateRandomCodeVerifier(entropy: Int): String {
        return Companion.generateRandomCodeVerifier(entropyBytes = entropy)
    }

    override fun generateCodeChallenge(codeVerifier: String): String {
        return Companion.generateCodeChallenge(codeVerifier)
    }

    companion object {
        /**
         * The minimum permitted length for a code verifier.
         *
         * @see "Proof Key for Code Exchange by OAuth Public Clients"
         */
        const val MIN_CODE_VERIFIER_LENGTH = 43

        /**
         * The maximum permitted length for a code verifier.
         *
         * @see "Proof Key for Code Exchange by OAuth Public Clients"
         */
        const val MAX_CODE_VERIFIER_LENGTH = 128

        /**
         * The default entropy (in bytes) used for the code verifier.
         */
        const val DEFAULT_CODE_VERIFIER_ENTROPY = 64

        /**
         * The minimum permitted entropy (in bytes) for use with
         * [.generateRandomCodeVerifier].
         */
        const val MIN_CODE_VERIFIER_ENTROPY = 32

        /**
         * The maximum permitted entropy (in bytes) for use with
         * [.generateRandomCodeVerifier].
         */
        const val MAX_CODE_VERIFIER_ENTROPY = 96

        /**
         * Base64 encoding settings used for generated code verifiers.
         */
        private const val PKCE_BASE64_ENCODE_SETTINGS: Int =
            Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE

        /**
         * Regex for legal code verifier strings, as defined in the spec.
         *
         * @see "Proof Key for Code Exchange by OAuth Public Clients"
         */
        private val REGEX_CODE_VERIFIER: Pattern =
            Pattern.compile("^[0-9a-zA-Z\\-._~]{43,128}$")

        /**
         * SHA-256 based code verifier challenge method.
         *
         * @see "Proof Key for Code Exchange by OAuth Public Clients"
         */
        const val CODE_CHALLENGE_METHOD_S256 = "S256"

        /**
         * Plain-text code verifier challenge method. This is only used by AppAuth for Android if
         * SHA-256 is not supported on this platform.
         *
         * @see "Proof Key for Code Exchange by OAuth Public Clients"
         */
        const val CODE_CHALLENGE_METHOD_PLAIN = "plain"

        const val ERROR_TOO_SHORT = "codeVerifier length is shorter than allowed by the PKCE specification"

        const val ERROR_TOO_LONG = "codeVerifier length is longer than allowed by the PKCE specification"

        const val ERROR_DO_NOT_MATCH = "codeVerifier string does not match legal code verifier strings REGEX"

        /**
         * Throws an IllegalArgumentException if the provided code verifier is invalid.
         *
         * @see [4.1.  Client Creates a Code Verifier](https://datatracker.ietf.org/doc/html/rfc7636#section-4.1)
         */
        fun checkCodeVerifier(codeVerifier: String) {
            require(
                MIN_CODE_VERIFIER_LENGTH <= codeVerifier.length
            ) { ERROR_TOO_SHORT }
            require(
                codeVerifier.length <= MAX_CODE_VERIFIER_LENGTH
            ) { ERROR_TOO_LONG }
            require(
                REGEX_CODE_VERIFIER.matcher(codeVerifier).matches()
            ) { ERROR_DO_NOT_MATCH }
        }

        /**
         * Generates a random code verifier string using the provided entropy source and the specified
         * number of bytes of entropy.
         */
        /**
         * Generates a random code verifier string using [SecureRandom] as the source of
         * entropy, with the default entropy quantity as defined by
         * [.DEFAULT_CODE_VERIFIER_ENTROPY].
         *
         * @see [Client Creates a Code Verifier](https://datatracker.ietf.org/doc/html/rfc7636#section-4.1)
         */
        fun generateRandomCodeVerifier(
            entropySource: SecureRandom = SecureRandom(),
            entropyBytes: Int = DEFAULT_CODE_VERIFIER_ENTROPY
        ): String {
            require(
                MIN_CODE_VERIFIER_ENTROPY <= entropyBytes
            ) { "entropyBytes is less than the minimum permitted" }
            require(
                entropyBytes <= MAX_CODE_VERIFIER_ENTROPY
            ) { "entropyBytes is greater than the maximum permitted" }

            val randomBytes = ByteArray(entropyBytes)
            entropySource.nextBytes(randomBytes)
            return Base64.encodeToString(randomBytes, PKCE_BASE64_ENCODE_SETTINGS)
        }

        /**
         * Produces a challenge from a code verifier, using SHA-256 as the challenge method if the
         * system supports it (all Android devices _should_ support SHA-256), and falls back
         * to the [&quot;plain&quot; challenge type][CODE_CHALLENGE_METHOD_PLAIN] if
         * unavailable.
         *
         * See [Example for the S256 code_challenge_method](https://datatracker.ietf.org/doc/html/rfc7636#appendix-B)
         */
        fun generateCodeChallenge(codeVerifier: String): String {
            return try {
                val sha256Digester = MessageDigest.getInstance("SHA-256")
                sha256Digester.update(codeVerifier.toByteArray(charset("ISO_8859_1")))
                val digestBytes = sha256Digester.digest()
                Base64.encodeToString(digestBytes, PKCE_BASE64_ENCODE_SETTINGS)
            } catch (e: NoSuchAlgorithmException) {
                Timber.w("SHA-256 is not supported on this device! Using plain challenge", e)
                codeVerifier
            } catch (e: UnsupportedEncodingException) {
                Timber.e("ISO-8859-1 encoding not supported on this device!", e)
                throw IllegalStateException("ISO-8859-1 encoding not supported", e)
            }
        }

        private val codeVerifierChallengeMethod: String
            /**
             * Returns the challenge method utilized on this system: typically
             * [SHA-256][CODE_CHALLENGE_METHOD_S256] if supported by
             * the system, [plain][CODE_CHALLENGE_METHOD_PLAIN] otherwise.
             */
            get() = try {
                MessageDigest.getInstance("SHA-256")
                // no exception, so SHA-256 is supported
                CODE_CHALLENGE_METHOD_S256
            } catch (e: NoSuchAlgorithmException) {
                CODE_CHALLENGE_METHOD_PLAIN
            }
    }
}
