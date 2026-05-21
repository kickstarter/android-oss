package com.kickstarter.features.socialshare.viewmodel

import android.content.Intent
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
 * @param shareData Snapshot of the project information used across every sharing action.
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
