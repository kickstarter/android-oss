package com.kickstarter.libs.utils.extensions

import com.kickstarter.KSRobolectricTestCase
import org.junit.Test

class StringExtKtTest : KSRobolectricTestCase() {

    @Test
    fun isEmail_whenGivenEmail_shouldReturnTrue() {
        assertTrue("hello@kickstarter.com".isEmail())
    }

    @Test
    fun isEmail_whenNotGivenEmail_shouldReturnFalse() {
        assertFalse("hello@kickstarer".isEmail())
    }

    @Test
    fun isEmpty_whenGivenEmptyString_shouldReturnTrue() {
        val nullString: String? = null
        assertTrue("".isEmpty())
        assertTrue(" ".isEmpty())
        assertTrue("    ".isEmpty())
        assertTrue(nullString.isEmpty())
    }

    @Test
    fun isEmpty_whenStringNotEmpty_shouldReturnFalse() {
        assertFalse("a".isEmpty())
        assertFalse(" a ".isEmpty())
    }

    @Test
    fun isPresent_whenStringEmpty_shouldReturnFalse() {
        val nullString: String? = null
        assertFalse("".isPresent())
        assertFalse(" ".isPresent())
        assertFalse("    ".isPresent())
        assertFalse(nullString.isPresent())
    }

    @Test
    fun isPresent_whenStringNotEmpty_shouldReturnTrue() {
        assertTrue("a".isPresent())
        assertTrue(" a ".isPresent())
    }

    @Test
    fun sentenceCase_whenGivenLowerCaseString_shouldReformatToSentenceCase() {
        assertEquals("A", "a".sentenceCase());
        assertEquals("Apple", "apple".sentenceCase());
    }

    @Test
    fun sentenceCase_whenGivenUpperCaseString_shouldReformatToSentenceCase() {
        assertEquals("Apple", "APPLE".sentenceCase());
    }

    @Test
    fun sentenceCase_whenGivenMixedCaseString_shouldReformatToSentenceCase() {
        assertEquals("Apple", "APple".sentenceCase());
    }

    @Test
    fun sentenceCase_whenGivenTitleCaseString_shouldReformatToSentenceCase() {
        assertEquals("Snapple apple", "Snapple Apple".sentenceCase());
        assertEquals("Snapple apple apple", "Snapple Apple Apple".sentenceCase());
    }

    @Test
    fun sentenceCase_whenGivenEmptyString_shouldEmptyString() {
        assertEquals("", "".sentenceCase());
    }

    @Test
    fun trim_whenGivenEmptyString_shouldReturnEmptyString() {
        assertEquals("", "".trim())
        assertEquals("", " ".trim())
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



}