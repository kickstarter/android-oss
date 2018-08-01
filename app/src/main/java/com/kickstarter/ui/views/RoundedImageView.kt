package com.kickstarter.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.AttributeSet
import android.view.View
import com.kickstarter.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

class RoundedImageView(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet), Target {
    constructor(context: Context) : this(context, null)

    private var drawable: Drawable? = null
        set(value) {
            field = value
            postInvalidate()
        }

    fun loadImage(url: String?) {
        if (url == null) {
            drawable = null
        } else {
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.divider_dark_grey_500_horizontal)
                    .error(R.drawable.divider_dark_grey_500_horizontal)
                    .into(this)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        drawable?.setBounds(0, 0, width, height)
        drawable?.draw(canvas)
    }

    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        drawable = placeHolderDrawable
    }

    override fun onBitmapFailed(errorDrawable: Drawable?) {
        drawable = errorDrawable
    }

    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
        val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap)
        roundedDrawable.isCircular = true
        drawable = roundedDrawable
    }
}
