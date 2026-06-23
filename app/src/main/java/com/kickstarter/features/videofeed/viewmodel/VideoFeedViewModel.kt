package com.kickstarter.features.videofeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventContextValues.CtaContextName
import com.kickstarter.models.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext

data class VideoFeedUIState(
    val items: List<VideoFeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true
)

/**
 * ViewModel for the Video Feed feature.
 * Handles loading video feed items from Apollo, project bookmarking, and analytics tracking.
 *
 * @param environment The app environment providing dependencies like Apollo client and analytics.
 * @param entrySurface The surface from which the user entered the video feed (for analytics).
 * @param testDispatcher Optional [CoroutineDispatcher] for testing purposes.
 */
class VideoFeedViewModel(
    private val environment: Environment,
    private val entrySurface: String,
    private val testDispatcher: CoroutineDispatcher? = null,
    // EXPERIMENT: when true, the feed loops back to the first page instead of ending, so it feels
    // infinite. Off by default (ViewModel tests get normal pagination); the Activity turns it on.
    private val loopFeed: Boolean = false
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val currentUserV2 = requireNotNull(environment.currentUserV2())
    private val analyticEvents = requireNotNull(environment.analytics())

    private val _videoFeedUIState = MutableStateFlow(VideoFeedUIState())
    val videoFeedUIState: StateFlow<VideoFeedUIState> = _videoFeedUIState.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private var errorAction: (message: String?) -> Unit = {}

    private var nextPage: String? = null
    private var hasMore: Boolean = true

    init {
        scope.launch {
            currentUserV2.isLoggedIn.asFlow().collect { _isUserLoggedIn.value = it }
        }

        loadVideoFeed()
    }

    /**
     * Provides an error action callback to be used by the ViewModel when errors occur.
     *
     * @param errorAction A callback that takes an optional error message.
     */
    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    /**
     * Loads the next page of the video feed.
     * If a load is already in progress or there are no more items, it does nothing.
     */
    fun loadVideoFeed() {
        if (!hasMore) return
        scope.launch {
            if (_videoFeedUIState.value.isLoading) return@launch
            try {
                _videoFeedUIState.emit(_videoFeedUIState.value.copy(isLoading = true))

                val result = apolloClient.getVideoFeed(first = 10, cursor = nextPage)

                if (result.isFailure) {
                    Timber.e(result.exceptionOrNull())
                    errorAction.invoke(null)
                    return@launch
                }

                val envelope = result.getOrNull()
                val backendHasMore = envelope?.pageInfo?.hasNextPage ?: false
                nextPage = envelope?.pageInfo?.endCursor
                hasMore = backendHasMore
                // EXPERIMENT — artificial infinite feed: when enabled and the backend runs out of
                // pages, loop the cursor back to the first page (null) so the next load re-fetches it
                // and the user keeps scrolling the same content; the feed never reports the end.
                if (loopFeed && !backendHasMore) {
                    nextPage = null
                    hasMore = true
                }
                val newItems = envelope?.items ?: emptyList()

                _videoFeedUIState.emit(
                    VideoFeedUIState(
                        items = _videoFeedUIState.value.items + newItems,
                        isLoading = false,
                        hasMore = hasMore
                    )
                )
            } finally {
                if (_videoFeedUIState.value.isLoading) {
                    _videoFeedUIState.emit(_videoFeedUIState.value.copy(isLoading = false))
                }
            }
        }
    }

    /**
     * Bookmarks or un-bookmarks a project at the given index.
     * Performs an optimistic update of the UI state and reverts if the network request fails.
     *
     * @param project The [Project] to bookmark/un-bookmark.
     * @param index The index of the item in the current list.
     */
    fun bookmarkProject(project: Project, index: Int) {
        scope.launch {
            val isStarred = project.isStarred()
            val items = _videoFeedUIState.value.items
            if (index !in items.indices) return@launch

            // Optimistic update so the animation starts immediately on tap
            val newWatchesCount = if (isStarred) items[index].project.watchesCount() - 1 else items[index].project.watchesCount() + 1
            val optimisticItems = items.toMutableList()
            optimisticItems[index] = items[index].copy(
                project = items[index].project.toBuilder()
                    .isStarred(!isStarred)
                    .watchesCount(newWatchesCount)
                    .build()
            )
            _videoFeedUIState.emit(_videoFeedUIState.value.copy(items = optimisticItems))

            val result = if (isStarred) {
                apolloClient.unWatchProjectSuspend(project)
            } else {
                apolloClient.watchProjectSuspend(project)
            }

            if (result.isFailure) {
                Timber.e(result.exceptionOrNull())
                errorAction.invoke(null)
                // Revert optimistic update on failure
                _videoFeedUIState.emit(_videoFeedUIState.value.copy(items = items))
            }
        }
    }

    /**
     * Tracks a video impression event.
     *
     * @param item The [VideoFeedItem] that was impressed.
     * @param position The position of the item in the feed.
     */
    fun onVideoImpression(item: VideoFeedItem, position: Int) {
        analyticEvents.trackVideoFeedImpression(item, position, entrySurface)
    }

    /**
     * Tracks a video page settled event, including watch time data from the previous page.
     *
     * @param videoFeedItem The [VideoFeedItem] that is now settled.
     * @param toPosition The position of the settled item.
     * @param fromVideoFeedItem The [VideoFeedItem] that was previously settled.
     * @param watchTimeMs The watch time in milliseconds of the previous video.
     * @param videoDurationMs The total duration in milliseconds of the previous video.
     */
    fun onVideoPageSettled(
        videoFeedItem: VideoFeedItem,
        toPosition: Int,
        fromVideoFeedItem: VideoFeedItem,
        watchTimeMs: Long?,
        videoDurationMs: Long?
    ) {
        analyticEvents.trackVideoFeedPageViewed(videoFeedItem, toPosition, fromVideoFeedItem, watchTimeMs, videoDurationMs, entrySurface)
    }

    /**
     * Tracks a progress bar tap event.
     *
     * @param item The [VideoFeedItem] being interacted with.
     * @param percentageWatched The percentage of the video watched at the time of the tap.
     * @param watchTimeAtClick The watch time in milliseconds at the time of the tap.
     */
    fun onProgressBarTapped(item: VideoFeedItem, percentageWatched: Float, watchTimeAtClick: Long? = null) {
        analyticEvents.trackVideoFeedProgressBarTap(item, percentageWatched, watchTimeAtClick)
    }

    /**
     * Tracks a CTA (Call To Action) click event.
     *
     * @param project The [Project] associated with the CTA.
     * @param ctaType The type of CTA clicked.
     * @param watchTimeAtClick The watch time in milliseconds at the time of the click.
     */
    fun onCTAClicked(project: Project, ctaType: CtaContextName, watchTimeAtClick: Long? = null) {
        analyticEvents.trackVideoFeedCTAClicked(project, ctaType, watchTimeAtClick)
    }

    class Factory(
        private val environment: Environment,
        private val entrySurface: String,
        private val testDispatcher: CoroutineDispatcher? = null,
        private val loopFeed: Boolean = false
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VideoFeedViewModel(environment, entrySurface, testDispatcher, loopFeed) as T
        }
    }
}
