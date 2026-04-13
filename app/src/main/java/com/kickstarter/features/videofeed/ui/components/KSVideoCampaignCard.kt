package com.kickstarter.features.videofeed.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSOutlinedButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.KSVideoProgressIndicator
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Check

enum class KSVideoCampaignCardTestTag {
    CARD_CONTAINER,
    TITLE_SUBTITLE_CONTAINER,
    PROGRESS_INDICATOR,
    BUTTON
}

/**
 * A composable card component used within the video feed to display campaign information and progress.
 *
 * This component displays the project title, subtitle, a circular progress indicator,
 * and a primary call-to-action button. It is designed to be used as an overlay or
 * complementary UI element in a video-focused interface.
 */
@Composable
fun KSVideoCampaignCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    progress: Float = 0f
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensions.paddingMedium)
            .padding(top = dimensions.paddingXSmall)
            .testTag(KSVideoCampaignCardTestTag.CARD_CONTAINER.name)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            modifier = Modifier
                .semantics(mergeDescendants = true) {
                    // Combine title and subtitle for a better screen reader experience
                    contentDescription = "$title, $subtitle"
                }
                .testTag(KSVideoCampaignCardTestTag.TITLE_SUBTITLE_CONTAINER.name)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { } // Semantics are handled by the parent Row
            ) {
                Text(
                    text = title,
                    style = typographyV2.headingXL,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(dimensions.videoFeedCampaignTitleSubtitleSpacing))
                Text(
                    text = subtitle,
                    style = typographyV2.headingMD,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val isComplete = progress >= 100
            val progressValue = (progress / 100f).coerceIn(0f, 1f)

            KSVideoProgressIndicator(
                modifier = Modifier.testTag(KSVideoCampaignCardTestTag.PROGRESS_INDICATOR.name),
                progress = progressValue,
                icon = if (isComplete) Check else null,
                text = if (!isComplete) progress.toInt().toString() else "",
                contentDescription = if (isComplete) stringResource(id = R.string.fpo_Campaign_goal_reached) else ""
            )
        }

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        KSOutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                }
                .testTag(KSVideoCampaignCardTestTag.BUTTON.name),
            text = buttonText,
            textColor = KSTheme.colors.videoPlayer.buttonText,
            textStyle = typographyV2.headingXL,
            backgroundColor = Color.Transparent,
            contentPadding = PaddingValues(horizontal = dimensions.paddingLarge, vertical = dimensions.paddingMediumSmall),
            onClickAction = {
                onButtonClick.invoke()
            }
        )
    }
}

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSVideoCampaignCardPreview() {
    KSTheme {
        Column(modifier = Modifier.background(Color.Black)) {
            KSVideoCampaignCard(
                title = "Ringo Move - The Ultimate Workout Bottle",
                subtitle = "$50,134 pledged • Join 431 backers",
                buttonText = "Back this project",
                onButtonClick = {},
                progress = 100.0F,
            )
        }
    }
}

@Composable
@Preview(name = "Progress State")
fun KSVideoCampaignCardProgressPreview() {
    KSTheme {
        Column(modifier = Modifier.background(Color.Black)) {
            KSVideoCampaignCard(
                title = "Caira: the intelligent camera of the future",
                subtitle = "$10,903 pledged • Help bring this idea to life",
                buttonText = "Back this project",
                onButtonClick = {},
                progress = 50f,
            )
        }
    }
}
