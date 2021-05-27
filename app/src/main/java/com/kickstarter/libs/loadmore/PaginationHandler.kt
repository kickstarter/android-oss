package com.kickstarter.libs.loadmore

import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kickstarter.ui.adapters.KSListAdapter

private const val PAGE_SIZE = 25

class PaginationHandler(
    private val adapter: KSListAdapter,
    private val recyclerView: RecyclerView,
    private val swipeRefreshLayout: SwipeRefreshLayout,
    private val pageSize: Int = PAGE_SIZE,
) {

    var onLoadMoreListener: (() -> Unit)? = null
    var onRefreshListener: (() -> Unit)? = null
    var loadMoreEnabled = true
    private var isLoading = false

    init {
        initView()
    }

    private fun initView() {
        initSwipeRefresh()
        addScrollListener()
    }

    private fun initSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            onRefreshListener?.invoke()
        }
    }

    private fun addScrollListener() {
        recyclerView.addOnScrollListener(object :
                PaginationListener(requireNotNull(recyclerView.layoutManager)) {
                override fun loadMoreItems() {
                    isLoading = true
                    onLoadMoreListener?.invoke()
                }

                override fun visiblePositionItem(position: Int) {
                }

                override fun isLastPage(): Boolean {
                    return getCurListData().isEmpty() || getCurListData().size < pageSize || !loadMoreEnabled
                }

                override fun isLoading(): Boolean {
                    return isLoading
                }
            })
    }

     fun refreshing(refreshing: Boolean) {
        swipeRefreshLayout.isRefreshing = refreshing
    }

     fun isLoading(isLoading: Boolean) {
       this.isLoading = isLoading
    }

    fun enableRefreshing(isEnabled: Boolean) {
        swipeRefreshLayout.isEnabled = isEnabled
    }

     fun loadMoreDone() {
        isLoading = false
    }

     fun getCurListData(): List<*> = adapter.currentList
}

enum class LoadingType {
    NORMAL,
    LOAD_MORE,
    PULL_REFRESH
}
