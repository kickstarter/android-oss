package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

interface BackingDetailsViewModel {

    interface Inputs

    interface Outputs {
        /** Emits the URL of the backing details to load in the web view. */
        fun url(): Observable<String>
    }

    class BackingDetailsViewModel(environment: Environment, private val intent: Intent? = null) : ViewModel(), Inputs, Outputs {

        private val url = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private val disposables = CompositeDisposable()

        private fun intent() = this.intent?.let { Observable.just(it) } ?: Observable.empty()

        init {
            intent()
                .map { it.getStringExtra(IntentKey.URL) }
                .ofType(String::class.java)
                .subscribe { this.url.onNext(it) }
                .addToDisposable(disposables)
        }
        override fun url(): Observable<String> = this.url

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BackingDetailsViewModel(environment, intent) as T
            }
        }
    }
}
