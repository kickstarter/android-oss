package com.kickstarter.features.videofeed.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.ui.components.KSVideoActionsColumn
import com.kickstarter.features.videofeed.ui.components.KSVideoBadgesRow
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer

class VideoFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KSTheme {
                // TODO: In future tickets this hardcoded list will be substituted by the result of a query
                val badges = listOf(
                    KSVideoBadgeType.ProjectWeLove,
                    KSVideoBadgeType.DaysLeft("3 days left"),
                    KSVideoBadgeType.JustLaunched,
                    KSVideoBadgeType.Trending
                )

                KSVideoPlayer(
                    videoUrl = "https://v2.kickstarter.com/1773073706-HOVpU84sHNp8zagSPildS7HOpqW2s%2BHtBG0zpREVy%2F4%3D/projects/5287238/video-1418061-hls_playlist.m3u8",
                    isActive = true,
                    overlayContent = { hazeState ->
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            KSVideoActionsColumn(
                                modifier = Modifier.align(Alignment.End),
                                profileImageUrl = "https://www.kickstarter.com/assets/default/user_default-738555160848037617b84803d360098f99.png",
                                bookmarkCount = "1k",
                                shareCount = "50",
                                onProfileClick = { },
                                onBookmarkClick = { },
                                onShareClick = { },
                                onMoreOptionsClick = { }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            KSVideoBadgesRow(
                                badges = badges,
                                hazeState = hazeState
                            )
                        }
                    }
                )
            }
        }
    }
}
