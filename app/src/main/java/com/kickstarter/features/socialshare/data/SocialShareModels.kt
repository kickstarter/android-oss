package com.kickstarter.features.socialshare.data

import android.graphics.Bitmap
import android.net.Uri
import com.kickstarter.libs.RefTag
import com.kickstarter.libs.utils.EventContextValues.SharePlatformContextType

data class SocialShareData(
    val projectName: String,
    val projectUrl: String,
    val imageUrl: String,
    val creatorName: String
)

enum class SocialSharePlatform(val targetPackage: String?) {
    COPY_LINK(null),
    INSTAGRAM_FEED("com.instagram.android"),
    INSTAGRAM_STORIES("com.instagram.android"),
    X("com.twitter.android"),
    FACEBOOK_FEED("com.facebook.katana"),
    FACEBOOK_STORIES("com.facebook.katana"),
    WHATSAPP("com.whatsapp"),
    MESSAGES(null),
    EMAIL(null),
    MORE(null);

    fun requiresImage(): Boolean = when (this) {
        INSTAGRAM_FEED,
        INSTAGRAM_STORIES,
        FACEBOOK_FEED,
        FACEBOOK_STORIES -> true
        else -> false
    }
}

fun SocialSharePlatform.refTag(): RefTag = when (this) {
    SocialSharePlatform.COPY_LINK -> RefTag.projectShareCopyLink()
    SocialSharePlatform.INSTAGRAM_FEED -> RefTag.projectShareInstagramFeed()
    SocialSharePlatform.INSTAGRAM_STORIES -> RefTag.projectShareInstagramStories()
    SocialSharePlatform.X -> RefTag.projectShareX()
    SocialSharePlatform.FACEBOOK_FEED -> RefTag.projectShareFacebookFeed()
    SocialSharePlatform.FACEBOOK_STORIES -> RefTag.projectShareFacebookStories()
    SocialSharePlatform.WHATSAPP -> RefTag.projectShareWhatsApp()
    SocialSharePlatform.MESSAGES -> RefTag.projectShareMessages()
    SocialSharePlatform.EMAIL -> RefTag.projectShareEmail()
    SocialSharePlatform.MORE -> RefTag.projectShareMore()
}

fun SocialSharePlatform.analyticsContextType(): String = when (this) {
    SocialSharePlatform.COPY_LINK -> SharePlatformContextType.COPY_LINK.contextName
    SocialSharePlatform.INSTAGRAM_FEED -> SharePlatformContextType.INSTAGRAM_FEED.contextName
    SocialSharePlatform.INSTAGRAM_STORIES -> SharePlatformContextType.INSTAGRAM_STORIES.contextName
    SocialSharePlatform.X -> SharePlatformContextType.X.contextName
    SocialSharePlatform.FACEBOOK_FEED -> SharePlatformContextType.FACEBOOK_FEED.contextName
    SocialSharePlatform.FACEBOOK_STORIES -> SharePlatformContextType.FACEBOOK_STORIES.contextName
    SocialSharePlatform.WHATSAPP -> SharePlatformContextType.WHATSAPP.contextName
    SocialSharePlatform.MESSAGES -> SharePlatformContextType.MESSAGES.contextName
    SocialSharePlatform.EMAIL -> SharePlatformContextType.EMAIL.contextName
    SocialSharePlatform.MORE -> SharePlatformContextType.MORE.contextName
}

data class SocialShareUIState(
    val availablePlatforms: List<SocialSharePlatform> = emptyList(),
    /**
     * The downloaded hero image, fed into [com.kickstarter.features.socialshare.ui.components.SocialShareProjectCard]
     * so it renders deterministically before the card is captured. Null until the retrieve step completes.
     */
    val heroBitmap: Bitmap? = null,
    /**
     * `content://` URI of the captured, branded share card (PNG). This — not the raw hero image — is
     * the asset attached to image-bearing intents. Null until the card has been captured and cached.
     */
    val shareImageUri: Uri? = null,
    /**
     * True from the moment the hero image starts downloading until the captured card has been cached.
     * While true, platforms that [SocialSharePlatform.requiresImage] are gated in the ViewModel.
     */
    val isGeneratingImage: Boolean = false,
    val copiedToClipboard: Boolean = false
)
