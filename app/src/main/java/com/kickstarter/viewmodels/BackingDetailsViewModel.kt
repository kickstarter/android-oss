package com.kickstarter.viewmodels

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.libs.Environment
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow

class BackingDetailsViewModel(environment: Environment, private val intent: Intent? = null) : ViewModel() {

    private var mutableUrl = MutableSharedFlow<String>()
    val url: SharedFlow<String>
        get() = mutableUrl
            .asSharedFlow()
    private fun intent() = this.intent?.let { Observable.just(it) } ?: Observable.empty()

    init {
        viewModelScope.launch {
            intent()
                .map { it.getStringExtra(IntentKey.URL) }
                .ofType(String::class.java)
                .asFlow().map { url ->
                    mutableUrl.emit(url)
                }.collect()
        }
    }

    class Factory(private val environment: Environment, private val intent: Intent? = null) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackingDetailsViewModel(environment, intent) as T
        }
    }
}
