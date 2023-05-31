package com.kickstarter.libs.recyclerviewpagination

import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observable

/**
 * Static factory methods for creating [observables][Observable] for [RecyclerView].
 */
class RxRecyclerView private constructor() {
    init {
        throw AssertionError("No instances.")
    }

    companion object {

        /**
         * Create an observable of scroll events on `recyclerView`.
         *
         *
         * *Warning:* The created observable keeps a strong reference to `recyclerView`.
         * Unsubscribe to free this reference.
         */
        fun scrollEvents(
            view: RecyclerView
        ): Observable<RecyclerViewScrollEvent> {
            return Observable.create(RecyclerViewScrollEventOnSubscribe(view))
        }
    }
}
