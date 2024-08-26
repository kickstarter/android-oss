@file:JvmName("StringExt")

package com.kickstarter.libs.utils.extensions

import android.text.Html
import android.text.Spanned
import android.text.TextUtils
import android.util.Base64
import android.util.Patterns
import com.braze.support.emptyToNull
import com.kickstarter.R
import org.jsoup.Jsoup
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.MessageDigest
import java.text.NumberFormat
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

const val MINIMUM_PASSWORD_LENGTH = 6

fun String.encrypt(secretKey: Key?): String? {
    return try {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val cipherText = Base64.encodeToString(cipher.doFinal(this.toByteArray()), Base64.DEFAULT)
        val iv = Base64.encodeToString(cipher.iv, Base64.DEFAULT)

        "$cipherText.$iv"
    } catch (e: Exception) {
        null
    }
}

fun String.decrypt(secretKey: Key?): String? {
    return try {
        val array = this.split(".")
        val cipherData = Base64.decode(array[0], Base64.DEFAULT)
        val iv = Base64.decode(array[1], Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
        val spec = IvParameterSpec(iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val clearText = cipher.doFinal(cipherData)

        String(clearText, 0, clearText.size, StandardCharsets.UTF_8)
    } catch (e: Exception) {
        null
    }
}

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
 * Returns a boolean that reflects if the string contains the .webp extension
 */
fun String.isWebp(): Boolean {
    val gifPattern = "(?:\\/\\/.*\\.(?:webp))"
    return gifPattern.toRegex().find(this) != null
}

/**
 * Returns a boolean that reflects if the string contains the .gif extension
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
 * validate password is not empty, contains at least 6 characters, and is not only whitespace
 */
fun String.validPassword() = this.isNotEmpty() && this.length >= MINIMUM_PASSWORD_LENGTH && !all { it.isWhitespace() }

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
    return Html.fromHtml(TextUtils.htmlEncode(this), Html.FROM_HTML_MODE_LEGACY)
}

fun String.toHashedSHAEmail(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

fun String?.toInteger(): Int? {
    return if (this != null) {
        try {
            this.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    } else null
}

fun String.format(key1: String, value1: String?): String {
    val substitutions: HashMap<String, String?> = object : HashMap<String, String?>() {
        init {
            put(key1, value1)
        }
    }
    return this.replace(substitutions)
}
fun String.replace(substitutions: Map<String, String?>): String {
    val builder = StringBuilder()
    for (key in substitutions.keys) {
        if (builder.isNotEmpty()) {
            builder.append("|")
        }
        builder
            .append("(%\\{")
            .append(key)
            .append("\\})")
    }

    val pattern = Pattern.compile(builder.toString())
    val matcher = pattern.matcher(this)
    val buffer = StringBuffer()

    while (matcher.find()) {
        val key = NON_WORD_REGEXP.matcher(matcher.group()).replaceAll("")
        val value = substitutions[key]
        val replacement = Matcher.quoteReplacement(value ?: "")
        matcher.appendReplacement(buffer, replacement)
    }
    matcher.appendTail(buffer)

    return buffer.toString()
}

private val NON_WORD_REGEXP = Pattern.compile("[^\\w]")
