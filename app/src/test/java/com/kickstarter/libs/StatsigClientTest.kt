package com.kickstarter.libs

import com.kickstarter.KSApplication
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.featureflag.StatsigClient
import com.statsig.androidsdk.InitializationDetails
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsigClientTest : KSRobolectricTestCase() {

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Test
    fun testInitialize_success() = runTest(testDispatcher) {
        val mockCurrentUser: CurrentUserTypeV2 = requireNotNull(environment().currentUserV2())
        val mockBuildObject = mockk<Build>()

        val initDet = InitializationDetails(2, true)

        val stClient = object : StatsigClient(build = mockBuildObject, context = application(), currentUser = mockCurrentUser) {
            override suspend fun init(
                application: KSApplication,
                sdkKey: String
            ): InitializationDetails? {
                return initDet
            }

            override fun getSDKKey(): String {
                return ""
            }
        }

        var eCounter = 0
        stClient.initialize(this, errorCallback = { eCounter++ })

        assertEquals(stClient.scope, this)
        assertEquals(eCounter, 0)
    }

//    @Test
//    fun testInitialize_error_callback() = runTest(testDispatcher) {
//        val mockCurrentUser: CurrentUserTypeV2 = requireNotNull(environment().currentUserV2())
//        val mockBuildObject = mockk<Build>()
//
//        val initDet = InitializationDetails(
//            2,
//            false,
//            InitializeResponse.FailedInitializeResponse(InitializeFailReason.NetworkError, Exception("Something went wrong"))
//        )
//
//        val stClient = object : StatsigClient(build = mockBuildObject, context = application(), currentUser = mockCurrentUser) {
//            override suspend fun init(
//                application: KSApplication,
//                sdkKey: String
//            ): InitializationDetails? {
//                return initDet
//            }
//
//            override fun getSDKKey(): String {
//                return ""
//            }
//        }
//
//        var eCounter = 0
//        var exception: Exception? = null
//        stClient.initialize(this, errorCallback = { e ->
//            exception = e
//            eCounter++
//        })
//
//        advanceUntilIdle()
//        assertEquals(stClient.scope, this)
//        assertEquals(eCounter, 1)
//        assertEquals(exception?.message, "Something went wrong")
//    }
}
