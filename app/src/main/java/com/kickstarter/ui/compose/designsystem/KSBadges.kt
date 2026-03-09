package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kickstarter.R
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions
import com.kickstarter.ui.compose.designsystem.KSTheme.typographyV2
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
fun KSBadgesPreview() {
    KSTheme {
        Column(
            Modifier
                .background(color = colors.kds_white)
                .fillMaxWidth()
                .padding(dimensions.paddingSmall)
        ) {

            KSGreenBadge(text = "Add-ons available")

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSCoralBadge(text = "3 days left")

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSAlertBadge(
                icon = ImageVector.vectorResource(id = R.drawable.ic_alert),
                message = "Payment Failed"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSWarningBadge(
                icon = ImageVector.vectorResource(id = R.drawable.ic_clock),
                message = "Address locks in 7 days"
            )

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSBetaBadge()

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSCountBadge(4)

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSSecretRewardBadge()

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSFeaturedRewardBadge()

            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))

            KSVideoBadge(text = "Just launched")
        }
    }
}

@Composable
@Preview(name = "Video Badges", backgroundColor = 0xFF000000, showBackground = true)
fun KSVideoBadgesPreview() {
    KSTheme {
        Column(
            Modifier
                .padding(dimensions.paddingSmall)
        ) {
            KSProjectWeLoveVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSDaysLeftVideoBadge(text = "3 days left")
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSJustLaunchedVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSFirstTimeCreatorVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSSuperbackerVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSNearYouVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSNSFWVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSAlmostThereVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSTrendingVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSPopularVideoBadge()
            Spacer(modifier = Modifier.height(dimensions.listItemSpacingSmall))
            KSHotVideoBadge()
        }
    }
}

@Composable
fun KSGreenBadge(
    modifier: Modifier = Modifier,
    text: String,
    leadingIcon: (@Composable (iconTint: Color) -> Unit)? = null,
    iconTint: Color = colors.textAccentGreen,
    textColor: Color = colors.textAccentGreen,
    textStyle: TextStyle = typographyV2.footNoteMedium
) {
    Row(
        modifier
            .background(
                color = colors.backgroundAccentGreenSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingMediumSmall,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                end = dimensions.paddingMediumSmall
            )
    ) {
        leadingIcon?.invoke(iconTint)
        Text(
            text = text,
            color = textColor,
            style = textStyle
        )
    }
}

@Composable
fun KSCoralBadge(
    leadingIcon: @Composable () -> Unit = {},
    text: String,
    textColor: Color = colors.textSecondary
) {
    Row(
        modifier = Modifier
            .background(
                color = colors.backgroundDangerSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingMediumSmall,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                end = dimensions.paddingMediumSmall
            )
    ) {
        leadingIcon()

        Text(
            text = text,
            color = textColor,
            style = typographyV2.footNoteMedium
        )
    }
}

@Composable
fun KSAlertBadge(
    icon: ImageVector?,
    message: String?
) {
    if (!message.isNullOrEmpty()) {
        Row(
            modifier = Modifier
                .background(
                    color = colors.backgroundDangerSubtle,
                    shape = shapes.small
                )
                .padding(
                    start = dimensions.paddingMediumSmall,
                    top = dimensions.paddingSmall,
                    bottom = dimensions.paddingSmall,
                    end = dimensions.paddingMediumSmall
                )
        ) {
            if (icon != null) {
                Image(
                    modifier = Modifier
                        .padding(end = dimensions.paddingXSmall)
                        .size(dimensions.alertIconSize),
                    imageVector = icon,
                    contentDescription = message,
                    colorFilter = ColorFilter.tint(colors.textAccentRedBold)
                )
            }

            Text(
                text = message,
                color = colors.textAccentRedBold,
                style = typographyV2.footNoteMedium
            )
        }
    }
}

@Composable
fun KSWarningBadge(
    icon: ImageVector?,
    message: String?
) {
    if (!message.isNullOrEmpty()) {
        Row(
            modifier = Modifier
                .background(
                    color = colors.backgroundAccentOrangeSubtle,
                    shape = shapes.small
                )
                .padding(
                    start = dimensions.paddingMediumSmall,
                    top = dimensions.paddingSmall,
                    bottom = dimensions.paddingSmall,
                    end = dimensions.paddingMediumSmall
                )
        ) {
            if (icon != null) {
                Image(
                    modifier = Modifier
                        .padding(end = dimensions.paddingXSmall)
                        .size(dimensions.alertIconSize),
                    imageVector = icon,
                    contentDescription = message,
                    colorFilter = ColorFilter.tint(colors.textSecondary)
                )
            }
            Text(
                text = message,
                color = colors.textSecondary,
                style = typographyV2.footNoteMedium
            )
        }
    }
}

@Composable
fun KSBetaBadge() {
    Text(
        modifier = Modifier
            .background(
                color = colors.backgroundAccentGreenSubtle,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingXSmall,
                top = dimensions.paddingXSmall,
                bottom = dimensions.paddingXSmall,
                end = dimensions.paddingXSmall
            ),
        text = stringResource(R.string.Beta).uppercase(),
        color = colors.textAccentGreen,
        style = typographyV2.headingXS
    )
}

@Composable
fun KSCountBadge(
    count: Int
) {
    Box(
        modifier = Modifier
            .background(colors.backgroundAccentGraySubtle, RoundedCornerShape(50))
            .padding(horizontal = dimensions.radiusSmall, vertical = dimensions.strokeWidth),
        contentAlignment = Alignment.Center
    ) {
        Text(text = count.toString(), color = colors.textAccentGrey, fontSize = 12.sp)
    }
}

@Composable
fun KSSecretRewardBadge(
    modifier: Modifier = Modifier,
    iconTint: Color = colors.textAccentGreenBold
) {
    KSGreenBadge(
        modifier = modifier,
        text = stringResource(R.string.Secret_reward),
        textColor = colors.textAccentGreenBold,
        iconTint = iconTint,
        leadingIcon = { tint ->
            Image(
                modifier = Modifier
                    .padding(end = dimensions.paddingXSmall)
                    .size(dimensions.alertIconSize),
                imageVector = secretRewardLock,
                contentDescription = stringResource(R.string.Secret_reward),
                colorFilter = ColorFilter.tint(tint)
            )
        },
        textStyle = typographyV2.headingSM
    )
}

@Composable
fun KSFeaturedRewardBadge(
    modifier: Modifier = Modifier,
    iconTint: Color = purple_08
) {
    Row(
        modifier
            .background(
                color = purple_02,
                shape = shapes.small
            )
            .padding(
                start = dimensions.paddingMediumSmall,
                top = dimensions.paddingSmall,
                bottom = dimensions.paddingSmall,
                end = dimensions.paddingMediumSmall
            )
    ) {
        Image(
            modifier = Modifier
                .padding(end = dimensions.paddingXSmall)
                .size(dimensions.alertIconSize),
            imageVector = featuredRewardStar,
            contentDescription = stringResource(R.string.fpo_featured_reward),
            colorFilter = ColorFilter.tint(iconTint)
        )
        Text(
            text = stringResource(R.string.fpo_featured_reward),
            color = purple_08,
            style = typographyV2.headingSM
        )
    }
}

/**
 * Generic badge designed for use within the Video Player.
 * It features a semi-transparent background with optional glassmorphism effect via [HazeState].
 */
@Composable
fun KSVideoBadge(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    iconTint: Color = Color.White,
    hazeState: HazeState? = null
) {
    Box(
        modifier = modifier
            .clip(shapes.small)
            .then(
                if (hazeState != null) {
                    Modifier.hazeEffect(state = hazeState) {
                        blurRadius = 28.dp
                        noiseFactor = 0.05f
                        val baseColor = Color(0xFF2B2B2D).copy(alpha = 0.25f)
                        backgroundColor = baseColor
                        tints = listOf(HazeTint(baseColor))
                    }
                } else {
                    Modifier.background(Color(0xFF2B2B2D).copy(alpha = 0.25f))
                }
            )
            .border(
                width = 1.38.dp,
                color = Color.White.copy(alpha = 0.25f),
                shape = shapes.small
            )
            .padding(horizontal = dimensions.paddingSmall, vertical = dimensions.paddingXSmall)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .padding(end = dimensions.paddingXSmall)
                        .size(dimensions.alertIconSize)
                )
            }
            Text(
                text = text,
                color = Color.White,
                style = typographyV2.footNoteMedium
            )
        }
    }
}

@Composable
fun KSProjectWeLoveVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.fpo_Project_We_Love),
        icon = projectWeLove,
        iconTint = colors.kds_create_500,
        hazeState = hazeState
    )
}

@Composable
fun KSDaysLeftVideoBadge(
    modifier: Modifier = Modifier,
    text: String,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = text,
        icon = ImageVector.vectorResource(id = R.drawable.ic_clock),
        hazeState = hazeState
    )
}

@Composable
fun KSJustLaunchedVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.fpo_Just_launched),
        hazeState = hazeState
    )
}

@Composable
fun KSFirstTimeCreatorVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.fpo_First_time_creator),
        hazeState = hazeState
    )
}

@Composable
fun KSSuperbackerVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.fpo_By_a_superbacker),
        hazeState = hazeState
    )
}

@Composable
fun KSNearYouVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.fpo_Near_you),
        hazeState = hazeState
    )
}

@Composable
fun KSNSFWVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = "NSFW",
        hazeState = hazeState
    )
}

@Composable
fun KSAlmostThereVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.Almost_there),
        hazeState = hazeState
    )
}

@Composable
fun KSTrendingVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.fpo_Trending),
        icon = Whatshot,
        hazeState = hazeState
    )
}

@Composable
fun KSPopularVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.Popular),
        icon = Whatshot,
        hazeState = hazeState
    )
}

@Composable
fun KSHotVideoBadge(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    KSVideoBadge(
        modifier = modifier,
        text = stringResource(R.string.fpo_Hot_right_now),
        icon = Whatshot,
        hazeState = hazeState
    )
}
