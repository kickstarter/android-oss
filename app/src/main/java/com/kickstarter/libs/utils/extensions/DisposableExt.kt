package com.kickstarter.libs.utils.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addToDisposable(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}
