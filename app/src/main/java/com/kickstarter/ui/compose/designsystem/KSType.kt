package com.kickstarter.ui.compose.designsystem

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun KSTextPreview() {
    KSTheme {
        Column(modifier = Modifier.background(KSTheme.colors.kds_white)) {
            Text(text = "Title1", style = KSTheme.typography.title1)
            Text(text = "Title1Bold", style = KSTheme.typography.title1Bold)
            Text(text = "TitleRewardMedium", style = KSTheme.typography.title2)
            Text(text = "TitleRewardBold", style = KSTheme.typography.title2)
            Text(text = "Title2", style = KSTheme.typography.title2)
            Text(text = "Title2Bold", style = KSTheme.typography.title2Bold)
            Text(text = "Title3", style = KSTheme.typography.title3)
            Text(text = "Title3Bold", style = KSTheme.typography.title3Bold)
            Text(text = "headline", style = KSTheme.typography.headline)
            Text(text = "body", style = KSTheme.typography.body)
            Text(text = "callout", style = KSTheme.typography.callout)
            Text(text = "calloutMedium", style = KSTheme.typography.calloutMedium)
            Text(text = "subheadline", style = KSTheme.typography.subheadline)
            Text(text = "subheadlineMedium", style = KSTheme.typography.subheadlineMedium)
            Text(text = "BUTTON", style = KSTheme.typography.buttonText)
            Text(text = "Body2", style = KSTheme.typography.body2)
            Text(text = "Body2Medium", style = KSTheme.typography.body2Medium)
            Text(text = "Footnote", style = KSTheme.typography.footnote)
            Text(text = "FootnoteMedium", style = KSTheme.typography.footnoteMedium)
            Text(text = "Caption1", style = KSTheme.typography.caption1)
            Text(text = "Caption1Medium", style = KSTheme.typography.caption1Medium)
            Text(text = "Caption2", style = KSTheme.typography.caption2)
            Text(text = "Caption2Medium", style = KSTheme.typography.caption2Medium)
        }
    }
}

@Immutable
data class KSTypography(
    val title1: TextStyle = TextStyle.Default,
    val title1Bold: TextStyle = TextStyle.Default,
    val title2: TextStyle = TextStyle.Default,
    val titleRewardMedium: TextStyle = TextStyle.Default,
    val titleRewardBold: TextStyle = TextStyle.Default,
    val title2Bold: TextStyle = TextStyle.Default,
    val title3: TextStyle = TextStyle.Default,
    val title3Bold: TextStyle = TextStyle.Default,
    val headline: TextStyle = TextStyle.Default,
    val body: TextStyle = TextStyle.Default,
    val callout: TextStyle = TextStyle.Default,
    val calloutMedium: TextStyle = TextStyle.Default,
    val subheadline: TextStyle = TextStyle.Default,
    val subheadlineMedium: TextStyle = TextStyle.Default,
    val buttonText: TextStyle = TextStyle.Default,
    val body2: TextStyle = TextStyle.Default,
    val body2Medium: TextStyle = TextStyle.Default,
    val footnote: TextStyle = TextStyle.Default,
    val footnoteMedium: TextStyle = TextStyle.Default,
    val caption1: TextStyle = TextStyle.Default,
    val caption1Medium: TextStyle = TextStyle.Default,
    val caption2: TextStyle = TextStyle.Default,
    val caption2Medium: TextStyle = TextStyle.Default,
)

val LocalKSCustomTypography = staticCompositionLocalOf {
    KSTypography()
}

val KSCustomTypography = KSTypography(
    title1 = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = kds_support_700
    ),

    title1Bold = TextStyle(
        fontWeight = FontWeight(700),
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = kds_support_700
    ),

    titleRewardMedium = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 24.sp,
        lineHeight = 26.sp,
        color = kds_support_700
    ),

    titleRewardBold = TextStyle(
        fontWeight = FontWeight(700),
        fontSize = 24.sp,
        lineHeight = 26.sp,
        color = kds_support_700
    ),

    title2 = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 22.sp,
        lineHeight = 26.sp,
        color = kds_support_700
    ),

    title2Bold = TextStyle(
        fontWeight = FontWeight(700),
        fontSize = 22.sp,
        lineHeight = 26.sp,
        color = kds_support_700
    ),

    title3 = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 20.sp,
        lineHeight = 25.sp,
        color = kds_support_700
    ),

    title3Bold = TextStyle(
        fontWeight = FontWeight(700),
        fontSize = 20.sp,
        lineHeight = 25.sp,
        color = kds_support_700
    ),

    headline = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 18.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp,
        color = kds_support_700
    ),

    body = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 18.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp,
        color = kds_support_700
    ),

    callout = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp,
        color = kds_support_700
    ),

    calloutMedium = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.25.sp,
        color = kds_support_700
    ),

    subheadline = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp,
        color = kds_support_700
    ),

    subheadlineMedium = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.25.sp,
        color = kds_support_700
    ),

    buttonText = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.25.sp,
        color = kds_create_700
    ),

    body2 = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = kds_support_700
    ),

    body2Medium = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.25.sp,
        color = kds_support_700
    ),

    footnote = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 13.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp,
        color = kds_support_700
    ),

    footnoteMedium = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 13.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp,
        color = kds_support_700
    ),

    caption1 = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = kds_support_700
    ),

    caption1Medium = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
        color = kds_support_700
    ),

    caption2 = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.4.sp,
        color = kds_support_700
    ),

    caption2Medium = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.4.sp,
        color = kds_support_700
    )
)
