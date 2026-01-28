package com.kickstarter.features.videofeed.ui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kickstarter.ui.compose.designsystem.KickstarterApp

class VideoFeedActivity : AppCompatActivity() {

    data class Project(
        val id: Int,
        val category: String,
        val title: String,
        val subtitle: String,
        val percentageFunded: Int,
        val videoUrl: String
    )

    private val sampleProjects = listOf(
        Project(
            1,
            "Project We Love",
            "Kode Dot: The All-in-One Device",
            "$20,150 pledged • 498 backers",
            20,
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        ),
        Project(
            2,
            "Project We Love",
            "Ringo Move – The Ultimate Bottle",
            "$812,134 pledged • 5k backers",
            75,
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        ),
        Project(
            3,
            "3 days left",
            "Hyodo MagBase™ — Inter-swappable Wallet",
            "$23,903 pledged • Help bring this idea to life",
            75,
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KickstarterApp(useDarkTheme = true) {
                MobileVisioningView()
            }
        }
    }

    @Composable
    fun MobileVisioningView() {
        val listState = rememberLazyListState()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            itemsIndexed(sampleProjects) { index, project ->
                val isVisible by remember {
                    derivedStateOf { listState.firstVisibleItemIndex == index }
                }
                ProjectFullscreenCard(project, isVisible, modifier = Modifier.fillParentMaxSize())
            }
        }
    }

    @Composable
    fun ActionButton(icon: ImageVector, label: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
            if (label.isNotEmpty()) {
                Text(label, color = Color.White, fontSize = 12.sp)
            }
        }
    }

    @Composable
    fun ProjectFullscreenCard(project: Project, isVisible: Boolean, modifier: Modifier) {
        Box(modifier = modifier.background(Color.Black)) {
            VideoPlayer(videoUrl = project.videoUrl, isActive = isVisible)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 1200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp, bottom = 180.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                ActionButton(Icons.Default.Favorite, "1k")
                ActionButton(Icons.Default.Share, "50")
                ActionButton(Icons.Default.MoreVert, "")
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProjectBadge(text = project.category, icon = null)
                    ProjectBadge(text = "3 days left", icon = Icons.Default.Settings)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = project.subtitle,
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                        CircularProgressIndicator(
                            progress = 1f, // The background track
                            color = Color.White.copy(alpha = 0.2f),
                            strokeWidth = 3.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        CircularProgressIndicator(
                            progress = project.percentageFunded / 100f,
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "${project.percentageFunded}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    onClick = { /* Action */ },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Back this project",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // TODO: ProgressBar for videoPlayback
                LinearProgressIndicator(
                    progress = { 0.3f },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }

    @Composable
    fun ProjectBadge(text: String, icon: ImageVector?) {
        Surface(
            color = Color.White.copy(alpha = 0.15f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    @OptIn(UnstableApi::class)
    @Composable
    fun VideoPlayer(videoUrl: String, isActive: Boolean) {
        val context = LocalContext.current
        val exoPlayer = remember(videoUrl) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
            }
        }

        LaunchedEffect(isActive) {
            exoPlayer.playWhenReady = isActive
        }

        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        DisposableEffect(videoUrl) {
            onDispose { exoPlayer.release() }
        }
    }
}
