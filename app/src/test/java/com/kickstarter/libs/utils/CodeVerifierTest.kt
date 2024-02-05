package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.CodeVerifier.Companion.MAX_CODE_VERIFIER_ENTROPY
import com.kickstarter.libs.utils.CodeVerifier.Companion.MIN_CODE_VERIFIER_ENTROPY
import org.junit.Assert.assertThrows
import org.junit.Test

class CodeVerifierTest : KSRobolectricTestCase() {

    @Test
    fun checkCodeVerifier_tooShort_throwsException() {
        val codeVerifier = createString(CodeVerifier.MIN_CODE_VERIFIER_LENGTH - 1)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            CodeVerifier.checkCodeVerifier(codeVerifier)
        }

        assertEquals(exception.message, "codeVerifier length is shorter than allowed by the PKCE specification")
    }

    @Test
    fun checkCodeVerifier_tooLong_throwsException() {
        val codeVerifier = createString(CodeVerifier.MAX_CODE_VERIFIER_LENGTH + 1)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            CodeVerifier.checkCodeVerifier(codeVerifier)
        }

        assertEquals(exception.message, "codeVerifier length is longer than allowed by the PKCE specification")
    }

    @Test
    fun generateRandomCodeVerifier_tooLittleEntropy_throwsException() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            CodeVerifier.generateRandomCodeVerifier(
                entropyBytes = MIN_CODE_VERIFIER_ENTROPY - 1
            )
        }
        assertEquals(exception.message, "entropyBytes is less than the minimum permitted")
    }

    @Test
    fun generateRandomCodeVerifier_tooMuchEntropy_throwsException() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            CodeVerifier.generateRandomCodeVerifier(
                entropyBytes = MAX_CODE_VERIFIER_ENTROPY + 1
            )
        }
        assertEquals(exception.message, "entropyBytes is greater than the maximum permitted")
    }

    private fun createString(length: Int): String {
        val strChars = CharArray(length)
        for (i in strChars.indices) {
            strChars[i] = 'a'
        }
        return String(strChars)
    }
}
