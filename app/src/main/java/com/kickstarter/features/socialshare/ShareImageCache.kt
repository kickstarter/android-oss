package com.kickstarter.features.socialshare

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Downloads a remote image and makes it available
 * to other apps via a [FileProvider] URI.
 *
 * Three steps are required because Android prevents apps from sharing raw file paths
 * directly with other apps since API 24:
 *
 * **Download** — Coil fetches the remote image into a [android.graphics.Bitmap].
 *    [allowHardware(false)][coil.request.ImageRequest.Builder.allowHardware] is set so the
 *    bitmap lives in software memory; without it Coil may return a hardware-backed bitmap
 *    that cannot be compressed or written to disk.
 *
 * **Cache** — The bitmap is written as a JPEG into [Context.getCacheDir]. No storage
 *    permission is required for the app's own cache directory on any Android version.
 *
 * **FileProvider URI** — [FileProvider.getUriForFile] converts the local path into a
 *    `content://` URI scoped to the authority declared in AndroidManifest.xml.
 *    Passing [Intent.FLAG_GRANT_READ_URI_PERMISSION][android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION]
 *    on the outgoing intent then gives the target app (Instagram, Facebook, etc.) temporary
 *    read access to that single file — no broader storage access is granted.
 */
object ShareImageCache {

    private const val SHARE_DIR = "share_images"
    private const val SHARE_FILE_NAME = "kickstarter_share.jpg"

    /**
     * High-level entry point to cache a remote image for sharing.
     *
     * Performs the download, disk write, and URI conversion in sequence.
     * Returns null if any step fails.
     *
     * @param context The calling context (usually an Activity or Application).
     * @param imageUrl The remote URL of the image to be shared.
     * @return A content:// Uri pointing to the local copy, or null on failure.
     */
    suspend fun cache(context: Context, imageUrl: String): Uri? {
        return try {
            val bitmap = download(context, imageUrl) ?: return null
            writeAndGetUri(context, bitmap)
        } catch (e: Exception) {
            Timber.e(e, "ShareImageCache failed for url=$imageUrl")
            null
        }
    }

    /**
     * Step 1: Downloads the image using Coil.
     *
     * Forces `allowHardware(false)` to ensure the resulting bitmap is stored in
     * software memory. This is critical because hardware-backed bitmaps cannot
     * be compressed and written to disk via [android.graphics.Bitmap.compress].
     *
     * @param context Application context for the ImageRequest.
     * @param imageUrl Remote URL to download.
     * @return A software-backed [android.graphics.Bitmap] or null if the download fails.
     */
    private suspend fun download(context: Context, imageUrl: String): android.graphics.Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            // - Hardware-backed bitmaps live on the GPU, setting allowHardware(false) forces a software copy so we can compress it.
            .allowHardware(false)
            .build()
        val result = Coil.imageLoader(context).execute(request)
        return (result as? SuccessResult)?.drawable?.toBitmap()
    }

    /**
     * Step 2: Writes the bitmap to the app's internal cache directory.
     *
     * Saves the image as a JPEG into a dedicated 'share_images' subfolder.
     * This location is private to the app, necessitating Step 3 for sharing.
     *
     * @param context Context to access [Context.getCacheDir].
     * @param bitmap The software-backed bitmap to write.
     * @return A content:// Uri for the file, or null if the write operation fails.
     */
    private fun writeAndGetUri(context: Context, bitmap: android.graphics.Bitmap): Uri? {
        return try {
            val dir = File(context.cacheDir, SHARE_DIR).apply { mkdirs() }
            val file = File(dir, SHARE_FILE_NAME)
            FileOutputStream(file).use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, it) }
            // Step 3: Wrap in a FileProvider URI
            toFileProviderUri(context, file)
        } catch (e: Exception) {
            Timber.e(e, "Failed to write share image to cache")
            null
        }
    }

    /**
     * Step 3: Converts a local [File] into a shareable content [Uri].
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
