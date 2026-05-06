package com.kickstarter.features.socialshare.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.socialshare.SocialShareService
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.features.socialshare.data.SocialShareUIState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.EmptyCoroutineContext

/**
 * ViewModel for the Social Share bottom sheet.
 *
 * @param shareService Abstraction over Android framework operations (clipboard, package
 *   detection, image caching, intent construction). In production this is
 *   [com.kickstarter.features.socialshare.AndroidSocialShareService]; in tests a fake
 *   implementation is injected directly.
 *
 * @param shareData Snapshot of the project information used across every sharing action:
 *   - [SocialShareData.projectName] — included as text in platform captions and SMS/email subjects.
 *   - [SocialShareData.projectUrl]  — the canonical link appended to every share payload.
 *   - [SocialShareData.imageUrl]    — remote URL of the project hero image; downloaded and
 *     cached as a `content://` URI during [init] so it can be attached to image-bearing
 *     intents (Instagram, Facebook, WhatsApp, etc.). An empty value skips caching.
 *   - [SocialShareData.creatorName] — displayed in email body copy.
 *   This object is immutable for the lifetime of the ViewModel; one ViewModel instance
 *   corresponds to one sharing session for a specific project.
 */
class SocialShareViewModel(
    private val shareService: SocialShareService,
    private val shareData: SocialShareData,
    private val testDispatcher: CoroutineDispatcher? = null
) : ViewModel() {

    private val scope = viewModelScope + (testDispatcher ?: EmptyCoroutineContext)

    private val _uiState = MutableStateFlow(SocialShareUIState())
    val uiState: StateFlow<SocialShareUIState> = _uiState.asStateFlow()

    private var errorAction: (message: String?) -> Unit = {}
    private var intentLaunchAction: (Intent) -> Unit = {}

    init {
        detectInstalledPlatforms()
        cacheShareImage()
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

        val intent = shareService.buildIntent(
            platform = platform,
            shareData = shareData,
            imageUri = _uiState.value.shareImageUri
        )

        if (intent == null) {
            errorAction.invoke("Could not open ${platform.name}")
            return
        }

        intentLaunchAction.invoke(intent)
    }

    fun onCopyLinkClicked() {
        shareService.copyToClipboard("Kickstarter project link", shareData.projectUrl)
        scope.launch {
            _uiState.emit(_uiState.value.copy(copiedToClipboard = true))
        }
    }

    fun onCopiedToastShown() {
        scope.launch {
            _uiState.emit(_uiState.value.copy(copiedToClipboard = false))
        }
    }

    private fun detectInstalledPlatforms() {
        scope.launch {
            val available = shareService.getInstalledPlatforms()
            _uiState.emit(_uiState.value.copy(availablePlatforms = available))
        }
    }

    private fun cacheShareImage() {
        if (shareData.imageUrl.isEmpty()) return

        scope.launch {
            _uiState.emit(_uiState.value.copy(isGeneratingImage = true))
            val uri = shareService.cacheImage(shareData.imageUrl)
            if (uri == null) {
                errorAction.invoke("Failed to cache share image")
            }
            _uiState.emit(_uiState.value.copy(shareImageUri = uri, isGeneratingImage = false))
        }
    }

    class Factory(
        private val service: SocialShareService,
        private val shareData: SocialShareData,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SocialShareViewModel(service, shareData, testDispatcher) as T
        }
    }
}
