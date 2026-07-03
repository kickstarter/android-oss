package com.kickstarter.features.socialshare.viewmodel

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.features.socialshare.SocialShareService
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.libs.utils.EventName
import com.kickstarter.libs.utils.UrlUtils
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
        Uri.parse("content://com.kickstarter.fileprovider/share_images/kickstarter_share.png")

    private fun fakeBitmap(): Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

    private fun fakePlatforms() = listOf(
        SocialSharePlatform.X,
        SocialSharePlatform.WHATSAPP,
        SocialSharePlatform.EMAIL,
        SocialSharePlatform.MORE
    )

    private fun buildViewModel(
        service: SocialShareService,
        data: SocialShareData = shareData,
        contextPage: ContextPageName = ContextPageName.VIDEO_FEED,
        dispatcher: kotlinx.coroutines.CoroutineDispatcher
    ) = SocialShareViewModel(environment(), service, data, contextPage, dispatcher)

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
    // init: loadHeroImage (retrieve) + onCardCaptured (persist)
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Test
    fun `init loads hero bitmap and keeps generating until the card is captured`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val heroBitmap = fakeBitmap()

        val service = object : FakeSocialShareService() {
            override suspend fun loadShareImage(imageUrl: String): Bitmap = heroBitmap
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        assertEquals(heroBitmap, viewModel.uiState.value.heroBitmap)
        // shareImageUri is produced from the captured card, not the raw hero, so it stays null...
        assertNull(viewModel.uiState.value.shareImageUri)
        // ...and image-requiring platforms remain gated until the card has been captured.
        assertTrue(viewModel.uiState.value.isGeneratingImage)
    }

    @Test
    fun `init calls errorAction when hero image fails to load`() = runTest {
        // StandardTestDispatcher is required: with UnconfinedTestDispatcher the init
        // coroutines execute synchronously inside the constructor, before provideErrorAction
        // can be registered. StandardTestDispatcher queues them so we can wire callbacks first.
        val dispatcher = StandardTestDispatcher(testScheduler)

        val service = object : FakeSocialShareService() {
            override suspend fun loadShareImage(imageUrl: String): Bitmap? = null
        }

        var errorMessage: String? = null
        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        viewModel.provideErrorAction { errorMessage = it }
        advanceUntilIdle()

        assertNotNull(errorMessage)
        assertNull(viewModel.uiState.value.heroBitmap)
        assertNull(viewModel.uiState.value.shareImageUri)
        assertFalse(viewModel.uiState.value.isGeneratingImage)
    }

    @Test
    fun `init skips loading the hero image when imageUrl is empty`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var loadWasCalled = false

        val service = object : FakeSocialShareService() {
            override suspend fun loadShareImage(imageUrl: String): Bitmap {
                loadWasCalled = true
                return fakeBitmap()
            }
        }

        val viewModel = buildViewModel(service, data = shareData.copy(imageUrl = ""), dispatcher = dispatcher)
        advanceUntilIdle()

        assertFalse(loadWasCalled)
        assertNull(viewModel.uiState.value.heroBitmap)
        assertNull(viewModel.uiState.value.shareImageUri)
        assertFalse(viewModel.uiState.value.isGeneratingImage)
    }

    @Test
    fun `onCardCaptured caches the captured card and stops generating`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val service = object : FakeSocialShareService() {
            override suspend fun cacheShareImage(bitmap: Bitmap): Uri = fakeImageUri
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        viewModel.onCardCaptured(fakeBitmap())
        advanceUntilIdle()

        assertEquals(fakeImageUri, viewModel.uiState.value.shareImageUri)
        assertFalse(viewModel.uiState.value.isGeneratingImage)
    }

    @Test
    fun `onCardCaptured calls errorAction when caching fails`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val service = object : FakeSocialShareService() {
            override suspend fun cacheShareImage(bitmap: Bitmap): Uri? = null
        }

        var errorMessage: String? = null
        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        viewModel.provideErrorAction { errorMessage = it }
        advanceUntilIdle()

        viewModel.onCardCaptured(fakeBitmap())
        advanceUntilIdle()

        assertNotNull(errorMessage)
        assertNull(viewModel.uiState.value.shareImageUri)
        assertFalse(viewModel.uiState.value.isGeneratingImage)
    }

    @Test
    fun `onCardCaptured only caches once per session`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var cacheCount = 0

        val service = object : FakeSocialShareService() {
            override suspend fun cacheShareImage(bitmap: Bitmap): Uri {
                cacheCount++
                return fakeImageUri
            }
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        viewModel.onCardCaptured(fakeBitmap())
        viewModel.onCardCaptured(fakeBitmap())
        advanceUntilIdle()

        assertEquals(1, cacheCount)
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

        // loadShareImage never completes → isGeneratingImage stays true
        val service = object : FakeSocialShareService() {
            override suspend fun loadShareImage(imageUrl: String): Bitmap? {
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

        // loadShareImage never completes → isGeneratingImage stays true
        val service = object : FakeSocialShareService() {
            override suspend fun loadShareImage(imageUrl: String): Bitmap? {
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

    @Test
    fun `onPlatformSelected appends platform reftag to URL before building intent`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        var capturedShareData: SocialShareData? = null

        val service = object : FakeSocialShareService() {
            override fun buildIntent(
                platform: SocialSharePlatform,
                shareData: SocialShareData,
                imageUri: Uri?
            ): Intent {
                capturedShareData = shareData
                return Intent(Intent.ACTION_SEND)
            }
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        viewModel.onPlatformSelected(SocialSharePlatform.X)

        val expectedUrl = UrlUtils.appendRefTag(shareData.projectUrl, RefTag.projectShareX().tag())
        assertEquals(expectedUrl, capturedShareData?.projectUrl)
    }

    @Test
    fun `onPlatformSelected fires CTA_CLICKED analytics event`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val service = object : FakeSocialShareService() {
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        viewModel.onPlatformSelected(SocialSharePlatform.WHATSAPP)

        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
    }

    @Test
    fun `onPlatformSelected does not fire analytics when intent is null`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val service = object : FakeSocialShareService() {
            override fun buildIntent(
                platform: SocialSharePlatform,
                shareData: SocialShareData,
                imageUri: Uri?
            ): Intent? = null
        }

        val viewModel = buildViewModel(service, dispatcher = dispatcher)
        advanceUntilIdle()

        viewModel.onPlatformSelected(SocialSharePlatform.WHATSAPP)

        segmentTrack.assertNoValues()
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // onCopyLinkClicked / onCopiedToastShown
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    @Test
    fun `onCopyLinkClicked copies URL with copy link reftag appended`() = runTest {
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

        val expectedUrl = UrlUtils.appendRefTag(shareData.projectUrl, RefTag.projectShareCopyLink().tag())
        assertNotNull(copiedLabel)
        assertEquals(expectedUrl, copiedUrl)
        assertTrue(viewModel.uiState.value.copiedToClipboard)
    }

    @Test
    fun `onCopyLinkClicked fires CTA_CLICKED analytics event`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)

        val viewModel = buildViewModel(FakeSocialShareService(), dispatcher = dispatcher)
        advanceUntilIdle()

        viewModel.onCopyLinkClicked()
        advanceUntilIdle()

        segmentTrack.assertValue(EventName.CTA_CLICKED.eventName)
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
        override suspend fun loadShareImage(imageUrl: String): Bitmap? = fakeBitmap()
        override suspend fun cacheShareImage(bitmap: Bitmap): Uri? = fakeImageUri
        override fun buildIntent(
            platform: SocialSharePlatform,
            shareData: SocialShareData,
            imageUri: Uri?
        ): Intent? = Intent(Intent.ACTION_SEND)
    }
}
