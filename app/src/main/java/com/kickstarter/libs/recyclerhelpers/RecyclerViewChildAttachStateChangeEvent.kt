package com.kickstarter.libs.recyclerhelpers

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class RecyclerViewChildAttachStateChangeEvent internal constructor(
    view: RecyclerView,
    private val child: View
) :
    ViewEvent<RecyclerView>(view) {
    /** The child from which this event occurred.  */
    fun child(): View {
        return child
    }
}