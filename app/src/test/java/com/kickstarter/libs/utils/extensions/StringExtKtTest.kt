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
    fun isEmpty_whenGivenEmptyString_shouldReturnTrue() {
        assertTrue(EMPTY_STRING.isEmpty())
        assertTrue(EMPTY_SPACE.isEmpty())
        assertTrue(EMPTY_MULTI_SPACE.isEmpty())
        assertTrue(NULL_STRING.isEmpty())
    }

    @Test
    fun isEmpty_whenStringNotEmpty_shouldReturnFalse() {
        assertFalse("k".isEmpty())
        assertFalse(" k ".isEmpty())
    }

    @Test
    fun isPresent_whenStringEmpty_shouldReturnFalse() {
        assertFalse(EMPTY_STRING.isPresent())
        assertFalse(EMPTY_SPACE.isPresent())
        assertFalse(EMPTY_MULTI_SPACE.isPresent())
        assertFalse(NULL_STRING.isPresent())
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
    fun trim_whenGivenEmptyString_shouldReturnEmptyString() {
        assertEquals(EMPTY_STRING, EMPTY_STRING.trim())
        assertEquals(EMPTY_STRING, EMPTY_SPACE.trim())
    }

    @Test
    fun trim_whenGivenStringWithLeadingOrTrailingSpace_shouldReturnTrimmedString() {
        assertEquals("A", "A ".trim())
        assertEquals("A", " A".trim())
        assertEquals("A", " A ".trim())
    }

    @Test
    fun trim_whenGivenStringWithNonBreakingSpaceWithChar_shouldReturnTrimmedString() {
        assertEquals("A", "\u00A0A".trim())
        assertEquals("A", "A\u00A0".trim())
        assertEquals("A", "\u00A0A\u00A0".trim())
        assertEquals("A", "\u00A0 A".trim())
        assertEquals("A", "A \u00A0".trim())
        assertEquals("A", "A\u00A0 ".trim())
        assertEquals("A", "\u00A0 A \u00A0".trim())
    }

    @Test
    fun trim_whenGivenStringWithNonBreakingSpace_shouldReturnEmptydString() {
        assertEquals("", "\u00A0".trim())
        assertEquals("", "\u00A0 ".trim())
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
        private const val KICKSTARTER_PARENTHESES= "kickstarter"
        private const val PASSWORD_LENGTH_FIVE = "kicks"
        private const val PASSWORD_LENGTH_SIX = "kickst"
        private const val PASSWORD_LENGTH_TEN = "kickstarts"
        private val NULL_STRING = null
    }
}