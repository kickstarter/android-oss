package com.kickstarter.features.videofeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext

data class VideoFeedUIState(
    val projects: List<Project> = emptyList()
)

class VideoFeedViewModel(
    private val environment: Environment,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val apolloClient = requireNotNull(environment.apolloClientV2())

    private val _videoFeedUIState = MutableStateFlow(VideoFeedUIState())

    val videoFeedUIState: StateFlow<VideoFeedUIState> = _videoFeedUIState.asStateFlow()

    init {
        loadDemoProjects()
    }

    private fun loadDemoProjects() {
        // TODO: replace with a single paginated feed query.
        listOf(
            "ringobottle/ringo-move",
            "cameraintelligence/caira-worlds-first-ai-native-mirrorless-camera",
            "rollbed/roll-real-bed-in-an-unreal-size",
            "wowfactories/the-combine-driver-2-in-1-ratchet-and-torque-screwdriver",
            "hyodo/magbasetm-swappable-wallet-for-magsafe-designed-by-hyodo",
            "kode/kode-dot-the-all-in-one-pocket-size-maker-device",
        ).forEach { getProject(it) }
    }

    private fun getProject(slug: String) {
        scope.launch {
            apolloClient.getProject(slug)
                .asFlow()
                .catch { Timber.d(it) }
                .collect { project ->
                    _videoFeedUIState.update { state ->
                        state.copy(projects = state.projects + project)
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
