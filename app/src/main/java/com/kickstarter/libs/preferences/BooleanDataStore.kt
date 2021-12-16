package com.kickstarter.libs.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.jvm.JvmOverloads

class BooleanDataStore @JvmOverloads constructor(
    private val dataStore: DataStore<Preferences>,
    private val key: String,
    private val defaultValue: Boolean = false
) : BooleanDataStoreType {

    override fun get(): Boolean {
        val flow: Flow<Boolean> = dataStore.data.map {
            it[booleanPreferencesKey(key)] ?: defaultValue
        }
        return unwrapFlowValue(flow) as Boolean
    }

    // - Blocking current thread here, we should return FLOW to be consumed by a coroutine,
    // for now as we slowly adopt Coroutines we do not want to change the nomenclature
    // for BooleanPreferenceType, reason why we "force" the value extraction.
    private fun unwrapFlowValue(flow: Flow<Any>): Any {
        var flowValue: Any
        runBlocking(Dispatchers.IO) {
            flowValue = flow.first()
        }

        return flowValue
    }

    override val isSet: Boolean
        get(): Boolean {

            val flow: Flow<Boolean> = dataStore.data.catch { exception ->
                // dataStore.data throws an IOException if it can't read the data
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { it.contains(booleanPreferencesKey(key)) }

            return unwrapFlowValue(flow) as Boolean
        }

    override suspend fun set(value: Boolean) {
        dataStore.edit {
            it[booleanPreferencesKey(key)] = !(it[booleanPreferencesKey(key)] ?: false)
        }
    }

    override suspend fun delete() {
        dataStore.edit {
            if (it.contains(booleanPreferencesKey(key)))
                it.remove(booleanPreferencesKey(key))
        }
    }
}
