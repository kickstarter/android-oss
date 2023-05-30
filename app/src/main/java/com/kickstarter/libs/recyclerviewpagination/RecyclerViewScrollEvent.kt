package com.kickstarter.libs.recyclerviewpagination

import androidx.recyclerview.widget.RecyclerView

class RecyclerViewScrollEvent private constructor(
    recyclerView: RecyclerView,
    private val dx: Int,
    private val dy: Int
) :
    ViewEvent<RecyclerView>(recyclerView) {
    fun dx(): Int {
        return dx
    }

    fun dy(): Int {
        return dy
    }

    override fun equals(o: Any?): Boolean {
        if (o === this) return true
        if (o !is RecyclerViewScrollEvent) return false
        val other = o as RecyclerViewScrollEvent
        return other.view() === view() && dx == other.dx && dy == other.dy
    }

    override fun hashCode(): Int {
        var result = 17
        result = result * 37 + view().hashCode()
        result = result * 37 + dx
        result = result * 37 + dy
        return result
    }

    override fun toString(): String {
        return (
            "RecyclerViewScrollEvent{view=" +
                view() +
                ", dx=" +
                dx +
                ", dy=" +
                dy +
                '}'
            )
    }

    companion object {
        fun create(recyclerView: RecyclerView, dx: Int, dy: Int): RecyclerViewScrollEvent {
            return RecyclerViewScrollEvent(recyclerView, dx, dy)
        }
    }
}
