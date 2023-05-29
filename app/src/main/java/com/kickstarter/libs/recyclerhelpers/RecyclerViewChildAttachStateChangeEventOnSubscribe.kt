package com.kickstarter.libs.recyclerhelpers

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.MainThreadDisposable

internal class RecyclerViewChildAttachStateChangeEventOnSubscribe(val recyclerView: RecyclerView) :
    ObservableOnSubscribe<RecyclerViewChildAttachStateChangeEvent> {
    override fun subscribe(emitter: ObservableEmitter<RecyclerViewChildAttachStateChangeEvent>) {
        MainThreadDisposable.verifyMainThread()

        val listener: OnChildAttachStateChangeListener = object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(childView: View) {
                if (!emitter.isDisposed) {
                    emitter.onNext(RecyclerViewChildAttachEvent.create(recyclerView, childView))
                }
            }

            override fun onChildViewDetachedFromWindow(childView: View) {
                if (!emitter.isDisposed) {
                    emitter.onNext(RecyclerViewChildDetachEvent.create(recyclerView, childView))
                }
            }
        }

//        emitter.add(object : MainThreadSubscription() {
//            override fun onUnsubscribe() {
//                recyclerView.removeOnChildAttachStateChangeListener(listener)
//            }
//        })
        recyclerView.addOnChildAttachStateChangeListener(listener)
    }
}