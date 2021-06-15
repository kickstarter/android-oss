package com.kickstarter.libs.utils.extensions

import android.view.View
import androidx.constraintlayout.widget.Group

fun Group.setAllOnClickListener(listener: (View) -> Unit) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}
