package com.kickstarter.features.videofeed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.features.videofeed.data.KSVideoBadgeType
import com.kickstarter.ui.compose.designsystem.KSAlmostThereVideoBadge
import com.kickstarter.ui.compose.designsystem.KSDaysLeftVideoBadge
import com.kickstarter.ui.compose.designsystem.KSFirstTimeCreatorVideoBadge
import com.kickstarter.ui.compose.designsystem.KSHotVideoBadge
import com.kickstarter.ui.compose.designsystem.KSJustLaunchedVideoBadge
import com.kickstarter.ui.compose.designsystem.KSNSFWVideoBadge
import com.kickstarter.ui.compose.designsystem.KSNearYouVideoBadge
import com.kickstarter.ui.compose.designsystem.KSPopularVideoBadge
import com.kickstarter.ui.compose.designsystem.KSProjectWeLoveVideoBadge
import com.kickstarter.ui.compose.designsystem.KSSuperbackerVideoBadge
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTrendingVideoBadge
import dev.chrisbanes.haze.HazeState

enum class KSVideoBadgesRowTestTag {
    BADGES_ROW_CONTAINER
}

@Composable
fun KSVideoBadgesRow(
    modifier: Modifier = Modifier,
    badges: List<KSVideoBadgeType>,
    hazeState: HazeState? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.paddingMedium)
            .padding(vertical = dimensions.paddingXSmall)
            .semantics(mergeDescendants = true) {}
            .testTag(KSVideoBadgesRowTestTag.BADGES_ROW_CONTAINER.name),
        horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall)
    ) {
        // - Only show the first 2 badges from the list
        badges.take(2).forEach { badgeType ->
            when (badgeType) {
                KSVideoBadgeType.ProjectWeLove -> KSProjectWeLoveVideoBadge(hazeState = hazeState)
                is KSVideoBadgeType.DaysLeft -> KSDaysLeftVideoBadge(text = badgeType.text, hazeState = hazeState)
                KSVideoBadgeType.JustLaunched -> KSJustLaunchedVideoBadge(hazeState = hazeState)
                KSVideoBadgeType.FirstTimeCreator -> KSFirstTimeCreatorVideoBadge(hazeState = hazeState)
                KSVideoBadgeType.ByASuperbacker -> KSSuperbackerVideoBadge(hazeState = hazeState)
                KSVideoBadgeType.NearYou -> KSNearYouVideoBadge(hazeState = hazeState)
                KSVideoBadgeType.NSFW -> KSNSFWVideoBadge(hazeState = hazeState)
                KSVideoBadgeType.AlmostThere -> KSAlmostThereVideoBadge(hazeState = hazeState)
                KSVideoBadgeType.Trending -> KSTrendingVideoBadge(hazeState = hazeState)
                KSVideoBadgeType.Popular -> KSPopularVideoBadge(hazeState = hazeState)
                KSVideoBadgeType.HotRightNow -> KSHotVideoBadge(hazeState = hazeState)
            }
        }
    }
}

@Preview
@Composable
fun KSVideoBadgesRowPreview() {
    KSTheme {
        KSVideoBadgesRow(
            modifier = Modifier.background(Color.Black),
            badges = listOf(
                KSVideoBadgeType.ProjectWeLove,
                KSVideoBadgeType.DaysLeft("3 days left"),
                KSVideoBadgeType.Trending,
                KSVideoBadgeType.HotRightNow,
                KSVideoBadgeType.Popular,
            )
        )
    }
}
