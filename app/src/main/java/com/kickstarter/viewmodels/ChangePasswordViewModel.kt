package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

const val GENERIC_ERROR = "GENERIC"

class ChangePasswordViewModel(val environment: Environment) : ViewModel() {

    private val mutableError = MutableStateFlow("")
    val error: Flow<String> get() = mutableError

    private val mutableSuccess = MutableStateFlow("")
    val success: Flow<String> get() = mutableSuccess

    private val mutableIsLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> get() = mutableIsLoading

    private val apolloClient = requireNotNull(this.environment.apolloClientV2())
    private val analytics = this.environment.analytics()

    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            mutableIsLoading.emit(true)
            val response =
                apolloClient.updateUserPasswordNew(oldPassword, newPassword, newPassword)
            if (response.hasErrors()) {
                mutableError.emit(response.errors?.first()?.message ?: GENERIC_ERROR)
            } else {
                analytics?.reset()
                response.data?.updateUserAccount()?.user()?.email()?.let { email ->
                    mutableSuccess.emit(email)
                } ?: run {
                    mutableError.emit(GENERIC_ERROR)
                }
            }
            mutableIsLoading.emit(false)
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
