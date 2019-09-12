package com.kickstarter.ui.itemdecorations

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RewardCardItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.apply {
            left = if (parent.getChildAdapterPosition(view) == 0) {
                margin
            } else {
                0
            }
            top = margin
            bottom = margin
            right = if (parent.getChildAdapterPosition(view) == state.itemCount - 1) {
                margin
            } else {
                0
            }
        }
    }
}
