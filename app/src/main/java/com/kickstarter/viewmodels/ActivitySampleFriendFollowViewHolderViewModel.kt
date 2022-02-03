package com.kickstarter.viewmodels

import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Activity
import com.kickstarter.ui.viewholders.ActivitySampleFriendFollowViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject

class ActivitySampleFriendFollowViewHolderViewModel {
    interface Inputs {
        /** Configure with current [Activity]. */
        fun configureWith(activity: Activity)
    }

    interface Outputs {
        fun bindActivity(): Observable<Activity>
    }

    class ViewModel(@NonNull val environment: Environment) : ActivityViewModel<ActivitySampleFriendFollowViewHolder>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val activityInput = BehaviorSubject.create<Activity>()
        private val bindActivity = BehaviorSubject.create<Activity>()
        init {
            activityInput
                .filter { ObjectUtils.isNotNull(it) }
                .filter { ObjectUtils.isNotNull(it.user()) }
                .map { requireNotNull(it) }
                .compose(bindToLifecycle())
                .subscribe {
                    bindActivity.onNext(it)
                }
        }

        override fun configureWith(activity: Activity) =
            this.activityInput.onNext(activity)
        override fun bindActivity(): Observable<Activity> = this.bindActivity
    }
}
