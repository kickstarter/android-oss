package com.kickstarter.ui.extensions
import android.widget.EditText
import com.kickstarter.R

/**
 * EditText extension function: use for those EditTexts
 * with the field `android:maxLength`.
 *
 * Setting as text a string longer than android:maxLength
 * results in a IndexOutOfBoundsException.
 *
 * This Extension function restricts the longitude of the text
 * and the appropriate selection
 *
 * @param text string to display in the EditText
 */
fun EditText.setTextAndSelection(text: String) {
    val maxLength = resources.getInteger(R.integer.max_length)
    val stringAmount = if (text.length >= maxLength) text.substring(0, maxLength -1) else text
    this.setText(stringAmount)
    this.setSelection(stringAmount.length)
}