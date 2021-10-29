package com.kickstarter.ui.extensions

import android.content.Context
import android.widget.ImageView
import com.kickstarter.libs.transformations.CircleTransformation
import com.kickstarter.libs.utils.extensions.isKSApplication
import com.squareup.picasso.Picasso

fun ImageView.loadCircleImage(url: String?) {
    url?.let {
        Picasso.get().load(it)
            .transform(CircleTransformation())
            .into(this)
    }
}

fun ImageView.loadImage(url: String?, context: Context) {
    if (context.applicationContext.isKSApplication()) {
        Picasso.get().load(url).into(this)
    } else {
        // TODO: suitable for tests, Picasso library not acting
    }
}
