package com.kickstarter.libs.utils

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.CodeVerifier.Companion.ERROR_DO_NOT_MATCH
import com.kickstarter.libs.utils.CodeVerifier.Companion.ERROR_TOO_LONG
import com.kickstarter.libs.utils.CodeVerifier.Companion.ERROR_TOO_SHORT
import com.kickstarter.libs.utils.CodeVerifier.Companion.MAX_CODE_VERIFIER_ENTROPY
import com.kickstarter.libs.utils.CodeVerifier.Companion.MIN_CODE_VERIFIER_ENTROPY
import com.kickstarter.libs.utils.CodeVerifier.Companion.generateCodeChallenge
import com.kickstarter.libs.utils.CodeVerifier.Companion.generateRandomCodeVerifier
import org.junit.Assert.assertThrows
import org.junit.Test

class CodeVerifierTest : KSRobolectricTestCase() {

    @Test
    fun checkCodeVerifier_tooShort_throwsException() {
        val codeVerifier = createString(CodeVerifier.MIN_CODE_VERIFIER_LENGTH - 1)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            CodeVerifier.checkCodeVerifier(codeVerifier)
        }

        assertEquals(exception.message, ERROR_TOO_SHORT)
    }

    @Test
    fun checkCodeVerifier_tooLong_throwsException() {
        val codeVerifier = createString(CodeVerifier.MAX_CODE_VERIFIER_LENGTH + 1)
        val exception = assertThrows(IllegalArgumentException::class.java) {
            CodeVerifier.checkCodeVerifier(codeVerifier)
        }

        assertEquals(exception.message, ERROR_TOO_LONG)
    }

    @Test
    fun checkCodeVerifier_languageSentence_notValid() {
        val sentence = "Hello, world. I am a string. Hello, world. I am a string."
        val exception = assertThrows(IllegalArgumentException::class.java) {
            CodeVerifier.checkCodeVerifier(sentence)
        }

        assertEquals(exception.message, ERROR_DO_NOT_MATCH)
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
            generateRandomCodeVerifier(
                entropyBytes = MAX_CODE_VERIFIER_ENTROPY + 1
            )
        }
        assertEquals(exception.message, "entropyBytes is greater than the maximum permitted")
    }

    /**
     * Generates random String with @param length
     */
    private fun createString(length: Int): String {
        val strChars = CharArray(length)
        for (i in strChars.indices) {
            strChars[i] = 'a'
        }
        return String(strChars)
    }

    @Test
    fun givenSentence_generateCodeChallengeWithSHA256Hash() {
        // - Use https://oauth.school/exercise/refresh/ to obtain givenCodeChallenge
        val givenCodeChallenge = "wcaGQDnzgCSNMKc1Jcg1FCfH-0aNWLexAF8-NyegQqE"

        val givenCodeVerifier = "Hello, world. I am a string."
        val generatedChallenge = generateCodeChallenge(givenCodeVerifier)
        assertEquals(givenCodeChallenge, generatedChallenge)
    }

    @Test
    fun givenCodeVerifierMinEntropy_generateCodeChallengeWithSHA256Hash() {
        // - [givenVerifier] generated using generateRandomCodeVerifier(MIN_CODE_VERIFIER_ENTROPY)
        // - Use https://oauth.school/exercise/refresh/ to obtain [givenCodeChallenge]
        val givenVerifier = "HaTkldnGaT3PcENU5EAY8rtDDNIikQSvBXFFEYBa3MA"
        val codeChallenge = generateCodeChallenge(givenVerifier)

        val givenCodeChallenge = "khL4OfhvX-uphctb0gMMmE_O5xNX-MfjMPvHxAbpsZk"
        assertEquals(codeChallenge, givenCodeChallenge)
    }

    @Test
    fun givenCodeVerifierDefaultEntropy_generateCodeChallengeWithSHA256Hash() {
        // - [givenVerifier] generated using generateRandomCodeVerifier(DEFAULT_CODE_VERIFIER_ENTROPY)
        // - Use https://oauth.school/exercise/refresh/ to obtain [givenCodeChallenge]
        val givenVerifier = "BAxigyqguFpLKXnGiqc0iabt-Epr3YL-wJvPL0CfDSTGB45_jOwrSrFa0_T4FK5y9amhhYQAk-Bkr2zpD8Gpxw"
        val codeChallenge = generateCodeChallenge(givenVerifier)

        val givenCodeChallenge = "DcimCRjKEAmp3cl0mFMc12oCsHfN931jzpot2HCkBNo"
        assertEquals(codeChallenge, givenCodeChallenge)
    }
    @Test
    fun givenCodeVerifierMaxEntropy_generateCodeChallengeWithSHA256Hash() {
        // - [givenVerifier] generated using generateRandomCodeVerifier(MAX_CODE_VERIFIER_ENTROPY)
        // - Use https://oauth.school/exercise/refresh/ to obtain [givenCodeChallenge]
        val givenVerifier = "YfZIzxXTx7Dc58fLNl2uO6cRzWSevpEPeKSXGBFhN8fisOA3XjV_AF0Buz2ZjYxu7S30j15dlzPCzbtHZEHCqAo94YcaZV4JNfJYWCi1jWavu8UUSdCw9n6Y3dinTRfe"
        val codeChallenge = generateCodeChallenge(givenVerifier)

        val givenCodeChallenge = "cJXeRcpWhJLlsCD8DG3OLkLtjGF8yip6Hf0Jd560Pgg"
        assertEquals(codeChallenge, givenCodeChallenge)
    }

    @Test
    fun generateSeveralCodeVerifiers_checkDoNotMatch() {
        val codeVerifierA = generateRandomCodeVerifier()
        val codeVerifierB = generateRandomCodeVerifier()
        val codeVerifierD = generateRandomCodeVerifier()

        assertTrue(codeVerifierA != codeVerifierB)
        assertTrue(codeVerifierA != codeVerifierD)
        assertTrue(codeVerifierB != codeVerifierD)
    }
}
