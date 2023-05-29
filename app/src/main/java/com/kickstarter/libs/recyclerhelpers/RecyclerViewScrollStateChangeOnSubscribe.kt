package com.kickstarter.libs.recyclerhelpers

import androidx.recyclerview.widget.RecyclerView
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.MainThreadDisposable

internal class RecyclerViewScrollStateChangeOnSubscribe(val recyclerView: RecyclerView) :
    ObservableOnSubscribe<Int> {
    override fun subscribe(emitter: ObservableEmitter<Int>) {
        MainThreadDisposable.verifyMainThread()
        val listener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!emitter.isDisposed) {
                    emitter.onNext(newState)
                }
            }
        }
//        subscriber.add(object : MainThreadSubscription() {
//            override fun onUnsubscribe() {
//                recyclerView.removeOnScrollListener(listener)
//            }
//        })
        recyclerView.addOnScrollListener(listener)
    }
}