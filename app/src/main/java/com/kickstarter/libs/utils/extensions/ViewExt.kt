package com.kickstarter.libs.utils.extensions

import android.view.View
import rx.functions.Action1

/**
 * Sets the visiblity of a view to [View.VISIBLE] or [View.GONE]. Setting
 * the view to GONE removes it from the layout so that it no longer takes up any space.
 */
fun View.setGone(gone: Boolean) {
    visibility = if (gone) {
        View.GONE
    } else {
        View.VISIBLE
    }
}

fun View.setGone(): Action1<Boolean> {
    return Action1 { gone: Boolean -> this.setGone(gone) }
}

/**
 * Sets the visibility of a view to [View.VISIBLE] or [View.INVISIBLE]. Setting
 * the view to INVISIBLE makes it hidden, but it still takes up space.
 */
fun View.setInvisible(hidden: Boolean) {
    visibility = if (hidden) {
        View.INVISIBLE
    } else {
        View.VISIBLE
    }
}

fun View.setInvisible(): Action1<Boolean> {
    return Action1 { invisible: Boolean -> this.setInvisible(invisible) }
}
