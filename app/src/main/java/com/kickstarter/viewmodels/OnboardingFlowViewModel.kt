package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.ui.SharedPreferenceKey.HAS_SEEN_NOTIF_PERMISSIONS
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.rx2.asFlow
import kotlin.coroutines.EmptyCoroutineContext

class OnboardingFlowViewModel(
    environment: Environment,
    testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {
    private val currentUser = requireNotNull(environment.currentUserV2())
    val sharedPreferences = requireNotNull(environment.sharedPreferences())
    val ffClient = requireNotNull(environment.featureFlagClient())

    private var isUserLoggedIn = false
    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    init {
        scope.launch {
            currentUser.observable().asFlow()
                .collectLatest {
                    isUserLoggedIn = it.isPresent()
                }
        }
    }

    fun isUserLoggedIn(): Boolean = isUserLoggedIn

    fun hasSeenNotificationsPermission(hasSeen: Boolean) {
        sharedPreferences.edit().putBoolean(HAS_SEEN_NOTIF_PERMISSIONS, hasSeen).apply()
    }


    class Factory(
        private val environment: Environment,
        private val testDispatcher: CoroutineDispatcher? = null
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingFlowViewModel(environment, testDispatcher) as T
        }
    }
}
