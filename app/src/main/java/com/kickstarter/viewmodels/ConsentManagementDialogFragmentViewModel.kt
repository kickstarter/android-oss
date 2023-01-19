package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.ui.SharedPreferenceKey
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

interface ConsentManagementDialogFragmentViewModel {

    interface Inputs {
        /** The consent preference of the user represented as a [Boolean] value. True if they allow consent, false if they deny consent. */
        fun userConsentPreference(consentPreference: Boolean)
    }

    interface Outputs

    class ConsentManagementDialogFragmentViewModel(environment: Environment) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val userConsentPreference = BehaviorSubject.create<Boolean>()

        private val sharedPreferences = requireNotNull(environment.sharedPreferences())

        private val disposables = CompositeDisposable()

        init {
            disposables.add(
                userConsentPreference
                    .subscribe {
                        sharedPreferences.edit().putBoolean(SharedPreferenceKey.CONSENT_MANAGEMENT_PREFERENCE, it).apply()
                    }
            )
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        override fun userConsentPreference(consentPreference: Boolean) {
            this.userConsentPreference.onNext(consentPreference)
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConsentManagementDialogFragmentViewModel(environment) as T
        }
    }
}
