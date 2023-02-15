package com.kickstarter.libs.rx.transformers

import android.util.Pair
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class TakePairWhenTransformerV2<S, T>(private val `when`: Observable<T>) :
    ObservableTransformer<S, Pair<S, T>> {

    override fun apply(upstream: Observable<S>): ObservableSource<Pair<S, T>> {
        return `when`.withLatestFrom(upstream) { x: T, y: S -> Pair(y, x) }
    }
}
