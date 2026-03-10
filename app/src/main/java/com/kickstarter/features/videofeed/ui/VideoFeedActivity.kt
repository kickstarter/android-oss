package com.kickstarter.features.videofeed.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.features.videofeed.ui.components.KSVideoBadgesRow
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer

class VideoFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KSTheme {
                // - Simulating a list of badges, this is what will be provided by the API
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
                        KSVideoBadgesRow(
                            modifier = Modifier.align(Alignment.BottomStart),
                            badges = badges,
                            hazeState = hazeState
                        )
                    }
                )
            }
        }
    }
}
