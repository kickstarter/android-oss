package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.VideoActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface VideoViewModel {
    interface Outputs {
        /** Emits the url of the video for the player.  */
        fun preparePlayerWithUrl(): Observable<String>
        fun onVideoStarted(): Observable<Pair<Long,Long>>
    }

    class ViewModel(environment: Environment) : ActivityViewModel<VideoActivity>(environment), Outputs {
        private val preparePlayerWithUrl = BehaviorSubject.create<String>()
        private val onVideoStarted = BehaviorSubject.create<Pair<Long,Long>>()
        @JvmField
        val outputs: Outputs = this
        override fun preparePlayerWithUrl(): Observable<String> {
            return preparePlayerWithUrl
        }

        override fun onVideoStarted(): Observable<Pair<Long, Long>> {
            return onVideoStarted
        }

        init {
            intent()
                    .map<Any> { it.getParcelableExtra(IntentKey.PROJECT) }
                    .ofType(Project::class.java)
                    .filter {  ObjectUtils.isNotNull(it) }
                    .map { it.video() }
                    .filter { ObjectUtils.isNotNull(it) }
                    .map {  it?.hls()?: it?.high() }
                    .distinctUntilChanged()
                    .take(1)
                    .compose(bindToLifecycle())
                    .subscribe {  preparePlayerWithUrl.onNext(it) }

            onVideoStarted
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { requireNotNull(it) }
                    .compose(bindToLifecycle())
                    .subscribe {
                      //  this.lake.onNext(it)
                    }
        }
    }
}