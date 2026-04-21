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

    private val _videoFeedUIState = MutableStateFlow(VideoFeedUIState())
    val videoFeedUIState: StateFlow<VideoFeedUIState> = _videoFeedUIState.asStateFlow()

    private var errorAction: (message: String?) -> Unit = {}

    init {
        loadVideoFeed()
    }

    fun provideErrorAction(errorAction: (message: String?) -> Unit) {
        this.errorAction = errorAction
    }

    fun bookmarkProject(project: Project) {
        scope.launch {
            val isStarred = project.isStarred()
            val result = if (isStarred) {
                apolloClient.unWatchProjectSuspend(project)
            } else {
                apolloClient.watchProjectSuspend(project)
            }

            if (result.isFailure) {
                Timber.e(result.exceptionOrNull())
                errorAction.invoke(null)
            }

            if (result.isSuccess) {
                val updatedStarred = !isStarred
                val updatedItems = _videoFeedUIState.value.items.map { item ->
                    if (item.project.id() == project.id()) {
                        item.copy(project = item.project.toBuilder().isStarred(updatedStarred).build())
                    } else item
                }
                _videoFeedUIState.emit(_videoFeedUIState.value.copy(items = updatedItems))
            }
        }
    }

    private fun loadVideoFeed() {
        scope.launch {
            _videoFeedUIState.emit(VideoFeedUIState(isLoading = true))

            val result = apolloClient.getVideoFeed(first = 10)

            if (result.isFailure) {
                Timber.e(result.exceptionOrNull())
                errorAction.invoke(null)
                _videoFeedUIState.emit(VideoFeedUIState(isLoading = false))
            }

            if (result.isSuccess) {
                val envelope = result.getOrNull()
                _videoFeedUIState.emit(
                    VideoFeedUIState(
                        items = envelope?.items ?: emptyList(),
                        isLoading = false
                    )
                )
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
