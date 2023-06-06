package com.kickstarter.libs.rx.transformers

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class IgnoreValuesTransformerV2<S> : ObservableTransformer<S, Unit> {
    override fun apply(upstream: Observable<S>): ObservableSource<Unit> {
        return upstream.map { Unit }
    }
}
