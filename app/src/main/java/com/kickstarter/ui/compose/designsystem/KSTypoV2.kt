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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Preview(name = "Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun KSTextPreviewV2() {
    KSTheme {
        Column(modifier = Modifier.background(KSTheme.colors.kds_white)) {
            Text(text = "heading2XL", style = KSTheme.typographyV2.heading2XL)
            Text(text = "headingXL", style = KSTheme.typographyV2.headingXL)
            Text(text = "headingLG", style = KSTheme.typographyV2.headingLG)
            Text(text = "headingMD", style = KSTheme.typographyV2.headingMD)
            Text(text = "headingSM", style = KSTheme.typographyV2.headingSM)
            Text(text = "headingXS", style = KSTheme.typographyV2.headingXS)
            Text(text = "bodyXL", style = KSTheme.typographyV2.bodyXL)
            Text(text = "bodyBoldXL", style = KSTheme.typographyV2.bodyBoldXL)
            Text(text = "bodyLG", style = KSTheme.typographyV2.bodyLG)
            Text(text = "bodyBoldLG", style = KSTheme.typographyV2.bodyBoldLG)
            Text(text = "bodyMD", style = KSTheme.typographyV2.bodyMD)
            Text(text = "bodyBoldMD", style = KSTheme.typographyV2.bodyBoldMD)
            Text(text = "bodySM", style = KSTheme.typographyV2.bodySM)
            Text(text = "bodyBoldSM", style = KSTheme.typographyV2.bodyBoldSM)
            Text(text = "bodyXS", style = KSTheme.typographyV2.bodyXS)
            Text(text = "bodyBoldXS", style = KSTheme.typographyV2.bodyBoldXS)
            Text(text = "bodyXXS", style = KSTheme.typographyV2.bodyXXS)
            Text(text = "bodyBoldXXS", style = KSTheme.typographyV2.bodyBoldXXS)
            Text(text = "buttonLabel", style = KSTheme.typographyV2.buttonLabel)
            Text(text = "linkLG", style = KSTheme.typographyV2.linkLG)
            Text(text = "linkMD", style = KSTheme.typographyV2.linkMD)
            Text(text = "linkSM", style = KSTheme.typographyV2.linkSM)
            Text(text = "linkXS", style = KSTheme.typographyV2.linkXS)
        }
    }
}


@Immutable
data class KSTypographyV2(
    val heading2XL: TextStyle = TextStyle.Default,
    val headingXL: TextStyle = TextStyle.Default,
    val headingLG: TextStyle = TextStyle.Default,
    val headingMD: TextStyle = TextStyle.Default,
    val headingSM: TextStyle = TextStyle.Default,
    val headingXS: TextStyle = TextStyle.Default,
    val bodyXL: TextStyle = TextStyle.Default,
    val bodyBoldXL: TextStyle = TextStyle.Default,
    val bodyLG: TextStyle = TextStyle.Default,
    val bodyBoldLG: TextStyle = TextStyle.Default,
    val bodyMD: TextStyle = TextStyle.Default,
    val bodyBoldMD: TextStyle = TextStyle.Default,
    val bodySM: TextStyle = TextStyle.Default,
    val bodyBoldSM: TextStyle = TextStyle.Default,
    val bodyXS: TextStyle = TextStyle.Default,
    val bodyBoldXS: TextStyle = TextStyle.Default,
    val bodyXXS: TextStyle = TextStyle.Default,
    val bodyBoldXXS: TextStyle = TextStyle.Default,
    val buttonLabel: TextStyle = TextStyle.Default,
    val linkLG: TextStyle = TextStyle.Default,
    val linkMD: TextStyle = TextStyle.Default,
    val linkSM: TextStyle = TextStyle.Default,
    val linkXS: TextStyle = TextStyle.Default,
)

val LocalKSCustomTypographyV2 = staticCompositionLocalOf {
    KSTypographyV2()
}

val KSCustomTypographyV2 = KSTypographyV2(
    heading2XL = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.8).sp,

        color = kds_support_700
    ),
    headingXL = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.67).sp,
        color = kds_support_700
    ),
    headingLG = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.37).sp,
        color = kds_support_700
    ),
    headingMD = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.32).sp,
        color = kds_support_700
    ),
    headingSM = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.15).sp,
        color = kds_support_700
    ),
    headingXS = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.15).sp,
        color = kds_support_700
    ),
    bodyXL = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = kds_support_700
    ),
    bodyBoldXL = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = kds_support_700
    ),
    bodyLG = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = kds_support_700
    ),
    bodyBoldLG = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = kds_support_700
    ),
    bodyMD = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = kds_support_700
    ),
    bodyBoldMD = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = kds_support_700
    ),
    bodySM = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = kds_support_700
        ),
    bodyBoldSM = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = kds_support_700
    ),
    bodyXS = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = kds_support_700
    ),
    bodyBoldXS = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = kds_support_700
    ),
    bodyXXS = TextStyle(
        fontWeight = FontWeight(400),
        fontSize = 10.sp,
        lineHeight = 13.sp,
        color = kds_support_700
    ),
    bodyBoldXXS = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 10.sp,
        lineHeight = 13.sp,
        color = kds_support_700
    ),
    buttonLabel = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.4).sp,
        color = kds_support_700
    ),
    linkLG = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.4).sp,
        textDecoration = TextDecoration.Underline,
        color = kds_support_700
    ),
    linkMD = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.4).sp,
        textDecoration = TextDecoration.Underline,
        color = kds_support_700
    ),
    linkSM = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        textDecoration = TextDecoration.Underline,
        color = kds_support_700
    ),
    linkXS = TextStyle(
        fontWeight = FontWeight(500),
        fontSize = 11.sp,
        lineHeight = 14.sp,
        textDecoration = TextDecoration.Underline,
        color = kds_support_700
    ),
)
