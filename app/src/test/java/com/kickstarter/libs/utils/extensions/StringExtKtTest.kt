package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class StringExtKtTest : KSRobolectricTestCase() {

    @Test
    fun isEmail_whenGivenEmail_shouldReturnTrue() {
        assertTrue(VALID_EMAIL.isEmail())
    }

    @Test
    fun isEmail_whenNotGivenEmail_shouldReturnFalse() {
        assertFalse(INVALID_EMAIL.isEmail())
    }

    @Test
    fun isTrimmedEmpty_whenGivenEmptyString_shouldReturnTrue() {
        assertTrue(EMPTY_STRING.isTrimmedEmpty())
        assertTrue(EMPTY_SPACE.isTrimmedEmpty())
        assertTrue(EMPTY_MULTI_SPACE.isTrimmedEmpty())
    }

    @Test
    fun isTrimmedEmpty_whenStringNotEmpty_shouldReturnFalse() {
        assertFalse("k".isTrimmedEmpty())
        assertFalse(" k ".isTrimmedEmpty())
    }

    @Test
    fun isPresent_whenStringEmpty_shouldReturnFalse() {
        assertFalse(EMPTY_STRING.isPresent())
        assertFalse(EMPTY_SPACE.isPresent())
        assertFalse(EMPTY_MULTI_SPACE.isPresent())
    }

    @Test
    fun isPresent_whenStringNotEmpty_shouldReturnTrue() {
        assertTrue("k".isPresent())
        assertTrue(" k ".isPresent())
    }

    @Test
    fun isValidPassword_whenStringIsEmpty_shouldReturnFalse(){
        assertFalse(EMPTY_STRING.isValidPassword())
        assertFalse(EMPTY_SPACE.isValidPassword())
        assertFalse(EMPTY_MULTI_SPACE.isValidPassword())
    }

    @Test
    fun isValidPassword_whenStringIsLessThanRequiredLength_shouldReturnFalse(){
        assertFalse(PASSWORD_LENGTH_FIVE.isValidPassword())
    }

    @Test
    fun isValidPassword_whenStringIsNotNullAndGreaterThanOrEqualToRequiredLength_shouldReturnTrue() {
        assertTrue(PASSWORD_LENGTH_SIX.isValidPassword())
        assertTrue(PASSWORD_LENGTH_TEN.isValidPassword())
    }

    @Test
    fun sentenceCase_whenGivenLowerCaseString_shouldReformatToSentenceCase() {
        assertEquals("K", "k".sentenceCase());
        assertEquals("Kickstarter", "kickstarter".sentenceCase());
    }

    @Test
    fun sentenceCase_whenGivenUpperCaseString_shouldReformatToSentenceCase() {
        assertEquals("Kickstarter", "KICKSTARTER".sentenceCase());
    }

    @Test
    fun sentenceCase_whenGivenMixedCaseString_shouldReformatToSentenceCase() {
        assertEquals("Kickstarter", "KiCkSTarTer".sentenceCase());
    }

    @Test
    fun sentenceCase_whenGivenTitleCaseString_shouldReformatToSentenceCase() {
        assertEquals("Kickstarter kickstarter", "Kickstarter Kickstarter".sentenceCase());
        assertEquals("Kickstarter is great", "Kickstarter Is Great".sentenceCase());
    }

    @Test
    fun sentenceCase_whenGivenEmptyString_shouldEmptyString() {
        assertEquals(EMPTY_STRING, EMPTY_STRING.sentenceCase());
    }

    @Test
    fun trimAllWhitespace_whenGivenEmptyString_shouldReturnEmptyString() {
        assertEquals(EMPTY_STRING, EMPTY_STRING.trimAllWhitespace())
        assertEquals(EMPTY_STRING, EMPTY_SPACE.trimAllWhitespace())
    }

    @Test
    fun trimAllWhitespace_whenGivenStringWithLeadingOrTrailingSpace_shouldReturnTrimmedString() {
        assertEquals("A", "A ".trimAllWhitespace())
        assertEquals("A", " A".trimAllWhitespace())
        assertEquals("A", " A ".trimAllWhitespace())
    }

    @Test
    fun trimAllWhitespace_whenGivenStringWithNonBreakingSpaceWithChar_shouldReturnTrimmedString() {
        assertEquals("A", "\u00A0A".trimAllWhitespace())
        assertEquals("A", "A\u00A0".trimAllWhitespace())
        assertEquals("A", "\u00A0A\u00A0".trimAllWhitespace())
        assertEquals("A", "\u00A0 A".trimAllWhitespace())
        assertEquals("A", "A \u00A0".trimAllWhitespace())
        assertEquals("A", "A\u00A0 ".trimAllWhitespace())
        assertEquals("A", "\u00A0 A \u00A0".trimAllWhitespace())
    }

    @Test
    fun trimAllWhitespace_whenGivenStringWithNonBreakingSpace_shouldReturnEmptydString() {
        assertEquals("", "\u00A0".trimAllWhitespace())
        assertEquals("", "\u00A0 ".trimAllWhitespace())
    }

    @Test
    fun wrapInParentheses_whenGivenString_shouldReturnFlankedString() {
        assertEquals(KICKSTARTER.wrapInParentheses(), KICKSTARTER_PARENTHESES)
        assertEquals(EMPTY_STRING.wrapInParentheses(), "()")
    }

    companion object {
        private const val VALID_EMAIL = "hello@kickstarter.com"
        private const val INVALID_EMAIL = "hello@kickstarer"
        private const val EMPTY_STRING = ""
        private const val EMPTY_SPACE = " "
        private const val EMPTY_MULTI_SPACE = "    "
        private const val KICKSTARTER = "kickstarter"
        private const val KICKSTARTER_PARENTHESES= "(kickstarter)"
        private const val PASSWORD_LENGTH_FIVE = "kicks"
        private const val PASSWORD_LENGTH_SIX = "kickst"
        private const val PASSWORD_LENGTH_TEN = "kickstarts"
    }
}