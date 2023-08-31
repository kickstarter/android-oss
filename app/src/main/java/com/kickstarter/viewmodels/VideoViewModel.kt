package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment

import com.kickstarter.libs.utils.extensions.isNotNull
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
        fun preparePlayerWithUrlAndPosition(): Observable<Pair<String, Long>>
    }

    interface Inputs {
        fun onVideoStarted(videoLength: Long, videoPosition: Long)
        fun onVideoCompleted(videoLength: Long, videoPosition: Long)
    }

    class ViewModel(environment: Environment) : ActivityViewModel<VideoActivity>(environment), Inputs, Outputs {
        private val preparePlayerWithUrl = BehaviorSubject.create<String>()
        private val preparePlayerWithUrlAndPosition = BehaviorSubject.create<Pair<String, Long>>()
        private val onVideoStarted = BehaviorSubject.create<Pair<Long, Long>>()
        private val onVideoCompleted = BehaviorSubject.create<Pair<Long, Long>>()
        @JvmField
        val outputs: Outputs = this

        @JvmField
        val inputs: Inputs = this

        override fun preparePlayerWithUrl(): Observable<String> {
            return preparePlayerWithUrl
        }

        override fun preparePlayerWithUrlAndPosition(): Observable<Pair<String, Long>> {
            return preparePlayerWithUrlAndPosition
        }

        override fun onVideoStarted(videoLength: Long, videoPosition: Long) {
            return onVideoStarted.onNext(Pair(videoLength, videoPosition))
        }

        override fun onVideoCompleted(videoLength: Long, videoPosition: Long) {
            return onVideoCompleted.onNext(Pair(videoLength, videoPosition))
        }

        init {

            intent()
                .map { Pair(it?.getStringExtra(IntentKey.VIDEO_URL_SOURCE), it?.getLongExtra(IntentKey.VIDEO_SEEK_POSITION, 0) ?: 0) }
                .filter { it.first.isNotNull() }
                .map { Pair(requireNotNull(it.first), it.second) }
                .distinctUntilChanged()
                .take(1)
                .compose(bindToLifecycle())
                .subscribe { preparePlayerWithUrlAndPosition.onNext(it) }

            val project = intent()
                .map { it.getParcelableExtra(IntentKey.PROJECT) as Project? }
                .filter { it.isNotNull() }
                .map { requireNotNull(it) }

            project.map { it.video() }
                .filter { it.isNotNull() }
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
                    this.analyticEvents.trackVideoStarted(
                        it.first,
                        TimeUnit.MILLISECONDS.toSeconds(it.second.first),
                        TimeUnit.MILLISECONDS.toSeconds(it.second.second)
                    )
                }

            Observable.combineLatest(project, onVideoCompleted) { p, u ->
                Pair(p, u)
            }.distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    this.analyticEvents.trackVideoCompleted(
                        it.first,
                        TimeUnit.MILLISECONDS.toSeconds(it.second.first),
                        TimeUnit.MILLISECONDS.toSeconds(it.second.second)
                    )
                }
        }
    }
}
