package com.kickstarter.libs.utils

import android.graphics.Rect
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.recyclerview.widget.RecyclerView

class RewardDecoration(private val margin: Int,
                       val interpolator: Interpolator = AccelerateDecelerateInterpolator()) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        with(outRect) {
            val position = parent.getChildAdapterPosition(view)
            val itemCount = state.itemCount
            left = when (position) {
                0 -> margin
                else -> margin / 2
            }
            right = when (position) {
                itemCount - 1 -> margin
                else -> margin / 2
            }
            top = margin
        }
    }
}
