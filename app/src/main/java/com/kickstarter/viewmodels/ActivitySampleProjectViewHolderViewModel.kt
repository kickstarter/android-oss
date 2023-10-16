package com.kickstarter.viewmodels

import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Activity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class ActivitySampleProjectViewHolderViewModel {
    interface Inputs {
        /** Configure with current [Activity]. */
        fun configureWith(activity: Activity)

        fun onCleared()
    }

    interface Outputs {
        fun bindActivity(): Observable<Activity>
    }

    class ActivitySampleProjectHolderViewModel : Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val activityInput = BehaviorSubject.create<Activity>()
        private val bindActivity = BehaviorSubject.create<Activity>()
        private val disposables = CompositeDisposable()
        init {
            activityInput
                .filter { it.isNotNull() }
                .filter { it.user().isNotNull() }
                .filter { it.project().isNotNull() }
                .map { requireNotNull(it) }
                .subscribe {
                    bindActivity.onNext(it)
                }.addToDisposable(disposables)
        }

        override fun configureWith(activity: Activity) =
            this.activityInput.onNext(activity)
        override fun bindActivity(): Observable<Activity> = this.bindActivity

        override fun onCleared() {
            disposables.clear()
        }
    }
}
