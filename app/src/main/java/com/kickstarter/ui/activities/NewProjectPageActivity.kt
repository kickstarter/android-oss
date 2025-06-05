package com.kickstarter.ui.activities

import android.R
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kickstarter.mock.factories.RewardFactory
import com.kickstarter.models.Project
import com.kickstarter.models.Reward
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.compose.KSAsyncImage
import com.kickstarter.ui.compose.designsystem.KSPrimaryBlackButton
import com.kickstarter.ui.compose.designsystem.KSSecondaryGreyButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KickstarterApp
import com.kickstarter.ui.compose.designsystem.black
import com.kickstarter.utils.WindowInsetsUtil

class NewProjectPageActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rootView = window.decorView.findViewById<View>(R.id.content)
        WindowInsetsUtil.manageEdgeToEdge(window, rootView)

        val project = (intent.extras?.getParcelable(IntentKey.PROJECT) as Project?)?.let {
            it
        } ?: Project.builder().build()


        setContent {
            KickstarterApp(useDarkTheme = false) {
                ProjectDetailScreen(project = project)
            }
        }

    }

    @Composable
    fun ProjectDetailScreen(context: Context = LocalContext.current, project: Project) {
        val videoHeight = 400.dp
        val sheetCornerRadius = 24.dp
        val sheetPeekOffset = videoHeight - 20.dp

        var isPlaying by remember { mutableStateOf(true) }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // 1. VIDEO (paused initially)
            VideoBackgroundPlayer(
                context = context,
                videoUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                playWhenReady = isPlaying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(videoHeight)
                    .align(Alignment.TopStart)
            )

            // 2. PLAY BUTTON above bottom sheet
            if (!isPlaying) {
                IconButton(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.TopStart)
                        .offset(y = sheetPeekOffset - 64.dp) // hover above sheet
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                    onClick = { isPlaying = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // 3. SCROLLABLE CONTENT
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(sheetPeekOffset))
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = sheetCornerRadius,
                            topEnd = sheetCornerRadius
                        ),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = sheetCornerRadius,
                                    topEnd = sheetCornerRadius
                                )
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                project.name(),
                                style = KSTheme.typographyV2.title1
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = project.blurb(),
                                style = KSTheme.typographyV2.subHeadline
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row {

                                Text(
                                    text = "$${project.pledged().toInt()} pledged",
                                    style = KSTheme.typographyV2.heading2XL,
                                    modifier = Modifier
                                        .weight(1f)
                                        .align(Alignment.Bottom)
                                )

                                // Progress
                                Box(
                                    contentAlignment = Alignment.CenterEnd,
                                    modifier = Modifier
                                        .size(48.dp)

                                ) {
                                    CircularProgressIndicator(
                                        progress = project.percentageFunded() / 100f,
                                        modifier = Modifier.fillMaxSize(),
                                        strokeWidth = 4.dp,
                                        color = colors.textAccentGreen
                                    )
                                    Text(
                                        text = "${project.percentageFunded().toInt()}%",
                                        color = Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(15.dp))

                            Text(
                                text = "$10,015 goal • 15 days to go • 7,001 backers",
                                style = KSTheme.typography.subheadline,
                                color = black,

                                )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                "Rewards",
                                style = KSTheme.typographyV2.headingXL
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            val rewards = project.rewards()!!.toList()
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(items = sampleRewards) { reward ->
                                    RewardCard(reward)
                                }
                            }

                            KSSecondaryGreyButton(
                                onClickAction = {},
                                text = "See all rewards",
                                isEnabled = false
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Story",
                                style = KSTheme.typographyV2.headingXL
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            project.photo()?.let {
                                MyProjectCard(project)
                            }

                            Text(
                                project.blurb() + project.blurb() + project.blurb()
                            )
                            KSSecondaryGreyButton(
                                onClickAction = {},
                                text = "See full story",
                                isEnabled = false
                            )


                            Spacer(modifier = Modifier.height(20.dp)) // scroll buffer

                        }
                    }
                }
            }

            // 4. STICKY BUTTON
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                KSPrimaryBlackButton(
                    onClickAction = { },
                    text = "Back this project",
                    isEnabled = true
                )
            }
        }
    }


    val sampleRewards = listOf(
        RewardFactory.reward(),
        RewardFactory.rewardWithShipping(),
        RewardFactory.rewardHasAddOns()
    )

    @Composable
    fun RewardCard(reward: Reward) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(width = 200.dp, height = 120.dp),
            elevation = dimensions.elevationMedium
        ) {
            KSAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(dimensions.projectCardImageAspectRatio),
                image = reward.image()
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = reward.pledgeAmount().toString(),
                    color = colors.kds_white,
                    style = KSTheme.typography.titleRewardMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = reward.title().toString(), color = colors.kds_white)
            }
        }
    }

    @OptIn(UnstableApi::class)
    @Composable
    fun VideoBackgroundPlayer(
        context: Context,
        videoUri: String,
        playWhenReady: Boolean,
        modifier: Modifier = Modifier
    ) {
        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoUri))
                prepare()
            }
        }

        // Toggle playback state
        LaunchedEffect(playWhenReady) {
            exoPlayer.playWhenReady = playWhenReady
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
                    resizeMode =
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Crop landscape video to fill portrait

                }
            },
            modifier = modifier
        )

        DisposableEffect(Unit) {
            onDispose { exoPlayer.release() }
        }
    }


    @Composable
    fun MyProjectCard(project: Project) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .clip(RoundedCornerShape(16.dp))
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            KSAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(dimensions.projectCardImageAspectRatio),
                image = project.photo(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(16.dp)
            ) {

                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {

                    Text(
                        text = "HiDock P1 and P1 mini",
                        color = Color(0xFFDDDDDD),
                        fontSize = 23.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = project.name(),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shadow(8.dp, RoundedCornerShape(16.dp))
                            .align(Alignment.CenterHorizontally)
                    ) {
                        SquareCardContent(Icons.Default.Favorite)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Surface(
                            color = Color.Gray,
                            modifier = Modifier
                                .width(80.dp)
                                .height(80.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .align(Alignment.CenterVertically)
                        ) {
                            SquareCardContent(Icons.Default.Build)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color.Gray,
                            modifier = Modifier
                                .width(80.dp)
                                .height(80.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .align(Alignment.CenterVertically)
                        ) {
                            SquareCardContent(Icons.Default.AccountCircle)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color.Gray,
                            modifier = Modifier
                                .width(80.dp)
                                .height(80.dp)
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .align(Alignment.CenterVertically)
                        ) {
                            SquareCardContent(Icons.Default.Notifications)
                        }
                    }

                }
            }
        }
    }

    @Composable
    fun SquareCardContent(imageVector: ImageVector) {
        Image(
            modifier = Modifier.padding(8.dp),
            imageVector = imageVector,
            contentDescription = "",
            colorFilter = ColorFilter.tint(Color.White)
        )
    }


}