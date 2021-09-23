@file:JvmName("StringExt")
package com.kickstarter.libs.utils.extensions

import android.util.Patterns
import org.jsoup.Jsoup
import java.util.Locale

const val MINIMUM_PASSWORD_LENGTH = 6

/**
 * Returns a boolean that reflects if the string is an email address
 */
fun String.isEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
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
    return if (this.length <= 1) this.toUpperCase(Locale.getDefault())
    else this.substring(0, 1).toUpperCase(Locale.getDefault()) + this.substring(1).toLowerCase(Locale.getDefault())
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
 * - In case the string is null or cannot be converted to double
 * it will return 0.0
 */
fun String?.toDouble(): Double {
    return this?.toDoubleOrNull() ?: 0.0
}
