package com.kickstarter.libs

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.featureflag.FeatureFlagClient
import com.kickstarter.libs.featureflag.FeatureFlagClient.Companion.INTERNAL_INTERVAL
import com.kickstarter.libs.featureflag.FeatureFlagClient.Companion.RELEASE_INTERVAL
import com.kickstarter.libs.featureflag.FlagKey
import com.kickstarter.libs.featureflag.getFetchInterval
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Test

class FeatureFlagClientTest : KSRobolectricTestCase() {

    @Test
    fun testGetIntervalRelease() {
        val mockFirebase = mockk<FirebaseRemoteConfig>(relaxed = true)
        mockkStatic(Build::class)
        val mockBuildObject = mockk<Build>()
        every { Build.isInternal() } returns false
        every { Build.isExternal() } returns true
        every { mockBuildObject.isDebug } returns false

        val ffClient = FeatureFlagClient(mockBuildObject)
        ffClient.initialize(mockFirebase)

        assertEquals(ffClient.getFetchInterval(), RELEASE_INTERVAL)
    }

    @Test
    fun testGetIntervalDebug() {
        val mockFirebase = mockk<FirebaseRemoteConfig>(relaxed = true)
        mockkStatic(Build::class)
        val mockBuildObject = mockk<Build>()
        every { Build.isInternal() } returns true
        every { Build.isExternal() } returns false
        every { mockBuildObject.isDebug } returns true

        val ffClient = FeatureFlagClient(mockBuildObject)
        ffClient.initialize(mockFirebase)
        assertEquals(ffClient.getFetchInterval(), INTERNAL_INTERVAL)
    }

    @Test
    fun testGetBoolean() {
        val mockFirebase = mockk<FirebaseRemoteConfig>(relaxed = true)
        val mockBuild = mockk<Build>()
        every { mockFirebase.getBoolean(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG.key) } returns true
        every { mockBuild.isDebug } returns true

        val ffClient = FeatureFlagClient(mockBuild)
        ffClient.initialize(mockFirebase)
        assertEquals(ffClient.getBoolean(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG), true)

        val ffClient2 = FeatureFlagClient(mockBuild)
        ffClient2.initialize(null)
        assertEquals(ffClient2.getBoolean(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG), false)
    }

    @Test
    fun testGetLong() {
        val mockFirebase = mockk<FirebaseRemoteConfig>(relaxed = true)
        val mockBuild = mockk<Build>()
        every { mockFirebase.getLong(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG.key) } returns 9L
        every { mockBuild.isDebug } returns true

        val ffClient = FeatureFlagClient(mockBuild)
        ffClient.initialize(mockFirebase)
        assertEquals(ffClient.getLong(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG), 9L)

        val ffClient2 = FeatureFlagClient(mockBuild)
        ffClient2.initialize(null)
        assertEquals(ffClient2.getLong(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG), 0L)
    }

    @Test
    fun testGetDouble() {
        val mockFirebase = mockk<FirebaseRemoteConfig>(relaxed = true)
        val mockBuild = mockk<Build>()
        every { mockFirebase.getDouble(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG.key) } returns 100.0
        every { mockBuild.isDebug } returns true

        val ffClient = FeatureFlagClient(mockBuild)
        ffClient.initialize(mockFirebase)
        assertEquals(ffClient.getDouble(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG), 100.0)

        val ffClient2 = FeatureFlagClient(mockBuild)
        ffClient2.initialize(null)
        assertEquals(ffClient2.getDouble(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG), 0.0)
    }

    @Test
    fun testGetString() {
        val mockFirebase = mockk<FirebaseRemoteConfig>(relaxed = true)
        val mockBuild = mockk<Build>()
        every { mockFirebase.getString(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG.key) } returns "String"
        every { mockBuild.isDebug } returns true

        val ffClient = FeatureFlagClient(mockBuild)
        ffClient.initialize(mockFirebase)
        assertEquals(ffClient.getString(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG), "String")

        val ffClient2 = FeatureFlagClient(mockBuild)
        ffClient.initialize(null)
        assertEquals(ffClient2.getString(FlagKey.ANDROID_HIDE_APP_RATING_DIALOG), "")
    }
}
