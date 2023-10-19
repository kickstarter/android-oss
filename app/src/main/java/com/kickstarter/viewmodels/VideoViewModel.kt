package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
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
        fun resume(): Observable<Boolean>
    }

    class VideoViewModel(private val environment: Environment, private val intent: Intent? = null) : ViewModel(), Inputs, Outputs {

        private val analyticEvents = requireNotNull(environment.analytics())
        private val preparePlayerWithUrl = BehaviorSubject.create<String>()
        private val preparePlayerWithUrlAndPosition = BehaviorSubject.create<Pair<String, Long>>()
        private val onVideoStarted = BehaviorSubject.create<Pair<Long, Long>>()
        private val onVideoCompleted = BehaviorSubject.create<Pair<Long, Long>>()

        private val disposables = CompositeDisposable()

        @JvmField
        val outputs: Outputs = this

        @JvmField
        val inputs: Inputs = this

        private fun intent() = intent?.let { Observable.just(it) } ?: Observable.empty()

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

        override fun resume(): Observable<Boolean> = Observable.just(true)

        init {
            val project = intent()
                .filter { (it.getParcelableExtra(IntentKey.PROJECT) as? Project).isNotNull() }
                .map { it.getParcelableExtra(IntentKey.PROJECT) as? Project }

            intent()
                .map { Pair(it?.getStringExtra(IntentKey.VIDEO_URL_SOURCE) ?: "", it?.getLongExtra(IntentKey.VIDEO_SEEK_POSITION, -1) ?: -1) }
                .map { Pair(requireNotNull(it.first), it.second) }
                .distinctUntilChanged()
                .take(1)
                .withLatestFrom(this.resume()) { p, _ ->
                    p
                }
                .subscribe {
                    if (it.first.isNotEmpty() && it.second >= 0) {
                        preparePlayerWithUrlAndPosition.onNext(it)
                    } else {
                        preparePlayerWithUrl.onNext(it.first)
                    }
                }
                .addToDisposable(disposables)

            Observable.combineLatest(project, onVideoStarted) { p, u ->
                Pair(p, u)
            }.distinctUntilChanged()
                .subscribe {
                    this.analyticEvents.trackVideoStarted(
                        it.first,
                        TimeUnit.MILLISECONDS.toSeconds(it.second.first),
                        TimeUnit.MILLISECONDS.toSeconds(it.second.second)
                    )
                }
                .addToDisposable(disposables)

            Observable.combineLatest(project, onVideoCompleted) { p, u ->
                Pair(p, u)
            }.distinctUntilChanged()
                .subscribe {
                    this.analyticEvents.trackVideoCompleted(
                        it.first,
                        TimeUnit.MILLISECONDS.toSeconds(it.second.first),
                        TimeUnit.MILLISECONDS.toSeconds(it.second.second)
                    )
                }
                .addToDisposable(disposables)
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VideoViewModel(environment, intent) as T
        }
    }
}
