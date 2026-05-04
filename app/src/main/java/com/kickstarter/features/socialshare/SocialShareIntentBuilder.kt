package com.kickstarter.features.socialshare

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.data.SocialSharePlatform

object SocialShareIntentBuilder {

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
        SocialSharePlatform.MORE -> nativeChooserIntent(shareData)
    }

    private fun instagramFeedIntent(imageUri: Uri?): Intent? {
        imageUri ?: return null
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.instagram.android")
        }
    }

    private fun instagramStoriesIntent(context: Context, imageUri: Uri?): Intent? {
        imageUri ?: return null
        return Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(imageUri, "image/jpeg")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("source_application", context.packageName)
        }
    }

    private fun xIntent(shareData: SocialShareData): Intent {
        val text = "I just backed ${shareData.projectName} on @Kickstarter! Check it out: ${shareData.projectUrl}"
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.twitter.android")
        }
    }

    private fun facebookFeedIntent(imageUri: Uri?): Intent? {
        imageUri ?: return null
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.facebook.katana")
        }
    }

    private fun facebookStoriesIntent(imageUri: Uri?): Intent? {
        imageUri ?: return null
        return Intent("com.facebook.stories.ADD_TO_STORY").apply {
            type = "image/jpeg"
            putExtra("backgroundAssetUri", imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.facebook.katana")
        }
    }

    private fun whatsAppIntent(shareData: SocialShareData): Intent {
        val text = "Hey! I backed this project on Kickstarter: ${shareData.projectName} ${shareData.projectUrl}"
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
    }

    private fun messagesIntent(shareData: SocialShareData): Intent {
        val text = "Hey! I backed this project on Kickstarter: ${shareData.projectName} ${shareData.projectUrl}"
        return Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
            putExtra("sms_body", text)
        }
    }

    private fun emailIntent(shareData: SocialShareData): Intent {
        return Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
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

    private fun nativeChooserIntent(shareData: SocialShareData): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareData.projectUrl)
            putExtra(Intent.EXTRA_TITLE, shareData.projectName)
        }
        return Intent.createChooser(sendIntent, null)
    }
}
