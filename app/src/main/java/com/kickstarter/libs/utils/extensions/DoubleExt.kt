package com.kickstarter.libs.utils.extensions

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.multiplyRound2Decimal(other: Double, scale: Int = 2): Double {
    return BigDecimal(this).multiply(BigDecimal(other)).setScale(scale, RoundingMode.HALF_UP).toDouble()
}
