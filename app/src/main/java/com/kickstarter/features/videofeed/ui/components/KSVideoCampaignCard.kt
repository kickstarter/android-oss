package com.kickstarter.features.videofeed.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSButton
import com.kickstarter.ui.compose.designsystem.KSButtonType
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.KSVideoProgressIndicator
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Check
import dev.chrisbanes.haze.HazeState

@Composable
fun KSVideoCampaignCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    isBacked: Boolean = false,
    progress: Float? = null,
    progressText: String? = null,
    hazeState: HazeState? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensions.paddingMedium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensions.paddingSmall),
            modifier = Modifier.semantics(mergeDescendants = true) {
                // Combine title and subtitle for a better screen reader experience
                contentDescription = "$title, $subtitle"
            }
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clearAndSetSemantics { } // Semantics are handled by the parent Row
            ) {
                Text(
                    text = title,
                    style = typographyV2.headingLG,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = typographyV2.bodySM,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isBacked) {
                KSVideoProgressIndicator(
                    progress = 1f,
                    icon = Check,
                    contentDescription = stringResource(id = R.string.fpo_You_have_backed_this_project),
                    hazeState = hazeState
                )
            } else if (progress != null && progressText != null) {
                KSVideoProgressIndicator(
                    progress = progress,
                    text = progressText,
                    hazeState = hazeState
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        KSButton(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                },
            type = KSButtonType.OUTLINED,
            text = buttonText,
            onClickAction = onButtonClick
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
                isBacked = true
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
                isBacked = false,
                progress = 0.5f,
                progressText = "50"
            )
        }
    }
}
