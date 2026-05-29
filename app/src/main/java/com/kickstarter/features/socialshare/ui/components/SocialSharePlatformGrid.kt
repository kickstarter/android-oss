package com.kickstarter.features.socialshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.features.socialshare.data.SocialSharePlatform
import com.kickstarter.features.socialshare.ui.icons.SocialShareCopyLink
import com.kickstarter.features.socialshare.ui.icons.SocialShareEmail
import com.kickstarter.features.socialshare.ui.icons.SocialShareFacebook
import com.kickstarter.features.socialshare.ui.icons.SocialShareInstagram
import com.kickstarter.features.socialshare.ui.icons.SocialShareMessage
import com.kickstarter.features.socialshare.ui.icons.SocialShareMore
import com.kickstarter.features.socialshare.ui.icons.SocialShareWhatsApp
import com.kickstarter.features.socialshare.ui.icons.SocialShareX
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SocialSharePlatformGridPreview() {
    KSTheme {
        SocialSharePlatformGrid(
            platforms = SocialSharePlatform.entries,
            onPlatformSelected = {},
            onCopyLinkSelected = {}
        )
    }
}

enum class SocialSharePlatformGridTestTag {
    GRID
}

@Composable
fun SocialSharePlatformGrid(
    platforms: List<SocialSharePlatform>,
    onPlatformSelected: (SocialSharePlatform) -> Unit,
    onCopyLinkSelected: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(
            horizontal = dimensions.paddingMedium,
            vertical = dimensions.paddingMedium
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
        verticalArrangement = Arrangement.spacedBy(dimensions.paddingMedium),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(SocialSharePlatformGridTestTag.GRID.name)
    ) {
        items(platforms) { platform ->
            PlatformButton(
                platform = platform,
                onClick = {
                    if (platform == SocialSharePlatform.COPY_LINK) onCopyLinkSelected()
                    else onPlatformSelected(platform)
                }
            )
        }
    }
}

@Composable
private fun PlatformButton(
    platform: SocialSharePlatform,
    onClick: () -> Unit
) {
    val label = platform.label()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(platform.name)
            .semantics(mergeDescendants = true) {
                contentDescription = platform.accessibilityLabel()
                role = Role.Button
            }
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = platform.icon(),
            contentDescription = null,
            tint = if (platform.isBrandIcon()) Color.Unspecified else colors.socialShare.monochromeIconTint,
            modifier = Modifier
                .size(dimensions.socialSharePlatformIconSize)
                .clip(CircleShape)
                .background(colors.socialShare.iconBackground)
                .padding(dimensions.socialSharePlatformIconPadding)
        )
        Text(
            text = label,
            style = KSTheme.typographyV2.bodyXXS,
            color = colors.socialShare.platformLabel,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = dimensions.paddingXSmall)
        )
    }
}

private fun SocialSharePlatform.isBrandIcon(): Boolean = when (this) {
    SocialSharePlatform.INSTAGRAM_FEED,
    SocialSharePlatform.INSTAGRAM_STORIES,
    SocialSharePlatform.FACEBOOK_FEED,
    SocialSharePlatform.FACEBOOK_STORIES,
    SocialSharePlatform.WHATSAPP -> true
    else -> false
}

private fun SocialSharePlatform.icon(): ImageVector = when (this) {
    SocialSharePlatform.COPY_LINK -> SocialShareCopyLink
    SocialSharePlatform.INSTAGRAM_FEED,
    SocialSharePlatform.INSTAGRAM_STORIES -> SocialShareInstagram
    SocialSharePlatform.X -> SocialShareX
    SocialSharePlatform.FACEBOOK_FEED,
    SocialSharePlatform.FACEBOOK_STORIES -> SocialShareFacebook
    SocialSharePlatform.WHATSAPP -> SocialShareWhatsApp
    SocialSharePlatform.MESSAGES -> SocialShareMessage
    SocialSharePlatform.EMAIL -> SocialShareEmail
    SocialSharePlatform.MORE -> SocialShareMore
}

private fun SocialSharePlatform.label(): String = when (this) {
    SocialSharePlatform.COPY_LINK -> "Copy link"
    SocialSharePlatform.INSTAGRAM_FEED -> "Feed"
    SocialSharePlatform.INSTAGRAM_STORIES -> "Stories"
    SocialSharePlatform.X -> "X"
    SocialSharePlatform.FACEBOOK_FEED -> "Feed"
    SocialSharePlatform.FACEBOOK_STORIES -> "Stories"
    SocialSharePlatform.WHATSAPP -> "Whatsapp"
    SocialSharePlatform.MESSAGES -> "Messages"
    SocialSharePlatform.EMAIL -> "Email"
    SocialSharePlatform.MORE -> "More"
}

/**
 * Full accessibility description announced by TalkBack.
 * Unlike [label], these are unambiguous across all platforms — "Feed" alone
 * cannot distinguish Instagram Feed from Facebook Feed, but
 * "Share to Instagram Feed" can.
 *
 * // TODO: Explore if these require translations
 */
internal fun SocialSharePlatform.accessibilityLabel(): String = when (this) {
    SocialSharePlatform.COPY_LINK -> "Copy project link"
    SocialSharePlatform.INSTAGRAM_FEED -> "Share to Instagram Feed"
    SocialSharePlatform.INSTAGRAM_STORIES -> "Share to Instagram Stories"
    SocialSharePlatform.X -> "Share to X"
    SocialSharePlatform.FACEBOOK_FEED -> "Share to Facebook Feed"
    SocialSharePlatform.FACEBOOK_STORIES -> "Share to Facebook Stories"
    SocialSharePlatform.WHATSAPP -> "Share via WhatsApp"
    SocialSharePlatform.MESSAGES -> "Share via Messages"
    SocialSharePlatform.EMAIL -> "Share via Email"
    SocialSharePlatform.MORE -> "More sharing options"
}
