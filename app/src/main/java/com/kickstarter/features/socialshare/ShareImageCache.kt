package com.kickstarter.features.socialshare

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Turns bitmaps into shareable `content://` URIs for the social share flow, exposing the two parts
 * of this flow independently so the UI can slot the compose-card capture in between:
 *
 * **[loadBitmap] (retrieve)** — Coil fetches the remote hero image into a [android.graphics.Bitmap].
 *    [allowHardware(false)][coil.request.ImageRequest.Builder.allowHardware] is set so the
 *    bitmap lives in software memory; without it Coil may return a hardware-backed bitmap
 *    that cannot be compressed or written to disk. This bitmap is fed into the share card composable so it
 *    renders deterministically before the card is captured.
 *
 * **[cacheBitmap] (persist)** — share card composable is captured to a bitmap, that is written as a PNG into
 *    [Context.getCacheDir] then [FileProvider.getUriForFile] converts the local path into a `content://` URI
 *    scoped to the authority declared in AndroidManifest.xml. Passing
 *    [Intent.FLAG_GRANT_READ_URI_PERMISSION][android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION]
 *    on the outgoing intent then gives the target app (Instagram, Facebook, etc.) temporary
 *    read access to that single file.
 */
object ShareImageCache {

    private const val SHARE_DIR = "share_images"
    private const val SHARE_FILE_NAME = "kickstarter_share.png"

    /**
     * Retrieve step: downloads the remote hero image so the share card composable can render it.
     *
     * Forces `allowHardware(false)` to ensure the resulting bitmap is stored in
     * software memory. This is critical because hardware-backed bitmaps cannot
     * be compressed and written to disk via [android.graphics.Bitmap.compress] — which is
     * exactly what the later [cacheBitmap] step needs to do to the captured card.
     *
     * @param context Application context for the ImageRequest.
     * @param imageUrl Remote URL to download.
     * @return A software-backed [android.graphics.Bitmap] or null if the download fails.
     */
    suspend fun loadBitmap(context: Context, imageUrl: String): android.graphics.Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            // - Hardware-backed bitmaps live on the GPU, setting allowHardware(false) forces a software copy so we can compress it.
            .allowHardware(false)
            .build()
        val result = Coil.imageLoader(context).execute(request)
        return (result as? SuccessResult)?.drawable?.toBitmap()
    }

    /**
     * Persist step: writes a rendered bitmap (the captured [SocialShareProjectCard]) to the app's
     * internal cache directory and returns a shareable `content://` URI for it.
     *
     * Saves the image as a lossless **PNG** into a dedicated 'share_images' subfolder: the card is
     * text/logo-heavy and JPEG compression would introduce visible artifacts around the type and
     * branding. This must match the MIME type declared in [SocialShareIntentBuilder]. The compress +
     * disk write run on [Dispatchers.IO] because the captured card can be large and this is called
     * from the ViewModel's main-scoped coroutine.
     *
     * @param context Context to access [Context.getCacheDir].
     * @param bitmap The software-backed bitmap to write (typically the captured share card, already
     *   flattened onto an opaque background).
     * @return A content:// Uri for the file, or null if the write operation fails.
     */
    suspend fun cacheBitmap(context: Context, bitmap: android.graphics.Bitmap): Uri? =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
                val file = File(dir, SHARE_FILE_NAME)
                FileOutputStream(file).use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
                toFileProviderUri(context, file)
            } catch (e: Exception) {
                Timber.e(e, "Failed to write share image to cache")
                null
            }
        }

    /**
     * Converts a local [File] into a shareable content [Uri].
     *
     * Uses [FileProvider] to create a URI that other apps can access. The authority
     * used here ("${context.packageName}.fileprovider") MUST match the authority
     * declared in the AndroidManifest.xml.
     *
     * @param context Context used to generate the URI authority.
     * @param file The local file to be shared.
     * @return A shareable content:// Uri.
     */
    private fun toFileProviderUri(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
