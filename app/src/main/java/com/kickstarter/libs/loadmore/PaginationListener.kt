package com.kickstarter.libs.loadmore

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationListener(private val layoutManager: RecyclerView.LayoutManager) :
    RecyclerView.OnScrollListener() {

    private val threshold = 1
    private var visibleItemCount = -1
    private var totalItemCount = -1
    private var firstVisibleItemPosition = -1
    private var isScrollingDown = false

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

        visibleItemCount = layoutManager.childCount
        totalItemCount = layoutManager.itemCount
        firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        isScrollingDown = dy > 0

        if (!isLoading() && !isLastPage() && isScrollingDown) {
            if ((visibleItemCount + firstVisibleItemPosition) >= (totalItemCount - threshold) &&
                firstVisibleItemPosition >= 0
            ) {
                loadMoreItems()
            }
        }

        visiblePositionItem(layoutManager.findFirstCompletelyVisibleItemPosition())
    }

    abstract fun loadMoreItems()

    abstract fun visiblePositionItem(position: Int)

    abstract fun isLastPage(): Boolean

    abstract fun isLoading(): Boolean
}

private fun RecyclerView.LayoutManager.findFirstVisibleItemPosition(): Int {
    return when (this) {
        is LinearLayoutManager -> findFirstVisibleItemPosition()
        is GridLayoutManager -> findFirstVisibleItemPosition()
        else -> 0
    }
}

private fun RecyclerView.LayoutManager.findFirstCompletelyVisibleItemPosition(): Int {
    return when (this) {
        is LinearLayoutManager -> findFirstCompletelyVisibleItemPosition()
        is GridLayoutManager -> findFirstCompletelyVisibleItemPosition()
        else -> 0
    }
}
