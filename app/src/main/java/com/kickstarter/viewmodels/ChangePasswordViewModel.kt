package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

data class UpdatePasswordUIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val email: String? = null
)
class ChangePasswordViewModel(val environment: Environment) : ViewModel() {

    private val apolloClient = requireNotNull(this.environment.apolloClientV2())
    private val analytics = requireNotNull(this.environment.analytics())

    private val mutableUIState = MutableStateFlow(UpdatePasswordUIState())
    val uiState: StateFlow<UpdatePasswordUIState> get() =
        mutableUIState.asStateFlow()
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(),
                initialValue = UpdatePasswordUIState(isLoading = true)
            )

    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            // TODO: Avoid using GraphQL generated types such as UpdateUserPasswordMutation.Data, return data model defined within the app.
            apolloClient.updateUserPassword(oldPassword, newPassword, newPassword)
                .asFlow()
                .onStart {
                    mutableUIState.emit(UpdatePasswordUIState(isLoading = true))
                }
                .map {
                    analytics.reset()
                    mutableUIState.emit(UpdatePasswordUIState(isLoading = false, email = it.updateUserAccount?.user?.email ?: ""))
                }
                .catch {
                    mutableUIState.emit(UpdatePasswordUIState(errorMessage = it.message ?: "", isLoading = false))
                }
                .collect()
        }
    }

    fun resetError() {
        viewModelScope.launch {
            mutableUIState.emit(UpdatePasswordUIState(errorMessage = null))
        }
    }
}

class ChangePasswordViewModelFactory(private val environment: Environment) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChangePasswordViewModel(environment) as T
    }
}
