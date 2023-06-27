package com.kickstarter.libs.rx.transformers

import android.util.Pair
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction

class ZipPairTransformerV2<T, R>(private val second: Observable<R>) : ObservableTransformer<T, Pair<T, R>> {
    override fun apply(upstream: Observable<T>): ObservableSource<Pair<T, R>> {
        return Observable.zip(
            upstream, second,
            (BiFunction { first: T, second: R -> Pair(first, second) })
        )
    }
}
