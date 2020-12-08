package com.kickstarter.ui.itemdecorations

import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.R

class TableItemDecoration :RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val backgroundColor = if (position % 2 == 0) R.color.kds_transparent else R.color.kds_support_300
        view.setBackgroundColor(ContextCompat.getColor(view.context, backgroundColor))
    }
}