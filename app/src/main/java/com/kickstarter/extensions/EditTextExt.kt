package com.kickstarter.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.onChange(input: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            input(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    })
}