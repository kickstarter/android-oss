package com.kickstarter.screenshoot.testing

import android.view.ViewGroup
import java.util.*

data class TestDevice(val width: Int, val height: Int) {

    lateinit var info: String

    lateinit var locale: Locale

    var isDarkTheme: Boolean = false

    constructor(width: Int, height: Int, isDarkTheme: Boolean, locale: Locale) : this(width, height) {
        // avoid showing constant value of WRAP_CONTENT in screenshot file names
        val heightText = if (height == ViewGroup.LayoutParams.WRAP_CONTENT) "WRAP_CONTENT" else height.toString()

        this.info = width.toString() + "x" + heightText + "_" + locale.displayName + "_" + if (isDarkTheme) "DarkTheme" else "LightTheme"
        this.locale = locale
        this.isDarkTheme = isDarkTheme
    }
}
