package com.kickstarter.viewmodels.projectpage

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.htmlparser.AudioViewElement
import com.kickstarter.ui.fragments.projectpage.ProjectCampaignFragment
import com.trello.rxlifecycle.FragmentEvent
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import timber.log.Timber

interface AudioViewElementViewHolderViewModel {

    interface Inputs {
        fun configureWith(audioViewElement: AudioViewElement)
        fun fragmentLifeCycle(lifecycleEvent: FragmentEvent)
        fun onPlayButtonPressed()
    }

    interface Outputs {
        fun preparePlayerWithUrl(): Observable<String>
        fun stopPlayer(): Observable<Void>
        fun pausePlayer(): Observable<Void>
        fun startPlayer(): Observable<Void>
    }

    class ViewModel(@NonNull environment: Environment) :
        FragmentViewModel<ProjectCampaignFragment>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val audioElement = BehaviorSubject.create<AudioViewElement>()
        private val lifecycleObservable = BehaviorSubject.create<FragmentEvent>()
        private val playButtonPressed = PublishSubject.create<Void>()

        private val sourceUrl = PublishSubject.create<String>()
        private val pausePlayer = PublishSubject.create<Void>()
        private val stopPlayer = PublishSubject.create<Void>()
        private val startPlayer = PublishSubject.create<Void>()

        init {
            this.lifecycleObservable
                .compose(bindToLifecycle())
                .subscribe {
                    Timber.d("$this is aware of the lifecycle changes on  parent fragment: $it")
                    when (it) {
                        FragmentEvent.PAUSE -> this.pausePlayer.onNext(null)
                        FragmentEvent.STOP -> this.stopPlayer.onNext(null)
                        else -> {
                        }
                    }
                }

            this.audioElement
                .filter {
                    !it.sourceUrl.isNullOrBlank()
                }
                .compose(bindToLifecycle())
                .subscribe {
                    this.sourceUrl.onNext(it.sourceUrl)
                }

            this.playButtonPressed
                .compose(bindToLifecycle())
                .subscribe {
                    this.stopPlayer.onNext(null)
                }
        }

        // - Inputs
        override fun configureWith(audioViewElement: AudioViewElement) =
            this.audioElement.onNext(audioViewElement)

        override fun fragmentLifeCycle(lifecycleEvent: FragmentEvent) =
            this.lifecycleObservable.onNext(lifecycleEvent)

        override fun onPlayButtonPressed() =
            this.playButtonPressed.onNext(null)

        // - Outputs
        override fun preparePlayerWithUrl(): PublishSubject<String> =
            this.sourceUrl

        override fun stopPlayer(): Observable<Void> =
            this.stopPlayer

        override fun startPlayer(): Observable<Void> =
            this.startPlayer

        override fun pausePlayer(): Observable<Void> =
            this.pausePlayer
    }
}
