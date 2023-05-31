package com.kickstarter.libs.rx.transformers

import com.kickstarter.services.apiresponses.ErrorEnvelope
import com.kickstarter.services.apiresponses.ErrorEnvelope.Companion.fromThrowable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.functions.Consumer

open class NeverApiErrorTransformerV2<T> : ObservableTransformer<T, T> {
    private val errorAction: Consumer<ErrorEnvelope>?

    constructor() {
        errorAction = null
    }

    protected constructor(errorAction: Consumer<ErrorEnvelope>?) {
        this.errorAction = errorAction
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream
            .doOnError { e: Throwable ->
                val env = fromThrowable(e)
                env?.let { errorAction?.accept(env) }
            }
            .onErrorResumeNext { e: Throwable ->
                val env = fromThrowable(e)
                env?.let { Observable.empty() } ?: Observable.error(e)
            }
    }
}
