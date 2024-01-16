package com.kickstarter.libs.rx.transformers

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class IncrementalCountTransformerV2<T> : ObservableTransformer<T, Int> {
    private val firstPage: Int

    constructor() {
        firstPage = 1
    }

    constructor(firstPage: Int) {
        this.firstPage = firstPage
    }

    override fun apply(upstream: Observable<T>): ObservableSource<Int> {
        return upstream.scan(firstPage - 1) { accum: Int, _: T -> accum + 1 }.skip(1)
    }
}
