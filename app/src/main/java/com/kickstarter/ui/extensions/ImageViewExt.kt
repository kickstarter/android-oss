package com.kickstarter.ui.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.isKSApplication

const val header = "User-Agent"
fun ImageView.loadCircleImage(url: String?, userAgent: String = "") {
    url?.let {
        try {
            if (it.isBlank()) { // - load with drawable
                Glide.with(context)
                    .load(ColorDrawable(Color.TRANSPARENT))
                    .circleCrop()
                    .into(this)
            } else { // - load with url string
                val glideUrl = GlideUrl(
                    it,
                    LazyHeaders.Builder()
                        .addHeader(header, userAgent)
                        .build()
                )
                Glide.with(context)
                    .load(glideUrl)
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

fun ImageView.loadImage(url: String?, userAgent: String = "") {
    url?.let {
        try {
            val glideUrl = GlideUrl(
                it,
                LazyHeaders.Builder()
                    .addHeader(header, userAgent)
                    .build()
            )
            Glide.with(context)
                .load(glideUrl)
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
    placeholder: Drawable,
    userAgent: String = ""
) {
    url?.let {
        try {
            val glideUrl = GlideUrl(
                it,
                LazyHeaders.Builder()
                    .addHeader(header, userAgent)
                    .build()
            )
            Glide.with(context)
                .load(glideUrl)
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
fun ImageView.loadImage(url: String?, context: Context, imageZoomablePlaceholder: AppCompatImageView? = null, userAgent: String = "") {
    url?.let {
        val targetView = this
        if (context.applicationContext.isKSApplication()) {
            try {
                val glideUrl = GlideUrl(
                    it,
                    LazyHeaders.Builder()
                        .addHeader(header, userAgent)
                        .build()
                )
                Glide.with(context)
                    .load(glideUrl)
                    .listener(object : RequestListener<Drawable> {
                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            targetView.setImageDrawable(resource)
                            imageZoomablePlaceholder?.setImageDrawable(resource)
                            return isFirstResource
                        }

                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            targetView.setImageDrawable(null)
                            imageZoomablePlaceholder?.setImageDrawable(null)
                            return isFirstResource
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .load(url)
                    .into(this)
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

fun ImageView.loadWebp(url: String?, context: Context, userAgent: String = "") {
    url?.let {
        try {
            val glideUrl = GlideUrl(
                it,
                LazyHeaders.Builder()
                    .addHeader(header, userAgent)
                    .build()
            )
            val roundedCorners = RoundedCorners(1)
            Glide.with(this)
                .load(glideUrl)
                .downsample(DownsampleStrategy.AT_LEAST)
                .override(Resources.getSystem().displayMetrics.widthPixels, Resources.getSystem().displayMetrics.heightPixels)
                .optionalTransform(roundedCorners)
                .optionalTransform(WebpDrawable::class.java, WebpDrawableTransformation(roundedCorners))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(this)
        } catch (e: Exception) {
            this.setImageResource(R.drawable.image_placeholder)
            FirebaseCrashlytics.getInstance().setCustomKey("ImageView.loadImageWithResize", " with url: $url ${e.message ?: ""}")
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }
}

fun ImageView.loadGifImage(url: String?, context: Context, userAgent: String = "") {
    url?.let {
        if (context.applicationContext.isKSApplication()) {
            try {
                val glideUrl = GlideUrl(
                    it,
                    LazyHeaders.Builder()
                        .addHeader(header, userAgent)
                        .build()
                )
                Glide.with(context)
                    .asGif()
                    .load(glideUrl)
                    .downsample(DownsampleStrategy.AT_LEAST)
                    .override(Resources.getSystem().displayMetrics.widthPixels, Resources.getSystem().displayMetrics.heightPixels)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
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
