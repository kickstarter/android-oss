package com.kickstarter.features.videofeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext

data class VideoFeedUIState(
    val project: Project? = null,
)

class VideoFeedViewModel(
    private val environment: Environment,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private val _videoFeedUIState = MutableStateFlow(VideoFeedUIState())
    val videoFeedUIState: StateFlow<VideoFeedUIState>
        get() = _videoFeedUIState
            .asStateFlow()
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = VideoFeedUIState()
            )

    init {
        getProject("musicalbeings/tembo-a-new-musical-instrument-for-playful-music-making")
    }

    private fun getProject(slug: String) {
        scope.launch {
            apolloClient.getProject(slug)
                .asFlow()
                .catch {
                    Timber.d(it)
                }
                .collect { project ->
                    _videoFeedUIState.update { it.copy(project = project) }
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
