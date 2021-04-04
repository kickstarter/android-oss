package com.kickstarter.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView

class BottomCropImageView : AppCompatImageView {

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setup()
    }

    private fun setup() {
        scaleType = ImageView.ScaleType.MATRIX
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        if (drawable == null)
            return super.setFrame(l, t, r, b)

        val matrix = imageMatrix

        val viewWidth = (measuredWidth - paddingLeft - paddingRight).toFloat()
        val drawableWidth = drawable.intrinsicWidth.toFloat()

        val scale = viewWidth / drawableWidth

        matrix.setScale(scale, scale, 0f, 0f)

        imageMatrix = matrix

        return super.setFrame(l, t, r, b)
    }
}
