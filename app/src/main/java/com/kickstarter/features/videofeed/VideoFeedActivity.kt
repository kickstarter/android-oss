package com.kickstarter.features.videofeed

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer
import com.kickstarter.utils.WindowInsetsUtil

class VideoFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://v.kickstarter.com/1715848316_0209e992928646b9783f60570b8054612459955e/projects/4836640/video-1311059-hls_playlist.m3u8",
                    isActive = true
                )
            }
        }
    }
}
