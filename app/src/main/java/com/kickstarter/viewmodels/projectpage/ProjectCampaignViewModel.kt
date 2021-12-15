package com.kickstarter.viewmodels.projectpage

import androidx.annotation.NonNull
import com.kickstarter.libs.Environment
import com.kickstarter.libs.FragmentViewModel
import com.kickstarter.libs.htmlparser.HTMLParser
import com.kickstarter.libs.htmlparser.VideoViewElement
import com.kickstarter.libs.htmlparser.ViewElement
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.ui.data.ProjectData
import com.kickstarter.ui.fragments.projectpage.ProjectOverviewFragment
import rx.Observable
import rx.subjects.BehaviorSubject

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
        fun onOpenVideoInFullScreen(): Observable< Pair<String, Long>>
        fun updateVideoCloseSeekPosition(): Observable< Pair<Int, Long>>
    }

    class ViewModel(@NonNull val environment: Environment) : FragmentViewModel<ProjectOverviewFragment>(environment), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val htmlParser = HTMLParser()
        private val projectDataInput = BehaviorSubject.create<ProjectData>()
        private val storyViewElementsList = BehaviorSubject.create<List<ViewElement>>()

        private val closeFullScreenVideo = BehaviorSubject.create<Long>()
        private val openVideoInFullScreen = BehaviorSubject.create<Pair<Int, Pair<String, Long>>>()
        private val onScrollToVideoPosition = BehaviorSubject.create<Int>()
        private val onOpenVideoInFullScreen = BehaviorSubject.create< Pair<String, Long>>()
        private val updateVideoCloseSeekPosition = BehaviorSubject.create<Pair<Int, Long>>()
        init {
            val project = projectDataInput
                .map { it.project() }
                .filter { ObjectUtils.isNotNull(it) }
                .map { requireNotNull(it) }

            project
                .distinctUntilChanged()
                .filter { ObjectUtils.isNotNull(it.story()) }
                .map { requireNotNull(it.story()) }
                .map { htmlParser.parse(it) }
                .compose(bindToLifecycle())
                .subscribe {

                    storyViewElementsList.onNext(it)
                }

            closeFullScreenVideo
                .withLatestFrom(openVideoInFullScreen) {
                    closePosition, videoOpenPosition ->
                    Pair(videoOpenPosition.first, closePosition)
                }.withLatestFrom(storyViewElementsList) { pair, list ->
                    Pair(pair, list)
                }
                .compose(bindToLifecycle())
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
                }

            closeFullScreenVideo
                .withLatestFrom(openVideoInFullScreen) {
                    closePosition, videoOpenPosition ->
                    Pair(videoOpenPosition.first, closePosition)
                }
                .compose(bindToLifecycle())
                .subscribe {

                    // updateVideoCloseSeekPosition.onNext(it)
                    onScrollToVideoPosition.onNext(it.first)
                }

            openVideoInFullScreen
                .distinctUntilChanged()
                .compose(bindToLifecycle())
                .subscribe {
                    onOpenVideoInFullScreen.onNext(it.second)
                }
        }

        // - Inputs
        override fun configureWith(projectData: ProjectData) =
            this.projectDataInput.onNext(projectData)
        override fun closeFullScreenVideo(position: Long) = closeFullScreenVideo.onNext(position)
        override fun openVideoInFullScreen(index: Int, source: String, seekPosition: Long) =
            openVideoInFullScreen.onNext(Pair(index, Pair(source, seekPosition)))

        override fun storyViewElements(): Observable<List<ViewElement>> = storyViewElementsList
        override fun onScrollToVideoPosition(): Observable<Int> = onScrollToVideoPosition
        override fun onOpenVideoInFullScreen(): Observable< Pair<String, Long>> = onOpenVideoInFullScreen
        override fun updateVideoCloseSeekPosition(): Observable< Pair<Int, Long>> =
            updateVideoCloseSeekPosition
    }
}
