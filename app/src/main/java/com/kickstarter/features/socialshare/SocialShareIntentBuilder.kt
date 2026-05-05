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
 * It handles the nuances of each app's deep linking or sharing requirements.
 */
object SocialShareIntentBuilder {

    /**
     * Entry point to create an intent based on the selected platform.
     *
     * @param context Calling context.
     * @param platform The target social media platform.
     * @param shareData Metadata about the project being shared.
     * @param imageUri Optional URI to a local cached image for image-based sharing.
     * @return A configured [Intent], or null if the platform doesn't use standard Intents (e.g. COPY_LINK).
     */
    fun buildIntent(
        context: Context,
        platform: SocialSharePlatform,
        shareData: SocialShareData,
        imageUri: Uri?
    ): Intent? = when (platform) {
        SocialSharePlatform.COPY_LINK -> null
        SocialSharePlatform.INSTAGRAM_FEED -> instagramFeedIntent(imageUri)
        SocialSharePlatform.INSTAGRAM_STORIES -> instagramStoriesIntent(context, imageUri)
        SocialSharePlatform.X -> xIntent(shareData)
        SocialSharePlatform.FACEBOOK_FEED -> facebookFeedIntent(imageUri)
        SocialSharePlatform.FACEBOOK_STORIES -> facebookStoriesIntent(imageUri)
        SocialSharePlatform.WHATSAPP -> whatsAppIntent(shareData)
        SocialSharePlatform.MESSAGES -> messagesIntent(shareData)
        SocialSharePlatform.EMAIL -> emailIntent(shareData)
        SocialSharePlatform.MORE -> nativeChooserIntent(shareData, imageUri)
    }

    /**
     * Builds an intent to share an image directly to the Instagram Feed.
     * Requires the Instagram app package.
     */
    private fun instagramFeedIntent(imageUri: Uri?): Intent? {
        imageUri ?: return null
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            // ClipData is required for URI permission granting on API 29+
            clipData = ClipData.newRawUri(null, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.instagram.android")
        }
    }

    /**
     * Builds an intent for Instagram Stories. Uses Instagram's specific custom action
     * which allows placing an image as a "sticker" or background.
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
     * Builds a text-based share intent for X (formerly Twitter).
     * Includes a predefined mention and project URL.
     */
    private fun xIntent(shareData: SocialShareData): Intent {
        val text = "I just backed ${shareData.projectName} on @Kickstarter! Check it out: ${shareData.projectUrl}"
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.twitter.android")
        }
    }

    /**
     * Builds an intent to share an image to the Facebook Feed.
     */
    private fun facebookFeedIntent(imageUri: Uri?): Intent? {
        imageUri ?: return null
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            clipData = ClipData.newRawUri(null, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.facebook.katana")
        }
    }

    /**
     * Builds an intent for Facebook Stories using their specific "ADD_TO_STORY" action.
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
     * Builds a text share intent specifically for WhatsApp.
     */
    private fun whatsAppIntent(shareData: SocialShareData): Intent {
        val text = "Hey! I backed this project on Kickstarter: ${shareData.projectName} ${shareData.projectUrl}"
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
    }

    /**
     * Builds an intent to send a text via SMS/MMS apps.
     */
    private fun messagesIntent(shareData: SocialShareData): Intent {
        val text = "Hey! I backed this project on Kickstarter: ${shareData.projectName} ${shareData.projectUrl}"
        return Intent(Intent.ACTION_SENDTO, "smsto:".toUri()).apply {
            putExtra("sms_body", text)
        }
    }

    /**
     * Builds an intent to share via Email apps.
     */
    private fun emailIntent(shareData: SocialShareData): Intent {
        return Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
            putExtra(
                Intent.EXTRA_SUBJECT,
                "Check out this Kickstarter project: ${shareData.projectName}"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "I thought you'd love this — ${shareData.projectName} by ${shareData.creatorName}:\n\n${shareData.projectUrl}"
            )
        }
    }

    /**
     * Creates a generic system share sheet chooser. This is the fallback for the "More" option.
     * It will include the image if it's already cached, otherwise it shares only the text URL.
     */
    private fun nativeChooserIntent(shareData: SocialShareData, imageUri: Uri?): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            if (imageUri != null) {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                clipData = ClipData.newRawUri(null, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // - Include text for apps that can handle both (e.g. Slack, Discord, some email clients)
                putExtra(Intent.EXTRA_TEXT, shareData.projectUrl)
            } else {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareData.projectUrl)
            }
            putExtra(Intent.EXTRA_TITLE, shareData.projectName)
        }
        return Intent.createChooser(sendIntent, "Share project")
    }
}
