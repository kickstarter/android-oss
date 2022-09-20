package com.kickstarter.libs.rx.transformers

import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.ObservableSource

class ValuesTransformerV2<T> : ObservableTransformer<Notification<T>, T?> {
    override fun apply(upstream: Observable<Notification<T>>): ObservableSource<T?> {
        return upstream
            .filter { obj: Notification<T> -> obj.isOnNext }
            .map { obj: Notification<T> -> obj.value }
    }
}