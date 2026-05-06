package com.kickstarter.features.socialshare.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.socialshare.AndroidSocialShareService
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
 * All Android framework access (PackageManager, ClipboardManager, FileProvider, Intent
 * construction) is delegated to [SocialShareService]. The ViewModel itself holds no
 * [Context] reference, which prevents memory leaks and allows pure JUnit testing via a
 * fake [SocialShareService].
 */
class SocialShareViewModel(
    private val shareService: SocialShareService,
    val shareData: SocialShareData,
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
            errorAction.invoke("Please wait, preparing image...") // TODO: review in place just in case image generation takes time
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

    /**
     * Creates a [SocialShareViewModel] pre-wired with [AndroidSocialShareService].
     *
     * The factory accepts a [Context] so callers (Compose/Activity) can provide
     * [Context.getApplicationContext], which the service stores safely.
     */
    class Factory(
        private val context: Context,
        private val shareData: SocialShareData,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val service = AndroidSocialShareService(context.applicationContext)
            return SocialShareViewModel(service, shareData, testDispatcher) as T
        }
    }
}
