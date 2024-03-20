package com.kickstarter.libs.preferences

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.rxjava2.RxDataStore
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

interface RxStringPreferenceType {
    /**
     * Get the current value of the preference.
     */
    fun get(): Observable<String>

    /**
     * Returns whether a value has been explicitly set for the preference.
     */
    val isSet: Observable<Boolean>

    /**
     * Set the preference to a value.
     */
    fun set(value: String?)

    /**
     * Delete the currently stored preference.
     */
    fun delete()
}

class StringRXDataStorePreference @JvmOverloads constructor(
    private val dataStore: RxDataStore<Preferences>,
    private val key: String,
    private val defaultValue: String = ""
) : RxStringPreferenceType {

    // Key for saving integer value
    private val stringKey: Preferences.Key<String> = stringPreferencesKey(key)
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun get(): Observable<String> = dataStore.data().map { prefString ->
            prefString[stringKey] ?: ""
        }.toObservable()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val isSet: Observable<Boolean>
        get() =
            dataStore.data().map { prefString ->
                prefString.contains(stringKey)
            }.toObservable()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun set(value: String?) {
        value?.let {
            dataStore.updateDataAsync { prefString ->
                val mutablePrefs = prefString.toMutablePreferences()
                mutablePrefs[stringKey] = value
                Single.just(mutablePrefs)
            }.doOnError {
                Timber.d("WTF")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun delete() {
        dataStore.updateDataAsync { prefString ->
            val mutablePrefs = prefString.toMutablePreferences()
            mutablePrefs.clear()
            Single.just(mutablePrefs)
        }
    }
}
