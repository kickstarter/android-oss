package com.kickstarter.features.socialshare.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.kickstarter.features.socialshare.data.SocialShareData
import com.kickstarter.features.socialshare.ui.icons.KSLogo
import com.kickstarter.models.Photo
import com.kickstarter.ui.compose.KSAsyncImage
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
    Column(
        modifier = Modifier
            .width(dimensions.socialShareCardWidth)
            .heightIn(max = dimensions.socialShareCardHeight)
            .clip(RoundedCornerShape(dimensions.socialShareCardRadius))
            .background(colors.backgroundSurfaceRaised)
            .padding(dimensions.socialShareCardContentPadding),
    ) {
        KSAsyncImage(
            image = Photo.builder().full(shareData.imageUrl).altText("Project Description").build(),
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(dimensions.socialShareImageRadius))
        )
        Spacer(modifier = Modifier.height(dimensions.paddingSmall))
        Text(
            text = shareData.projectName,
            style = KSTheme.typographyV2.headingLG.copy(
                fontSize = 17.sp,
                lineHeight = 22.sp,
                letterSpacing = (-0.41).sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = colors.textPrimary,
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(dimensions.paddingXSmall))
        Text(
            text = shareData.creatorName,
            style = KSTheme.typographyV2.bodyMD.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                letterSpacing = (-0.08).sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(dimensions.paddingXLarge))
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            Icon(
                imageVector = KSLogo,
                contentDescription = "KSLogo",
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}
