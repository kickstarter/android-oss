package com.kickstarter.ui.views

import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class CenterSpan : MetricAffectingSpan() {
    internal var ratio = 0.5f

    override fun updateDrawState(paint: TextPaint) {
        paint.baselineShift += (paint.ascent() * ratio).toInt()
    }

    override fun updateMeasureState(paint: TextPaint) {
        paint.baselineShift += (paint.ascent() * ratio).toInt()
    }
}
