package com.kickstarter.libs.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.jvm.JvmOverloads

class BooleanDataStore @JvmOverloads constructor(
    private val context: Context,
    private val key: String,
    private val defaultValue: Boolean = false
) : BooleanDataStoreType {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val prefKey = booleanPreferencesKey(this.key)

    override fun get(): Boolean {
        val flow: Flow<Boolean> = context.dataStore.data.map {
            it[prefKey] ?: defaultValue
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

            val flow: Flow<Boolean> = context.dataStore.data.catch { exception ->
                // dataStore.data throws an IOException if it can't read the data
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { it.contains(prefKey) }

            return unwrapFlowValue(flow) as Boolean
        }

    override suspend fun set(value: Boolean) {
        context.dataStore.edit {
            it[prefKey] = !(it[prefKey] ?: false)
        }
    }

    override suspend fun delete() {
        context.dataStore.edit {
            if (it.contains(prefKey))
                it.remove(prefKey)
        }
    }
}
