package com.kickstarter.ui.extensions

import android.widget.ImageView
import com.kickstarter.libs.transformations.CircleTransformation
import com.squareup.picasso.Picasso

fun ImageView.loadCircleImage(url: String?) {
    url?.let {
        Picasso.get().load(it)
                .transform(CircleTransformation())
                .into(this)
    }
}
