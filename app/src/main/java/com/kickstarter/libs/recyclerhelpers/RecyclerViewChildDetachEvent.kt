package com.kickstarter.libs.recyclerhelpers

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewChildDetachEvent private constructor(
    view: RecyclerView,
    child: View
) :
    RecyclerViewChildAttachStateChangeEvent(view, child) {
    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is RecyclerViewChildDetachEvent) return false
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
        return ("RecyclerViewChildDetachEvent{view="
                + view()
                + ", child="
                + child()
                + '}')
    }

    companion object {
        fun create(
            view: RecyclerView,
            child: View
        ): RecyclerViewChildDetachEvent {
            return RecyclerViewChildDetachEvent(view, child)
        }
    }
}