package com.kickstarter.features.socialshare.viewmodel

import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.socialshare.SocialShareService
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.features.socialshare.data.SocialShareUIState
import com.kickstarter.features.socialshare.data.refTag
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.EventContextValues.ContextPageName
import com.kickstarter.libs.utils.UrlUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.EmptyCoroutineContext

/**
 * ViewModel for the Social Share bottom sheet.
 *
 * @param environment Provides analytics and other app-level dependencies.
 * @param shareService Abstraction over Android framework operations (clipboard, package
 *   detection, image caching, intent construction). In production this is
 *   [com.kickstarter.features.socialshare.AndroidSocialShareService]; in tests a fake
 *   implementation is injected directly.
 * @param shareData Snapshot of the project information used across every sharing action:
 *   - [SocialShareData.projectName] — included as text in platform captions and SMS/email subjects.
 *   - [SocialShareData.projectUrl]  — the canonical link appended to every share payload.
 *   - [SocialShareData.imageUrl]    — remote URL of the project hero image; downloaded and
 *     cached as a `content://` URI during [init] so it can be attached to image-bearing
 *     intents (Instagram, Facebook, WhatsApp, etc.). An empty value skips caching.
 *   - [SocialShareData.creatorName] — displayed in email body copy.
 *   This object is immutable for the lifetime of the ViewModel; one ViewModel instance
 *   corresponds to one sharing session for a specific project.
 * @param contextPage The screen from which the share sheet was opened, used as the
 *   analytics context_page property.
 */
class SocialShareViewModel(
    private val environment: Environment,
    private val shareService: SocialShareService,
    private val shareData: SocialShareData,
    private val contextPage: ContextPageName,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)
    private val analyticEvents = requireNotNull(environment.analytics())

    private val _uiState = MutableStateFlow(SocialShareUIState())
    val uiState: StateFlow<SocialShareUIState> = _uiState.asStateFlow()

    private var errorAction: (message: String?) -> Unit = {}
    private var intentLaunchAction: (Intent) -> Unit = {}

    init {
        detectInstalledPlatforms()
        loadHeroImage()
    }

    fun provideErrorAction(action: (message: String?) -> Unit) {
        errorAction = action
    }

    fun provideIntentLaunchAction(action: (Intent) -> Unit) {
        intentLaunchAction = action
    }

    fun onPlatformSelected(platform: SocialSharePlatform) {
        if (_uiState.value.isGeneratingImage && platform.requiresImage()) {
            // TODO: review in place just in case for now
            // more work related to the image to share on next ticket DISC-208
            errorAction.invoke("Please wait, preparing image...")
            return
        }

        val urlWithRefTag = UrlUtils.appendRefTag(shareData.projectUrl, platform.refTag().tag())
        val intent = shareService.buildIntent(
            platform = platform,
            shareData = shareData.copy(projectUrl = urlWithRefTag),
            imageUri = _uiState.value.shareImageUri
        )

        if (intent == null) {
            errorAction.invoke("Could not open ${platform.name}")
            return
        }

        analyticEvents.trackSharePlatformCTAClicked(platform, contextPage)
        intentLaunchAction.invoke(intent)
    }

    fun onCopyLinkClicked() {
        val urlWithRefTag = UrlUtils.appendRefTag(shareData.projectUrl, SocialSharePlatform.COPY_LINK.refTag().tag())
        shareService.copyToClipboard("Kickstarter project link", urlWithRefTag)
        analyticEvents.trackSharePlatformCTAClicked(SocialSharePlatform.COPY_LINK, contextPage)
        _uiState.update { it.copy(copiedToClipboard = true) }
    }

    fun onCopiedToastShown() {
        _uiState.update { it.copy(copiedToClipboard = false) }
    }

    private fun detectInstalledPlatforms() {
        scope.launch {
            val available = shareService.getInstalledPlatforms()
            _uiState.update { it.copy(availablePlatforms = available) }
        }
    }

    /**
     * Retrieve step. Downloads the hero image and exposes it via [SocialShareUIState.heroBitmap] so
     * the share card can render it. [SocialShareUIState.isGeneratingImage] stays true after this
     * completes — it is only cleared once the rendered card has been captured and cached in
     * [onCardCaptured], since that captured card is the asset image-bearing platforms actually share.
     */
    private fun loadHeroImage() {
        if (shareData.imageUrl.isEmpty()) return

        scope.launch {
            _uiState.update { it.copy(isGeneratingImage = true) }
            val bitmap = shareService.loadShareImage(shareData.imageUrl)
            if (bitmap == null) {
                errorAction.invoke("Failed to load share image")
                _uiState.update { it.copy(isGeneratingImage = false) }
                return@launch
            }
            _uiState.update { it.copy(heroBitmap = bitmap) }
        }
    }

    /**
     * Capture + persist step, driven by the UI once the share card has been rasterized from a
     * [androidx.compose.ui.graphics.layer.GraphicsLayer]. Writes the captured card to the cache as a
     * PNG and publishes its `content://` URI as [SocialShareUIState.shareImageUri]. No-ops if a card
     * has already been captured for this session.
     */
    fun onCardCaptured(bitmap: Bitmap) {
        if (_uiState.value.shareImageUri != null) return

        scope.launch {
            val uri = shareService.cacheShareImage(bitmap)
            if (uri == null) {
                errorAction.invoke("Failed to cache share image")
            }
            _uiState.update { it.copy(shareImageUri = uri, isGeneratingImage = false) }
        }
    }

    class Factory(
        private val environment: Environment,
        private val service: SocialShareService,
        private val shareData: SocialShareData,
        private val contextPage: ContextPageName,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SocialShareViewModel(environment, service, shareData, contextPage, testDispatcher) as T
        }
    }
}
