package com.kickstarter.libs.utils.extensions

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.round(scale: Int = 2): Double {
    return BigDecimal(this).setScale(scale, RoundingMode.HALF_UP).toDouble()
}
