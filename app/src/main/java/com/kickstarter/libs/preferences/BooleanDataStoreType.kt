package com.kickstarter.libs.preferences

interface BooleanDataStoreType {
    /**
     * Get the current value of the preference.
     */
    fun get(): Boolean

    /**
     * Returns whether a value has been explicitly set for the preference.
     */
    val isSet: Boolean

    /**
     * Set the preference to a value.
     */
    suspend fun set(value: Boolean)

    /**
     * Delete the currently stored preference.
     */
    suspend fun delete()
}
