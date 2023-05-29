package com.kickstarter.libs.recyclerhelpers

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewChildAttachEvent private constructor(
    view: RecyclerView,
    child: View
) :
    RecyclerViewChildAttachStateChangeEvent(view, child) {
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is RecyclerViewChildAttachEvent) return false
        val other = o
        return (other.view() === view()
                && other.child() === child())
    }

    override fun hashCode(): Int {
        var result = 17
        result = result * 37 + view().hashCode()
        result = result * 37 + child().hashCode()
        return result
    }

    override fun toString(): String {
        return ("RecyclerViewChildAttachEvent{view="
                + view()
                + ", child="
                + child()
                + '}')
    }

    companion object {
        fun create(
            view: RecyclerView,
            child: View
        ): RecyclerViewChildAttachEvent {
            return RecyclerViewChildAttachEvent(view, child)
        }
    }
}