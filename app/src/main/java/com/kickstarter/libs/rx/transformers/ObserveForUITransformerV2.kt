package com.kickstarter.libs.rx.transformers

import com.kickstarter.libs.utils.ThreadUtils
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ObserveForUITransformerV2<T> : ObservableTransformer<T, T> {
    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream.flatMap { value ->
            if (ThreadUtils.isMainThread()) {
                Observable.just(value).observeOn(Schedulers.trampoline())
            } else {
                Observable.just(value).observeOn(AndroidSchedulers.mainThread())
            }
        }
    }
}
