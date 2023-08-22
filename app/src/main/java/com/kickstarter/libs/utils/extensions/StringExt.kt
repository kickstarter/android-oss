@file:JvmName("StringExt")

package com.kickstarter.libs.utils.extensions

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Patterns
import com.braze.support.emptyToNull
import com.kickstarter.R
import org.jsoup.Jsoup
import java.security.MessageDigest
import java.text.NumberFormat
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
 * Returns a double for the given string taking into account the locale of the device or 0.0
 * if the string is null or is invalid
 */
fun String?.parseToDouble(): Double {
    val format = NumberFormat.getInstance()
    try {
        this?.emptyToNull()?.let {
            val number = format.parse(it)
            return number?.toDouble() ?: 0.0
        } ?: return 0.0
    } catch (t: Throwable) {
        return 0.0
    }
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
    val regex = """(.{1,4}@)""".toRegex()
    return this.replace(regex, "****@")
}

/**
 * validate password isNotEmptyAndAtLeast6Chars
 */
fun String.isNotEmptyAndAtLeast6Chars() = this.isNotEmpty() && this.length >= MINIMUM_PASSWORD_LENGTH

/**
 * new Password Validation Warnings message
 */
fun String.newPasswordValidationWarnings(confirmPassword: String): Int? {
    return if (this.isNotEmpty() && this.length in 1 until MINIMUM_PASSWORD_LENGTH) {
        R.string.Password_min_length_message
    } else if (confirmPassword.isNotEmpty() && confirmPassword != this) {
        R.string.Passwords_matching_message
    } else null
}

/**
 * Takes a String resource with HTMl <a> tag, an splits it
 */
fun String.stringsFromHtmlTranslation(): List<String> {
    val matchResults = "^(.*?)<a.*?>(.*?)</a>(.*)$".toRegex().find(this)

    val list = matchResults?.groups?.map {
        it?.value ?: ""
    } ?: emptyList()

    val ret = list.toMutableList()

    // The first group is the entire string, take it out
    ret.removeAt(0)
    return ret.toList()
}

/**
 * Takes a String resource with HTMl <a> tag, and takes out the href parameter
 */
fun String.hrefUrlFromTranslation(): String {
    val matchResults = "<a href=%(.*?)>".toRegex().find(this)

    val list = matchResults?.groups?.map {
        it?.value ?: ""
    } ?: emptyList()

    val ret = list.toMutableList()

    // The first group is the entire string, take it out
    ret.removeAt(0)
    return ret.last()
}

/**
 * Takes a String resource with HTMl Returns displayable styled text from the provided HTML string.
 */
fun String.toHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(TextUtils.htmlEncode(this), Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(TextUtils.htmlEncode(this))
    }
}

fun String.toHashedSHAEmail(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

fun String?.toInteger() : Int? {
    return if (this != null) {
        try {
            this.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    } else null
}


