package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.VideoActivity
import rx.Observable
import rx.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

interface VideoViewModel {
    interface Outputs {
        /** Emits the url of the video for the player.  */
        fun preparePlayerWithUrl(): Observable<String>
    }

    interface Inputs {
        fun onVideoStarted(videoLength: Long, videoPosition: Long)
    }

    class ViewModel(environment: Environment) : ActivityViewModel<VideoActivity>(environment), Inputs, Outputs {
        private val preparePlayerWithUrl = BehaviorSubject.create<String>()
        private val onVideoStarted = BehaviorSubject.create<Pair<Long, Long>>()
        @JvmField
        val outputs: Outputs = this

        @JvmField
        val inputs: Inputs = this

        override fun preparePlayerWithUrl(): Observable<String> {
            return preparePlayerWithUrl
        }

        override fun onVideoStarted(videoLength: Long, videoPosition: Long) {
            return onVideoStarted.onNext(Pair(videoLength, videoPosition))
        }

        init {
            val project = intent()
                .map { it.getParcelableExtra(IntentKey.PROJECT) as Project }

            project.map { it.video() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { it?.hls() ?: it?.high() }
                .distinctUntilChanged()
                .take(1)
                .compose(bindToLifecycle())
                .subscribe { preparePlayerWithUrl.onNext(it) }

            Observable.combineLatest(project, onVideoStarted) { p, u ->
                Pair(p, u)
            }.distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.lake.trackVideoStarted(
                        it.first,
                        TimeUnit.MILLISECONDS.toSeconds(it.second.first),
                        TimeUnit.MILLISECONDS.toSeconds(it.second.second)
                    )
                }
        }
    }
}
