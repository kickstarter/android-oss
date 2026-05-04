package com.kickstarter.features.videofeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.videofeed.data.VideoFeedItem
import com.kickstarter.libs.Environment
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
    val isLoading: Boolean = false
)

class VideoFeedViewModel(
    private val environment: Environment,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val apolloClient = requireNotNull(environment.apolloClientV2())
    private val currentUserV2 = requireNotNull(environment.currentUserV2())

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
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

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
                nextPage = envelope?.pageInfo?.endCursor
                hasMore = envelope?.pageInfo?.hasNextPage ?: false
                _videoFeedUIState.emit(
                    VideoFeedUIState(
                        items = _videoFeedUIState.value.items + (envelope?.items ?: emptyList()),
                        isLoading = false
                    )
                )
            } finally {
                if (_videoFeedUIState.value.isLoading) {
                    _videoFeedUIState.emit(_videoFeedUIState.value.copy(isLoading = false))
                }
            }
        }
    }

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

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VideoFeedViewModel(environment, testDispatcher) as T
        }
    }
}
