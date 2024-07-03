package com.kickstarter.features.pledgedprojectsoverview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.kickstarter.R
import com.kickstarter.features.pledgedprojectsoverview.ui.PPOCardViewType
import com.kickstarter.features.pledgedprojectsoverview.data.PPOCard
import com.kickstarter.libs.Environment
import com.kickstarter.models.Project
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class PledgedProjectsOverviewUIState(
    val totalAlerts: Int = 10,
    val isLoading: Boolean = false,
    val isErrored: Boolean = false,
)

class PledgedProjectsOverviewViewModel(environment: Environment) : ViewModel() {

    private val ppoCards = MutableStateFlow<PagingData<PPOCard>>(PagingData.empty())
    private val totalAlerts = MutableStateFlow<Int>(0)
    private var mutableProjectFlow = MutableSharedFlow<Project>()
    private var snackbarMessage: (stringID: Int) -> Unit = {}

    private val apolloClient = requireNotNull(environment.apolloClientV2())
    val ppoCardsState: StateFlow<PagingData<PPOCard>> = ppoCards.asStateFlow()
    val totalAlertsState: StateFlow<Int> = totalAlerts.asStateFlow()
    private val mutablePledgedProjectsOverviewUIState = MutableStateFlow(PledgedProjectsOverviewUIState())

    val pledgedProjectsOverviewUIState: StateFlow<PledgedProjectsOverviewUIState>
        get() = mutablePledgedProjectsOverviewUIState
            .asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = PledgedProjectsOverviewUIState(),
            )

    val projectFlow: SharedFlow<Project>
        get() = mutableProjectFlow
            .asSharedFlow()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
            )

    fun showSnackbarAndRefreshCardsList() {
        snackbarMessage.invoke(R.string.address_confirmed_snackbar_text_fpo)

        // TODO: MBL-1556 refresh the PPO list (i.e. requery the PPO list).
    }

    val projectFlow: SharedFlow<Project>
        get() = mutableProjectFlow
            .asSharedFlow()
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
            )

    class Factory(private val environment: Environment) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PledgedProjectsOverviewViewModel(environment) as T
        }
    }

    fun provideSnackbarMessage(snackBarMessage: (Int) -> Unit) {
        this.snackbarMessage = snackBarMessage
    }

    fun onMessageCreatorClicked(projectName: String) {
        viewModelScope.launch {
            apolloClient.getProject(
                slug = projectName,
            )
                .asFlow()
                .onStart {
                    emitCurrentState(isLoading = true)
                }.map { project ->
                    mutableProjectFlow.emit(project)
                }.catch {
                    snackbarMessage.invoke(R.string.Something_went_wrong_please_try_again)
                }.onCompletion {
                    emitCurrentState()
                }
                .collect()
        }
    }

    private suspend fun emitCurrentState(isLoading: Boolean = false, isErrored: Boolean = false) {
        mutablePledgedProjectsOverviewUIState.emit(
            PledgedProjectsOverviewUIState(
                totalAlerts = totalAlerts,
                isLoading = isLoading,
                isErrored = isErrored,
            )
        )
    }
}
