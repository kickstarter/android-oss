package com.kickstarter.viewmodels

import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Activity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class ActivitySampleFriendFollowViewHolderViewModel {
    interface Inputs {
        /** Configure with current [Activity]. */
        fun configureWith(activity: Activity)
    }

    interface Outputs {
        fun bindActivity(): Observable<Activity>
    }

    class ViewModel : Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        val disposables = CompositeDisposable()

        private val activityInput = BehaviorSubject.create<Activity>()
        private val bindActivity = BehaviorSubject.create<Activity>()
        init {
            activityInput
                .filter { it.isNotNull() }
                .filter { it.user().isNotNull() }
                .subscribe {
                    bindActivity.onNext(it)
                }
                .addToDisposable(disposables)
        }

        override fun configureWith(activity: Activity) =
            this.activityInput.onNext(activity)
        override fun bindActivity(): Observable<Activity> = this.bindActivity

        fun clear() {
            disposables.clear()
        }
    }
}
