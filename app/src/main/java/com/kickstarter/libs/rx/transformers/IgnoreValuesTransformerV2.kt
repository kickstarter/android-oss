package com.kickstarter.libs.rx.transformers

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class IgnoreValuesTransformerV2<S> : ObservableTransformer<S, Void?> {
    override fun apply(upstream: Observable<S>): ObservableSource<Void?> {
        return upstream.map { null }
    }
}
