package com.kickstarter.ui.fragments.projectpage.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.kickstarter.R
import com.kickstarter.libs.utils.extensions.deadlineCountdown
import com.kickstarter.models.Photo
import com.kickstarter.models.Project
import com.kickstarter.ui.activities.compose.search.CardProjectState
import com.kickstarter.ui.compose.designsystem.KSProjectCardLarge
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.viewmodels.projectpage.SimilarProjectsUiState
import org.joda.time.DateTime

@Preview(
    name = "Light",
    showBackground = true,
    backgroundColor = 0xFFF0EAE2,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    widthDp = 384
)
@Preview(
    name = "Dark",
    showBackground = true,
    backgroundColor = 0xFFF0EAE2,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 384
)
@Composable
fun SimilarProjectCardPreview() {
    KSTheme {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .width(312.dp),
        ) {
            SimilarProjectCard(
                Project
                    .builder()
                    .name("Dernière Lune, Collection of Dark Fantasy Short Stories")
                    .photo(Photo.builder().full("https://i-dev.kickstarter.com/assets/043/680/711/102888b0a91af18b778235aac4414cbd_original.png?anim=false&fit=cover&gravity=auto&height=576&origin=ugc-qa&q=92&v=1705426857&width=1024&sig=b8unMD58v%2BZsl3sF%2Fe5BNriEoWNSDAp7CE1EjIRDPGk%3D").build())
                    .state(Project.STATE_LIVE)
                    .deadline(DateTime.now().plusDays(10))
                    .currency("USD")
                    .currentCurrency("USD")
                    .build()
            )
        }
    }
}

@Composable
fun SimilarProjectCard(
    project: Project,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val countdown = project.deadlineCountdown(context)
    val percentFunded = project.percentFunded() ?: 0
    val fundedString = stringResource(R.string.discovery_baseball_card_stats_funded)

    KSProjectCardLarge(
        modifier = Modifier,
        photo = project.photo(),
        title = project.name(),
        titleMinMaxLines = 2..2,
        state = CardProjectState.LIVE,
        fundingInfoString = "$countdown left • $percentFunded% $fundedString",
        fundedPercentage = project.percentFunded() ?: 0,
        onClick = onClick,
    )
}

@Preview(
    name = "Light",
    device = "id:pixel_8_pro",
    showBackground = true,
    backgroundColor = 0xFFF0EAE2,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Dark",
    device = "id:pixel_8_pro",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SimilarProjectsComponentPreview() {
    val data: List<Project> = listOf(
        Project
            .builder()
            .name("Dernière Lune, Collection of Dark Fantasy Short Stories")
            .photo(Photo.builder().full("").build())
            .state(Project.STATE_LIVE)
            .currency("USD")
            .currentCurrency("USD")
            .build(),
        Project
            .builder()
            .name("Sublime Thresholds Tarot")
            .photo(Photo.builder().full("").build())
            .deadline(DateTime.now().plusDays(17))
            .build()
    )

    val similarProjectsUiState = SimilarProjectsUiState(data = data)

    val uiState = remember { mutableStateOf(similarProjectsUiState) }

    KSTheme {
        SimilarProjectsComponent(
            uiState = uiState
        )
    }
}

private val CustomPageSize = object : PageSize {
    override fun Density.calculateMainAxisPageSize(
        availableSpace: Int,
        pageSpacing: Int
    ): Int {
        return availableSpace - pageSpacing
    }
}

private val placeholderProject = Project.builder()
    .name("")
    .photo(Photo.builder().full("").build())
    .deadline(DateTime.now())
    .build()

/**
 * Does NOT consume, just spies.
 */
private suspend fun PointerInputScope.detectTouch(onTouchChange: (Boolean) -> Unit = {}) = awaitEachGesture {
    awaitFirstDown(false, PointerEventPass.Initial)
    onTouchChange(true)
    try {
        do {
            val event = awaitPointerEvent(PointerEventPass.Initial)
        } while (!event.changes.all { it.changedToUpIgnoreConsumed() })
    } finally {
        onTouchChange(false)
    }
}

@Composable
fun SimilarProjectsComponent(
    uiState: State<SimilarProjectsUiState>,
    onTouchChange: (Boolean) -> Unit = {},
    onClick: (Project) -> Unit = {},
) {
    val projects = uiState.value.data

    val pagerState = rememberPagerState { projects?.size ?: 0 }

    Column {
        Spacer(
            modifier = Modifier
                .height(18.dp)
        )
        Row(
            modifier = Modifier
                .absolutePadding(left = 18.dp, right = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Similar projects",
                style = KSTheme.typographyV2.headingXL,
                color = KSTheme.colors.kds_black
            )
            Row(
                Modifier
                    .weight(1f, true)
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.End
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration)
                        KSTheme.colors.backgroundSelected
                    else
                        KSTheme.colors.backgroundInversePressed
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .height(12.dp)
        )
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .absolutePadding(18.dp, 0.dp, 24.dp, 0.dp)
                    .alpha(0f)
                    .clickable(false) {},
                contentAlignment = Alignment.Center
            ) {
                SimilarProjectCard(placeholderProject)
            }
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues.Absolute(18.dp, 0.dp, 24.dp, 0.dp),
                pageSpacing = 12.dp,
                modifier = Modifier.pointerInput(Unit) {
                    detectTouch { touching ->
                        onTouchChange(touching)
                    }
                }
            ) { page ->
                val project = projects?.getOrNull(page)
                if (project != null) {
                    SimilarProjectCard(project) {
                        onClick(project)
                    }
                }
            }
        }
        Spacer(
            modifier = Modifier
                .height(24.dp)
        )
    }
}
