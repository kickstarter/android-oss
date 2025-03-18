package com.kickstarter.viewmodels.projectpage

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.EmptyCoroutineContext

data class ParentUiState(
    val scrollable: Boolean = true,
)

data class SimilarProjectsUiState(
    val isLoading: Boolean = false,
    val data: List<Project>? = null,
    val error: Throwable? = null,
)

class SimilarProjectsViewModel(
    environment: Environment,
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val apolloClient = environment.apolloClientV2()!!

    private val _parentUiState = MutableStateFlow(ParentUiState())
    val parentUiState = _parentUiState.asStateFlow()

    private val _similarProjectsUiState = MutableStateFlow(SimilarProjectsUiState())
    val similarProjectsUiState = _similarProjectsUiState.asStateFlow()

    private var project: Project? = null
    private var job: Job? = null

    fun provideProject(project: Project) {
        if (this.project?.id() != project.id()) {
            this.project = project
            fetchSimilarProjects()
        }
    }

    @VisibleForTesting
    fun fetchSimilarProjects() {
        val pid = project?.id() ?: return

        job?.cancel()
        job = scope.launch {
            _similarProjectsUiState.value = SimilarProjectsUiState(isLoading = true)
            apolloClient
                .fetchSimilarProjects(pid)
                .onSuccess {
                    _similarProjectsUiState.value = SimilarProjectsUiState(
                        isLoading = false,
                        error = null,
                        data = it,
                    )
                }
                .onFailure {
                    _similarProjectsUiState.value = SimilarProjectsUiState(
                        isLoading = false,
                        error = it,
                        data = listOf(),
                    )
                }
        }
    }

    fun setParentScrollable(scrollable: Boolean) {
        _parentUiState.value = ParentUiState(scrollable)
    }

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SimilarProjectsViewModel(environment, testDispatcher) as T
        }
    }
}
