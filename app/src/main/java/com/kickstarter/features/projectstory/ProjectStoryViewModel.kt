package com.kickstarter.features.projectstory

import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.projectstory.data.StoriedProject
import com.kickstarter.features.projectstory.data.transform
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.isProjectUri
import com.kickstarter.models.Project
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import timber.log.Timber
import kotlin.coroutines.EmptyCoroutineContext

data class ProjectStoryUiState(
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val storiedProject: StoriedProject? = null,
)

class ProjectStoryViewModel(
    environment: Environment,
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val apolloClient = environment.apolloClientV2()!!

    private val _projectStoryUiState = MutableStateFlow(ProjectStoryUiState())
    val projectStoryUiState = _projectStoryUiState.asStateFlow()

    private var project: Project? = null

    val txt = mutableStateOf("https://www.kickstarter.com/projects/peak-design/roller-pro-carry-on-luggage-by-peak-design")

    private var job: Job? = null

    fun provideProject(project: Project) {
        if (this.project?.id() != project.id()) {
            this.project = project
        }
    }

    fun updateTxt(txt: String) {
        this.txt.value = txt
    }

    private fun parseSlug(s: String): String? {
        val uri = s.toUri()
        return if (uri.isProjectUri()) uri.path else null
    }

    fun fetchProject() {
        Timber.d("project.slug: ${project?.slug()}")
        val slug = project?.slug() ?: parseSlug(txt.value) ?: return

        job?.cancel()
        job = scope.launch {
            _projectStoryUiState.value = ProjectStoryUiState(isLoading = true)
            apolloClient
                .fetchProjectStory(slug)
                .onSuccess {
                    _projectStoryUiState.value = ProjectStoryUiState(
                        isLoading = false,
                        error = null,
                        storiedProject = transform(it?.projectStoryFragment)
                    )
                    Timber.d("storiedProject: ${projectStoryUiState.value.storiedProject}")
                }
                .onFailure {
                    _projectStoryUiState.value = ProjectStoryUiState(
                        isLoading = false,
                        error = it,
                        storiedProject = null
                    )
                }
        }
    }

    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProjectStoryViewModel(environment, testDispatcher) as T
        }
    }
}
