package com.kickstarter.features.videofeed.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSVideoPlayerIconButton
import com.kickstarter.ui.compose.designsystem.KSVideoPlayerProfileButton
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Bookmark
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Ellipsis
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Share

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
        modifier = modifier
            .width(64.dp)
            .padding(end = dimensions.paddingSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
    ) {
        profileImageUrl?.let {
            KSVideoPlayerProfileButton(
                imageUrl = it,
                onClick = onProfileClick,
                contentDescription = stringResource(id = R.string.fpo_Profile)
            )
        }

        KSVideoPlayerIconButton(
            icon = Bookmark,
            text = bookmarkCount,
            onClick = onBookmarkClick,
            contentDescription = stringResource(id = R.string.fpo_Bookmark)
        )

        KSVideoPlayerIconButton(
            icon = Share,
            text = shareCount,
            onClick = onShareClick,
            contentDescription = stringResource(id = R.string.fpo_Share)
        )

        KSVideoPlayerIconButton(
            icon = Ellipsis,
            onClick = onMoreOptionsClick,
            contentDescription = stringResource(id = R.string.fpo_More_options)
        )
    }
}
