package com.kickstarter.ui.extensions

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

fun ImageView.loadCircleImage(url: String?) {
    url?.let {
        try {
            if (it.isBlank()) { // - load with drawable
                Glide.with(context)
                    .load(ColorDrawable(Color.TRANSPARENT))
                    .circleCrop()
                    .into(this)
            } else { // - load with url string
                Glide.with(context)
                    .load(it)
                    .placeholder(ColorDrawable(Color.TRANSPARENT))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .circleCrop()
                    .into(this)
            }
        } catch (e: Exception) {
            // - Empty by default in case or error
            this.setImageResource(R.drawable.image_placeholder)
            FirebaseCrashlytics.getInstance().setCustomKey("ImageView.loadCircleImage", " with url: $it ${e.message ?: ""}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}

fun ImageView.loadImage(url: String?) {
    url?.let {
        try {
            Glide.with(context)
                .load(url)
                .placeholder(ColorDrawable(Color.TRANSPARENT))
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(this)
        } catch (e: Exception) {
            this.setImageResource(R.drawable.image_placeholder)
            FirebaseCrashlytics.getInstance().setCustomKey("ImageView.loadImage", " with url: $it ${e.message ?: ""}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}

fun ImageView.loadImageWithResize(
    url: String?,
    targetImageWidth: Int,
    targetImageHeight: Int,
    placeholder: Drawable
) {
    url?.let {
        try {
            Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .apply(RequestOptions().override(targetImageWidth, targetImageHeight))
                .centerCrop()
                .placeholder(placeholder)
                .into(this)
        } catch (e: Exception) {
            this.setImageResource(R.drawable.image_placeholder)
            FirebaseCrashlytics.getInstance().setCustomKey("ImageView.loadImageWithResize", " with url: $it ${e.message ?: ""}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}
fun ImageView.loadImage(url: String?, context: Context, imageViewPlaceholder: AppCompatImageView? = null) {
    url?.let {
        val target = this
        if (context.applicationContext.isKSApplication()) {
            try {
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
            } catch (e: Exception) {
                this.setImageResource(R.drawable.image_placeholder)
                FirebaseCrashlytics.getInstance().setCustomKey("ImageView.loadImageWithResize", " with url: $it ${e.message ?: ""}")
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        } else {
            this.setImageResource(R.drawable.image_placeholder)
        }
    }
}

fun ImageView.loadGifImage(url: String?, context: Context) {
    url?.let {
        if (context.applicationContext.isKSApplication()) {
            try {
                Glide.with(context)
                    .asGif()
                    .placeholder(ColorDrawable(Color.TRANSPARENT))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .load(url)
                    .into(this)
            } catch (e: Exception) {
                this.setImageResource(R.drawable.image_placeholder)
                FirebaseCrashlytics.getInstance().setCustomKey("ImageView.loadImageWithResize", " with url: $url ${e.message ?: ""}")
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        } else {
            this.setImageResource(R.drawable.image_placeholder)
        }
    }
}
