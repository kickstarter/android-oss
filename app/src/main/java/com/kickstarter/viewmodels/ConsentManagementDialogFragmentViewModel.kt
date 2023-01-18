package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.ui.SharedPreferenceKey
import rx.subjects.BehaviorSubject

interface ConsentManagementDialogFragmentViewModel {

    interface Inputs {
        fun onAllow()
        fun onDeny()
    }

    interface Outputs {}

    class ConsentManagementDialogFragmentViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val onUserTapsAllow = BehaviorSubject.create<Void>()
        private val onUserTapsDeny = BehaviorSubject.create<Void>()

        private val sharedPreferences = requireNotNull(environment.sharedPreferences())

        init {
            onUserTapsAllow
                .subscribe {
                    sharedPreferences.edit().putBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, true).apply()
                }

            onUserTapsDeny
                .subscribe {
                    sharedPreferences.edit().putBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, false).apply()
                }
        }

        override fun onAllow() {
            this.onUserTapsAllow.onNext(null)
        }

        override fun onDeny() {
            this.onUserTapsDeny.onNext(null)
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConsentManagementDialogFragmentViewModel(environment) as T
        }
    }
}