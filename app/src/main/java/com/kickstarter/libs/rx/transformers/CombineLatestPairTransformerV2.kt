package com.kickstarter.libs.rx.transformers

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class CombineLatestPairTransformerV2<S, T>(private val second: Observable<T>) :
    ObservableTransformer<S, android.util.Pair<S, T>> {
    override fun apply(upstream: Observable<S>): ObservableSource<android.util.Pair<S, T>> {
        return Observable.combineLatest(
            upstream,
            second
        ) { first: S, second: T -> android.util.Pair(first, second) }
    }
}
