package com.kickstarter.ui.viewholders

import android.content.Context
import android.view.View
import com.kickstarter.KSApplication
import com.kickstarter.libs.ActivityLifecycleType
import com.kickstarter.libs.Environment
import com.trello.rxlifecycle.ActivityEvent
import com.trello.rxlifecycle.RxLifecycle
import rx.Observable
import rx.subjects.PublishSubject

abstract class KSArrayViewHolder(private val view: View) :  ActivityLifecycleType {

    private val lifecycle = PublishSubject.create<ActivityEvent>()
    abstract fun bindData(any: Any?)

    override fun lifecycle(): Observable<ActivityEvent> {
        return this.lifecycle
    }

    /**
     * This method is intended to be called only from `KSArrayAdapter` in order for it to inform the view holder
     * of its lifecycle.
     */
    fun lifecycleEvent(event: ActivityEvent) {
        this.lifecycle.onNext(event)

        if (ActivityEvent.DESTROY == event) {
            destroy()
        }
    }

    /**
     * Completes an observable when an [ActivityEvent] occurs in the activity's lifecycle.
     */
    fun <T> bindUntilEvent(event: ActivityEvent): Observable.Transformer<T, T> {
        return RxLifecycle.bindUntilActivityEvent(this.lifecycle, event)
    }

    /**
     * Completes an observable when the lifecycle event opposing the current lifecyle event is emitted.
     * For example, if a subscription is made during [ActivityEvent.CREATE], the observable will be completed
     * in [ActivityEvent.DESTROY].
     */
    fun <T> bindToLifecycle(): Observable.Transformer<T, T> {
        return RxLifecycle.bindActivity(this.lifecycle)
    }

    /**
     * Called when the ViewHolder is being detached. Subclasses should override if they need to do any work
     * when the ViewHolder is being de-allocated.
     */
    protected fun destroy() {}

    protected fun view(): View {
        return this.view
    }

    protected fun context(): Context {
        return this.view.context
    }


    protected fun environment(): Environment {
        return (context().applicationContext as KSApplication).component().environment()
    }
}
