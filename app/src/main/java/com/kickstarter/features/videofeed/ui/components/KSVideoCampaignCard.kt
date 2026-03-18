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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kickstarter.ui.compose.designsystem.KSOutlinedButton
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import com.kickstarter.ui.compose.designsystem.KSVideoProgressIndicator
import com.kickstarter.ui.compose.designsystem.videoplayer.icons.Check

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
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = subtitle,
                    style = typographyV2.headingSM,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (progress >= 100) {
                KSVideoProgressIndicator(
                    progress = 1f,
                    icon = Check,
                )
            } else {
                val progressText = try {
                    progress.toInt().toString()
                } catch (exception: Exception) {
                    ""
                }

                KSVideoProgressIndicator(
                    progress = progress / 100,
                    text = progressText
                )
            }
        }

        Spacer(modifier = Modifier.height(dimensions.paddingMedium))

        KSOutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    role = Role.Button
                },
            text = buttonText,
            textColor = KSTheme.colors.videoPlayerButtonText,
            backgroundColor = Color.Transparent,
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
