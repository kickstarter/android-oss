package com.kickstarter.libs.recyclerhelpers

import androidx.recyclerview.widget.RecyclerView
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.MainThreadDisposable

internal class RecyclerViewScrollEventOnSubscribe(val recyclerView: RecyclerView) :
    ObservableOnSubscribe<RecyclerViewScrollEvent> {
    override fun subscribe(emitter: ObservableEmitter<RecyclerViewScrollEvent>) {
        MainThreadDisposable.verifyMainThread()
        val listener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!emitter.isDisposed) {
                    emitter.onNext(RecyclerViewScrollEvent.create(recyclerView, dx, dy))
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