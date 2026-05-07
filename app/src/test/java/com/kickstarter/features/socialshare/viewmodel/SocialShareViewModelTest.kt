package com.kickstarter.features.socialshare.viewmodel

import android.content.Intent
import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.socialshare.SocialShareService
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SocialShareViewModelTest : KSRobolectricTestCase() {

    private val shareData = SocialShareData(
        projectName = "Ringo Move - The Ultimate Workout Bottle",
        projectUrl = "https://www.kickstarter.com/projects/ringo/ringo-move",
        imageUrl = "https://example.com/image.jpg",
        creatorName = "Ringo"
    )

    private val fakeImageUri: Uri =
        Uri.parse("content://com.kickstarter.fileprovider/share_images/kickstarter_share.jpg")

    private fun fakePlatforms() = listOf(
        SocialSharePlatform.X,
        SocialSharePlatform.WHATSAPP,
        SocialSharePlatform.EMAIL,
        SocialSharePlatform.MORE
    )

    private fun buildViewModel(
        service: SocialShareService,
        data: SocialShareData = shareData,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher
    ) = SocialShareViewModel(service, data, dispatcher)

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // init: detectInstalledPlatforms
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Test
    fun `init emits available platforms from service`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val platforms = fakePlatforms()

        val service = object : FakeSocialShareService() {
            override fun getInstalledPlatforms() = platforms
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        assertEquals(platforms, viewModel.uiState.value.availablePlatforms)
    }

    @Test
    fun `init emits empty platform list when no apps are installed`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val service = object : FakeSocialShareService() {
            override fun getInstalledPlatforms() = emptyList<SocialSharePlatform>()
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.availablePlatforms.isEmpty())
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // init: cacheShareImage
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Test
    fun `init stores cached image URI in uiState`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val service = object : FakeSocialShareService() {
            override suspend fun cacheImage(imageUrl: String): Uri = fakeImageUri
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        assertEquals(fakeImageUri, viewModel.uiState.value.shareImageUri)
        assertFalse(viewModel.uiState.value.isGeneratingImage)
    }

    @Test
    fun `init calls errorAction when cacheImage returns null`() = runTest {
        // StandardTestDispatcher is required: with UnconfinedTestDispatcher the init
        // coroutines execute synchronously inside the constructor, before provideErrorAction
        // can be registered. StandardTestDispatcher queues them so we can wire callbacks first.
        val dispatcher = StandardTestDispatcher(testScheduler)

        val service = object : FakeSocialShareService() {
            override suspend fun cacheImage(imageUrl: String): Uri? = null
        }

        var errorMessage: String? = null
        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        viewModel.provideErrorAction { errorMessage = it }
        advanceUntilIdle()

        assertNotNull(errorMessage)
        assertNull(viewModel.uiState.value.shareImageUri)
        assertFalse(viewModel.uiState.value.isGeneratingImage)
    }

    @Test
    fun `init skips cacheShareImage when imageUrl is empty`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var cacheWasCalled = false

        val service = object : FakeSocialShareService() {
            override suspend fun cacheImage(imageUrl: String): Uri? {
                cacheWasCalled = true
                return fakeImageUri
            }
        }

        val viewModel = buildViewModel(service, data = shareData.copy(imageUrl = ""), dispatcher = dispatcher)
        advanceUntilIdle()

        assertFalse(cacheWasCalled)
        assertNull(viewModel.uiState.value.shareImageUri)
        assertFalse(viewModel.uiState.value.isGeneratingImage)
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // onPlatformSelected
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Test
    fun `onPlatformSelected calls intentLaunchAction when intent is available`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeIntent = Intent(Intent.ACTION_SEND)
        var capturedIntent: Intent? = null

        val service = object : FakeSocialShareService() {
            override suspend fun cacheImage(imageUrl: String): Uri = fakeImageUri
            override fun buildIntent(
                platform: SocialSharePlatform,
                shareData: SocialShareData,
                imageUri: Uri?
            ): Intent = fakeIntent
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        viewModel.provideIntentLaunchAction { capturedIntent = it }
        advanceUntilIdle()

        viewModel.onPlatformSelected(SocialSharePlatform.X)

        assertEquals(fakeIntent, capturedIntent)
    }

    @Test
    fun `onPlatformSelected calls errorAction when intent is null`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val service = object : FakeSocialShareService() {
            override suspend fun cacheImage(imageUrl: String): Uri = fakeImageUri
            override fun buildIntent(
                platform: SocialSharePlatform,
                shareData: SocialShareData,
                imageUri: Uri?
            ): Intent? = null
        }

        var errorMessage: String? = null
        var launchedIntent: Intent? = null
        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        viewModel.provideErrorAction { errorMessage = it }
        viewModel.provideIntentLaunchAction { launchedIntent = it }
        advanceUntilIdle()

        viewModel.onPlatformSelected(SocialSharePlatform.X)

        assertNotNull(errorMessage)
        assertNull(launchedIntent)
    }

    @Test
    fun `onPlatformSelected calls errorAction when image is still generating and platform requires image`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        // cacheImage never completes → isGeneratingImage stays true
        val service = object : FakeSocialShareService() {
            override suspend fun cacheImage(imageUrl: String): Uri? {
                kotlinx.coroutines.awaitCancellation()
            }
        }

        var errorMessage: String? = null
        var launchedIntent: Intent? = null
        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        viewModel.provideErrorAction { errorMessage = it }
        viewModel.provideIntentLaunchAction { launchedIntent = it }

        // With UnconfinedTestDispatcher the init coroutine runs until it suspends at
        // awaitCancellation, so isGeneratingImage is already true at this point.
        viewModel.onPlatformSelected(SocialSharePlatform.INSTAGRAM_FEED)

        assertNotNull(errorMessage)
        assertNull(launchedIntent)
    }

    @Test
    fun `onPlatformSelected does not block on image generation for platforms that do not require image`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeIntent = Intent(Intent.ACTION_SEND)

        // cacheImage never completes → isGeneratingImage stays true
        val service = object : FakeSocialShareService() {
            override suspend fun cacheImage(imageUrl: String): Uri? {
                kotlinx.coroutines.awaitCancellation()
            }

            override fun buildIntent(
                platform: SocialSharePlatform,
                shareData: SocialShareData,
                imageUri: Uri?
            ): Intent = fakeIntent
        }

        var capturedIntent: Intent? = null
        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        viewModel.provideIntentLaunchAction { capturedIntent = it }

        // X does not requiresImage() so the guard should not fire
        viewModel.onPlatformSelected(SocialSharePlatform.X)

        assertEquals(fakeIntent, capturedIntent)
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // onCopyLinkClicked / onCopiedToastShown
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Test
    fun `onCopyLinkClicked delegates to service and sets copiedToClipboard true`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var copiedLabel: String? = null
        var copiedUrl: String? = null

        val service = object : FakeSocialShareService() {
            override fun copyToClipboard(label: String, url: String) {
                copiedLabel = label
                copiedUrl = url
            }
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        viewModel.onCopyLinkClicked()
        advanceUntilIdle()

        assertNotNull(copiedLabel)
        assertEquals(shareData.projectUrl, copiedUrl)
        assertTrue(viewModel.uiState.value.copiedToClipboard)
    }

    @Test
    fun `onCopiedToastShown resets copiedToClipboard to false`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val viewModel = buildViewModel(FakeSocialShareService(), dispatcher = dispatcher)
        advanceUntilIdle()

        viewModel.onCopyLinkClicked()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.copiedToClipboard)

        viewModel.onCopiedToastShown()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.copiedToClipboard)
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // Helpers
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /**
     * Default no-op fake; override individual methods in each test.
     */
    private open inner class FakeSocialShareService : SocialShareService {
        override fun getInstalledPlatforms(): List<SocialSharePlatform> = fakePlatforms()
        override fun copyToClipboard(label: String, url: String) {}
        override suspend fun cacheImage(imageUrl: String): Uri? = null
        override fun buildIntent(
            platform: SocialSharePlatform,
            shareData: SocialShareData,
            imageUri: Uri?
        ): Intent? = Intent(Intent.ACTION_SEND)
    }
}
