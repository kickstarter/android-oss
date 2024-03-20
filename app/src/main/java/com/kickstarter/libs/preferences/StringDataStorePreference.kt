package com.kickstarter.libs.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.reactivex.Observable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asObservable
import timber.log.Timber

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("oauth")

class StringDataStorePreference @JvmOverloads constructor(
    private val context: Context,
    private val key: String,
    private val defaultValue: String = ""
) : RxStringPreferenceType{

    // Key for saving integer value
    private val stringKey: Preferences.Key<String> = stringPreferencesKey(key)

    override fun get() =
        context.dataStore.data
            .map { preferences ->
                preferences[stringKey] ?: defaultValue
            }.asObservable()

    override val isSet: Observable<Boolean>
        get() = context.dataStore.data
                .map { preferences ->
                    preferences.contains(stringKey)
                }.asObservable()

    @OptIn(DelicateCoroutinesApi::class)
    override fun set(value: String?) {
        value?.let {
            GlobalScope.launch {
                try {
                    context.dataStore.edit { setting ->
                        setting[stringKey] = value
                    }
                }catch (e: Exception) {
                    Timber.d("wtf")
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun delete() {
        GlobalScope.launch {
            context.dataStore.edit { setting ->
                setting.remove(stringKey)
            }
        }
    }
}
