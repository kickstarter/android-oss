package com.kickstarter.features.socialshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.ui.compose.designsystem.KSTheme
import com.kickstarter.ui.compose.designsystem.KSTheme.colors
import com.kickstarter.ui.compose.designsystem.KSTheme.dimensions

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun SocialShareProjectCardPreview() {
    KSTheme {
        SocialShareProjectCard(
            shareData = SocialShareData(
                projectName = "Ringo Move - The Ultimate Workout Bottle",
                projectUrl = "https://kickstarter.com",
                imageUrl = "",
                creatorName = "Ringo"
            )
        )
    }
}

@Composable
fun SocialShareProjectCard(shareData: SocialShareData) {
    // Width fixed at 276dp per design spec, centered by the parent Column.
    Column(
        modifier = Modifier
            .width(276.dp)
            .height(304.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.backgroundSurfaceRaised),
        // 32dp gap between the image child and the content block child.
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Image spans the full card width edge-to-edge (no padding).
        AsyncImage(
            model = shareData.imageUrl,
            contentDescription = shareData.projectName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
        )
        // Text content with 12dp padding on all sides (bottom padding included
        // via the parent Arrangement gap + the Spacer at the end of the Column).
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(dimensions.paddingXSmall)
        ) {
            Text(
                text = shareData.projectName,
                style = KSTheme.typographyV2.headingLG,
                color = colors.textPrimary,
                maxLines = 2
            )
            Text(
                text = shareData.creatorName,
                style = KSTheme.typographyV2.bodyMD,
                color = colors.textSecondary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimensions.radiusExtraSmall))
                    .background(colors.backgroundAccentGreenBrand)
                    .padding(horizontal = dimensions.paddingSmall, vertical = dimensions.paddingXSmall)
            ) {
                Text(
                    text = "KICKSTARTER",
                    style = KSTheme.typographyV2.bodyXS,
                    color = colors.textInversePrimary
                )
            }
        }
    }
}
