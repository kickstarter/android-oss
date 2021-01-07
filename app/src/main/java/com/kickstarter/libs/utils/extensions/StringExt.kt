@file:JvmName("StringExt")
package com.kickstarter.libs.utils.extensions

import android.util.Patterns
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
     * Returns a string with no leading or trailing whitespace. This method calls on the kotlin trim string method,
     * which removes more types of whitespace than the java string method for trim.
     */
    fun String.trimAllWhitespace(): String {
        return this.replace('\u00A0', ' ').trimStart().trim()
    }

    /**
     * Returns a string wrapped in parentheses.
     */
    fun String.wrapInParentheses() = "($this)"
