package com.kickstarter.features.videofeed.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSProfileButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSVideoPlayerIconButton
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Bookmark
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Ellipsis
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Share

enum class KSVideoActionsColumnTestTag {
    COLUMN_CONTAINER,
    PROFILE_BUTTON,
    BOOKMARK_BUTTON,
    SHARE_BUTTON,
    MORE_OPTIONS_BUTTON
}

@Composable
fun KSVideoActionsColumn(
    modifier: Modifier = Modifier,
    profileImageUrl: String? = null,
    bookmarkCount: String? = null,
    shareCount: String? = null,
    onProfileClick: () -> Unit = {},
    onBookmarkClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.testTag(KSVideoActionsColumnTestTag.COLUMN_CONTAINER.name),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        profileImageUrl?.let {
            KSProfileButton(
                modifier = Modifier.testTag(KSVideoActionsColumnTestTag.PROFILE_BUTTON.name),
                imageUrl = it,
                onClick = onProfileClick,
                contentDescription = stringResource(id = R.string.fpo_Profile),
                onClickLabel = stringResource(id = R.string.fpo_View_creator_profile)
            )
        }

        KSVideoPlayerIconButton(
            modifier = Modifier
                .semantics {
                    stateDescription = bookmarkCount ?: ""
                }
                .testTag(KSVideoActionsColumnTestTag.BOOKMARK_BUTTON.name),
            icon = Bookmark,
            text = bookmarkCount,
            onClick = onBookmarkClick,
            contentDescription = stringResource(id = R.string.fpo_Bookmark),
            onClickLabel = stringResource(id = R.string.fpo_Bookmark_this_project)
        )

        KSVideoPlayerIconButton(
            modifier = Modifier
                .semantics {
                    stateDescription = shareCount ?: ""
                }
                .testTag(KSVideoActionsColumnTestTag.SHARE_BUTTON.name),
            icon = Share,
            text = shareCount,
            onClick = onShareClick,
            contentDescription = stringResource(id = R.string.fpo_Share),
            onClickLabel = stringResource(id = R.string.fpo_Share_this_project)
        )

        KSVideoPlayerIconButton(
            modifier = Modifier.testTag(KSVideoActionsColumnTestTag.MORE_OPTIONS_BUTTON.name),
            icon = Ellipsis,
            onClick = onMoreOptionsClick,
            contentDescription = stringResource(id = R.string.fpo_More_options),
            onClickLabel = stringResource(id = R.string.fpo_View_more_options)
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSVideoActionsColumnPreview() {
    KSTheme {
        KSVideoActionsColumn(
            modifier = Modifier.background(Color.Green.copy(alpha = 0.5f)),
            profileImageUrl = "https://www.kickstarter.com/assets/default/user_default-738555160848037617b84803d360098f99.png",
            bookmarkCount = "1.2k",
            shareCount = "500"
        )
    }
}
