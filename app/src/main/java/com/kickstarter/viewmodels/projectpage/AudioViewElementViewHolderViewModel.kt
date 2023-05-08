package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import com.kickstarter.libs.KSLifecycleEvent
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isMP3Url
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface AudioViewElementViewHolderViewModel {

    interface Inputs {
        fun configureWith(audioViewElement: AudioViewElement)
        fun fragmentLifeCycle(lifecycleEvent: KSLifecycleEvent)
        fun onPlayButtonPressed()
    }

    interface Outputs {
        fun preparePlayerWithUrl(): Observable<String>
        fun stopPlayer(): Observable<Unit>
        fun pausePlayer(): Observable<Unit>
    }

    class AudioViewElementViewHolderViewModel(private val lifecycleObservable: BehaviorSubject<KSLifecycleEvent>) :
        ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()
        private val audioElement = BehaviorSubject.create<AudioViewElement>()
        private val playButtonPressed = PublishSubject.create<Unit>()

        private val sourceUrl = PublishSubject.create<String>()
        private val pausePlayer = PublishSubject.create<Unit>()
        private val stopPlayer = PublishSubject.create<Unit>()

        init {

            this.lifecycleObservable
                .subscribe {
                    when (it) {
                        KSLifecycleEvent.PAUSE -> this.pausePlayer.onNext(Unit)
                        KSLifecycleEvent.STOP -> this.stopPlayer.onNext(Unit)
                        else -> {
                        }
                    }
                }.addToDisposable(disposables)

            this.audioElement
                .filter {
                    !it.sourceUrl.isNullOrBlank() && it.sourceUrl.isMP3Url()
                }
                .distinctUntilChanged()
                .subscribe {
                    this.sourceUrl.onNext(it.sourceUrl)
                }.addToDisposable(disposables)

            this.playButtonPressed
                .subscribe {
                    this.stopPlayer.onNext(Unit)
                }.addToDisposable(disposables)
        }

        // - Inputs
        override fun configureWith(audioViewElement: AudioViewElement) =
            this.audioElement.onNext(audioViewElement)

        override fun fragmentLifeCycle(lifecycleEvent: KSLifecycleEvent) =
            this.lifecycleObservable.onNext(lifecycleEvent)

        override fun onPlayButtonPressed() =
            this.playButtonPressed.onNext(Unit)

        // - Outputs
        override fun preparePlayerWithUrl(): PublishSubject<String> =
            this.sourceUrl

        override fun stopPlayer(): Observable<Unit> =
            this.stopPlayer

        override fun pausePlayer(): Observable<Unit> =
            this.pausePlayer

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }
}
