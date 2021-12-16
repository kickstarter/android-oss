package com.kickstarter.libs

import android.content.Context
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.preferences.BooleanDataStore
import org.junit.Test

class BooleanDataStoreTest : KSRobolectricTestCase() {

    lateinit var build: Build
    lateinit var context: Context

    override fun setUp() {
        super.setUp()
        build = environment().build()
        context = application()
    }

    @Test
    fun testSetGetDeleteTrueValue() {
        val key = "FirstKey"
        val boolDataStorePrefs = BooleanDataStore(context, key, false)

        // - test default value
        assertFalse(boolDataStorePrefs.get())

        // - test set value
        boolDataStorePrefs.set(true)
        assert(boolDataStorePrefs.isSet)
        assert(boolDataStorePrefs.get())

        // - test delete
        boolDataStorePrefs.delete()
        assertFalse(boolDataStorePrefs.isSet)
    }

    @Test
    fun testSetGetDeleteFalseValue() {
        val key = "SecondKey"
        val boolDataStorePrefs = BooleanDataStore(context, key, true)

        // - test default value
        assert(boolDataStorePrefs.get())

        // - test set value
        boolDataStorePrefs.set(false)
        assert(boolDataStorePrefs.isSet)
        assertFalse(boolDataStorePrefs.get())

        // - test delete
        boolDataStorePrefs.delete()
        assertFalse(boolDataStorePrefs.isSet)
    }
}
