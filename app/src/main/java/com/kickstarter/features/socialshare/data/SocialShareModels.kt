package com.kickstarter.features.socialshare.data

import android.net.Uri

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
    MORE(null)
}

data class SocialShareUIState(
    val availablePlatforms: List<SocialSharePlatform> = emptyList(),
    val shareImageUri: Uri? = null,
    val isGeneratingImage: Boolean = false,
    val copiedToClipboard: Boolean = false
)
