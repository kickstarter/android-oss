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
                    videoUrl = "https://v2.kickstarter.com/1772750628-dJKPaHstEs68OwPdrf7rxLRP3TxRvYCw4zL6p8vFMS4%3D/projects/5232086/video-1415838-hls_playlist.m3u8",
                    isActive = true
                )
            }
        }
    }
}
