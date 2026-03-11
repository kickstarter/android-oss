package com.kickstarter.features.videofeed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.videoplayer.KSVideoPlayer

class VideoFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KSTheme {
                KSVideoPlayer(
                    videoUrl = "https://v2.kickstarter.com/1773073706-HOVpU84sHNp8zagSPildS7HOpqW2s%2BHtBG0zpREVy%2F4%3D/projects/5287238/video-1418061-hls_playlist.m3u8",
                    isActive = true,
                    overlayContent = {
                        Box(
                            modifier = Modifier
                                .padding(100.dp)
                                .align(alignment = BottomCenter)
                        ) {
                            Text(
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth(),
                                text = "This is a container for future UI pieces!"
                            )
                        }
                    }
                )
            }
        }
    }
}
