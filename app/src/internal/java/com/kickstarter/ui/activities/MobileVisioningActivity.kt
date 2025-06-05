package com.kickstarter.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.utils.WindowInsetsUtil

class MobileVisioningActivity : AppCompatActivity() {
    @Preview
    @Composable
    fun PreviewProjectCard() {
        ProjectCard(project = sampleProjects[0])
    }

    // Using a fake Project model and fake list of projects for now! But should be able to incorporate real ones easily
    data class Project(
        val id: Int,
        val category: String,
        val title: String,
        val subtitle: String,
        val percentageFunded: Int,
        val videoUrl: String
    )
    val sampleProjects = listOf(
        Project(
            id = 1,
            category = "Hardware",
            title = "ZimaBoard 2 – Hack Out New Rules",
            subtitle = "Technology • 2 days left • $918,293 raised",
            percentageFunded = 170,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        ),
        Project(
            id = 2,
            category = "Hardware",
            title = "ZimaBoard 2 – Hack Out New Rules",
            subtitle = "Technology • 2 days left • $918,293 raised",
            percentageFunded = 170,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
        ),
        Project(
            id = 3,
            category = "Hardware",
            title = "ZimaBoard 2 – Hack Out New Rules",
            subtitle = "Technology • 2 days left • $918,293 raised",
            percentageFunded = 170,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        ),
        Project(
            id = 4,
            category = "Hardware",
            title = "ZimaBoard 2 – Hack Out New Rules",
            subtitle = "Technology • 2 days left • $918,293 raised",
            percentageFunded = 170,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/VolkswagenGTIReview.mp4"
        ),
        Project(
            id = 5,
            category = "Hardware",
            title = "ZimaBoard 2 – Hack Out New Rules",
            subtitle = "Technology • 2 days left • $918,293 raised",
            percentageFunded = 170,
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
        ),

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        WindowInsetsUtil.manageEdgeToEdge(window, rootView)
        setContent {
            KickstarterApp(useDarkTheme = false) {
                MobileVisioningView()
            }
        }
    }

    @Composable
    fun MobileVisioningView() {
        ProjectList(projects = sampleProjects)
    }

    @OptIn(UnstableApi::class)
    @Composable
    fun VideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val exoPlayer = remember(videoUrl) {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(videoUrl)
                setMediaItem(mediaItem)
                prepare()
               // playWhenReady = true // Not sure if better to use LaunchedEffect
            }
        }
        LaunchedEffect(videoUrl) {
            exoPlayer.playWhenReady = true
        }

        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Crop landscape video to fill portrait
                }
            },
            modifier = modifier
        )

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }
    }

    @Composable
    fun ProjectList(projects: List<Project>) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F9)),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(items = projects) { item ->
                ProjectCard(item)
            }
        }
    }

    @Composable
    fun ProjectCard(project: Project) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .clip(RoundedCornerShape(16.dp))
                .shadow(8.dp, RoundedCornerShape(16.dp))
        ) {
            VideoPlayer(videoUrl = project.videoUrl, modifier = Modifier.matchParentSize())

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = project.category,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color(0x99000000), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart)
                )

                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = project.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.subtitle,
                        color = Color(0xFFDDDDDD),
                        fontSize = 13.sp
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    CircularProgressIndicator(
                        progress = project.percentageFunded / 100f,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 4.dp,
                        color = Color.White
                    )
                    Text(
                        text = "${project.percentageFunded}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

}