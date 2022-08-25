@file:JvmName("StringExt")
package com.kickstarter.libs.utils.extensions

import android.util.Patterns
import com.kickstarter.R
import org.jsoup.Jsoup
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

const val MINIMUM_PASSWORD_LENGTH = 6

/**
 * Returns a boolean that reflects if the string is an email address
 */
fun String.isEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

/**
 * Returns a boolean that reflects if the string url contains a valid MP3 format
 */
fun String.isMP3Url(): Boolean {
    val regex = "^https?://\\S+\\.mp3"
    val pattern: Pattern = Pattern.compile(regex)
    val matcher: Matcher = pattern.matcher(this)
    return matcher.find()
}

/**
 * Returns a boolean that reflects if the string is empty or the length is zero when white space
 * characters are trimmed
 */
fun String.isTrimmedEmpty(): Boolean {
    return this.trimAllWhitespace().length == 0
}

/**
 * Returns a boolean of if the string is not empty
 */
fun String.isPresent() = !this.isTrimmedEmpty()

/**
 * Returns a boolean of if the string is not empty and has more than 5 characters
 */
fun String.isValidPassword(): Boolean {
    return !this.isTrimmedEmpty() && this.length >= MINIMUM_PASSWORD_LENGTH
}

/**
 * Returns a string with only the first character capitalized.
 */
fun String.sentenceCase(): String {
    return if (this.length <= 1) this.uppercase(Locale.getDefault())
    else this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1)
        .lowercase(Locale.getDefault())
}

/**
 * Returns a string with no leading or trailing whitespace. Takes all the unicode and replaces it with
 * whiteSpace character, them trims the start of the string and the end.
 */
fun String.trimAllWhitespace(): String {
    return this.replace('\u00A0', ' ').trimStart().trim()
}

/**
 * Returns a string wrapped in parentheses.
 */
fun String.wrapInParentheses() = "($this)"

fun String.parseHtmlTag(): String {
    return Jsoup.parse(this).text()
}

/**
 * Takes an optional String and returns a Double
 * NOTE: NumberUtils.parse(String, Locale)
 * - Depending on the Locale the decimal separator 0.99 or 0,99
 * - Depending on the Locale the Character used for thousand separator can change 9.999 or 9,999
 *
 * We've compiled several Regex to account for use cases as not all the languages are listed as Default Locale
 * as example Spanish or Polish, take a look at
 * @see <a href="https://github.com/frohoff/jdk8u-jdk/blob/da0da73ab82ed714dc5be94acd2f0d00fbdfe2e9/src/share/classes/java/util/Locale.java#L484">Locale.java</a>
 *
 * The Strings will be modified to use "." as a decimal separator, and "" as the thousand separator
 *
 * - In case something wrong, it will return 0.0
 */
fun String?.parseToDouble(): Double {
    val numToParse = this?.let { numToParse ->
        return@let when {
            "[0-9]+,[0-9]+".toRegex().matches(numToParse) -> numToParse.replace(",", ".")
            "[0-9]+.[0-9]{3}".toRegex().matches(numToParse) -> numToParse.replace(".", "")
            "[0-9]+.[0-9]{3},[0-9]+".toRegex().matches(numToParse) -> numToParse.replace(".", "").replace(",", ".")
            else -> numToParse
        }
    }
    return numToParse?.toDoubleOrNull() ?: 0.0
}

/**
 * Returns a boolean that reflects if the string is an email address
 */
fun String.isGif(): Boolean {
    val gifPattern = "(?:\\/\\/.*\\.(?:gif))"
    return gifPattern.toRegex().find(this) != null
}

/**
 * Mask Email
 */
fun String.maskEmail(): String {
    val regex = """(?:\G(?!^)|(?<=^[^@]{4}))[^@](?!\.[^.]+${'$'})""".toRegex()
    return this.replace(regex, "*")
}

/**
 * validate password isNotEmptyAndAtLeast6Chars
 */
fun String.isNotEmptyAndAtLeast6Chars() = this.isNotEmpty() && this.length >= MINIMUM_PASSWORD_LENGTH

/**
 * new Password Validation Warnings message
 */
fun String.newPasswordValidationWarnings(confirmPassword: String): Int? {
    return if (this.isNotEmpty() && this.length in 1 until MINIMUM_PASSWORD_LENGTH)
        R.string.Password_min_length_message
    else if (confirmPassword.isNotEmpty() && confirmPassword != this)
        R.string.Passwords_matching_message
    else null
}
