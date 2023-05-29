package com.kickstarter.libs.recyclerhelpers

import android.view.View

abstract class ViewEvent<T : View> protected constructor(private val view: T) {

    /** The view from which this event occurred.  */
    fun view(): T {
        return view
    }
}
