package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class ChangePasswordViewModel(val environment: Environment) : ViewModel() {

    private val mutableError = MutableStateFlow("")
    val error: StateFlow<String> get() = mutableError.asStateFlow()

    private val mutableSuccess = MutableStateFlow("")
    val success: StateFlow<String> get() = mutableSuccess.asStateFlow()

    private val mutableIsLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = mutableIsLoading.asStateFlow()

    private val apolloClient = requireNotNull(this.environment.apolloClientV2())
    private val analytics = this.environment.analytics()

    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            apolloClient.updateUserPassword(oldPassword, newPassword, newPassword)
                .asFlow()
                .onStart {
                    mutableIsLoading.emit(true)
                }
                .onCompletion {
                    mutableIsLoading.emit(false)
                }
                .catch {
                    mutableError.emit(it.message ?: "")
                }
                .collect {
                    analytics?.reset()
                    mutableSuccess.emit(it.updateUserAccount()?.user()?.email() ?: "")
                }
        }
    }

    fun resetError() {
        viewModelScope.launch {
            mutableError.emit("")
        }
    }
}

class ChangePasswordViewModelFactory(private val environment: Environment) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChangePasswordViewModel(environment) as T
    }
}
