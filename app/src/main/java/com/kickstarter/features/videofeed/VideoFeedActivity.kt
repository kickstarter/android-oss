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
                    videoUrl = "https://v2.kickstarter.com/1772750628-dJKPaHstEs68OwPdrf7rxLRP3TxRvYCw4zL6p8vFMS4%3D/projects/5232086/video-1415838-hls_playlist.m3u8",
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
