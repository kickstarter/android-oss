package com.kickstarter.ui.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.kickstarter.R

/** This is an Extension function to implement a TextChangedListener for EditText */
fun EditText.onChange(input: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            input(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

/** This is an Extension function to get the string input for EditText */
fun EditText.text() = this.text.toString()

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