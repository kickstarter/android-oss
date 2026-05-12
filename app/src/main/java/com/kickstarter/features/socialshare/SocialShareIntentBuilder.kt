package com.kickstarter.features.socialshare

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform

/**
 * Helper object responsible for constructing specific [Intent]s for various social media platforms.
 *
 * General rules applied across all platforms:
 * - Every intent includes the project URL as text wherever the platform accepts it.
 * - Every intent includes the cached image wherever the platform accepts it.
 * - [Intent.FLAG_GRANT_READ_URI_PERMISSION] + [ClipData] are set on every image intent
 *   (required on Android 12+ for URI permission grants to work reliably).
 *
 * Platform-specific limitations:
 * - Instagram Stories / Facebook Stories: the custom ADD_TO_STORY action only accepts an
 *   image; there is no text/URL slot in that API.
 * - Messages (SMS): [Intent.ACTION_SENDTO] + `smsto:` cannot carry attachments; MMS would
 *   require [Intent.ACTION_SEND] and losing the ability to pre-fill the SMS body reliably.
 *   Text-only is intentional here.
 */
object SocialShareIntentBuilder {

    fun buildIntent(
        context: Context,
        platform: SocialSharePlatform,
        shareData: SocialShareData,
        imageUri: Uri?
    ): Intent? = when (platform) {
        SocialSharePlatform.COPY_LINK -> null
        SocialSharePlatform.INSTAGRAM_FEED -> instagramFeedIntent(shareData, imageUri)
        SocialSharePlatform.INSTAGRAM_STORIES -> instagramStoriesIntent(context, imageUri)
        SocialSharePlatform.X -> xIntent(shareData, imageUri)
        SocialSharePlatform.FACEBOOK_FEED -> facebookFeedIntent(shareData, imageUri)
        SocialSharePlatform.FACEBOOK_STORIES -> facebookStoriesIntent(imageUri)
        SocialSharePlatform.WHATSAPP -> whatsAppIntent(shareData, imageUri)
        SocialSharePlatform.MESSAGES -> messagesIntent(shareData)
        SocialSharePlatform.EMAIL -> emailIntent(shareData, imageUri)
        SocialSharePlatform.MORE -> nativeChooserIntent(shareData, imageUri)
    }

    /**
     * Instagram Feed — image required, URL added via EXTRA_TEXT.
     * Note: Instagram's compose screen does not pre-fill captions from EXTRA_TEXT,
     * but including it is harmless and may be respected in future app versions.
     */
    private fun instagramFeedIntent(shareData: SocialShareData, imageUri: Uri?): Intent? {
        imageUri ?: return null
        val text = "Hey! I backed this project on Kickstarter: ${shareData.projectName} ${shareData.projectUrl}"
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, text)
            clipData = ClipData.newRawUri(null, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.instagram.android")
        }
    }

    /**
     * Instagram Stories — image-only; the ADD_TO_STORY action has no text/URL slot.
     */
    private fun instagramStoriesIntent(context: Context, imageUri: Uri?): Intent? {
        imageUri ?: return null
        return Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(imageUri, "image/jpeg")
            clipData = ClipData.newRawUri(null, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("source_application", context.packageName)
        }
    }

    /**
     * X (Twitter) — text always included; image attached when available.
     * X's app accepts ACTION_SEND with image/jpeg + EXTRA_TEXT simultaneously.
     */
    private fun xIntent(shareData: SocialShareData, imageUri: Uri?): Intent {
        val text = "I just backed ${shareData.projectName} on @Kickstarter! Check it out: ${shareData.projectUrl}"
        return if (imageUri != null) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_STREAM, imageUri)
                clipData = ClipData.newRawUri(null, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.twitter.android")
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage("com.twitter.android")
            }
        }
    }

    /**
     * Facebook Feed — image required, URL added via EXTRA_TEXT.
     * Note: Facebook's app typically ignores EXTRA_TEXT when an image is present,
     * but including it is harmless and consistent.
     */
    private fun facebookFeedIntent(shareData: SocialShareData, imageUri: Uri?): Intent? {
        imageUri ?: return null
        val text = "Hey! I backed this project on Kickstarter: ${shareData.projectName} ${shareData.projectUrl}"
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, text)
            clipData = ClipData.newRawUri(null, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.facebook.katana")
        }
    }

    /**
     * Facebook Stories — image-only; the ADD_TO_STORY action has no text/URL slot.
     */
    private fun facebookStoriesIntent(imageUri: Uri?): Intent? {
        imageUri ?: return null
        return Intent("com.facebook.stories.ADD_TO_STORY").apply {
            type = "image/jpeg"
            putExtra("backgroundAssetUri", imageUri)
            clipData = ClipData.newRawUri(null, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.facebook.katana")
        }
    }

    /**
     * WhatsApp — text always included; image attached when available.
     * WhatsApp renders EXTRA_TEXT as a caption beneath the image.
     */
    private fun whatsAppIntent(shareData: SocialShareData, imageUri: Uri?): Intent {
        val text = "Hey! I backed this project on Kickstarter: ${shareData.projectName} ${shareData.projectUrl}"
        return if (imageUri != null) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_STREAM, imageUri)
                clipData = ClipData.newRawUri(null, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage("com.whatsapp")
            }
        }
    }

    /**
     * Messages (SMS) — text-only.
     * ACTION_SENDTO + smsto: is the reliable way to pre-fill an SMS body and open the
     * default SMS app directly. Adding an image would require switching to ACTION_SEND,
     * which loses the smsto: addressing and turns it into MMS unpredictably across OEMs.
     */
    private fun messagesIntent(shareData: SocialShareData): Intent {
        val text = "Hey! I backed this project on Kickstarter: ${shareData.projectName} ${shareData.projectUrl}"
        return Intent(Intent.ACTION_SENDTO, "smsto:".toUri()).apply {
            putExtra("sms_body", text)
        }
    }

    /**
     * Email — text always included; image attached as an inline attachment when available.
     * ACTION_SEND with image/jpeg opens email clients in compose mode with the image
     * attached and the body/subject pre-filled. Falls back to ACTION_SENDTO + mailto:
     * (no attachment support) when no image is cached yet.
     */
    private fun emailIntent(shareData: SocialShareData, imageUri: Uri?): Intent {
        val subject = "Check out this Kickstarter project: ${shareData.projectName}"
        val body = "I thought you'd love this — ${shareData.projectName} by ${shareData.creatorName}:\n\n${shareData.projectUrl}"
        return if (imageUri != null) {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_STREAM, imageUri)
                clipData = ClipData.newRawUri(null, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
        }
    }

    /**
     * More (native Android chooser) — image + text when image is available,
     * text-only otherwise. Serves as the universal fallback for any app not
     * listed explicitly.
     */
    private fun nativeChooserIntent(shareData: SocialShareData, imageUri: Uri?): Intent {
        val text = "Hey! I backed this project on Kickstarter: ${shareData.projectName} ${shareData.projectUrl}"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            if (imageUri != null) {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                clipData = ClipData.newRawUri(null, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_TEXT, text)
            } else {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            putExtra(Intent.EXTRA_TITLE, shareData.projectName)
        }
        return Intent.createChooser(sendIntent, "Share project")
    }
}
