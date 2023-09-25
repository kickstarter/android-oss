package com.kickstarter.viewmodels.projectpage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.htmlparser.HTMLParser
import com.kickstarter.libs.htmlparser.VideoViewElement
import com.kickstarter.libs.htmlparser.ViewElement
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.ui.data.ProjectData
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class ProjectCampaignViewModel {
    interface Inputs {
        /** Configure with current [ProjectData]. */
        fun configureWith(projectData: ProjectData)
        fun closeFullScreenVideo(seekPosition: Long)
        fun openVideoInFullScreen(index: Int, source: String, seekPosition: Long)
    }

    interface Outputs {
        /** Emits in a list format the DOM elements  */
        fun storyViewElements(): Observable<List<ViewElement>>
        fun onScrollToVideoPosition(): Observable<Int>
        fun onOpenVideoInFullScreen(): Observable<Pair<String, Long>>
        fun updateVideoCloseSeekPosition(): Observable<Pair<Int, Long>>
    }

    class ProjectCampaignViewModel(val environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val htmlParser = HTMLParser()
        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val storyViewElementsList = BehaviorSubject.create<List<ViewElement>>()

        private val closeFullScreenVideo = BehaviorSubject.create<Long>()
        private val openVideoInFullScreen = BehaviorSubject.create<Pair<Int, Pair<String, Long>>>()
        private val onScrollToVideoPosition = BehaviorSubject.create<Int>()
        private val onOpenVideoInFullScreen = BehaviorSubject.create<Pair<String, Long>>()
        private val updateVideoCloseSeekPosition = BehaviorSubject.create<Pair<Int, Long>>()

        private val disposables = CompositeDisposable()

        init {
            val project = projectDataInput
                .map { it.project() }
                .filter { it.isNotNull() }

            project.distinctUntilChanged()
                .filter { it.story().isNotNull() }
                .map { requireNotNull(it.story()) }
                .map { htmlParser.parse(it) }
                .subscribe {
                    storyViewElementsList.onNext(it)
                }.addToDisposable(disposables)

            closeFullScreenVideo
                .withLatestFrom(openVideoInFullScreen) { closePosition, videoOpenPosition ->
                    Pair(videoOpenPosition.first, closePosition)
                }
                .withLatestFrom(storyViewElementsList) { pair, list -> Pair(pair, list) }
                .subscribe {
                    val itemIndex = it.first.first
                    if (it.second[itemIndex] is VideoViewElement) {
                        (it.second[itemIndex] as? VideoViewElement?)
                            ?.let { videoViewElement ->
                                val updatedList = it.second.toMutableList()
                                updatedList[itemIndex] = VideoViewElement(
                                    videoViewElement
                                        .sourceUrl,
                                    videoViewElement.thumbnailUrl, it.first.second
                                )

                                updateVideoCloseSeekPosition.onNext(it.first)
                                storyViewElementsList.onNext(updatedList)
                            }
                    }
                }.addToDisposable(disposables)

            closeFullScreenVideo
                .withLatestFrom(openVideoInFullScreen) { closePosition, videoOpenPosition ->
                    Pair(videoOpenPosition.first, closePosition)
                }
                .subscribe {

                    // updateVideoCloseSeekPosition.onNext(it)
                    onScrollToVideoPosition.onNext(it.first)
                }.addToDisposable(disposables)

            openVideoInFullScreen
                .distinctUntilChanged()
                .subscribe {
                    onOpenVideoInFullScreen.onNext(it.second)
                }.addToDisposable(disposables)
        }

        // - Inputs
        override fun configureWith(projectData: ProjectData) =
            this.projectDataInput.onNext(projectData)

        override fun closeFullScreenVideo(position: Long) = closeFullScreenVideo.onNext(position)
        override fun openVideoInFullScreen(index: Int, source: String, seekPosition: Long) =
            openVideoInFullScreen.onNext(Pair(index, Pair(source, seekPosition)))

        override fun storyViewElements(): Observable<List<ViewElement>> = storyViewElementsList
        override fun onScrollToVideoPosition(): Observable<Int> = onScrollToVideoPosition
        override fun onOpenVideoInFullScreen(): Observable<Pair<String, Long>> =
            onOpenVideoInFullScreen

        override fun updateVideoCloseSeekPosition(): Observable<Pair<Int, Long>> =
            updateVideoCloseSeekPosition

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectCampaignViewModel(environment) as T
        }
    }
}
