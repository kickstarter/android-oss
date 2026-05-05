package com.kickstarter.features.socialshare.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kickstarter.features.socialshare.ShareImageCache
import com.kickstarter.features.socialshare.SocialShareIntentBuilder
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.features.socialshare.data.SocialShareUIState
import com.kickstarter.libs.Environment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.EmptyCoroutineContext

class SocialShareViewModel(
    private val environment: Environment,
    private val context: Context,
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
            errorAction.invoke("Please wait, preparing image...")
            return
        }

        val intent = SocialShareIntentBuilder.buildIntent(
            context = context,
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
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Kickstarter project link", shareData.projectUrl))
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
            val pm = context.packageManager
            val available = SocialSharePlatform.entries.filter { platform ->
                platform.targetPackage == null || isPackageInstalled(platform.targetPackage, pm)
            }
            _uiState.emit(_uiState.value.copy(availablePlatforms = available))
        }
    }

    private fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun cacheShareImage() {
        if (shareData.imageUrl.isEmpty()) return

        scope.launch {
            _uiState.emit(_uiState.value.copy(isGeneratingImage = true))
            val uri = ShareImageCache.cache(context, shareData.imageUrl)
            if (uri == null) {
                errorAction.invoke("Failed to cache share image")
            }
            _uiState.emit(_uiState.value.copy(shareImageUri = uri, isGeneratingImage = false))
        }
    }

    class Factory(
        private val environment: Environment,
        private val context: Context,
        private val shareData: SocialShareData,
        private val testDispatcher: CoroutineDispatcher? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SocialShareViewModel(environment, context, shareData, testDispatcher) as T
        }
    }
}
