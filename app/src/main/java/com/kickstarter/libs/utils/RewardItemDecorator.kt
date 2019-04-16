package com.kickstarter.libs.utils

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class RewardItemDecorator(private val divider: Drawable) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pos = parent.getChildAdapterPosition(view)
        val itemCount = parent.layoutManager?.itemCount?.let { it } ?: return
        val lastPosition = itemCount - 1

        if (pos != 0 && pos != lastPosition) {
            outRect.bottom = divider.intrinsicHeight
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val dividerLeft = parent.paddingLeft
        val dividerRight = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)

            val pos = parent.getChildAdapterPosition(child)
            val itemCount = parent.layoutManager?.itemCount?.let { it } ?: continue
            val lastPosition = itemCount - 1
            if (pos != lastPosition) {

                val params = child.layoutParams as RecyclerView.LayoutParams

                val dividerTop = child.bottom + params.bottomMargin
                val dividerBottom = dividerTop + divider.intrinsicHeight
                divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                divider.draw(canvas)
            }
        }
    }
}
