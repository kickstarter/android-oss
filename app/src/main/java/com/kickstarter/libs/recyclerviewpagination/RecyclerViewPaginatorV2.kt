package com.kickstarter.libs.recyclerviewpagination

import android.util.Pair
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kickstarter.libs.recyclerviewpagination.RxRecyclerView.Companion.scrollEvents
import com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair
import com.kickstarter.libs.utils.Secrets
import com.kickstarter.libs.utils.extensions.isFalse
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.subjects.PublishSubject

class RecyclerViewPaginatorV2 {
    private val recyclerView: RecyclerView
    private val isScrollEnabled: Boolean
    private val nextPage: Action
    private val isLoading: Observable<Boolean>
    private var subscription: Disposable? = null
    private var retrySubscription: Disposable? = null
    private val retryLoadingNextPageSubject = PublishSubject.create<Unit>()

    constructor(recyclerView: RecyclerView, nextPage: Action, isLoading: Observable<Boolean>) {
        this.recyclerView = recyclerView
        this.nextPage = nextPage
        this.isLoading = isLoading
        isScrollEnabled = true
        start()
    }

    constructor(
        recyclerView: RecyclerView,
        nextPage: Action,
        isLoading: Observable<Boolean>,
        isScrollEnabled: Boolean
    ) {
        this.recyclerView = recyclerView
        this.nextPage = nextPage
        this.isLoading = isLoading
        this.isScrollEnabled = isScrollEnabled
        start()
    }

    /**
     * Begin listening to the recycler view scroll events to determine
     * when pagination should happen.
     */
    fun start() {
        stop()
        val lastVisibleAndCount = scrollEvents(
            recyclerView
        )
            .filter { Secrets.IS_OSS.isFalse() }
            .map { recyclerView.layoutManager }
            .ofType(LinearLayoutManager::class.java)
            .map { displayedItemFromLinearLayout(it) }
            .filter { item: Pair<Int, Int> -> item.second != 0 }
            .distinctUntilChanged()

        val isNotLoading = isLoading
            .distinctUntilChanged()
            .filter { loading: Boolean -> !loading }

        val loadNextPage = lastVisibleAndCount
            .compose<Pair<Pair<Int, Int>, Boolean>>(
                combineLatestPair(
                    isNotLoading
                )
            )
            .distinctUntilChanged()
            .map { p: Pair<Pair<Int, Int>, Boolean> ->
                p.first
            }
            .filter { visibleItemOfTotal: Pair<Int, Int> ->
                visibleItemIsCloseToBottom(
                    visibleItemOfTotal
                )
            }

        subscription = loadNextPage
            .subscribe {
                if (isScrollEnabled) {
                    nextPage.run()
                }
            }

        retrySubscription = retryLoadingNextPageSubject
            .subscribe {
                nextPage.run()
            }
    }

    fun reload() {
        retryLoadingNextPageSubject.onNext(Unit)
    }

    /**
     * Stop listening to recycler view scroll events and discard the
     * associated resources. This should be done when the object that
     * created `this` is released.
     */
    fun stop() {
        this.recyclerView.clearOnScrollListeners()
        subscription?.dispose()
        retrySubscription?.dispose()
    }

    /**
     * Returns a (visibleItem, totalItemCount) pair given a linear layout manager.
     */
    private fun displayedItemFromLinearLayout(manager: LinearLayoutManager): Pair<Int, Int> {
        return Pair(manager.findLastVisibleItemPosition(), manager.itemCount)
    }

    private fun visibleItemIsCloseToBottom(visibleItemOfTotal: Pair<Int, Int>): Boolean {
        return visibleItemOfTotal.first == visibleItemOfTotal.second - 1
    }
}
