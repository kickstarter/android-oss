package com.kickstarter.libs.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.rxjava2.RxDataStore
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi


class StringDataStorePreference @JvmOverloads constructor(
    private val dataStore: RxDataStore<Preferences>,
    private val key: String,
    private val defaultValue: String = ""
) : StringPreferenceType {

    // Key for saving integer value
    private val stringKey: Preferences.Key<String> = stringPreferencesKey(key)
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun get(): String {
        var value = defaultValue

        dataStore.data().map { prefString ->
            prefString[stringKey] ?: ""
        }
            .subscribe { value = it }
            .dispose()
        return value
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val isSet: Boolean
        get() {
            var value = false
            dataStore.data().map { prefString ->
                prefString.contains(stringKey)
            }
                .subscribe { value = it }
                .dispose()

            return value
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun set(value: String?) {
        value?.let {
            dataStore.updateDataAsync { prefString ->
                val mutablePrefs = prefString.toMutablePreferences()
                mutablePrefs[stringKey] = value
                Single.just(mutablePrefs)
            }.subscribe().dispose()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun delete() {
        dataStore.updateDataAsync { prefString ->
            val mutablePrefs = prefString.toMutablePreferences()
            mutablePrefs.clear()
            Single.just(mutablePrefs)
        }.subscribe().dispose()
    }
}
