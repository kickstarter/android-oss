package com.kickstarter.features.videofeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.videofeed.ui.VideoFeedActivity
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.libs.utils.extensions.isTrue
import com.kickstarter.models.Project
import com.kickstarter.services.DiscoveryParams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import kotlin.coroutines.EmptyCoroutineContext

data class VideoFeedUIState(
    val isLoading: Boolean = false,
    val isErrored: Boolean = false,
    val projects: List<VideoFeedActivity.Project> = emptyList()
)

open class VideoFeedViewModel(
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

    val params = DiscoveryParams.builder()
        .sort(DiscoveryParams.Sort.MAGIC)
        .build()

    private var nextPage: String? = ""

    init {
        loadProjects()
    }

    fun loadProjects() {
        scope.launch {
            apolloClient.getProjects(params, nextPage)
                .asFlow()
                .map { envelope ->
                    val pList = envelope.projects().filter { it.hasVideo() && it.video().isNotNull() }.map {
                        VideoFeedActivity.Project(
                            id = it.id().toInt(),
                            category = it.category()?.name() ?: "category",
                            title = it.name() ?: "name",
                            subtitle = it.blurb() ?: "Subtitle",
                            percentageFunded = it.percentageFunded().toInt(),
                            videoUrl = it.video()?.hls() ?: it.video()?.base() ?: it.video()?.high()
                                ?: ""

                        )
                    }

                    if (envelope.pageInfoEnvelope()?.hasNextPage.isTrue()) {
                        nextPage = envelope.pageInfoEnvelope()?.endCursor
                    }

                    return@map pList
                }
                .collectLatest { projects ->
                    _videoFeedUIState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            projects = currentState.projects + projects
                        )
                    }
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
