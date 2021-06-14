package com.kickstarter.ui.views

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable


class GravityDrawable(  // inner Drawable
    private val mDrawable: Drawable
) : Drawable() {
    override fun getIntrinsicWidth(): Int {
        return mDrawable.intrinsicWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mDrawable.intrinsicHeight
    }

    override fun draw(canvas: Canvas) {
        val halfCanvas: Int = bounds.height() / 2
        val halfDrawable = mDrawable.intrinsicHeight / 2

        // align to top
        canvas.save()
        canvas.translate(0F, (-halfCanvas + halfDrawable).toFloat())
        mDrawable.draw(canvas)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {
        mDrawable.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mDrawable.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
       return mDrawable.opacity
    }
}