package com.kickstarter.libs.loadmore

import rx.Observable

interface PaginatedViewModelOutput<T> {
    var loadMoreListData: MutableList<T>
    fun isLoadingMoreItems(): Observable<Boolean>
    fun isRefreshing(): Observable<Boolean>
    fun enablePagination(): Observable<Boolean>
    fun bindPaginatedData(data: List<T>?)
    fun updatePaginatedState(enabled: Boolean)
    fun updatePaginatedData(loadingType: LoadingType, data: List<T>?) {
        if (loadingType != LoadingType.LOAD_MORE) {
            loadMoreListData = mutableListOf()
            updatePaginatedState(true)
        }
        if (data?.isEmpty() == true) {
            if (loadingType == LoadingType.LOAD_MORE) {
                updatePaginatedState(false)
            }
        }

        bindPaginatedData(data)
    }
}
