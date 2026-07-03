package com.kickstarter.features.socialshare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform

/**
 * Abstracts all Android framework operations needed by [SocialShareViewModel].
 *
 * This ensures the ViewModel does not hold a reference to any Activity or
 * UI-scoped context by interacting only with this interface.
 */
interface SocialShareService {

    /**
     * Returns the subset of [SocialSharePlatform] entries whose target app is currently
     * installed on the device. Platforms with no [SocialSharePlatform.targetPackage]
     * (e.g. COPY_LINK, EMAIL, MESSAGES) are always included.
     */
    fun getInstalledPlatforms(): List<SocialSharePlatform>

    /**
     * Places [url] on the system clipboard under [label].
     */
    fun copyToClipboard(label: String, url: String)

    /**
     * Retrieve step: downloads the hero image at [imageUrl] into a software [Bitmap] so the share
     * card can render it deterministically before being captured. Returns null if the download fails.
     */
    suspend fun loadShareImage(imageUrl: String): Bitmap?

    /**
     * Persist step: writes [bitmap] (the captured share card) to the app cache as a PNG and returns a
     * `content://` [Uri] that can be granted to other apps via [Intent] flags. Returns null on failure.
     */
    suspend fun cacheShareImage(bitmap: Bitmap): Uri?

    /**
     * Constructs a ready-to-fire [Intent] for [platform], or null when the platform
     * does not use an intent (e.g. [SocialSharePlatform.COPY_LINK]).
     */
    fun buildIntent(
        platform: SocialSharePlatform,
        shareData: SocialShareData,
        imageUri: Uri?
    ): Intent?
}

/**
 * Production implementation of [SocialShareService] that delegates to real Android APIs.
 *
 * **Important:** always pass [Context.getApplicationContext] — never an Activity context.
 * Application context is safe to hold indefinitely and cannot cause a memory leak.
 */
class AndroidSocialShareService(
    private val context: Context
) : SocialShareService {

    override fun getInstalledPlatforms(): List<SocialSharePlatform> {
        val pm = context.packageManager
        return SocialSharePlatform.entries.filter { platform ->
            platform.targetPackage == null || isPackageInstalled(platform.targetPackage, pm)
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

    override fun copyToClipboard(label: String, url: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, url))
    }

    override suspend fun loadShareImage(imageUrl: String): Bitmap? =
        ShareImageCache.loadBitmap(context, imageUrl)

    override suspend fun cacheShareImage(bitmap: Bitmap): Uri? =
        ShareImageCache.cacheBitmap(context, bitmap)

    override fun buildIntent(
        platform: SocialSharePlatform,
        shareData: SocialShareData,
        imageUri: Uri?
    ): Intent? = SocialShareIntentBuilder.buildIntent(context, platform, shareData, imageUri)
}
