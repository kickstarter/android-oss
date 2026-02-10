package com.kickstarter.features.videofeed.ui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.preload.DefaultPreloadManager
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kickstarter.features.videofeed.viewmodel.VideoFeedViewModel
import com.kickstarter.libs.utils.extensions.getEnvironment
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.getValue

@UnstableApi
class VideoFeedActivity : AppCompatActivity() {

    data class Project(
        val id: Int,
        val category: String,
        val title: String,
        val subtitle: String,
        val percentageFunded: Int,
        val videoUrl: String
    )

    private lateinit var environment: com.kickstarter.libs.Environment
    private lateinit var viewModelFactory: VideoFeedViewModel.Factory
    private val viewModel: VideoFeedViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.getEnvironment()?.let { env ->
            environment = env
            viewModelFactory = VideoFeedViewModel.Factory(env)
        }

        setContent {
            KickstarterApp(useDarkTheme = true) {
                val uiState by viewModel.videoFeedUIState.collectAsStateWithLifecycle()
                val projects = uiState.projects
                val currentIndex by viewModel.preloadManagerIndex.collectAsStateWithLifecycle()

                val scrollType = intent.extras?.getBoolean("scrollType", false)

                val (preloadManager, sharedPlayer) = remember {
                    val builder = DefaultPreloadManager.Builder(
                        application,
                        viewModel.preloadManager
                    )
                    Pair(builder.build(), builder.buildExoPlayer())
                }

                // Connect VM state to Manager side-effects
                LaunchedEffect(uiState.projects, currentIndex) {
                    // Update items list
                    uiState.projects.forEachIndexed { index, project ->
                        preloadManager.add(MediaItem.fromUri(project.videoUrl), index)
                    }

                    // Move the preload anchor
                    preloadManager.setCurrentPlayingIndex(currentIndex)

                    // Execute the math (Distance 1 = 5s, Distance 3 = Tracks, etc.)
                    preloadManager.invalidate()
                }

                if (scrollType == false)
                    VideoFeedList(projectsList = projects, preloadManager, sharedPlayer)
                if (scrollType == true)
                    VideoFeedPager(projectsList = projects, preloadManager, sharedPlayer)
            }
        }
    }

    @Composable
    fun VideoFeedPager(projectsList: List<Project>, preloadManager: DefaultPreloadManager, sharedPlayer: ExoPlayer) {
        val pagerState = rememberPagerState(pageCount = { projectsList.size })

        // Pagination Trigger
        LaunchedEffect(pagerState.currentPage) {
            viewModel.onPageChanged(pagerState.currentPage)

            val threshold = 3 // - 3 till the end, start quering for more
            if (pagerState.currentPage >= projectsList.size - threshold && projectsList.isNotEmpty()) {
                viewModel.loadProjects()
            }
        }

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
            userScrollEnabled = true,
            key = { projectsList[it].id }
        ) { page ->
            val isVisible = pagerState.currentPage == page
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ProjectFullscreenCard(projectsList[page], isVisible, modifier = Modifier.fillMaxSize(), preloadManager, sharedPlayer)
            }
        }
    }

    @Composable
    fun VideoFeedList(projectsList: List<Project>, preloadManager: DefaultPreloadManager, sharedPlayer: ExoPlayer) {
        val listState = rememberLazyListState()

        // Pagination Trigger
        val shouldLoadMore = remember {
            derivedStateOf {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                val totalItems = listState.layoutInfo.totalItemsCount

                // - 3 till the end, start quering for more
                lastVisibleItem != null && lastVisibleItem.index >= totalItems - 3
            }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { index ->
                    viewModel.onPageChanged(index)
                }
        }

        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value) {
                //viewModel.onPageChanged(pagerState.currentPage)
                viewModel.loadProjects()
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            itemsIndexed(projectsList) { index, project ->
                val isVisible by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex == index
                    }
                }
                ProjectFullscreenCard(
                    project,
                    isVisible,
                    modifier = Modifier.fillParentMaxSize(),
                    preloadManager,
                    sharedPlayer
                )
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
    fun ProjectFullscreenCard(
        project: Project,
        isVisible: Boolean,
        modifier: Modifier,
        preloadManager: DefaultPreloadManager,
        sharedPlayer: ExoPlayer
    ) {
        Box(modifier = modifier.background(Color.Black)) {
            VideoPlayer(videoUrl = project.videoUrl, isActive = isVisible, preloadManager, sharedPlayer)

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
    fun VideoPlayer(
        videoUrl: String,
        isActive: Boolean, // This is the crucial variable!
        preloadManager: DefaultPreloadManager,
        sharedPlayer: ExoPlayer
    ) {
        val mediaItem = remember(videoUrl) { MediaItem.fromUri(videoUrl) }

        // ONLY the active page should touch the sharedPlayer
        if (isActive) {
            DisposableEffect(videoUrl) {
                val preloadedSource = preloadManager.getMediaSource(mediaItem)

                if (preloadedSource != null) {
                    sharedPlayer.setMediaSource(preloadedSource)
                } else {
                    sharedPlayer.setMediaItem(mediaItem)
                }

                sharedPlayer.repeatMode = Player.REPEAT_MODE_ONE
                sharedPlayer.prepare()
                sharedPlayer.playWhenReady = true

                onDispose {
                    sharedPlayer.stop()
                    sharedPlayer.clearMediaItems()
                }
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = sharedPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.player = sharedPlayer
                }
            )
        } else {
            // Neighbors (isActive == false) should show nothing or a thumbnail
            // This prevents them from stealing the sharedPlayer from the active item
            Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        }
    }
}
