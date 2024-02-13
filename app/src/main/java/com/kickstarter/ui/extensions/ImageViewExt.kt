package com.kickstarter.ui.extensions

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.kickstarter.R
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

fun ImageView.loadCircleImage(url: String?) {
    url?.let {
        if (it.isBlank()) { // - load with drawable
            Picasso.get()
                .load(R.drawable.circle_grey_500)
                .transform(CircleTransformation())
                .into(this)
        } else { // - load with url string
            Picasso.get()
                .load(it)
                .placeholder(R.drawable.circle_grey_500)
                .transform(CircleTransformation())
                .into(this)
        }
    }
}

fun ImageView.loadImage(url: String?) {
    url?.let {
        Picasso
            .get()
            .load(it)
            .into(this)
    }
}

fun ImageView.loadImage(url: String?, context: Context, imageViewPlaceholder: AppCompatImageView? = null) {
    val target = this
    if (context.applicationContext.isKSApplication()) {
        Picasso
            .get()
            .load(url)
            .into(
                this,
                object : Callback {
                    override fun onSuccess() {
                        imageViewPlaceholder?.setImageDrawable(target.drawable)
                    }

                    override fun onError(e: Exception?) {
                        target.setImageDrawable(null)
                        imageViewPlaceholder?.setImageDrawable(null)
                    }
                }
            )
    } else {
        this.setImageResource(R.drawable.image_placeholder)
    }
}

fun ImageView.loadGifImage(url: String?, context: Context) {
    if (context.applicationContext.isKSApplication()) {
        Glide.with(context)
            .asGif()
            .placeholder(ColorDrawable(Color.TRANSPARENT))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .load(url)
            .into(this)
    } else {
        this.setImageResource(R.drawable.image_placeholder)
    }
}
